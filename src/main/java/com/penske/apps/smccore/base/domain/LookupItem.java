/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

/**
 * A single global configuration value from SMC_LOOKUP.
 */
public class LookupItem
{
	/** Internal database ID */
	private int lookupId;
	/** The name of this lookup. This is how lookups are referred to in code. Lookups of the same name usually relate to each other. */
	private String name;
	/** The value associated with this lookup. */
	private String value;
	/** Human-readable description of the lookup value */
	private String description;
	/**
	 * The sequence in which this lookup should be read or acted upon.
	 * Not all lookups have a meaningful way of ordering them, but this is here for those that do.
	 */
	private int sequence;
	
	/** MyBatis only */
	protected LookupItem() {}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Lookup - " + name + ": " + value + "}";
	}
	
	//***** DEFUALT ACCESSORS *****//
	/**
	 * @return the lookupId
	 */
	public int getLookupId()
	{
		return lookupId;
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @return the sequence
	 */
	public int getSequence()
	{
		return sequence;
	}

	public String getDescription()
	{
		return description;
	}
}
