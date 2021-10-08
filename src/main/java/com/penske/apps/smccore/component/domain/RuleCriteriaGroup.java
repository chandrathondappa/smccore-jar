/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.RuleResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaGroupResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaResult;

/**
 * A grouping of multiple criteria for determining whether a rule applies.
 * A rule can have more than one criteria group.
 * A criteria group contains one or more rule criteria.
 * Criteria in a group are AND'ed together. That is, in order for a criteria group to be true,
 * 	all criteria in the group have to be true.
 */
public class RuleCriteriaGroup
{
	/** The ID of the rule this criteria group belongs to. */
	private int ruleId;
	/** The number of this criteria group within the rule - not used by logic, but used for display order and identification of the group on screen. */
	private int criteriaGroup;
	/** The criteria that are evaluated when determining whether this criteria group is true. */
	private List<RuleCriteria> criteriaList = new ArrayList<RuleCriteria>();
	
	/** Null constructor - MyBatis only */
	protected RuleCriteriaGroup() {}

	/** Package-private constructor - only for use with the {@link RuleBuilder}. */
	RuleCriteriaGroup(int ruleId, int criteriaGroup, List<RuleCriteria> criteriaList)
	{
		if(criteriaList == null)
			criteriaList = Collections.emptyList();
		
		this.ruleId = ruleId;
		this.criteriaGroup = criteriaGroup;
		this.criteriaList.addAll(criteriaList);
		
		for(RuleCriteria criteria : criteriaList)
		{
			if(criteria.getRuleId() != ruleId)
				throw new IllegalArgumentException("Can not add criteria for rule " + criteria.getRuleId() + " to a criteria group for rule " + ruleId);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Criteria Group - Rule " + ruleId + ", Group " + criteriaGroup + " (" + criteriaList.size() + " criteria)}";
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Tests whether this criteria group is satisfied or not
	 * A group is satisfied if all criteria in it are satisfied.
	 * @param components The set of all relevant component values, from which this criteria group will pull the requisite components and compare the values.
	 * @param masterId The unit master or loadsheet containing the set of components this rule is being evaluated against
	 * @return A result object indicating whether the criteria group was satisfied or not.
	 */
	public RuleCriteriaGroupResult evaluate(ComponentVisibilityFilterData componentData, int masterId)
	{
		//Starts out satisfied, but it will become false if any criteria is false
		boolean satisfied = true;
		List<RuleCriteriaResult> criteriaResults = new ArrayList<RuleCriteriaResult>();
		for(RuleCriteria criteria : criteriaList)
		{
			RuleCriteriaResult criteriaResult;
			if(satisfied)
			{
				//If nothing has made the group false yet, evaluate the next criteria and include its result
				criteriaResult = criteria.evaluate(componentData, masterId);
				satisfied = satisfied && criteriaResult.isSatisfied();
			}
			else
			{
				//If we've hit a criteria that makes the group false, then we can short-circuit the evaluation,
				// since nothing else will possibly make the criteria group evaluate to true.
				int componentId = criteria.getComponentId();
				ComponentModel component = componentData.getSingleComponentForRules(componentId, masterId);
				criteriaResult = new RuleCriteriaResult(criteria, component, false, false, RuleResult.SKIPPED);
			}
			
			criteriaResults.add(criteriaResult);
		}
		return new RuleCriteriaGroupResult(this, true, satisfied, null, criteriaResults);
	}
	
	/**
	 * @return the criteria
	 */
	public List<RuleCriteria> getCriteria()
	{
		return Collections.unmodifiableList(criteriaList);
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the ruleId
	 */
	public int getRuleId()
	{
		return ruleId;
	}

	/**
	 * @return the criteriaGroup
	 */
	public int getCriteriaGroup()
	{
		return criteriaGroup;
	}
}
