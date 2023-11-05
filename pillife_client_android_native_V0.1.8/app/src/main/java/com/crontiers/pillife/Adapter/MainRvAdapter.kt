package com.crontiers.pillife.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crontiers.pillife.Dialog.Category
import com.crontiers.pillife.R

class MainRvAdapter(val context: Context, val Category: ArrayList<Category>):
    RecyclerView.Adapter<MainRvAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_rv_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return Category.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(Category[position], context)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ImageView = itemView?.findViewById<ImageView>(R.id.dogPhotoImg)
        //val image = itemView?.findViewById<ImageView>(R.id.dogPhotoImg)
        val title = itemView?.findViewById<TextView>(R.id.dogBreedTv)
        val link = itemView?.findViewById<TextView>(R.id.dogAgeTv)
        val price = itemView?.findViewById<TextView>(R.id.dogGenderTv)

        var replacekey : String = String()


        fun bind (Category: Category, context: Context) {
            /* dogPhoto의 setImageResource에 들어갈 이미지의 id를 파일명(String)으로 찾고,
            이미지가 없는 경우 안드로이드 기본 아이콘을 표시한다.*/

            Log.d("응답", Category.image)
            if (Category.image != "") {
                Glide.with(context)
                    .load(Category.image) // 불러올 이미지 url
                    .placeholder(R.mipmap.ic_launcher) // 이미지 로딩 시작하기 전 표시할 이미지
                    .error(R.mipmap.ic_launcher) // 로딩 에러 발생 시 표시할 이미지
                    .fallback(R.mipmap.ic_launcher) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .circleCrop() // 동그랗게 자르기
                    .into(ImageView)
            } else {
                ImageView?.setImageResource(R.mipmap.ic_launcher)
            }
            /* 나머지 TextView와 String 데이터를 연결한다. */
            replacekey = Category.title.replace("<b>"," ")
            replacekey = replacekey.replace("</b>"," ")
            title?.text = replacekey
            link?.text = Category.link
            price?.text = Category.price+" won"
        }
    }
}