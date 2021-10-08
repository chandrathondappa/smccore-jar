/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import com.penske.apps.smccore.base.domain.enums.SecurityFunction;

/**
 * Data about a particular security function and which tab it is associated with.
 */
public class SecurityFunctionView
{
	/** Internal database ID for the security function */
	private int securityFunctionId;
	/** The name used to refer to the security function in code - maps to the {@link SecurityFunction} enum */
	private String name;
	/** Human-readable description of the security function */
	private String description;
	/** The name used to refer in code to the tab the security function is assigned to */
	private String tabKey;
	/** A human-readable description of the tab the security function is assigned to */
	private String tabName;
	
	/** Null constructor - MyBatis only */
	protected SecurityFunctionView() {}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{SecurityFunction - " + name + " (Tab: " + tabName + ")}";
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Get the enum constant for this security function
	 * @return The enum constant value for this security function, or null if there is no matching enum constant.
	 */
	public SecurityFunction getSecurityFunction()
	{
		return SecurityFunction.findByName(name);
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getSecurityFunctionId()
	{
		return securityFunctionId;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getTabKey()
	{
		return tabKey;
	}

	public String getTabName()
	{
		return tabName;
	}
}
