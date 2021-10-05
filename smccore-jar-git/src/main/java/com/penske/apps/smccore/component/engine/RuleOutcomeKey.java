/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

/**
 * A business key object used to identify a particular rule outcome. Combines a parent container ID (the master ID) with a component ID.
 * This class is intended primarily for use inside the rules engine.
 */
public class RuleOutcomeKey
{
	/** The ID of the unit master the outcome is attached to. */
	private final int masterId;
	/** The ID of the component the outcome will affect. */
	private final int componentId;

	/**
	 * Creates a new rule outcome key.
	 * This is private since it is intended to be created only by the rules engine.
	 * @param masterId The ID of the unit master the outcome is attached to.
	 * @param componentId The ID of the component the outcome will affect.
	 */
	RuleOutcomeKey(int masterId, int componentId)
	{
		this.masterId = masterId;
		this.componentId = componentId;
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{OutcomeKey (Master " + masterId + ", Component " + componentId + ")}";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + componentId;
		result = prime * result + masterId;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleOutcomeKey other = (RuleOutcomeKey) obj;
		if (componentId != other.componentId)
			return false;
		if (masterId != other.masterId)
			return false;
		return true;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getMasterId()
	{
		return masterId;
	}

	public int getComponentId()
	{
		return componentId;
	}
	/** {@inheritDoc} */

}