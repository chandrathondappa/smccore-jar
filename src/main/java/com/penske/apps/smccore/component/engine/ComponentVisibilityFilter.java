/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.penske.apps.smccore.base.exception.ComponentRuleException;
import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.engine.EvaluationResult.ApplicableOutcome;

/**
 * This is an object that takes in information about components, and rules, and conflict resolutions,
 * and is able to run and apply component rules, filter components based on visibility,
 * and do most other calculations related to components.
 */
//FIXME: test with unit templates, as well.
public class ComponentVisibilityFilter
{
	private static final int MAX_ITERATIONS = 10;

	private final boolean debugEnabled;
	private final String debugLineSeparator;
	private final Set<Integer> debugComponentIds;
	private final Set<Integer> debugRuleIds;
	
	public ComponentVisibilityFilter()
	{
		this.debugEnabled = false;
		this.debugLineSeparator = null;
		this.debugComponentIds = Collections.emptySet();
		this.debugRuleIds = Collections.emptySet();
	}
	
	public ComponentVisibilityFilter(Set<Integer> debugComponentIds, Set<Integer> debugRuleIds, String debugLineSeparator)
	{
		this.debugEnabled = true;
		this.debugLineSeparator = debugLineSeparator;
		this.debugComponentIds = debugComponentIds;
		this.debugRuleIds = debugRuleIds;
	}

	/**
	 * Assembles components that reflect the existing component data merged with the user-submitted values. Only components with an object in {@code componentMasters} are included in the result.
     * 	This method will return component data based on what is in the component master collection, and the values will be pulled from the user values or the existing components, or, failing that, from the default component values.
	 * @param markerId Optional. An integer that can be used to identify the results of this run of the rules, or null if no such ID is desired.
	 * 	For example, when running loadsheet rules, this might be the ID of the loadsheet the components belong to, to allow for caching rule runs within a single request.
	 * @param componentMasters All the components that are available for all master IDs being processed by the rules engine in this run.
	 * @param existingComponentValues Optional. The component values from both the vehicle component file and any template-specific components
	 * @param defaultComponentValues Optional. The values to use to use when pre-populating components if no ComponentValue exists for a given component ID and master ID.
	 * @param userValues Optional. The user-submitted values for the components.
	 * @param rulesIgnoreInvisibleComponents True if components that are not visible should be ignored when evaluating rules.
	 * 	This should be set based on how the component file will be updated after running rules.
	 * 	If a component being invisible will result in deleting the component file record, this should be true (the rules should ignore invisible components).
	 * 	If a component being invisible means that component's value will not be updated in the component file, this should be false (the rules should not ignore invisible components).
	 * @return An object containing component data for use by the rest of the rules engine, with values pre-filled either from the current component values, or from the default component values, or from the user-submitted component values.
	 */
	//FIXME: test with vehicle component values, too
	public ComponentVisibilityFilterData mergeComponentValues(Integer markerId,
			NestedComponentMap<? extends ComponentMaster> componentMasters,
			NestedComponentMap<? extends ComponentValue> existingComponentValues,
			NestedComponentMap<String> defaultComponentValues,
			NestedComponentMap<String> userValues,
			NotVisibleBehavior notVisibleBehavior)
	{
    	if(componentMasters == null || componentMasters.isEmpty())
    		return ComponentVisibilityFilterData.emptyInstance(markerId);
    	
    	if(existingComponentValues == null)
    		existingComponentValues = NestedComponentMap.emptyMap();
    	if(defaultComponentValues == null)
    		defaultComponentValues = NestedComponentMap.emptyMap();
    	if(userValues == null)
    		userValues = NestedComponentMap.emptyMap();

    	Map<Integer, List<ComponentModel>> components = new HashMap<>();
    	Map<Integer, List<ComponentModel>> componentsWithoutUserChanges = new HashMap<>();
    	for(int masterId : componentMasters.getAllMasterIds())
    	{
    		for(Entry<Integer, ? extends ComponentMaster> entry : componentMasters.getComponentsForMasterId(masterId).entrySet())
    		{
    			int componentId = entry.getKey();
    			ComponentMaster master = entry.getValue();
    			
    			String defaultComponentValue = defaultComponentValues.get(masterId, componentId);
        		ComponentValue existingComponent = existingComponentValues.get(masterId, componentId);
        		String userValue = userValues.get(masterId, componentId);

        		/*
        		 * The order of precedence for component values (for both pre-pop and for save) is:
        		 * 1) Explicit user-supplied values
        		 * 2) Existing component values
        		 * 3) Default component values
        		 * 4) Empty value
        		 */
        		ComponentValue component = null;
        		if(userValue != null)
        			component = new ComponentValue(master, userValue);
        		else if(existingComponent != null)
        			component = existingComponent;
        		else if(StringUtils.isNotBlank(defaultComponentValue))
        			component = new ComponentValue(master, defaultComponentValue);
        		else
        			component = new ComponentValue(master, "");
        		
        		//Store the newly-constructed component model by its component ID.
        		if(!components.containsKey(componentId))
        			components.put(componentId, new ArrayList<ComponentModel>());
        		components.get(componentId).add(new ComponentModel(master, component, null));
        		
        		//If we need to pay attention to which components were affected by user changes, then we need to track the component visibilities without making user changes, too
        		if(notVisibleBehavior == NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED)
        		{
	        		ComponentValue componentWithoutUserChanges = null;
	        		if(existingComponent != null)
	        			componentWithoutUserChanges = existingComponent;
	        		else if(StringUtils.isNotBlank(defaultComponentValue))
	        			componentWithoutUserChanges = new ComponentValue(master, defaultComponentValue);
	        		else
	        			componentWithoutUserChanges = new ComponentValue(master, "");
	        		
	        		componentsWithoutUserChanges.computeIfAbsent(componentId, x -> new ArrayList<>());
	        		componentsWithoutUserChanges.get(componentId).add(new ComponentModel(master, componentWithoutUserChanges, null));
        		}
    		}
    	}
    	
    	return ComponentVisibilityFilterData.newInstance(markerId, existingComponentValues, components, notVisibleBehavior, componentsWithoutUserChanges);
	}
	
	/**
	 * Iteratively runs the component visibility rules and applies them to the given map of components to set the visibility accordingly.
	 * The rules will continue to be run until either no more component visibilities change, or a pre-set limit on the number of iterations has been reached.
	 * @param componentData The components with their initial visibilities set.
	 * @param rules The rules that can possibly be evaluated.
	 * @param outcomesByMasterId The possible visibility overrides that can be applied, based on whether a rule is true or not, keyed by master ID.
	 * @return An object that contains information about all the operations done during the process of running the rules, as well as the new set of components.
	 * @throws ComponentRuleException If attempting to run the rules results in more than the maximum number of allowed iterations.
	 * 	If one of these is encountered, the results in the exception should not be relied upon for accuracy, as they represent only a partial run of the rules.
	 */
	public ComponentVisibilityFilterData runRules(ComponentVisibilityFilterData componentData, List<Rule> rules, Map<Integer, List<RuleOutcome>> outcomesByMasterId) throws ComponentRuleException
	{
		Map<RuleOutcomeKey, TreeMap<Integer, RuleOutcome>> outcomesByComponentId = new HashMap<>();
		Map<Integer, Rule> rulesById = new HashMap<>();
		
		if(componentData == null)
			componentData = ComponentVisibilityFilterData.emptyInstance(null);
		if(rules == null)
			rules = Collections.emptyList();
		if(outcomesByMasterId == null)
			outcomesByMasterId = Collections.emptyMap();
		
		//Index the rules and outcomes for quicker access, and check that all outcomes have a corresponding rule that can actually trigger them.
		for(Rule rule : rules)
			rulesById.put(rule.getRuleId(), rule);
		
		for(Entry<Integer, List<RuleOutcome>> entry : outcomesByMasterId.entrySet())
		{
			int masterId = entry.getKey();
			if(entry.getValue() == null)
				continue;
			for(RuleOutcome outcome : entry.getValue())
			{
				int ruleId = outcome.getRuleId();
				int componentId = outcome.getComponentId();
				RuleOutcomeKey outcomeKey = new RuleOutcomeKey(masterId, componentId);
				
				if(rulesById.get(ruleId) == null)
					throw new IllegalStateException("Could not run component rules. Found an outcome for rule ID " + ruleId + " (outcome ID " + outcome.getCmpRulesId() + "), but did not find the corresponding rule.");
				
				if(outcomesByComponentId.get(outcomeKey) == null)
					outcomesByComponentId.put(outcomeKey, new TreeMap<Integer, RuleOutcome>());
				outcomesByComponentId.get(outcomeKey).put(outcome.getPriority(), outcome);
			}
		}
		
		//If rules should care about whether components are influenced by the user's changes, then compute the component IDs and master IDs that are so influenced now.
		if(componentData.getNotVisibleComponentBehavior() == NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED)
		{
			//We also need to run the rules on the component values without changes by the user, to determine if components should be ignored or not
			ComponentVisibilityFilterData componentDataWithoutUserChanges = ComponentVisibilityFilterData.withoutUserChanges(componentData);
			componentDataWithoutUserChanges = runAndApplyRules(componentDataWithoutUserChanges, rulesById, outcomesByComponentId);
			componentData = ComponentVisibilityFilterData.reintegrateUserUnchangedComponentsAfterRules(componentData, componentDataWithoutUserChanges);
		}
		
		componentData = runAndApplyRules(componentData, rulesById, outcomesByComponentId);
		
		return componentData;
	}
	
	//FIXME: test
	public ComponentVisibilityFilterData resolveConflicts(ComponentVisibilityFilterData componentData, ConflictResolver resolver)
	{
		if(componentData == null)
			return ComponentVisibilityFilterData.emptyInstance(null);
		
		Map<Integer, List<ComponentModel>> resultComponents = new HashMap<Integer, List<ComponentModel>>();
		for(Integer componentId : componentData.getAllComponentIds())
		{
			//Check each component ID to see if it has conflicts or not.
			List<ComponentModel> components = componentData.getComponentsForComponentId(componentId);
			components = resolveConflictInternal(components, resolver);
			resultComponents.put(componentId, components);
		}
	
		return ComponentVisibilityFilterData.withComponents(componentData, resultComponents);
	}
	
	/*
	 * Steps:
	 * 	x Look up unit masters (get signature, vendor ID, and master ID at least)
	 *  x Look up component masters (regenerating template: this comes from SMC_TEMPLATE_COMPONENTS; other times: this comes from SMC_UNIT_COMPONENT)
	 *  x Look up component values
	 *  x Look up rules and outcomes
	 *  x Look up global conflict resolutions (only need to do this once per run, hopefully, and just organize them by signature)
	 *  x Build global conflict resolver based on signature from unit master
	 *  X Merge component values (build data object: holds Map<Integer, List<ComponentModel>>, keyed by ID; also takes GlobalConflictResolver)
	 *  X Run Rules and apply outcomes
	 *  X Resolve conflicts
	 *  X Return overall evaluation result (augment returned container object to contain conflict info)
	 *  x Perform component save (send string value of components that are at least editable)
	 *  x Update data in DB
	 *  	(just a straight update, no delete or insert needed)
	 *  	(if validation failed, change "value provided" to different value - possibly wrap or make pairs for query or something)
	 *  	(if component is not editable, don't send to validation procedure for SMC components)
	 *  x Filter out not visible components for display (if needed)
	 */
	
	//***** HELPER METHODS *****//
	private ComponentVisibilityFilterData runAndApplyRules(ComponentVisibilityFilterData componentData, Map<Integer, Rule> rulesById, Map<RuleOutcomeKey, TreeMap<Integer, RuleOutcome>> outcomesByComponentId)
	{
		List<String> debugInfo = new ArrayList<String>();
		
		int iteration = 1;
		boolean runAgain = true;
		EvaluationResult evalResult = null;
		while(runAgain)
		{
			if(iteration > MAX_ITERATIONS)
			{
				String errorMessage = "Could not resolve component visibility rules after " + MAX_ITERATIONS + " iterations. There may be a circular dependency in the rules.";
				ComponentVisibilityFilterData resultContainer = ComponentVisibilityFilterData.withFinalRuleResults(componentData, evalResult, MAX_ITERATIONS, debugInfo);
				throw new ComponentRuleException(errorMessage, resultContainer, true);
			}
			
			evalResult = evaluateRules(componentData, rulesById, outcomesByComponentId);
			
			Pair<Boolean, ComponentVisibilityFilterData> ruleApplicationResults = applyRuleOutcomes(componentData, evalResult.getApplicableOutcomes());
			componentData = ruleApplicationResults.getRight();
			runAgain = ruleApplicationResults.getLeft();
			
			if(debugEnabled)
				debugInfo.add(evalResult.getDebugString(iteration, debugComponentIds, debugRuleIds, componentData, debugLineSeparator));
		
			iteration++;
		}
		
		return ComponentVisibilityFilterData.withFinalRuleResults(componentData, evalResult, iteration-1, debugInfo);
	}
	
	/**
	 * Runs all the rules once for a given set of components, and records the results, but does not apply the outcomes yet.
	 * 	It is possible that not all rules may get run. If a rule is not attached to any outcome, it will not be run.
	 * 	If all the outcomes a rule is attached to would not possibly get applied (ex: becuase they are are lower priority than other outcomes that actually do get applied), then that rule won't be run.
	 * @param componentData The components to get values from when evaluating each rule.
	 * @param rules The rules to evaluate.
	 * @param outcomes The possible outcomes that could be applied to the set of components based on the rules. The outer map is keyed by (master ID, component ID), and the inner map is keyed (and ordered) by priority.
	 * @return The results of evaluating the rules. The visibility of components is not changed by this method.
	 */
	private EvaluationResult evaluateRules(ComponentVisibilityFilterData componentData, Map<Integer, Rule> rules, Map<RuleOutcomeKey, TreeMap<Integer, RuleOutcome>> outcomes)
	{
		Map<Integer, Map<Integer, RuleResult>> ruleResults = new HashMap<Integer, Map<Integer, RuleResult>>();
		Map<RuleOutcomeKey, RuleOutcome> outcomesToApply = new HashMap<RuleOutcomeKey, RuleOutcome>();
		for(RuleOutcomeKey outcomeKey : outcomes.keySet())
		{
			TreeMap<Integer, RuleOutcome> outcomesForComponent = outcomes.get(outcomeKey);
			for(RuleOutcome outcome : outcomesForComponent.values())
			{
				int ruleId = outcome.getRuleId();
				int masterId = outcomeKey.getMasterId();
				Map<Integer, RuleResult> resultsForMaster = ruleResults.get(masterId);
				RuleResult ruleResult = resultsForMaster == null ? null : resultsForMaster.get(ruleId);
				
				//If the rule hasn't been evaluated yet, evaluate it now.
				if(ruleResult == null)
				{
					Rule rule = rules.get(ruleId);
					if(rule == null)
						throw new IllegalStateException("Rule " + ruleId + " is referred to by outcome " + outcome.getCmpRulesId() + ", but the rule was not loaded.");
					
					//Rules that evaluate true for one unit master might not be true for another master, since some components can have different values for different masters
					ruleResult = rule.evaluate(componentData, masterId);
					
					if(!ruleResults.containsKey(masterId))
						ruleResults.put(masterId, new TreeMap<Integer, RuleResult>());
					
					ruleResults.get(masterId).put(ruleId, ruleResult);
				}
				
				//Stop at the first rule that is satisfied.
				// Don't go on to evaluate other rules of lower priority if this one is satisfied,
				// since this one will trump any lower-priority rules for this component.
				if(ruleResult.isSatisfied())
				{
					outcomesToApply.put(outcomeKey, outcome);
					break;
				}
			}
		}
		
		//If there are any rules that didn't get evaluated at all (because they were lower-priority, perhaps),
		// then mark them as having been skipped now.
		for(Entry<Integer, Rule> ruleEntry : rules.entrySet())
		{
			int ruleId = ruleEntry.getKey();
			Rule rule = ruleEntry.getValue();
			for(Entry<Integer, Map<Integer, RuleResult>> ruleResultEntry : ruleResults.entrySet())
			{
				int masterId = ruleResultEntry.getKey();
				Map<Integer, RuleResult> resultsForMaster = ruleResultEntry.getValue();
				if(!resultsForMaster.containsKey(ruleId))
				{
					RuleResult ruleResult = RuleResult.createSkippedRuleInstance(rule, componentData, masterId);
					resultsForMaster.put(ruleId, ruleResult);
				}
			}
		}

		return new EvaluationResult(ruleResults, outcomesToApply, outcomes);
	}

	/**
	 * Applies the results of running the rules, and returns a new set of components with visibility updated appropriately.
	 * Components that don't have an applicable outcome will revert back to their base visibility (from the ComponentMaster object).
	 * The components passed into this method are not changed.
	 * @param componentData The current set of components with their visibilities before applying the rule outcomes.
	 * @param applicableOutcomes The visibility changes that should be applied to components as a result of running the rules, keyed by (master ID, component ID).
	 * @return The left side of the result pair indicates whether any component visibilities actually changed as a result of applying the rule outcomes.
	 * 	The right side is a new map of components, with the correct visibilities applied.
	 */
	private Pair<Boolean, ComponentVisibilityFilterData> applyRuleOutcomes(ComponentVisibilityFilterData componentData, Map<RuleOutcomeKey, ApplicableOutcome> applicableOutcomes)
	{
		boolean visibilityChanged = false;
		Map<Integer, List<ComponentModel>> components = new HashMap<Integer, List<ComponentModel>>();
		
		for(Integer componentId : componentData.getAllComponentIds())
		{
			List<ComponentModel> componentsForId = new ArrayList<ComponentModel>();
			
			for(ComponentModel component : componentData.getComponentsForComponentId(componentId))
			{
				int masterId = component.getMasterId();
				RuleOutcomeKey outcomeKey = new RuleOutcomeKey(masterId, componentId);
				ApplicableOutcome applicableOutcome = applicableOutcomes.get(outcomeKey);

				//If there is an applied outcome, the visibility comes from that. Otherwise, revert back to the component's base visibility
				Visibility newVisibility = applicableOutcome == null ? null : applicableOutcome.getOutcome().getVisibility();

				ComponentModel newComponent = new ComponentModel(component.getComponentMaster(), component.getComponentValue(), newVisibility);
				//If any component visibility changed from what it was last run, then we need to run another iteration of the rules.
				if(newComponent.getRuleVisibility() != component.getRuleVisibility())
					visibilityChanged = true;

				componentsForId.add(newComponent);
			}
			
			components.put(componentId, componentsForId);
		}
		
		ComponentVisibilityFilterData result = ComponentVisibilityFilterData.withComponents(componentData, components);
		
		return Pair.of(visibilityChanged, result);
	}

	/**
	 * Given a list of components, determines if there is a conflict, and returns a new list with conflict statuses and final visibilities set correctly.
	 * @param components The components to test for a conflict. It is assumed these are all for the same component ID.
	 * @param resolver An object capable of resolving component conflicts.
	 * @return A list containing the same component IDs and values as the original, but with visibility and conflict status set appropriately.
	 */
	private List<ComponentModel> resolveConflictInternal(List<ComponentModel> components, ConflictResolver resolver)
	{
		//Can't possibly have a conflict without at least two components having the same ID
		if(components.size() < 2)
			return components;
		
		//If there are at least two components, then figure out which ones are required
		Set<ComponentModel> componentsInConflict = new HashSet<ComponentModel>();
		for(ComponentModel component : components)
		{
			//Certain components are allowed to have multiple required ones for the same component ID and different master IDs (ex: delivery dates)
			if(component.getComponentMaster().isIgnoredInConflicts())
				continue;
			//Conflicts only happen when two components are both required with the same compnent ID.
			if(!component.isRequired())
				continue;
			componentsInConflict.add(component);
		}
		
		if(componentsInConflict.size() < 2)
			return components;
		
		ConflictStatus mainResolution = ConflictStatus.CONFLICT;
		ConflictStatus otherResolution = ConflictStatus.CONFLICT;
		ComponentModel requiredComponent = resolver.getIndividualResolution(componentsInConflict);
		if(requiredComponent != null)
		{
			mainResolution = ConflictStatus.INDIVIDUAL_RESOLUTION_REQUIRED;
			otherResolution = ConflictStatus.INDIVIDUAL_RESOLUTION_VISIBLE;
		}
		else
		{
			requiredComponent = resolver.getGlobalResolution(componentsInConflict);
			if(requiredComponent != null)
			{
				mainResolution = ConflictStatus.GLOBAL_RESOLUTION;
				otherResolution = ConflictStatus.GLOBAL_RESOLUTION;
			}	
		}
		
		Visibility mainVisibility = null;
		Visibility otherVisibility = null;
		if(requiredComponent != null)
		{
			mainVisibility = Visibility.REQUIRED;
			otherVisibility = Visibility.VISIBLE;
		}
		
		List<ComponentModel> result = new ArrayList<ComponentModel>(components.size());
		for(ComponentModel component : components)
		{
			if(requiredComponent == component)
				result.add(new ComponentModel(component.getComponentMaster(), component.getComponentValue(), component.getRuleVisibility(), mainVisibility, mainResolution));
			else if(componentsInConflict.contains(component))
				result.add(new ComponentModel(component.getComponentMaster(), component.getComponentValue(), component.getRuleVisibility(), otherVisibility, otherResolution));
			else
				result.add(component);
		}
		
		return result;
	}
}