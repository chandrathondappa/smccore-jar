/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.penske.apps.smccore.base.domain.enums.SecurityFunction;

/**
 * Annotation to be used on any method that requires SMC security checks. This is to be intercepted
 * by a spring intercepter and validated on a request basis.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SmcSecurity
{
	public SecurityFunction[] securityFunction();
}
