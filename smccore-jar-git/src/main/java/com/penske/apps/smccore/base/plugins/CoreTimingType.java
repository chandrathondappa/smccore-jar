/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

/**
 * Represents all timing types logged by the SMC core.
 */
public enum CoreTimingType implements TimingType
{
	/** Logs all query invocations */
	SMC_CORE_QUERY("Queries"),
	GROUP_UNIT_RANGE("Group Unit Ranges"),
	;
	
	private final String description;
	
	private CoreTimingType(String description)
	{
		this.description = description;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getDescription()
	{
		return description;
	}
}
