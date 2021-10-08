/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.RuleResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaGroupResult;

/**
 * A single rule that can be evaluated based on component values, and which can drive component visibility.
 * Each rule is composed of one or more criteria groups.
 * Each criteria group is composed of one or more criteria.
 * 
 * The rule's overall outcome is true if any one of the criteria groups is true. (i.e. Criteria Groups are OR'ed together)
 * A criteria group is true if all of its criteria are true. (i.e. Criteria are AND'ed together)
 */
public class Rule
{
	/** Internal database ID for this rule */
	private int ruleId;
	/** The ID of the template this rule applies to. */
	private int templateId;
	/** Short human-readable name for this rule */
	private String name;
	/** Longer human-readable description for this rule */
	private String description;
	/** The module of the application this rule applies to. */
	private RuleType ruleType;
	/** The criteria groups that are evaluated as part of evaluating this rule. */
	private List<RuleCriteriaGroup> criteriaGroupList = new ArrayList<RuleCriteriaGroup>();

	/** Null constructor - MyBatis only */
	protected Rule() {}

	/** Package-private constructor - only for use with the {@link RuleBuilder}. */
	Rule(int ruleId, int templateId, String name, String description, RuleType ruleType, List<RuleCriteriaGroup> criteriaGroupList)
	{
		if(StringUtils.isBlank(name))
			throw new IllegalArgumentException("Rule name is required: ID " + ruleId);
		if(ruleType == null)
			throw new IllegalArgumentException("Rule type is required: ID " + ruleId);
		if(criteriaGroupList == null)
			criteriaGroupList = Collections.emptyList();
		
		this.ruleId = ruleId;
		this.templateId = templateId;
		this.name = name;
		this.description = description;
		this.ruleType = ruleType;
		this.criteriaGroupList.addAll(criteriaGroupList);
		
		for(RuleCriteriaGroup group : criteriaGroupList)
		{
			if(group.getRuleId() != this.getRuleId())
				throw new IllegalArgumentException("Can not add criteria group for rule " + group.getRuleId() + " to rule " + ruleId);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Rule " + ruleId + " (" + name + ") - " + criteriaGroupList.size() + " criteria groups}";
	}
	
	//***** MODIFIED ACCESSORS *****//
	public RuleResult evaluate(ComponentVisibilityFilterData componentData, int masterId)
	{
		//Starts out not satisfied, and at least one criteria group has to be true to make the rule true.
		boolean satisfied = false;
		List<RuleCriteriaGroupResult> groupResults = new ArrayList<RuleCriteriaGroupResult>();
		for(RuleCriteriaGroup group : criteriaGroupList)
		{
			RuleCriteriaGroupResult groupResult;
			if(!satisfied)
			{
				//If nothing has made the rule true yet, evaluate the next criteria group and include its result
				groupResult = group.evaluate(componentData, masterId);
				satisfied = satisfied || groupResult.isSatisfied();
			}
			else
			{
				//If we've hit a group that makes the rule true, then we can short-circuit the evaluation,
				// since nothing else will possibly make the rule evaluate to false.
				groupResult = RuleCriteriaGroupResult.createSkippedGroupInstance(group, componentData, masterId);
			}
			
			groupResults.add(groupResult);
		}
		return new RuleResult(this, true, satisfied, null, groupResults);
	}
	
	/**
	 * @return the criteriaGroups
	 */
	public List<RuleCriteriaGroup> getCriteriaGroups()
	{
		return Collections.unmodifiableList(criteriaGroupList);
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
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	public RuleType getRuleType()
	{
		return ruleType;
	}

	public int getTemplateId()
	{
		return templateId;
	}
}
