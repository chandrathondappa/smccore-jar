/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.exception;

/**
 * Exception class the application can throw to indicate a business rule has been violated in a way the application did not expect.
 * This counts as a critical HumanReadableException, so getting one of these should not happen unless there's a programming bug.
 * 
 * Generally, these should be thrown by the domain layer (or rarely, the service layer) when checking preconditions and ensuring the domain objects are always in a valid state.
 * 
 * The difference between this and an {@link AppValidationException} is that this one indicates a programming bug and a condition that should never happen,
 * 	whereas the other one indicates a validation that we expected to do on the server did not pass.
 */
public class BusinessRuleException extends HumanReadableException
{
	private static final long serialVersionUID = -1786193764694111346L;

	/**
	 * Creates a new exception to indicate that a business rule has been violated at the domain layer.
	 * @param message A message suitible for display to the user in a popup
	 */
	public BusinessRuleException(String message)
	{
		super(message, true);
	}
	
	/**
	 * Creates a new exception to indicate that a business rule has been violated at the domain layer.
	 * @param message A message suitible for display to the user in a popup
	 * @param cause If this exception was caused by some other exception, it can be provided so the stack trace will reflect the root cause correctly.
	 */
	public BusinessRuleException(String message, Throwable cause)
	{
		super(message, cause, true);
	}
}
