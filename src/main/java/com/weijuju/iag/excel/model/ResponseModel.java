package com.weijuju.iag.excel.model;

import lombok.Data;

import java.util.List;

/**
 * 统一返回结果
 *
 * @author HUFENG
 * @create 2017-12-13
 */
@Data
public class ResponseModel<T> {

    /**
     * 返回码
     */
    private int code;

    /**
     * 数据
     */
    private List<T> data;

    /**
     * 返回信息
     */
    private String msg;
}
