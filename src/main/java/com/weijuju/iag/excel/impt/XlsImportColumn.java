package com.weijuju.iag.excel.impt;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsImportColumn {

    int order();
}
