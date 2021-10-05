/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.penske.apps.smccore.base.configuration.typeHandlers.SmcEnumTypeHandler;
import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * Annotation to contain the types that should be mapped via an {@link SmcEnumTypeHandler} in this application.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MappedEnumTypes
{
	public Class<? extends MappedEnum>[] value();
}