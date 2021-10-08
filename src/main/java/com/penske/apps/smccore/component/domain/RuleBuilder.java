/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.domain.enums.ProgramComponent;
import com.penske.apps.smccore.component.domain.enums.RuleType;

/**
 * A builder class to construct new rules and their criteria easily. Primarily intended for use internally by the rules engine,
 * 	although application code could theoretically use it to create rules programmatically.
 */
//FIXME: document
//FIXME: test
public class RuleBuilder
{
	private final int ruleId;
	private final int templateId;
	private final String name;
	private final RuleType ruleType;
	private final Map<Integer, List<RuleCriteria>> criteriaByGroup = new HashMap<Integer, List<RuleCriteria>>();
	
	public RuleBuilder(int ruleId, int templateId, String name, RuleType ruleType)
	{
		this.ruleId = ruleId;
		this.templateId = templateId;
		this.name = name;
		this.ruleType = ruleType;
	}
	
	public RuleBuilder addCriteria(int criteriaGroup, ProgramComponent programComponent, ComponentRuleOperator operator, String componentValue)
	{
		return this.addCriteria(criteriaGroup, programComponent.getComponentId(), operator, componentValue);
	}
	
	public RuleBuilder addCriteria(int criteriaGroup, int componentId, ComponentRuleOperator operator, String componentValue)
	{
		RuleCriteria criteria = new RuleCriteria(ruleId, criteriaGroup, componentId, operator, componentValue);
		
		if(criteriaByGroup.get(criteriaGroup) == null)
			criteriaByGroup.put(criteriaGroup, new ArrayList<RuleCriteria>());
		criteriaByGroup.get(criteriaGroup).add(criteria);
		
		return this;
	}
	
	public Rule build()
	{
		List<RuleCriteriaGroup> groups = new ArrayList<RuleCriteriaGroup>();
		for(int criteriaGroup : criteriaByGroup.keySet())
		{
			List<RuleCriteria> criteriaList = criteriaByGroup.get(criteriaGroup);
			RuleCriteriaGroup group = new RuleCriteriaGroup(ruleId, criteriaGroup, criteriaList);
			groups.add(group);
		}
		
		Rule rule = new Rule(ruleId, templateId, name, name, ruleType, groups);
		return rule;
	}
}
