/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote that a given query method on a DAO should not be automatically tested.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipQueryTest
{
	public String value() default "";
}