/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.RuleResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaResult;

/**
 * A single criteria used to determine whether or not a rule applies.
 * Criteria can be grouped together into criteria groups.
 * @see RuleCriteriaGroup
 */
public class RuleCriteria
{
	/** The internal database ID for this criteria. */
	private Integer ruleDefId;
	/** The ID of the rule this criteria belongs to */
	private int ruleId;
	/** The number of the criteria group this criteria belongs to */
	private int criteriaGroup;
	/** The ID of the component whose value will be compared when evaluating this criteria. */
	private int componentId;
	/** The comparison operator to use when evaluating this criteria. */
	private ComponentRuleOperator operator;
	/** The static value that will be compared to the component's value when evaluating this criteria. */
	private String componentValue;
	
	/** Null constructor - MyBatis only */
	protected RuleCriteria() {}
	
	/** Package-private constructor - only for use with the {@link RuleBuilder}. */
	RuleCriteria(int ruleId, int criteriaGroup, int componentId, ComponentRuleOperator operator, String componentValue)
	{
		if(operator == null)
			throw new IllegalArgumentException("Operator is required to make a rule criteria (component ID " + componentId + ", rule ID " + ruleId + ")");
		
		this.ruleId = ruleId;
		this.criteriaGroup = criteriaGroup;
		this.componentId = componentId;
		this.operator = operator;
		this.componentValue = componentValue;
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Criteria " + ruleDefId + " (Rule " + ruleId + "): Component " + componentId + " " + operator.getOperator() + " " + componentValue + "}";
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Tests whether this criteria is satisfied or not
	 * @param components The set of all relevant component values, from which this criteria will pull the requisite component and compare the value.
	 * @param masterId The unit master or loadsheet containing the set of components this rule is being evaluated against
	 * @return A result object indicating whether the criteria was satisfied or not.
	 */
	public RuleCriteriaResult evaluate(ComponentVisibilityFilterData componentData, int masterId)
	{
		ComponentModel component = componentData.getSingleComponentForRules(componentId, masterId);
		
		//If the component master object is missing, the rule is meaningless, since there is no component value.
		// This could happen, for instance, if the rules engine is set to ignore invisible components and the component in question is not visible on any template.
		if(component == null)
		{
			//Check to see if the component is present in the template, but just not visible, or if it's not even there
			ComponentModel componentForDisplay = componentData.getSingleComponent(componentId, masterId);
			if(componentForDisplay == null)
				return new RuleCriteriaResult(this, null, true, false, RuleResult.COMPONENT_NOT_FOUND);
			else
				return new RuleCriteriaResult(this, componentForDisplay, true, false, RuleResult.COMPONENT_NOT_VISIBLE);
		}
		
		boolean satisfied = operator.evaluate(component, componentValue);
		return new RuleCriteriaResult(this, component, true, satisfied, null);
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the ruleDefId
	 */
	public Integer getRuleDefId()
	{
		return ruleDefId;
	}

	/**
	 * @return the ruleId
	 */
	public int getRuleId()
	{
		return ruleId;
	}

	/**
	 * @return the componentId
	 */
	public int getComponentId()
	{
		return componentId;
	}

	/**
	 * @return the operator
	 */
	public ComponentRuleOperator getOperator()
	{
		return operator;
	}

	/**
	 * @return the componentValue
	 */
	public String getComponentValue()
	{
		return componentValue;
	}

	/**
	 * @return the criteriaGroup
	 */
	public int getCriteriaGroup()
	{
		return criteriaGroup;
	}
}
