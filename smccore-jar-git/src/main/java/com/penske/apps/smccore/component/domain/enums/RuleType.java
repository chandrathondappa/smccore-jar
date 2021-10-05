/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * The module of SMC that a given rule applies to.
 */
public enum RuleType implements MappedEnum
{
	/** The rule applies to components that Penske provides values for (in either PO or CO lodasheets) */
	LOADSHEET("L"),
	/** The rule applies to components the vendor provides values for (via unit details or unit template uploads) */
	UNIT_TEMPLATE("U")
	;
	
	/** Identifier to map this to the database */
	private final String code;
	
	private RuleType(String code)
	{
		this.code = code;
	}

	public static RuleType findByCode(String code)
	{
		for(RuleType ruleType : values())
		{
			if(ruleType.getCode().equals(code))
				return ruleType;
		}
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return code;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getCode()
	{
		return code;
	}
}
