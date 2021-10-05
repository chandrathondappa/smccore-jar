/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.exception;

import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;

/**
 * An exception indicating something went wrong when processing the rules (ex: there was a circular reference in the rules).
 * If one of these is thrown, the contents of the included result container should not be trusted as canonical, because they may be inaccurate if the rules didn't get a chance to finish.
 */
public class ComponentRuleException extends HumanReadableException
{
	private static final long serialVersionUID = -7542608304411001508L;
	
	/** The last result that did come out of the rules engine before the exception was thrown. This may help in debugging, but should not be relied upon for accuracy. */
	private final ComponentVisibilityFilterData componentData;
	
	public ComponentRuleException(String message, ComponentVisibilityFilterData componentData, boolean critical)
	{
		super(message, critical);
		this.componentData = componentData;
	}
	
	/**
	 * @return the resultContainer
	 */
	public ComponentVisibilityFilterData getComponentData()
	{
		return componentData;
	}
}
