/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.validation;

/**
 * An enum that lists the possible pre-formatted error codes that could be returned by the application, and their messages
 */
public interface ValidationErrorCode<T extends Enum<T>>
{
	/**
	 * Gets a constant indicating the severity of the error that occurred
	 * @return The severity
	 */
	public Severity getSeverity();
	
	/**
	 * The name of the property in validationMessages.properties containing the error message text.
	 * @return They property name in validationMessages.properties
	 */
	public String getMessageKey();
	
	/**
	 * The name of the field in the relevant domain object that is at fault if this error code occurs.
	 * @return The default name of the field that this error code validates 
	 */
	public String getDefaultPath();

	/**
	 * Gets the message key, but wrapped in brackets so that it is suitable for passing into JSR-303 Bean Validation.
	 * @return The message key, suitable for passing into bean validation
	 */
	public default String getWrappedMessageKey()
	{
		return "{" + getMessageKey() + "}";
	}
}
