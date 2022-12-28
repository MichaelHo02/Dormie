package com.michael.dormie.utils;

public class SignalCode {
    // todo: add request signal here with the code
    public final static int TEMPLATE_FORMAT = 0;
    public final static int SIGN_IN_WITH_GOOGLE = 10;
    public final static int NAVIGATE_SIGNUP_FORM = 20;
    public final static int NAVIGATE_HOME = 30;
    public static final int ITEM_CREATION_UPLOAD_PHOTO = 40;
    public static final int ITEM_CREATION_PERMISSION_CAM = 50;
    public static final int ITEM_CREATION_TAKE_PHOTO = 60;
    public static final int DOWNLOAD_SUCCESS = 200;
    public static final int UPDATE_ACCOUNT_SUCCESS = 210;
    public static final int UPDATE_USER_SUCCESS = 220;
    public static final int DOWNLOAD_ERROR = 500;
    public static final int UPDATE_ACCOUNT_ERROR = 510;
    public static final int UPDATE_USER_ERROR = 520;
    private SignalCode() {
    }
}
