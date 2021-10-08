/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * List of possible visibility values that a component can have. Determines whether a component is not visible, visible but not required, or visible and required.
 */
public enum Visibility implements MappedEnum
{
	//				Unit
	//			  Template	  Loadsheet 
	//				Code	    Code	Description
	NOT_VISIBLE(	"N",		"N",	"Not Visible"),
	NOT_VISIBLE_2(	null,		"V",	"Not Visible"),
	VISIBLE(		"V",		null,	"Visible"),
	EDITABLE(		"E",		"A",	"Editable"),
	REQUIRED(		"R",		"R",	"Required"),
	;

	/** The code used to represent this visibility in SMC Unit Templates. */
	private final String unitTemplateCode;
	/** The code used to represent this visibility in Loadsheet Templates */
	private final String loadsheetCode;
	/** A human-readable description of the visibility constant, for display in debugging screens. */
	private final String description;
	
	private Visibility(String unitTemplateCode, String loadsheetCode, String description)
	{
		this.unitTemplateCode = unitTemplateCode;
		this.loadsheetCode = loadsheetCode;
		this.description = description;
	}
	
	public static Visibility findByUnitTemplateCode(String code)
	{
		for(Visibility vis : values())
		{
			String visibilityCode = vis.getUnitTemplateCode();
			if(visibilityCode != null && visibilityCode.equals(code))
				return vis;
		}
		return null;
	}
	
	public static Visibility findByLoadsheetCode(String code)
	{
		for(Visibility vis : values())
		{
			String visibilityCode = vis.getLoadsheetCode();
			if(visibilityCode != null && visibilityCode.equals(code))
				return vis;
		}
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return unitTemplateCode;
	}
	
	/**
	 * @return True if this visibility constant allows the component to be visible
	 * 	(and hence whether the component should be considered as having a value when running rules).
	 */
	public boolean isVisible()
	{
		return this == VISIBLE || this == EDITABLE || this == REQUIRED;
	}
	
	/**
	 * @return True if this visibility constant allows the component to be updated by the user. False if it is not able to be edited.
	 */
	public boolean isEditable()
	{
		return this == EDITABLE || this == REQUIRED;
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the unit template code
	 */
	public String getUnitTemplateCode()
	{
		return unitTemplateCode;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	public String getLoadsheetCode()
	{
		return loadsheetCode;
	}
}
