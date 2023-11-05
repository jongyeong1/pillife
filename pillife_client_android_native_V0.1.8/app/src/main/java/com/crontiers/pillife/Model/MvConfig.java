package com.crontiers.pillife.Model;


import android.Manifest;

/**
 * Created by Jaewoo on 2016-08-19.
 */
public interface MvConfig extends HttpKeyValue {
    String TAG = "DRUGFILL";
    String RELEASE_HOST = "http://211.254.214.202/app";
//    String RELEASE_HOST = "http://172.16.13.82:8080";

//    String RELEASE_HOST = "http://211.254.214.202:8080/pill/v1.0";

    String DRUG_FIND_HOST = "http://dikmobile.health.kr/category02.do";
    String DRUG_DETAILS_HOST = "http://dikmobile.health.kr/category01Dtl1.do?drug_code=2013090200047";

    String EXTRA_PERMISSION         = "DRUGFILL.permmission";
    String EXTRA_INPUT_TYPE         = "DRUGFILL.input_type";
    String EXTRA_SEARCH_TYPE        = "DRUGFILL.search_type";
    String EXTRA_SEARCH_URL         = "DRUGFILL.search_url";
    String EXTRA_FILE_NAME          = "DRUGFILL.file_name";
    String EXTRA_SHAPE_TYPE         = "DRUGFILL.shape_type";
    String EXTRA_IDENTITY_F         = "DRUGFILL.identity_front";
    String EXTRA_IDENTITY_B         = "DRUGFILL.identity_back";
    String EXTRA_IDENTITY_TYPE      = "DRUGFILL.identity_type";

    int MY_PERMISSION_REQUEST = 10010;

    String[] PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, };

    int READ_TIMEOUT = 60;
    int WRITE_TIMEOUT = 5;
    int CONNECT_TIMEOUT = 5;
    int MAX_ITEMS_PER_REQUEST = 20;
    long SIMULATED_LOADING_TIME_IN_MS = 100;
    long INTRO_TIME = 1000;

    int BOARD_WIDTH = 600;
    int BOARD_HEIGHT = 400;

    float BIG_SCALE = 1.0f;
    float SMALL_SCALE = 1.0f;
    float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

    // Status indicators
    int DOWNLOAD_CATEGORY_NOT_FOUND = -5;
    int DOWNLOAD_CONTENTS_NOT_FOUND = -4;
    int DOWNLOAD_OVERFLOW = -3;
    int DOWNLOAD_CANCELED = -2;
    int DOWNLOAD_FAILED = -1;
    int DOWNLOAD_STARTED = 1;
    int DOWNLOAD_COMPLETE = 2;
    int DOWNLOAD_UPDATED = 3;

    int TASK_COMPLETE = 4;

    int CUSTOM_ANSWER_INPUT = 999;

    enum CONTENT_TYPE {
        JSON(1),
        URLENCODED(2);

        public int rc;

        CONTENT_TYPE(int rc) {
            this.rc = rc;
        }
    }

    enum INFO_TYPE {
        NOTICE(1),
        FAQ(2),
        LICENSE(3);

        public int rc;

        INFO_TYPE(int rc) {
            this.rc = rc;
        }
    }

    enum SHAPE_TYPE {
        CIRCLE("1"),
        ELLIPSE("2"),
        TRIANGLE("3"),
        OCTAGON("4"),
        PENTAGON("5"),
        RECTANGLE("6"),
        HEXAGON("7"),
        RHOMBUS("8"),
        ETC("9");

        public String rc;

        SHAPE_TYPE(String rc) {
            this.rc = rc;
        }
    }

    enum SEARCH_TYPE {
        LOADING(1),
        SUCCESS(2),
        FAIL(3),
        WEBVIEW(4);

        public int rc;

        SEARCH_TYPE(int rc) {
            this.rc = rc;
        }
    }

    enum POPUP_TYPE {
        CONTENTS,
        PERMISSION,
        NETWORK,
        DELETE,
        NFC,
        VIDEO,
        DOWNLOAD,
        VERSION,
        EMAIL,
        DRUG,
    }

    enum REQUEST_TYPE {
        GET,
        POST,
        PUT,
        DELETE;

        public static String valueOf(REQUEST_TYPE rc) {
            switch (rc) {
                case GET:
                    return "GET";
                case POST:
                    return "POST";
                case PUT:
                    return "PUT";
                case DELETE:
                    return "DELETE";
                default:
                    return "";
            }
        }
    }

    enum REQUEST_CALLBACK {
        NULL_LIST(1),
        RECOGNITION(2),
        RECONNECT(3),
        LOGIN(4),
        ANSWER(5);

        public int rc;

        REQUEST_CALLBACK(int rc) {
            this.rc = rc;
        }

        public static String valueOfStr(REQUEST_CALLBACK rc) {
            switch (rc) {
                case RECOGNITION:
                    return "/v3/recognition";
                case RECONNECT:
                    return "/v1/reconnect";
                case LOGIN:
                    return "/v1/login";
                case ANSWER:
                    return "/v1/answer";
                default:
                    return "/v1/";
            }
        }

        public static REQUEST_CALLBACK valueOf(int rc) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].rc == rc) {
                    return values()[i];
                }
            }
            return NULL_LIST;
        }
    }
}
