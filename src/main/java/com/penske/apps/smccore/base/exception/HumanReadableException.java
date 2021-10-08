/**
 * @author John Shiffler
 */
package com.penske.apps.smccore.base.exception;

/**
 * This class represents an exception with a message suitible for display to the user.
 * It is intended so that business logic can throw one of these, and it can be caught by the display layer and displayed appropriately.
 */
public class HumanReadableException extends RuntimeException
{
	private static final long serialVersionUID = 2204903442468624871L;
	
	/**
	 * True if the error is a critical error (should be logged as an error and emailed to the developers); false if it is non-critical (does not get emailed, and gets logged as a warning).
	 * A value of "true" indicates that this exception is a programming problem, not a user error. A value of false usually indicates a user error (ex: user entered an invalid value in a field and
	 * JavaScript validation did not - or could not - detect it).
	 */
	private final boolean critical;
	private final String humanReadableMessage;
	
	/**
	 * @param message A human-readable message to print to the user if this exception bubbles all the way up to the top layer of the application without being handled
	 * @param critical True if the error is a critical error (should be logged as an error and emailed to the developers); false if it is non-critical (does not get emailed, and gets logged as a warning)
	 */
	public HumanReadableException(String message, boolean critical)
	{
		super(message);
		this.critical = critical;
		this.humanReadableMessage = message;
	}

	/**
	 * @param message A human-readable message to print to the user if this exception bubbles all the way up to the top layer of the application without being handled
	 * @param cause The exception that caused this exception to get thrown
	 * @param critical True if the error is a critical error (should be logged as an error and emailed to the developers); false if it is non-critical (does not get emailed, and gets logged as a warning)
	 */
	public HumanReadableException(String message, Throwable cause, boolean critical)
	{
		super(message, cause);
		this.critical = critical;
		this.humanReadableMessage = message;
	}

	/**
	 * @param originalExceptionMessage The original message from the cause. This message is the one that will be recorded in the logs.
	 * @param humanReadableMessage A human-readable message to print to the user if this exception bubbles all the way up to the top layer of the application without being handled
	 * @param cause The exception that caused this exception to get thrown
	 * @param critical True if the error is a critical error (should be logged as an error and emailed to the developers); false if it is non-critical (does not get emailed, and gets logged as a warning)
	 */
	public HumanReadableException(String originalExceptionMessage, String humanReadableMessage, Throwable cause, boolean critical)
	{
		super(originalExceptionMessage, cause);
		this.critical = critical;
		this.humanReadableMessage = humanReadableMessage;
	}
	
	/**
	 * @return True if this exception is a critical error (should be logged as an error and emailed to the developers); false if it is non-critical (does not get emailed, and gets logged as a warning)
	 */
	public boolean isCritical()
	{
		return critical;
	}

	/**
	 * @return the humanReadableMessage
	 */
	public String getHumanReadableMessage()
	{
		return humanReadableMessage;
	}


}
