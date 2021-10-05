/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaGroupResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaResult;


/**
 * The results of running rules against a set of components. Includes information on what results were run, and the results of each criteria / criteria group evaluation.
 * Also includes information about what components will have their visibility changed and what the new visibility value will be.
 */
public class EvaluationResult
{
	private static final int MAX_COMPONENT_LENGTH = 40;
	
	/** The results of evaluating each rule, keyed by master ID, then by rule ID. If a rule was not evaluated, it will still have a value in this map.*/
	private final Map<Integer, Map<Integer, RuleResult>> ruleResults = new HashMap<Integer, Map<Integer, RuleResult>>();
	/** The changes to any components that need to occur as an outcome of evaluating the rules, keyed by (master ID, component ID). */
	private final Map<RuleOutcomeKey, ApplicableOutcome> applicableOutcomes = new LinkedHashMap<RuleOutcomeKey, ApplicableOutcome>();
	/** Stores data about all outcomes, for purposes of getting detailed debug output. */
	private final Map<RuleOutcomeKey, Map<Integer, RuleOutcome>> allOutcomes = new HashMap<RuleOutcomeKey, Map<Integer, RuleOutcome>>();

	/**
	 * Creates a new object to hold the results of evaluating rules.
	 * This is package-private, since only the {@link ComponentVisibilityFilter} should be creating instances of these.
	 * @param ruleResults The results of evaluating each rule, keyed by master ID, then by rule ID. Even if a rule was not evaluated, it should have a key and a value in this map, but the value should indicate it was not run.
	 * @param outcomesToApply The changes to any components that need to be made as a result of running the rules, keyed by (master ID, component ID).
	 * @param allOutcomes A map of all possible rule outcomes. Only needed for detailed debug information.
	 */
	EvaluationResult(Map<Integer, Map<Integer, RuleResult>> ruleResults, Map<RuleOutcomeKey, RuleOutcome> outcomesToApply, Map<RuleOutcomeKey, ? extends Map<Integer, RuleOutcome>> allOutcomes)
	{
		if(outcomesToApply == null)
			outcomesToApply = Collections.emptyMap();
		if(allOutcomes == null)
			allOutcomes = Collections.emptyMap();
		if(ruleResults == null)
			ruleResults = Collections.emptyMap();

		for(Entry<Integer, Map<Integer, RuleResult>> resultEntry : ruleResults.entrySet())
		{
			LinkedHashMap<Integer, RuleResult> ruleResultsForMasterId = new LinkedHashMap<Integer, RuleResult>(resultEntry.getValue());
			this.ruleResults.put(resultEntry.getKey(), ruleResultsForMasterId);
		}
		
		for(Entry<RuleOutcomeKey, RuleOutcome> outcomeEntry : outcomesToApply.entrySet())
		{
			RuleOutcomeKey outcomeKey = outcomeEntry.getKey();
			RuleOutcome outcome = outcomeEntry.getValue();
			int masterId = outcomeKey.getMasterId();
			int ruleId = outcome.getRuleId();
			Map<Integer, RuleResult> ruleResultsForMasterId = ruleResults.get(masterId);
			RuleResult result = ruleResultsForMasterId == null ? null : ruleResultsForMasterId.get(ruleId);
			if(result == null)
				throw new IllegalArgumentException("Tried to log application of outcome " + outcome.getCmpRulesId() + " for rule ID " + ruleId + ", but that rule was not in the set of evaluated rules.");
			this.applicableOutcomes.put(outcomeKey, new ApplicableOutcome(result.getRule(), outcome));
		}
		
		for(Entry<RuleOutcomeKey, ? extends Map<Integer, RuleOutcome>> outcomeEntry : allOutcomes.entrySet())
		{
			Map<Integer, RuleOutcome> outcomesForComponent = new TreeMap<Integer, RuleOutcome>(outcomeEntry.getValue());
			this.allOutcomes.put(outcomeEntry.getKey(), outcomesForComponent);
		}
	}
	
	/**
	 * Class to contain information about a rule outcome that should be applied as a result of running the rules.
	 */
	public static class ApplicableOutcome
	{
		private final String ruleName;
		private final RuleOutcome outcome;
		
		private ApplicableOutcome(Rule rule, RuleOutcome outcome)
		{
			this.ruleName = rule.getName();
			this.outcome = outcome;
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{ApplicableOutcome: " + ruleName + " - " + outcome + "}";
		}
		
		//***** DEFAULT ACCESSORS *****//
		/**
		 * @return the ruleName
		 */
		public String getRuleName()
		{
			return ruleName;
		}

		/**
		 * @return the outcome
		 */
		public RuleOutcome getOutcome()
		{
			return outcome;
		}
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * @return the applicableOutcomes
	 */
	public Map<RuleOutcomeKey, ApplicableOutcome> getApplicableOutcomes()
	{
		return Collections.unmodifiableMap(applicableOutcomes);
	}

	/**
	 * @return the ruleResults
	 */
	public Map<Integer, Map<Integer, RuleResult>> getRuleResults()
	{
		return Collections.unmodifiableMap(ruleResults);
	}

	/**
	 * Gets a pretty-printed description of the detailed logic that went into this evaluation result, showing only information that pertains to the given component and rule IDs.
	 * @param iteration The number iteration this evaluation result represents
	 * @param componentIds Only outcomes and rules affecting this set of components will be shown. If this is null, no filtering by component ID will be applied.
	 * @param ruleIds Only this set of rules (and any flagged by the {@code componentIds} parameter) will be shown. If this is null, no filtering by rule ID will be applied.
	 * @param componentData The components to pull human-readable values from
	 * @param lineSeparator A string used to separate lines in the output built by this method
	 * @return A pretty-printed description of the logic for this rule iteration, filtered by the given rules and components
	 */
	public String getDebugString(int iteration, Set<Integer> componentIds, Set<Integer> ruleIds, ComponentVisibilityFilterData componentData, String lineSeparator)
	{
		TreeMap<Integer, TreeMap<Integer, RuleResult>> rulesToPrint = new TreeMap<Integer, TreeMap<Integer, RuleResult>>();
		Map<Integer, List<RuleOutcome>> outcomesToPrint = new HashMap<Integer, List<RuleOutcome>>();
		
		//If no rule IDs were passed in, default to showing all of them
		if(ruleIds == null)
		{
			ruleIds = new HashSet<Integer>();
			for(Map<Integer, RuleResult> outcomeMap : ruleResults.values())
			{
				ruleIds.addAll(outcomeMap.keySet());
			}
		}
		
		//Index the outcomes by their rule ID, so we can easily figure out which ones to print
		Map<Integer, Map<Integer, List<RuleOutcome>>> outcomesByMasterIdAndRuleId = new HashMap<Integer, Map<Integer, List<RuleOutcome>>>();
		for(Entry<RuleOutcomeKey, Map<Integer, RuleOutcome>> entry : allOutcomes.entrySet())
		{
			int masterId = entry.getKey().getMasterId();
			for(RuleOutcome outcome : entry.getValue().values())
			{
				int ruleId = outcome.getRuleId();
				if(outcomesByMasterIdAndRuleId.get(masterId) == null)
					outcomesByMasterIdAndRuleId.put(masterId, new HashMap<Integer, List<RuleOutcome>>());
				Map<Integer, List<RuleOutcome>> outcomesByRuleId = outcomesByMasterIdAndRuleId.get(masterId);
				if(outcomesByRuleId.get(ruleId) == null)
					outcomesByRuleId.put(ruleId, new ArrayList<RuleOutcome>());
				outcomesByRuleId.get(ruleId).add(outcome);
			}
		}

		//Find all the rules and outcomes that pertain to the passed-in rule IDs
		for(Integer ruleId : ruleIds)
		{
			for(Entry<Integer, Map<Integer, RuleResult>> entry : ruleResults.entrySet())
			{
				int masterId = entry.getKey();
				RuleResult ruleResult = entry.getValue().get(ruleId);
				if(ruleResult != null)
				{
					if(!rulesToPrint.containsKey(masterId))
						rulesToPrint.put(masterId, new TreeMap<Integer, RuleResult>());
					rulesToPrint.get(masterId).put(ruleId, ruleResult);
				}
				
				Map<Integer, List<RuleOutcome>> outcomesByRuleId = outcomesByMasterIdAndRuleId.get(masterId);
				if(outcomesByRuleId != null && outcomesByRuleId.get(ruleId) != null)
				{
					if(!outcomesToPrint.containsKey(masterId))
						outcomesToPrint.put(masterId, new ArrayList<RuleOutcome>(outcomesByRuleId.get(ruleId)));
					else
						outcomesToPrint.get(masterId).addAll(outcomesByRuleId.get(ruleId));
				}
			}
		}
		
		//Also find all the rules and outcomes that pertain to the passed-in component IDs
		for(Entry<RuleOutcomeKey, Map<Integer, RuleOutcome>> entry : allOutcomes.entrySet())
		{
			int masterId = entry.getKey().getMasterId();
			Integer componentId = entry.getKey().getComponentId();
			if(componentIds != null && !componentIds.contains(componentId))
				continue;
			
			Map<Integer, RuleOutcome> outcomesForComponent = entry.getValue();
			for(RuleOutcome outcome : outcomesForComponent.values())
			{
				int ruleId = outcome.getRuleId();
				Map<Integer, RuleResult> resultsForMasterId = ruleResults.get(masterId);
				RuleResult ruleResult = resultsForMasterId == null ? null : resultsForMasterId.get(ruleId);
				if(ruleResult != null)
				{
					if(!rulesToPrint.containsKey(masterId))
						rulesToPrint.put(masterId, new TreeMap<Integer, RuleResult>());
					rulesToPrint.get(masterId).put(ruleId, ruleResult);
				}
				
				if(!outcomesToPrint.containsKey(masterId))
					outcomesToPrint.put(masterId, new ArrayList<RuleOutcome>());
				outcomesToPrint.get(masterId).add(outcome);
			}
		}
		
		TreeMap<Integer, Map<Integer, Map<Integer, RuleOutcome>>> sortedOutcomes = new TreeMap<Integer, Map<Integer, Map<Integer, RuleOutcome>>>();
		for(Entry<Integer, List<RuleOutcome>> entry : outcomesToPrint.entrySet())
		{
			int masterId = entry.getKey();
			for(RuleOutcome outcome : entry.getValue())
			{
				int componentId = outcome.getComponentId();
				int priority = outcome.getPriority();
				
				if(sortedOutcomes.get(masterId) == null)
					sortedOutcomes.put(masterId, new TreeMap<Integer, Map<Integer, RuleOutcome>>());
				Map<Integer, Map<Integer, RuleOutcome>> outcomesForMasterId = sortedOutcomes.get(masterId);
				if(outcomesForMasterId.get(componentId) == null)
					outcomesForMasterId.put(componentId, new TreeMap<Integer, RuleOutcome>());
				outcomesForMasterId.get(componentId).put(priority, outcome);
			}
		}
		
		StringBuilder sb = new StringBuilder("==== Iteration ").append(iteration).append(" ====").append(lineSeparator);
		
		//Print Rules
		for(Entry<Integer, TreeMap<Integer, RuleResult>> entry : rulesToPrint.entrySet())
		{
			Integer masterId = entry.getKey();
			Map<Integer, RuleResult> ruleResultsToPrintForMasterId = entry.getValue();
			
			sb.append("  == Rules (Master ID ").append(masterId).append(") ==").append(lineSeparator);
			
			for(RuleResult ruleResult : ruleResultsToPrintForMasterId.values())
			{
				String ruleDescription = StringUtils.rightPad(new StringBuilder("    RULE ").append(ruleResult.getRule().getRuleId())
					.append(" (").append(StringUtils.left(ruleResult.getRule().getName(), 30)).append(")")
					.toString(), MAX_COMPONENT_LENGTH+10);
				sb.append(ruleDescription).append(" - ").append(ruleResult.getTruthValue())
					.append(lineSeparator);
				
				for(RuleCriteriaGroupResult groupResult : ruleResult.getGroupResults())
				{
					String groupDescription = StringUtils.rightPad(new StringBuilder("      GROUP ").append(groupResult.getCriteriaGroup().getCriteriaGroup()).toString(), MAX_COMPONENT_LENGTH+10);
					sb.append(groupDescription)
						.append("    ").append(" - ").append(groupResult.getTruthValue())
						.append(lineSeparator);
					
					for(RuleCriteriaResult criteriaResult : groupResult.getCriteriaResults())
					{
						String criteriaDescription = StringUtils.rightPad(new StringBuilder("        ").append(criteriaResult.getCriteriaDescription()).toString(), MAX_COMPONENT_LENGTH+10);
						sb.append(criteriaDescription)
							.append("        ").append(" - ").append(criteriaResult.getTruthValue())
							.append(lineSeparator);
					}
				}
				sb.append(lineSeparator);
			}
		}
		
		//Print Outcomes
		if(!outcomesToPrint.isEmpty())
			sb.append(lineSeparator).append("  == Outcomes ==").append(lineSeparator);
		for(Entry<Integer, Map<Integer, Map<Integer, RuleOutcome>>> outcomesForMasterId : sortedOutcomes.entrySet())
		{
			int masterId = outcomesForMasterId.getKey();
			for(Map<Integer, RuleOutcome> outcomesForComponent : outcomesForMasterId.getValue().values())
			{
				for(RuleOutcome outcome : outcomesForComponent.values())
				{
					int componentId = outcome.getComponentId();
					ComponentModel component = componentData.getSingleComponent(componentId, masterId);
					RuleOutcomeKey key = new RuleOutcomeKey(masterId, componentId);
					ApplicableOutcome appliedOutcome = applicableOutcomes.get(key);
					boolean appliedOutcomeMatchesRule = appliedOutcome == null ? false : appliedOutcome.getOutcome().getRuleId() == outcome.getRuleId();

					StringBuilder componentDescription = new StringBuilder("  ").append(outcome.getComponentId());
					if(component != null)
						componentDescription.append(" (").append(component.getComponentMaster().getFullComponentName()).append(")");

					sb.append(StringUtils.rightPad(componentDescription.toString(), MAX_COMPONENT_LENGTH + 10))
						.append(" - T: ").append(StringUtils.leftPad(String.valueOf(outcome.getTemplateId()), 3))
						.append(" - M: ").append(StringUtils.leftPad(String.valueOf(masterId), 8))
						.append(" - Rule: ").append(StringUtils.leftPad(String.valueOf(outcome.getRuleId()), 4))
						.append(" (Priority ").append(StringUtils.leftPad(String.valueOf(outcome.getPriority()), 2)).append(")")
						.append(" - Old: {").append(StringUtils.leftPad(component == null ? "" : component.getComponentMaster().getVisibility().getDescription(), 11)).append("}")
						.append(" - Rule: {").append(StringUtils.leftPad(outcome.getVisibility().getDescription(), 11)).append("}")
						.append(" - After Rule: {").append(StringUtils.leftPad(component == null ? "" : component.getRuleVisibility().getDescription(), 11)).append("}")
						.append(" - After Conflicts: {").append(StringUtils.leftPad(component == null ? "" : component.getFinalVisibility().getDescription(), 11)).append("}")
						.append(appliedOutcomeMatchesRule ? " (Applied)" : "")
						.append(lineSeparator);
				}
			}
		}
		
		return sb.toString();
	}
}