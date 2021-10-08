/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleCriteria;
import com.penske.apps.smccore.component.domain.RuleCriteriaGroup;

/**
 * The results of evaluating a single rule, including how each of its criteria groups and criteria evaluated (or if they were skipped).
 */
public class RuleResult
{
	private static final String TRUE_TEXT = "True";
	private static final String FALSE_TEXT = "False";
	public static final String SKIPPED = "(Skipped)";
	public static final String COMPONENT_NOT_FOUND = "Cmp Not Found";
	public static final String COMPONENT_NOT_VISIBLE = "Cmp Not Visible";
	
	/** The rule whose results this object represents. */
	private final Rule rule;
	/** True if the rule was actually evaluated; false if was skipped due to short circuiting. */
	private final boolean evaluated;
	/** True if the rule should count as "true" (and hence have its outcomes applied). False otherwise. (Skipped rules get counted as false).*/
	private final boolean satisfied;
	/** If {@link satisfied} is false, this may optionally hold a reason that the rule evaluated to false. */
	private final String notSatisfiedReason;
	/** The results of evaluating each criteria group in this rule. */
	private final List<RuleCriteriaGroupResult> groupResults = new ArrayList<RuleCriteriaGroupResult>();
	
	/**
	 * Creates a new object to report on the results of running a single rule.
	 * Generally, only the {@link Rule} class should be creating these.
	 * @param rule The rule whose results are being reported.
	 * @param evaluated True if the rule was actually run; false if it was not run.
	 * @param satisfied True if the outcomes of the rule should be applied - false if they should not.
	 * 	(If {@code evaluated} is false, this should also be false - a rule can't apply unless it was first evaluated.)
	 * @param notSatisfiedReason Optional. If {@link satisfied} is true, this should be null.
	 * 	If {@link satisfied} is false, this may optionally contain a reason the rule evaluated to false (for display to the user)
	 * @param groupResults The results of running each criteria group in this rule
	 */
	public RuleResult(Rule rule, boolean evaluated, boolean satisfied, String notSatisfiedReason, List<RuleCriteriaGroupResult> groupResults)
	{
		if(rule == null)
			throw new IllegalArgumentException("Rule is required to create a rule result.");
		if(groupResults == null)
			groupResults = Collections.emptyList();
		
		this.rule = rule;
		this.evaluated = evaluated;
		//If the rule wasn't even evaluated, it can't possibly apply.
		this.satisfied = evaluated ? satisfied : false;
		
		if(this.satisfied && StringUtils.isNotBlank(notSatisfiedReason))
			throw new IllegalArgumentException("Rule " + rule.getRuleId() + " is satisfied, but the rule result has a not-satisfied reason: " + notSatisfiedReason);
		this.notSatisfiedReason = notSatisfiedReason;
		
		for(RuleCriteriaGroupResult groupResult : groupResults)
		{
			if(groupResult.getCriteriaGroup().getRuleId() != rule.getRuleId())
				throw new IllegalArgumentException("Error generating rule results. Criteria Group " + groupResult.getCriteriaGroup() + " does not belong to rule " + rule);
			this.groupResults.add(groupResult);
		}
	}
	
	/**
	 * Creates a result object for a rule that was completely skipped (possibly due to not being attached to any component outcomes that mattered).
	 * Populates all the child objects for criteria groups and criteria as having been skipped, too.
	 * @param rule The rule that was completely skipped.
	 * @param componentData Optional. Container object for rules, for pulling out human-readable component names.
	 * @param masterId The master containing the set of components this rule is being evaluated against
	 * @return The new result object.
	 */
	public static RuleResult createSkippedRuleInstance(Rule rule, ComponentVisibilityFilterData componentData, int masterId)
	{
		List<RuleCriteriaGroupResult> groupResults = new ArrayList<RuleCriteriaGroupResult>();
		for(RuleCriteriaGroup group : rule.getCriteriaGroups())
			groupResults.add(RuleCriteriaGroupResult.createSkippedGroupInstance(group, componentData, masterId));
		
		return new RuleResult(rule, false, false, RuleResult.SKIPPED, groupResults);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Rule Result (Rule ID " + rule.getRuleId() + ") - " + getTruthValue() + "}";
	}
	
	//***** HELPER CLASSES *****//
	/**
	 * Records the results of evaluating a single criteria group within a rule.
	 */
	public static class RuleCriteriaGroupResult
	{
		/** The criteria group being reported on by this object. */
		private final RuleCriteriaGroup criteriaGroup;
		/** True if this criteria group was evaluated. False if it was skipped due to short circuiting. */
		private final boolean evaluated;
		/** True if the criteria group should count as "true" when determining the overall rule's truth value. False otherwise. (Skipped groups get counted as false).*/
		private final boolean satisfied;
		/** If {@link satisfied} is false, this may optionally hold a reason that the criteria group evaluated to false. */
		private final String notSatisfiedReason;
		/** The results of evaluating each criteria in this group. */
		private final List<RuleCriteriaResult> criteriaResults = new ArrayList<RuleCriteriaResult>();
		
		/**
		 * Creates a new object to report on the results of evaluating a criteria group.
		 * @param criteriaGroup The group in question
		 * @param evaluated True if the group was actually evaluated. False if it was skipped due to short circuiting.
		 * @param satisfied True if the group should count as "true". False if it should count as "false". (Skipped groups count as false).
		 * @param notSatisfiedReason Optional. If {@link satisfied} is true, this should be null.
		 * 	If {@link satisfied} is false, this may optionally contain a reason the criteria group evaluated to false (for display to the user) 
		 * @param criteriaResults The results of evaluating each individual criteria in this group.
		 * 
		 * Generally only classes in the component rules engine should be creating these.
		 */
		public RuleCriteriaGroupResult(RuleCriteriaGroup criteriaGroup, boolean evaluated, boolean satisfied, String notSatisfiedReason, List<RuleCriteriaResult> criteriaResults)
		{
			if(criteriaGroup == null)
				throw new IllegalArgumentException("Criteria Group is required to create a criteria group result.");
			if(criteriaResults == null)
				criteriaResults = Collections.emptyList();
			
			this.criteriaGroup = criteriaGroup;
			this.evaluated = evaluated;
			//If the criteria group wasn't even evaluated, it can't possibly apply.
			this.satisfied = evaluated ? satisfied : false;
			
			if(this.satisfied && StringUtils.isNotBlank(notSatisfiedReason))
				throw new IllegalArgumentException("Criteria Group " + criteriaGroup.getRuleId() + "(" + criteriaGroup.getCriteriaGroup() + ") " + " is satisfied, but the result object has a not-satisfied reason: " + notSatisfiedReason);
			this.notSatisfiedReason = notSatisfiedReason;
			
			for(RuleCriteriaResult criteriaResult : criteriaResults)
			{
				RuleCriteria criteria = criteriaResult.getCriteria();
				if(criteria.getCriteriaGroup() != criteriaGroup.getCriteriaGroup())
					throw new IllegalArgumentException("Error generating rule results. Criteria " + criteria + " does not belong to criteria group " + criteriaGroup);
				if(criteria.getRuleId() != criteriaGroup.getRuleId())
					throw new IllegalArgumentException("Error generating rule results. Criteria " + criteria + " does not belong to rule " + criteriaGroup.getRuleId());
				this.criteriaResults.add(criteriaResult);
			}
		}
		
		/**
		 * Creates a result object for a criteria group that was completely skipped (possibly due to short-circuiting).
		 * Populates all the child objects for criteria as having been skipped, too.
		 * @param group The criteria group that was completely skipped.
		 * @param componentData Optional. Container object for rules, for pulling out human-readable component names.
		 * @param masterId The master containing the set of components this rule is being evaluated against
		 * @return The new result object.
		 */
		public static RuleCriteriaGroupResult createSkippedGroupInstance(RuleCriteriaGroup group, ComponentVisibilityFilterData componentData, int masterId)
		{
			List<RuleCriteriaResult> criteriaResults = new ArrayList<RuleCriteriaResult>();
			for(RuleCriteria criteria : group.getCriteria())
			{
				int componentId = criteria.getComponentId();
				ComponentModel componentModel = componentData == null ? null : componentData.getSingleComponent(componentId, masterId);
				criteriaResults.add(new RuleCriteriaResult(criteria, componentModel, false, false, RuleResult.SKIPPED));
			}
			
			return new RuleCriteriaGroupResult(group, false, false, RuleResult.SKIPPED, criteriaResults);
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{Criteria Group Result (Rule ID " + criteriaGroup.getRuleId() + ", Group " + criteriaGroup.getCriteriaGroup() + ") - " + getTruthValue() + "}";
		}
		
		//***** MODIFIED ACCESSORS *****//
		/**
		 * @return the criteriaResults
		 */
		public List<RuleCriteriaResult> getCriteriaResults()
		{
			return Collections.unmodifiableList(criteriaResults);
		}
		
		/**
		 * Returns a text label that indicates whether this criteria group is applied or not.
		 * If it evaluated to false, may also include a reason it did so.
		 */
		public String getTruthValue()
		{
			if(satisfied)
				return TRUE_TEXT;
			if(!evaluated)
				return SKIPPED;
			
			StringBuilder sb = new StringBuilder(FALSE_TEXT);
			if(StringUtils.isNotBlank(notSatisfiedReason))
				sb.append(" (").append(notSatisfiedReason).append(")");
			
			return sb.toString();
		}
		
		/**
		 * Returns the number of criteria in this group result.
		 */
		public int getCriteriaCount()
		{
			return criteriaResults.size();
		}
		
		//***** DEFAULT ACCESSORS *****//
		/**
		 * @return the criteriaGroup
		 */
		public RuleCriteriaGroup getCriteriaGroup()
		{
			return criteriaGroup;
		}

		/**
		 * @return the evaluated
		 */
		public boolean isEvaluated()
		{
			return evaluated;
		}

		public boolean isSatisfied()
		{
			return satisfied;
		}
		
		public String getNotSatisfiedReason()
		{
			return notSatisfiedReason;
		}
	}
	
	/**
	 * Object to track the results of evaluating a single criteria within a group.
	 */
	public static class RuleCriteriaResult
	{
		/** The criteria being reported on. */
		private final RuleCriteria criteria;
		/** The human-readable description of the component this criteria targets, suitible for display. */
		private final String componentName;
		/** True if the criteria was actually evaluated; false if was skipped due to short circuiting. */
		private final boolean evaluated;
		/** True if the criteria should count as "true" when determining the parent criteria group's truth value. False otherwise. (Skipped criteria get counted as false).*/
		private final boolean satisfied;
		/** If {@link satisfied} is false, this may optionally hold a reason that the criteria evaluated to false. */
		private final String notSatisfiedReason;
				
		/**
		 * Create a new object to track the results of evaluating a criteria.
		 * @param criteria The criteria being reported on.
		 * @param componentModel The component targeted by the given criteria
		 * @param evaluated True if the criteria was actually evaluated; false if was skipped due to short circuiting.
		 * @param satisfied True if the criteria should count as "true" when determining the parent criteria group's truth value. False otherwise. (Skipped criteria get counted as false).
		 * @param notSatisfiedReason Optional. A human-readable reason that the rule criteria was not satisfied.
		 * 
		 * Generally only classes in the component rules engine should be creating these.
		 */
		public RuleCriteriaResult(RuleCriteria criteria, ComponentModel componentModel, boolean evaluated, boolean satisfied, String notSatisfiedReason)
		{
			if(criteria == null)
				throw new IllegalArgumentException("Criteria is required to create a criteria result.");
			ComponentMaster master = componentModel == null ? null : componentModel.getComponentMaster();
			if(master != null && master.getComponentId() != criteria.getComponentId())
				throw new IllegalArgumentException("Error creating criteria result - component master " + master + " is not the one targeted by criteria " + criteria);
			
			this.criteria = criteria;
			this.evaluated = evaluated;
			//If the criteria wasn't even evaluated, it can't possibly apply.
			this.satisfied = evaluated ? satisfied : false;
			this.componentName = master == null ? ("{Cmp ID " + criteria.getComponentId() + "}") : master.getFullComponentName();
			
			if(this.satisfied && StringUtils.isNotBlank(notSatisfiedReason))
				throw new IllegalArgumentException("Criteria " + criteria.getRuleDefId() + " is satisfied, but the rule result has a not-satisfied reason: " + notSatisfiedReason);
			this.notSatisfiedReason = notSatisfiedReason;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{Criteria Result (" + getCriteriaDescription() + ") - " + getTruthValue() + "}";
		}
		
		//***** MODIFIED ACCESSORS *****//
		/**
		 * Returns a text label that indicates whether this criteria is applied or not
		 * If it evaluated to false, may also include a reason it did so.
		 */
		public String getTruthValue()
		{
			if(satisfied)
				return TRUE_TEXT;
			if(!evaluated)
				return SKIPPED;
			
			StringBuilder sb = new StringBuilder(FALSE_TEXT);
			if(StringUtils.isNotBlank(notSatisfiedReason))
				sb.append(" (").append(notSatisfiedReason).append(")");
			
			return sb.toString();
		}
		
		/**
		 * Gets a human-readable description of the criteria being reported on, including its component name, the operator, and the value compared against.
		 * @return A human-readable description of the criteria.
		 */
		public String getCriteriaDescription()
		{
			return componentName + " " + criteria.getOperator().getOperator() + " " + criteria.getComponentValue();
		}

		//***** DEFAULT ACCESSORS *****//
		/**
		 * @return the criteria
		 */
		public RuleCriteria getCriteria()
		{
			return criteria;
		}
		
		/**
		 * @return the evaluated
		 */
		public boolean isEvaluated()
		{
			return evaluated;
		}

		/**
		 * @return the applies
		 */
		public boolean isSatisfied()
		{
			return satisfied;
		}

		/**
		 * @return the componentName
		 */
		public String getComponentName()
		{
			return componentName;
		}

		public String getNotSatisfiedReason()
		{
			return notSatisfiedReason;
		}
	}

	//***** MODIFIED ACCESSORS *****//
	/**
	 * @return the groupResults
	 */
	public List<RuleCriteriaGroupResult> getGroupResults()
	{
		return Collections.unmodifiableList(groupResults);
	}

	/**
	 * Returns a text label that indicates whether this rule is applied or not.
	 * If it evaluated to false, may also include a reason it did so.
	 */
	public String getTruthValue()
	{
		if(satisfied)
			return TRUE_TEXT;
		if(!evaluated)
			return SKIPPED;
		
		StringBuilder sb = new StringBuilder(FALSE_TEXT);
		if(StringUtils.isNotBlank(notSatisfiedReason))
			sb.append(" (").append(notSatisfiedReason).append(")");
		
		return sb.toString();
	}
	
	/**
	 * Returns the total number of criteria in all groups within this rule.
	 */
	public int getCriteriaCount()
	{
		int total = 0;
		for(RuleCriteriaGroupResult groupResult : groupResults)
			total += groupResult.getCriteriaCount();
		return total;
	}
	
	/**
	 * Returns the total number of criteria groups in this rule result.
	 */
	public int getCriteriaGroupCount()
	{
		return groupResults.size();
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the rule
	 */
	public Rule getRule()
	{
		return rule;
	}

	/**
	 * @return the evaluated
	 */
	public boolean isEvaluated()
	{
		return evaluated;
	}

	public boolean isSatisfied()
	{
		return satisfied;
	}
	
	public String getNotSatisfiedReason()
	{
		return notSatisfiedReason;
	}
}
