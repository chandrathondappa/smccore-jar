/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.math.BigDecimal;
import java.util.Date;

import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.OptionalComponentValue;

/**
 * Reflects a particular vehicle component for a particular unit, whose visibility may be different from that of its base {@link ComponentMaster}.
 */
//FIXME: test
public class ComponentModel implements OptionalComponentValue, NestableComponentData
{
	/** The master component record that serves as a template for this component. */
	private final ComponentMaster componentMaster;
	/** The actual value of this component for this particular unit master. */
	private final ComponentValue componentValue;
	/** The component visibility after running rules. */
	private final Visibility ruleVisibility;
	/** The final component visibility after rules have been run and conflicts have been resolved. */
	private final Visibility finalVisibility;
	
	/** Whether this component is in conflict with another one on this unit, and how the conflict has been resolved, if at all. */
	private final ConflictStatus conflictStatus;
	
	/**
	 * Creates a new object to represent a component on screen and in the rules.
	 * @param componentMaster The master component record that serves as a template for this component.
	 * @param componentValue The actual value of this component for a particular unit master
	 * @param ruleVisibility Optional. Whether or not this component should be visible, required, or not visible.
	 * 	If this is null, the visibility from the component master will be used.
	 */
	public ComponentModel(ComponentMaster componentMaster, ComponentValue componentValue, Visibility ruleVisibility)
	{
		this(componentMaster, componentValue, ruleVisibility, null, null);
	}
	
	/**
	 * Creates a new object to represent a component on screen and in the rules.
	 * @param componentMaster The master component record that serves as a template for this component.
	 * @param componentValue The actual value of this component for a particular unit master
	 * @param ruleVisibility Optional. The visibility of this component after running the rules.
	 * 	If this is null, the base visibility from the component master will be used.
	 * @param finalVisibility Optional. The visibility of this component after conflicts, if any, are resolved.
	 * 	If this is null, the rule visibility will be used.
	 * @param conflictStatus Optional. Constant indicating whether this component is in conflict with some other component
	 * 	on the same unit with the same ID or not, and what the resolution is, if any.
	 * 	A value of null indicates this component is not in conflict.
	 */
	public ComponentModel(ComponentMaster componentMaster, ComponentValue componentValue, Visibility ruleVisibility, Visibility finalVisibility, ConflictStatus conflictStatus)
	{
		Integer componentMasterId = componentMaster == null ? null : componentMaster.getComponentId();
		Integer smcComponentId = componentValue == null ? null : componentValue.getComponentId();
		
		//We have to have a component master
		if(componentMaster == null)
		{
			String componentIdString = smcComponentId == null ? "" : " (ID " + smcComponentId + ")";
			throw new IllegalArgumentException("Component" + componentIdString + " not found in component master.");
		}
		
		//Validate that the two pieces match
		if(componentValue != null)
		{
			if(componentMasterId.intValue() != smcComponentId.intValue())
				throw new IllegalArgumentException("Tried to associate component value for " + smcComponentId + " with component master " + componentMasterId + ", but component IDs do not match.");
			if(componentMaster.getComponentType() != componentValue.getComponentType())
				throw new IllegalArgumentException("Tried to associate component value for " + smcComponentId + " with component master " + componentMasterId + ", but types do not match (" + componentMaster.getComponentType() + " vs. " + componentValue.getComponentType() + ")");
		}

		//Default the rule visibility to the base visibility if not provided.
		ruleVisibility = ruleVisibility == null ? componentMaster.getVisibility() : ruleVisibility;
		//Default the final visiblity to the rule visibility if not provided.
		finalVisibility = finalVisibility == null ? ruleVisibility : finalVisibility;

		//A component should only have different final and rule visibilities if it is in conflict
		if(finalVisibility != ruleVisibility && conflictStatus == null)
			throw new IllegalArgumentException("If a component is not in conflict, it can't have a final visibility different from its rule visibility. (Component ID " + componentMasterId + ", Master ID " + componentMaster.getMasterId() + ")");
		
		this.componentMaster = componentMaster;
		this.componentValue = componentValue;
		this.ruleVisibility = ruleVisibility;
		this.finalVisibility = finalVisibility;
		this.conflictStatus = conflictStatus;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return new StringBuilder("{ComponentModel ")
			.append("(")
				.append("ID ").append(componentMaster.getComponentId())
				.append(", Master ID ").append(componentMaster.getMasterId())
			.append(")")
			.append(" Value: ").append(getComponentValueAsString())
			.append(", Visibility: ").append(finalVisibility == null ? "" : finalVisibility.getUnitTemplateCode())
			.append(", Conflict: ").append(conflictStatus == null ? "" : conflictStatus.getCode())
		.append("}").toString();
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Renders the value of a component as a string, based on the component type.
	 * @return The component's value, rendered as a string.
	 */
	public String getComponentValueAsString()
	{
		ComponentType type = componentMaster.getComponentType();
		
		if(componentValue == null)
			return "";
		
		return type.getComponentValueAsString(componentValue.getTextValue(), componentValue.getNumericValue(), componentValue.getDateValue(), componentMaster.getDecimalPositions(), false);
	}
	
	/**
	 * Returns the value of the component as a BigDecimal, if it has a numeric component type, or null if it has a text component type.
	 * @return The numeric value of the component, or null if it doesn't have a numeric value.
	 */
	public BigDecimal getComponentValueAsNumber()
	{
		ComponentType type = componentMaster.getComponentType();
		
		if(componentValue == null)
			return null;
		
		switch(type)
		{
		case NUMERIC:
		case YEAR:
			return componentValue.getNumericValue();
		case TEXT:
		case DATE:
		case COMMENT:
			return null;
		}
		
		return null;
	}
	
	/**
	 * Returns the value of the component as a Date, if it has a date component type, or null if it any other component type.
	 * @return The date value of the component, or null if it doesn't have a date value.
	 */
	public Date getcomponentValueAsDate()
	{
		ComponentType type = componentMaster.getComponentType();
		
		if(componentValue == null)
			return null;
		
		switch(type)
		{
		case DATE:
			return componentValue.getDateValue();
		case NUMERIC:
		case YEAR:
		case TEXT:
		case COMMENT:
			return null;
		}
		
		return null;
	}

	public Visibility getBaseVisibility()
	{
		return componentMaster.getVisibility();
	}
	
	/**
	 * @return True if this component must be filled in; false if it is optional.
	 */
	public boolean isRequired()
	{
		return finalVisibility == Visibility.REQUIRED;
	}

	/**
	 * @return True if this component is allowed to be edited; false if it is not editable.
	 */
	public boolean isEditable()
	{
		if(finalVisibility == null)
			return false;
		return finalVisibility.isEditable();
	}
	
	/**
	 * @return True if this component should be visible on a given template. 
	 */
	public boolean isVisible()
	{
		if(finalVisibility == null)
			return false;
		return finalVisibility.isVisible();
	}

	/** {@inheritDoc} */
	public int getMasterId()
	{
		return componentMaster.getMasterId();
	}
	
	/** {@inheritDoc} */
	@Override
	public int getComponentId()
	{
		return componentMaster.getComponentId();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isValueEmpty()
	{
		return componentValue == null || componentValue.isValueEmpty();
	}
	
	public <T extends ComponentMaster> T unwrapComponentMaster()
	{
		@SuppressWarnings("unchecked")	//This can potentially be a dangerous cast, but we assume the caller will use the right implementation class to unwrap.
		T unwrapped = (T) componentMaster;
		return unwrapped;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public ComponentMaster getComponentMaster()
	{
		return componentMaster;
	}

	public ComponentValue getComponentValue()
	{
		return componentValue;
	}
	
	public Visibility getRuleVisibility()
	{
		return ruleVisibility;
	}

	public Visibility getFinalVisibility()
	{
		return finalVisibility;
	}

	public ConflictStatus getConflictStatus()
	{
		return conflictStatus;
	}
}