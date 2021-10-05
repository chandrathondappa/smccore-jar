/**
] * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.exception;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.validation.ValidationErrorCode;

/**
 * Exception class that the application can throw to indicate server-side validation did not pass for a given value.
 * This counts as a non-critical HumanReadableException, so getting one of these does not indicate there is a programming bug, but rather that the user provided incorrect data.
 * 
 * Generally, this should be thrown (if at all) by the controller or service layer if we need to manually validate the user's values before handing it to the domain layer.
 *  In general, this exception should be used as a last resort for validation - if the JSP can not be structured so submitting bad values is impossible,
 *  and if JavaScript validation is too complicated, and if Bean Validation is not practical, then the Controller or Service method can check manually and throw one of these if the validation fails.
 *  
 * 
 * The difference between this and the {@link BusinessRuleException} is that this one does not indicate a programming bug, but the other kind does.
 */
public class AppValidationException extends HumanReadableException
{
	private static final long serialVersionUID = -5194481392511907823L;
	
	private final ValidationErrorCode<?> errorCode;
	private final String path;
	private final String detailLabel;

	/**
	 * Creates an exception to represent a single validation error, using just the message given.
	 * @param message A human-readable description of what went wrong
	 */
	public AppValidationException(String message)
	{
		this(message, null, null, null);
	}
	
	/**
	 * Creates a new exception to represent a single validation error, using the default path to the field with the invalid value.
	 * @param errorCode Enum constant denoting what went wrong.
	 */
	public AppValidationException(ValidationErrorCode<?> errorCode)
	{
		this(errorCode, errorCode.getDefaultPath());
	}
	
	/**
	 * Creates a new exception to represent a single validation error.
	 * @param errorCode Enum constant denoting what went wrong.
	 * @param path The path of the field within the validated object that contains the invalid value.
	 */
	public AppValidationException(ValidationErrorCode<?> errorCode, String path)
	{
		this(errorCode, path, null);
	}

	/**
	 * Creates a new exception to represent a single validation error.
	 * @param message Optional. The human-readable message explaining what went wrong
	 * @param errorCode Optional. Enum constant denoting what went wrong
	 * @param path Optional. The path of the field that contains the invalid value within the validated object
	 * @param detailLabel Optional. If provided, this text is a human-readable label for what the index in the "path" represents (if any).
	 * 	So, if the path is "deliverables[285412]", and the detailLabel is "Unit Number", then the application is indicating the error occurred on unit number 285412.
	 */
	public AppValidationException(ValidationErrorCode<?> errorCode, String path, String detailLabel)
	{
		this(null, errorCode, path, detailLabel);
	}
	
	/**
	 * Creates a new exception to represent a single validation error.
	 * @param message Optional. The human-readable message explaining what went wrong
	 * @param errorCode Optional. Enum constant denoting what went wrong
	 * @param path Optional. The path of the field that contains the invalid value within the validated object
	 * @param detailLabel Optional. If provided, this text is a human-readable label for what the index in the "path" represents (if any).
	 * 	So, if the path is "deliverables[285412]", and the detailLabel is "Unit Number", then the application is indicating the error occurred on unit number 285412.
	 */
	public AppValidationException(String message, ValidationErrorCode<?> errorCode, String path, String detailLabel)
	{
		super(StringUtils.isBlank(message) ? "An error occurred while validating: " + (errorCode == null ? "Unknown" : errorCode.getMessageKey()) : message, false);
		this.path = path;
		this.errorCode = errorCode;
		this.detailLabel = detailLabel;
	}
	
	/**
	 * @return the errorCode
	 */
	public ValidationErrorCode<?> getErrorCode()
	{
		return errorCode;
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	public String getDetailLabel()
	{
		return detailLabel;
	}
}
