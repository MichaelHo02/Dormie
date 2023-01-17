package com.michael.dormie.utils;

public class SignalCode {
    // todo: add request signal here with the code
    public final static int TEMPLATE_FORMAT = 0;
    public final static int SIGN_IN_WITH_GOOGLE = 10;
    public static final int ITEM_CREATION_UPLOAD_PHOTO = 40;
    public static final int ITEM_CREATION_PERMISSION_CAM = 50;
    public static final int ITEM_CREATION_TAKE_PHOTO = 60;
    public final static int NAVIGATE_MAP = 70;

    public static final int UPDATE_ACCOUNT_SUCCESS = 210;
    public static final int UPDATE_USER_SUCCESS = 220;
    public static final int UPDATE_TENANT_SUCCESS = 230;
    public final static int UPLOAD_IMG_SUCCESS = 240;
    public final static int UPLOAD_POST_SUCCESS = 250;

    public static final int UPDATE_ACCOUNT_ERROR = 510;
    public static final int UPDATE_USER_ERROR = 520;
    public static final int UPDATE_TENANT_ERROR = 530;
    public final static int UPLOAD_IMG_ERROR = 540;
    public final static int UPLOAD_POST_ERROR = 550;

    private SignalCode() {
    }
}
