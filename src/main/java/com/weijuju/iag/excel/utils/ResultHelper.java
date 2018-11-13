package com.weijuju.iag.excel.utils;


import com.weijuju.iag.excel.constant.ResponseCode;
import com.weijuju.iag.excel.model.ResponseModel;

import java.util.List;

/**
 * 89005691
 */
public class ResultHelper<T> {

    public static ResponseModel fail(int code, String msg) {
        ResponseModel result = new ResponseModel();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public ResponseModel success(List<T> value) {
        ResponseModel result = new ResponseModel();
        result.setCode(ResponseCode.SUCCESS.getCode());
        result.setMsg("success");
        result.setData(value);
        return result;
    }
}
