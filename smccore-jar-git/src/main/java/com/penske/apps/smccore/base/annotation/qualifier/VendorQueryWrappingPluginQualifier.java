/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.annotation.qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Annotation to denote an {@link Interceptor} bean as the one that should be used to wrap queries that the vendor can see in extra SQL for security.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface VendorQueryWrappingPluginQualifier {}