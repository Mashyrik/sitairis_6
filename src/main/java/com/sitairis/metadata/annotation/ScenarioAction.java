package com.sitairis.metadata.annotation;

import com.sitairis.metadata.LibraryOperation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a public method as an executable step of the library scenario. */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScenarioAction {
    LibraryOperation value();
}
