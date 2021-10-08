/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

/**
 * One of the various things (usually, method calls) that the application can track timing information for.
 */
public interface TimingType
{
	/**
	 * Gets a human-readable description of the timing type for printing in the logs.
	 * @return The human-readable description of this timing type.
	 */
	public String getDescription();
}
