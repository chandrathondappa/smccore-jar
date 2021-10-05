/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

/**
 * Represents a component that may have a value or it may not.
 */
public interface OptionalComponentValue
{
	/**
	 * Checks if the value of this component is empty or not.
	 * @return True if the component is empty and has no value provided. False if it has a value provided.
	 */
	public boolean isValueEmpty();
}
