package com.weijuju.iag.excel.export;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsGenColumn {

    String value();

    int order();

    /**
     * 自定义处理,如果对于同一个属性同时出现了{@link XlsGenAbstractHandle}以及{@link XlsGenNewAbstractHandle},则{@link XlsGenNewAbstractHandle}优先
     * @return
     */
    Class<? extends XlsGenAbstractHandle> handle() default XlsGenAbstractHandle.class;

    /**
     * 自定义处理,如果对于同一个属性同时出现了{@link XlsGenAbstractHandle}以及{@link XlsGenNewAbstractHandle},则{@link XlsGenNewAbstractHandle}优先
     * @return
     */
    Class<? extends XlsGenNewAbstractHandle> newHandle() default XlsGenNewAbstractHandle.class;
}
