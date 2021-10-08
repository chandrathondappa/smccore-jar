/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * Describes what type of date is out of compliance, if any, in a given unit master.
 */
public enum CommentRequiredComplianceType implements MappedEnum
{
	PRODUCTION("Production"),
	DELIVERY("Delivery"),
	;
	
	private final String label;
	
	private CommentRequiredComplianceType(String label)
	{
		this.label = label;
	}

	public static CommentRequiredComplianceType findByLabel(String label)
	{
		for(CommentRequiredComplianceType value : values())
		{
			if(value.getLabel().equals(label))
				return value;
		}
		
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return label;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getLabel()
	{
		return label;
	}
}
