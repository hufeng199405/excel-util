package com.weijuju.iag.excel.export;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsGenColumnExclude {
}
