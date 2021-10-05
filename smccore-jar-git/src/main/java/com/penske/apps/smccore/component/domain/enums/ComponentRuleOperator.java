/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;
import com.penske.apps.smccore.base.util.DateUtil;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleCriteria;
import com.penske.apps.smccore.component.domain.RuleCriteriaGroup;
import com.penske.apps.smccore.component.engine.ComponentModel;

/**
 * An operator that can be applied to component values to determine if a rule criteria is satisfied when running component visibility rules.
 * @see Rule
 * @see RuleCriteriaGroup
 * @see RuleCriteria
 */
public enum ComponentRuleOperator implements MappedEnum
{
	LESS_THAN("<") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			int componentId = component.getComponentMaster().getComponentId();
			ComponentType componentType = component.getComponentMaster().getComponentType();
			
			//Can't use < with text components
			switch(componentType)
			{
			case TEXT:
			case COMMENT:
			case DATE:
				throw new UnsupportedOperationException(getUnsupportedErrorMessage(componentId, componentType));
			case NUMERIC:
			case YEAR:
				BigDecimal componentValue = component.getComponentValueAsNumber();
				//Null is not less than or greater than anything
				if(StringUtils.isBlank(valueToTest) || componentValue == null)
					return false;
				//Can't compare a numeric component to something that isn't a number
				if(!NumberUtils.isNumber(valueToTest))
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				BigDecimal numberToTest = new BigDecimal(valueToTest);
				return componentValue.compareTo(numberToTest) < 0;
			}
			
			throw new UnsupportedOperationException("Unrecognized component type: " + componentType);
		}
	},
	LESS_THAN_OR_EQUAL_TO("<=") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			int componentId = component.getComponentMaster().getComponentId();
			ComponentType componentType = component.getComponentMaster().getComponentType();
			
			//Can't use <= with text components
			switch(componentType)
			{
			case TEXT:
			case COMMENT:
			case DATE:
				throw new UnsupportedOperationException(getUnsupportedErrorMessage(componentId, componentType));
			case NUMERIC:
			case YEAR:
				BigDecimal componentValue = component.getComponentValueAsNumber();
				//Null is not less than or greater than anything
				if(StringUtils.isBlank(valueToTest) || componentValue == null)
					return false;
				//Can't compare a numeric component to something that isn't a number
				if(!NumberUtils.isNumber(valueToTest))
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				BigDecimal numberToTest = new BigDecimal(valueToTest);
				return componentValue.compareTo(numberToTest) <= 0;
			}
			
			throw new UnsupportedOperationException("Unrecognized component type: " + componentType);
		}
	},
	EQUAL("=") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			ComponentType componentType = component.getComponentMaster().getComponentType();
			
			switch(componentType)
			{
			case TEXT:
			case COMMENT:
				String componentValueString = component.getComponentValueAsString();
				return StringUtils.equals(componentValueString, valueToTest);
			case DATE:
				Date componentValueDate = component.getcomponentValueAsDate();
				//If both are null (or blank), that's ok.
				if(StringUtils.isBlank(valueToTest))
					return componentValueDate == null;
				//If only one is null, then they aren't equal
				if(componentValueDate == null)
					return false;
				Date valueToTestDate = DateUtil.parseDate(valueToTest);
				if(valueToTestDate == null)
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				//Two dates match if they are the same day
				return DateUtils.isSameDay(componentValueDate, valueToTestDate);
			case NUMERIC:
			case YEAR:
				BigDecimal componentValue = component.getComponentValueAsNumber();
				//If both are null (or blank), that's ok.
				if(StringUtils.isBlank(valueToTest))
					return componentValue == null;
				//If only one is null, then they aren't equal
				if(componentValue == null)
					return false;
				//Can't compare a numeric component to something that isn't a number
				if(!NumberUtils.isNumber(valueToTest))
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				BigDecimal numberToTest = new BigDecimal(valueToTest);
				return componentValue.compareTo(numberToTest) == 0;
			}
			
			throw new UnsupportedOperationException("Unrecognized component type: " + componentType);
		}
	},
	GREATER_THAN_OR_EQUAL_TO(">=") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			int componentId = component.getComponentMaster().getComponentId();
			ComponentType componentType = component.getComponentMaster().getComponentType();
			
			//Can't use >= with text components
			switch(componentType)
			{
			case TEXT:
			case COMMENT:
			case DATE:
				throw new UnsupportedOperationException(getUnsupportedErrorMessage(componentId, componentType));
			case NUMERIC:
			case YEAR:
				BigDecimal componentValue = component.getComponentValueAsNumber();
				//Null is not less than or greater than anything
				if(StringUtils.isBlank(valueToTest) || componentValue == null)
					return false;
				//Can't compare a numeric component to something that isn't a number
				if(!NumberUtils.isNumber(valueToTest))
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				BigDecimal numberToTest = new BigDecimal(valueToTest);
				return componentValue.compareTo(numberToTest) >= 0;
			}
			
			throw new UnsupportedOperationException("Unrecognized component type: " + componentType);
		}
	},
	GREATER_THAN(">") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			int componentId = component.getComponentMaster().getComponentId();
			ComponentType componentType = component.getComponentMaster().getComponentType();
			
			//Can't use > with text components
			switch(componentType)
			{
			case TEXT:
			case COMMENT:
			case DATE:
				throw new UnsupportedOperationException(getUnsupportedErrorMessage(componentId, componentType));
			case NUMERIC:
			case YEAR:
				BigDecimal componentValue = component.getComponentValueAsNumber();
				//Null is not less than or greater than anything
				if(StringUtils.isBlank(valueToTest) || componentValue == null)
					return false;
				//Can't compare a numeric component to something that isn't a number
				if(!NumberUtils.isNumber(valueToTest))
					throw new UnsupportedOperationException(getComparisonErrorMessage(component, valueToTest));
				
				BigDecimal numberToTest = new BigDecimal(valueToTest);
				return componentValue.compareTo(numberToTest) > 0;
			}
			
			throw new UnsupportedOperationException("Unrecognized component type: " + componentType);
		}
	},
	//FIXME: test
	EXISTS_ON_PO("E") {
		@Override public boolean evaluate(ComponentModel component, String valueToTest) {
			ComponentValue componentValue = component.getComponentValue();
			return componentValue != null && !componentValue.isValueEmpty();
		}
	}
	;
	
	private final String operator;
	
	private ComponentRuleOperator(String operator)
	{
		this.operator = operator;
	}

	/**
	 * Determines if the expression [component value] [operator] [valueToTest] is true or not.
	 * @param component The component model whose value should be tested against the given value
	 * @param valueToTest The value to compare against the component model's value
	 * @return True if the expression is true. False otherwise.
	 */
	public abstract boolean evaluate(ComponentModel component, String valueToTest);
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return operator;
	}

	protected String getUnsupportedErrorMessage(int componentId, ComponentType componentType)
	{
		return "Error evaluating component rules (component ID " + componentId + "). " + name() + " is not applicable to " + componentType.name() + " components.";
	}
	
	protected String getComparisonErrorMessage(ComponentModel componentModel, String valueToTest)
	{
		int componentId = componentModel.getComponentId();
		int masterId = componentModel.getMasterId();
		ComponentType componentType = componentModel.getComponentMaster().getComponentType();
		return "Error evaluating component rules (component ID " + componentId + ", master ID " + masterId + "). Can not compare a " + componentType.name() + " component to the value " + valueToTest + ", because it is not the right type. (Operator " + name() + ")";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getOperator()
	{
		return operator;
	}
}
