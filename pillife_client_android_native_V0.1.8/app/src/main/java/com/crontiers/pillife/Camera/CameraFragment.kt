/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crontiers.pillife.Camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CaptureMode
import androidx.camera.core.ImageCapture.Metadata
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crontiers.pillife.*
import com.crontiers.pillife.Utils.ANIMATION_FAST_MILLIS
import com.crontiers.pillife.Utils.ANIMATION_SLOW_MILLIS
import com.crontiers.pillife.Utils.AutoFitPreviewBuilder
import com.crontiers.pillife.Model.MvConfig
import com.crontiers.pillife.Model.MvConfig.*
import com.crontiers.pillife.R
import com.crontiers.pillife.Utils.Logging
import kotlinx.android.synthetic.main.camera_ui_container.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

/**
 * Main fragment for this app. Implements all camera operations including:
 * - Viewfinder
 * - Photo taking
 * - Image analysis
 */
class CameraFragment : Fragment() {
    private var cropRect: Rect? = null
    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: TextureView
    private lateinit var outputDirectory: File
    private lateinit var extraOutput: String

    private var displayId = -1
    private var lensFacing = CameraX.LensFacing.BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var isFlash: Boolean = false

    // capture preview rectangle size
    private var x: Int = 0
    private var y:Int = 0
    private var widthFilter:Int = 0
    private var heightFilter:Int = 0

    // capture level, first front and back last send image(EXIT), 0 -> 1 -> 2
    private var capturePhase = 0

    /** Declare worker thread at the class level so it can be reused after config changes */
    private val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }

    /** Internal reference of the [DisplayManager] */
    private lateinit var displayManager: DisplayManager

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                preview?.setTargetRotation(view.display.rotation)
                imageCapture?.setTargetRotation(view.display.rotation)
                imageAnalyzer?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }

    class ConsoleUtils {
        companion object {
            @JvmStatic
            fun newInstance(): CameraFragment {
                return CameraFragment()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unregister the broadcast receivers and listeners
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_camera, container, false)

    /** Define callback that will be triggered after a photo has been taken and saved to disk */
    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        private fun saveBitmapCropImage(image: Bitmap?, photoFile: File) {
            try {
                val filePhase = photoFile.nameWithoutExtension + "_crop"

                val fOut = FileOutputStream(outputDirectory.absolutePath + "/" + filePhase + PHOTO_EXTENSION)
                image?.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()

                if(photoFile.nameWithoutExtension.takeLast(1) != "n") {
                    GlobalApplication.img_process_finish = true
                    activity!!.onBackPressed()
                    activity!!.supportFragmentManager.popBackStack()
                }
//                    goIntent(ActivitySearch::class.java)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onError(
                error: ImageCapture.UseCaseError, message: String, exc: Throwable?) {
            Log.e(TAG, "Photo capture failed: $message")
            exc?.printStackTrace()
        }

        override fun onImageSaved(photoFile: File) {
            Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")

            Glide
                .with(activity!!.applicationContext)
                .asBitmap()
                .load(photoFile.absolutePath)
                .apply(RequestOptions().override(heightFilter))
//                .apply(RequestOptions().override(1280))
                .fitCenter()
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        val result = Bitmap.createBitmap(resource!!,
//                            (resource.width / 4 * 1.16).toInt(),        // X 시작위치
//                            (resource.width / 4 * 1.01).toInt(),       // Y 시작위치
//                            (resource.width / 2 * 0.8).toInt(),
//                            (resource.width / 2 * 0.8).toInt()
//                        )
//                        val result = Bitmap.createBitmap(resource!!,
//                            (resource.width / 4 * 1.16).toInt(),        // X 시작위치
//                            (resource.height / 4 * 1.01).toInt(),       // Y 시작위치
//                            (resource.width / 2 * 0.8).toInt(),
//                            (resource.height / 2 * 0.8).toInt()
//                        )
//                        val result = Bitmap.createBitmap(resource!!,
//                            (resource.width / 4 * 1.16).toInt(),        // X 시작위치
//                            (resource.height / 4 * 1.01).toInt(),       // Y 시작위치
//                            (resource.width / 2 * 0.8).toInt(),
//                            (resource.height / 2 * 0.8).toInt()
//                        )
                        val centerX = (resource!!.width / 2).toFloat()
                        val centerY = (resource!!.height / 2).toFloat() * 0.9
                        val default = 1024
                        val width = default
                        val height = default
                        val x = (centerX - (default / 2)).toInt()
                        val y = (centerY - (default / 2)).toInt()
                        val result = Bitmap.createBitmap(resource!!, x, y, width, height)
                        saveBitmapCropImage(result, photoFile)
                        return true
                    }
                }).submit()

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Update the gallery thumbnail with latest picture taken
                //setGalleryThumbnail(photoFile)
            }

            // Implicit broadcasts will be ignored for devices running API
            // level >= 24, so if you only target 24+ you can remove this statement
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(
                        Intent(Camera.ACTION_NEW_PICTURE, Uri.fromFile(photoFile)))
            }

            // If the folder selected is an external media directory, this is unnecessary
            // but otherwise other apps will not be able to access our images unless we
            // scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                    context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)

            capturePhase++
            if (capturePhase == 1) {
                activity!!.runOnUiThread {
                    textView1.text = getString(R.string.string_filter_description3)
                    imageButton3.visibility = View.GONE
                    imageButton4.visibility = View.GONE
                }
            } else {
                // 파일 전송 대기 화면으로 이동.
                activity!!.runOnUiThread {
//                    goIntent(ActivitySearch::class.java)
                    goIntent(ActivityInputDrugIdentity::class.java)

                    // 이미지 후처리가 끝나면 이동.
                    activity!!.finish()
                }
            }
        }
    }

    private fun goIntent(cls: Class<*>){
        activity!!.runOnUiThread {
            //activity!!.onBackPressed()

            val intent = Intent(context, cls)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_FILE_NAME, extraOutput)
            intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE.LOADING)
            intent.putExtra(EXTRA_SEARCH_URL, DRUG_FIND_HOST)
            var shapeType = SHAPE_TYPE.CIRCLE
            when(frameLayout_capture.tag) {
                SHAPE_TYPE.CIRCLE.rc -> shapeType = SHAPE_TYPE.CIRCLE
                SHAPE_TYPE.ELLIPSE.rc -> shapeType = SHAPE_TYPE.ELLIPSE
                SHAPE_TYPE.TRIANGLE.rc -> shapeType = SHAPE_TYPE.TRIANGLE
                SHAPE_TYPE.OCTAGON.rc -> shapeType = SHAPE_TYPE.OCTAGON
                SHAPE_TYPE.PENTAGON.rc -> shapeType = SHAPE_TYPE.PENTAGON
                SHAPE_TYPE.RECTANGLE.rc -> shapeType = SHAPE_TYPE.RECTANGLE
                SHAPE_TYPE.HEXAGON.rc -> shapeType = SHAPE_TYPE.HEXAGON
                SHAPE_TYPE.RHOMBUS.rc -> shapeType = SHAPE_TYPE.RHOMBUS
                SHAPE_TYPE.ETC.rc -> shapeType = SHAPE_TYPE.ETC
            }
//            if (frameLayout_capture.tag == "0")
//                intent.putExtra(EXTRA_SHAPE_TYPE, SHAPE_TYPE.CIRCLE)
//            else
//                intent.putExtra(EXTRA_SHAPE_TYPE, SHAPE_TYPE.ELLIPSE)
            intent.putExtra(EXTRA_SHAPE_TYPE, shapeType)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)

        // Every time the orientation of device changes, recompute layout
        displayManager = viewFinder.context
                .getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        // Determine the output directory
        outputDirectory = MainActivity.getOutputDirectory(requireContext())

        extraOutput = ""

        // Wait for the views to be properly laid out
        viewFinder.post {
            // Keep track of the display in which this view is attached
            displayId = viewFinder.display.displayId

            // Build UI controls and bind all camera use cases
            updateCameraUi()
            //bindCameraUseCases()

        }
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        // Set up the view finder use case to display camera preview
        val viewFinderConfig = PreviewConfig.Builder().apply {
//            setLensFacing(lensFacing)
//            // We request aspect ratio but no resolution to let CameraX optimize our use cases
//            setTargetAspectRatio(screenAspectRatio)
//            // Set initial target rotation, we will have to call this again if rotation changes
//            // during the lifecycle of this use case
//            setTargetRotation(viewFinder.display.rotation)

            setLensFacing(CameraX.LensFacing.BACK)
            //setTargetAspectRatio(Rational(1,1))
            //setTargetAspectRatio(screenAspectRatio)
            setTargetAspectRatio(Rational(9,16))
        }.build()

        // Use the auto-fit preview builder to automatically handle size and orientation changes
        preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

        // Set up the capture use case to allow users to take photos
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
//            setLensFacing(lensFacing)
//            setCaptureMode(CaptureMode.MIN_LATENCY)
//            // We request aspect ratio but no resolution to match preview config but letting
//            // CameraX optimize for whatever specific resolution best fits requested capture mode
//            setTargetAspectRatio(screenAspectRatio)
//            // Set initial target rotation, we will have to call this again if rotation changes
//            // during the lifecycle of this use case
//            setTargetRotation(viewFinder.display.rotation)

            setLensFacing(CameraX.LensFacing.BACK)
            setCaptureMode(CaptureMode.MIN_LATENCY)
//            setTargetResolution(Size (1280, 1024))
//            setTargetAspectRatio(Rational(1,1))
            setTargetAspectRatio(Rational(9,16))
//            setTargetAspectRatio(Rational(1,1))
        }.build()

        imageCapture = ImageCapture(imageCaptureConfig)

        // Setup image analysis pipeline that computes average pixel luminance in real time
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//            setLensFacing(lensFacing)
            // Use a worker thread for image analysis to prevent preview glitches
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            //setTargetRotation(viewFinder.display.rotation)
        }.build()

        imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
            analyzer = LuminosityAnalyzer { luma ->
                // Values returned from our analyzer are passed to the attached listener
                // We log image analysis results here -- you should do something useful instead!
                val fps = (analyzer as LuminosityAnalyzer).framesPerSecond
                Log.d(TAG, "Average luminosity: $luma. " +
                        "Frames per second: ${"%.01f".format(fps)}")
            }
        }

        // Apply declared configs to CameraX using the same lifecycle owner
        CameraX.bindToLifecycle(
                viewLifecycleOwner, preview, imageCapture, imageAnalyzer)
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes */
    @SuppressLint("RestrictedApi")
    private fun updateCameraUi() {

        // Remove previous UI if any
        container.findViewById<RelativeLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        // Inflate a new view containing all UI for controlling the camera
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        // Listener for button used to capture photo
        controls.findViewById<Button>(R.id.picture).setOnClickListener {
            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image
                val fileNamePhase = {
                    if(capturePhase == 0)
                        SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
                    else
                        extraOutput
                }
                extraOutput = fileNamePhase()
                val filePhase = { if(capturePhase == 0) extraOutput + "_origin" else extraOutput + "_origin_back" }
                val photoFile = createFile(outputDirectory, filePhase(), PHOTO_EXTENSION)

                // Setup image capture metadata
                val metadata = Metadata().apply {
                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
                }

                if(isFlash)
                    imageCapture.flashMode = FlashMode.ON
                else
                    imageCapture.flashMode = FlashMode.OFF

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(photoFile, imageSavedListener, metadata)

                // We can only change the foreground Drawable using API level 23+ API
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // Display flash animation to indicate that photo was captured
                    container.postDelayed({
                        container.foreground = ColorDrawable(Color.WHITE)
                        container.postDelayed(
                                { container.foreground = null }, ANIMATION_FAST_MILLIS
                        )
                    }, ANIMATION_SLOW_MILLIS)
                }
            }
        }

        // Listener for button used to flash on/off
        controls.findViewById<ImageButton>(R.id.imageButton1).setOnClickListener {
            if (imageButton1.tag == "0") {
                imageButton1.setImageResource(R.drawable.ic_flash_off_black_24dp)
                imageButton1.tag = "1"
                isFlash = true
            } else {
                imageButton1.setImageResource(R.drawable.ic_flash_on_black_24dp)
                imageButton1.tag = "0"
                isFlash = false
            }
        }

        // Listener for button used to back press
        controls.findViewById<ImageView>(R.id.imageView1).setOnClickListener {
            activity!!.onBackPressed()
        }

        // Listener for button used to camera help
        controls.findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(context, ActivityGuide::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(MvConfig.EXTRA_INPUT_TYPE, true)
            startActivity(intent)
        }

        // Listener for button used to change the left/right shape
        controls.findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            changeShape(-1)
        }
        controls.findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            changeShape(1)
        }

        if(arguments != null) {
            frameLayout_capture.tag = arguments!!.getString(EXTRA_SHAPE_TYPE)
            changeShape(0)
        } else {
            frameLayout_capture.tag = SHAPE_TYPE.CIRCLE.rc
        }

        frameLayout_capture.post {
            x = frameLayout_capture.x.toInt()
            y = frameLayout_capture.y.toInt()

            widthFilter = frameLayout_capture.width
            heightFilter = frameLayout_capture.height

            val cameraMgr = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics: CameraCharacteristics

            try {
                characteristics = cameraMgr.getCameraCharacteristics("0")

                val activeArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

                //noinspection ConstantConditions
                val cx = activeArraySize!!.centerX() - 300
                val cy = activeArraySize.centerX()
                val hw = widthFilter + 100
                val hh = heightFilter + 100
                cropRect = Rect(cx - hw, cy - hh, cx + hw, cy + hh)

                heightFilter = activeArraySize.height()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

            Logging.e("Capture Size:%d, %d, %d, %d", x, y, widthFilter, heightFilter)

            bindCameraUseCases()
        }
    }

    private fun changeShape(value : Int){
        /*
        if (frameLayout_capture.tag == "0") {
            frameLayout_capture.setBackgroundResource(R.drawable.shape_type_circle)
            frameLayout_capture.tag = "1"
        } else {
            frameLayout_capture.setBackgroundResource(R.drawable.shape_type_pentagon)
            frameLayout_capture.tag = "0"
        }
        */
        val shapeTypeArray = arrayOf(R.drawable.shape_type_circle, R.drawable.shape_type_ellipse1,
            R.drawable.shape_type_triangle, R.drawable.shape_type_octagon, R.drawable.shape_type_pentagon, R.drawable.shape_type_rectangle,
            R.drawable.shape_type_hexagon, R.drawable.shape_type_rhombus, R.drawable.shape_type_etc)
        var tag = frameLayout_capture.tag.toString().toInt() + value
        if(tag <= 0)
            tag = SHAPE_TYPE.ETC.rc.toInt()
        else if(tag > SHAPE_TYPE.ETC.rc.toInt())
            tag = SHAPE_TYPE.CIRCLE.rc.toInt()
        frameLayout_capture.tag = tag.toString();
        frameLayout_capture.setBackgroundResource(shapeTypeArray[tag - 1]);
    }


    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: do not close the image, it will be
         * automatically closed after this method returns
         * @return the image analysis result
         */
        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) return

            // Keep track of frames analyzed
            frameTimestamps.push(System.currentTimeMillis())

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            framesPerSecond = 1.0 / ((frameTimestamps.peekFirst() -
                    frameTimestamps.peekLast())  / frameTimestamps.size.toDouble()) * 1000.0

            // Calculate the average luma no more often than every second
            if (frameTimestamps.first - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
                lastAnalyzedTimestamp = frameTimestamps.first

                // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance
                //  plane
                val buffer = image.planes[0].buffer

                // Extract image data from callback object
                val data = buffer.toByteArray()

                // Convert the data into an array of pixel values ranging 0-255
                val pixels = data.map { it.toInt() and 0xFF }

                // Compute average luminance for the image
                val luma = pixels.average()

                // Call all listeners with new value
                listeners.forEach { it(luma) }
            }
        }
    }

    companion object {
        private const val TAG = "PilLife"
        private const val FILENAME = "ssSSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
                File(baseFolder,format + extension)
    }
}
