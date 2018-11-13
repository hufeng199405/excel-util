package com.weijuju.iag.excel.constant;

/**
 * 错误码
 */
public enum ResponseCode {

    SUCCESS(0, "正常"),

    INVALID_PARAM(-4,"参数错误");

    private int code;

    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
