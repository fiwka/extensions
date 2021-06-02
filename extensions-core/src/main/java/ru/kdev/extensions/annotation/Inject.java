package ru.kdev.extensions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Inject {

    /**
     * Target method(s)
     * */
    String[] method();

    /**
     * Target place
     * */
    At at();

    /**
     * Allows to change return node
     * Only on bottom inject
     * */
    boolean changeReturn() default false;

    enum At {
        TOP,
        BOTTOM
    }
}
