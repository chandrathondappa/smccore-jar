/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.validation;

/**
 * Varying levels of validation error severity.
 */
public enum Severity
{
	/** A hard-stop. Program execution should halt until the user fixes the issue. */
	ERROR,
	/** A soft-stop. Program execution can continue in some circumstances if the user chooses to continue. */
	WARNING,
	;
}
