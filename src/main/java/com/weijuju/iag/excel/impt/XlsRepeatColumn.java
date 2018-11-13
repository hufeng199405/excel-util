package com.weijuju.iag.excel.impt;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsRepeatColumn {

    /**
     * 分组号
     */
    int groupNum();

    /**
     * 列名称
     */
    String columnName();
}
