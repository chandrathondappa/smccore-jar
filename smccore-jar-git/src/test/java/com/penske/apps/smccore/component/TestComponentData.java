/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component;

import static com.penske.apps.smccore.component.domain.enums.Visibility.EDITABLE;
import static com.penske.apps.smccore.component.domain.enums.Visibility.NOT_VISIBLE;
import static com.penske.apps.smccore.component.domain.enums.Visibility.REQUIRED;
import static com.penske.apps.smccore.component.domain.enums.Visibility.VISIBLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleBuilder;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.engine.ConflictResolver;
import com.penske.apps.smccore.component.engine.NestedComponentMap;

/**
 * Class for setting up component data and rules to engineer specific test scenarios
 */
public class TestComponentData
{
	private final Map<Integer, Integer> templateIdsByMasterId = new HashMap<Integer, Integer>();
	private final NestedComponentMap<ComponentMaster> masters = NestedComponentMap.newInstance();
	private final NestedComponentMap<ComponentValue> componentValues = NestedComponentMap.newInstance();
	private final NestedComponentMap<String> defaultComponentValues = NestedComponentMap.newInstance();
	private final NestedComponentMap<String> userValues = NestedComponentMap.newInstance();
	private final Map<Integer, Rule> rules = new HashMap<Integer, Rule>();
	private final Map<Integer, Map<Integer, Map<Integer, RuleOutcome>>> outcomes = new HashMap<Integer, Map<Integer, Map<Integer, RuleOutcome>>>();
	private final TestConflictResolver resolver = new TestConflictResolver();
	
	private final int defaultMasterId;
	private final int defaultTemplateId;

	public TestComponentData(int defaultMasterId)
	{
		this.defaultMasterId = defaultMasterId;
		this.defaultTemplateId = 1;
		unitMaster(defaultMasterId, defaultTemplateId);
	}
	
	//***** COMPONENT SCENARIOS *****//
	public TestComponentData componentMasterOnly(int id)
	{
		masterNumber(id, defaultMasterId, "Without Value", VISIBLE, true);
		return this;
	}

	public TestComponentData componentDefaultValueOnly(int id)
	{
		masterText(id, defaultMasterId, "Default Value Only", VISIBLE, true);
		defaultValue(id, defaultMasterId, "Default Value");
		return this;
	}

	public TestComponentData componentDefaultValueAndExisting(int id)
	{
		TestComponentMaster master = masterText(id, defaultMasterId, "Default & Existing", VISIBLE, true);
		defaultValue(id, defaultMasterId, "Default Value");
		value(master, "Existing Value");
		return this;
	}

	public TestComponentData componentUserOnly(int id)
	{
		masterText(id, defaultMasterId, "User Only", VISIBLE, true);
		user(id, defaultMasterId, "User Value");
		return this;
	}

	public TestComponentData componentExistingDefaultAndUser(int id)
	{
		ComponentMaster master = masterText(id, defaultMasterId, "Merge All Three", VISIBLE, true);
		defaultValue(id, defaultMasterId, "Default Value");
		value(master, "Existing Value");
		user(id, defaultMasterId, "User Value");
		return this;
	}

	public TestComponentData componentNoMaster(int id)
	{
		value(masterText(id, defaultMasterId, "Dummy Master", VISIBLE, true), "Existing Value");
		defaultValue(id, defaultMasterId, "Default Value");
		user(id, defaultMasterId, "User Value");

		//We remove the component master from the map of component masters, since we need one for component values, but we don't want it to show up here
		masters.remove(defaultMasterId, id);

		return this;
	}
	
	public TestComponentData componentDefaultAndBlankExisting(int id)
	{
		ComponentMaster master = masterText(id, defaultMasterId, "Default & Existing", VISIBLE, true);
		defaultValue(id, defaultMasterId, "Default Value");
		value(master, "");
		return this;
	}

	public TestComponentData componentDefaultMismatchedType(int textComponentId, int numericComponentId, int mismatchedComponentId)
	{
		masterText(textComponentId, defaultMasterId, "Text Comp", EDITABLE, true);
		masterNumber(numericComponentId, defaultMasterId, "Numeric Comp", EDITABLE, true);
		masterNumber(mismatchedComponentId, defaultMasterId, "Mismatched Comp", EDITABLE, true);
		
		defaultValue(textComponentId, defaultMasterId, "SOMETHING");
		defaultValue(numericComponentId, defaultMasterId, "54.5");
		defaultValue(mismatchedComponentId, defaultMasterId, "WRONG");
		
		return this;
	}
	
	//***** RULE SCENARIOS *****//
	public TestComponentData ruleSingleCriteria(int ruleId, int targetComponentId, int criteriaComponentId)
	{
		masterNumber(criteriaComponentId, defaultMasterId, "Criteria Component", EDITABLE, true);
		masterText(targetComponentId, defaultMasterId, "Target Component", VISIBLE, true);
		
		//These values will make the rule succeed. Change them if you want it to fail.
		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId, defaultMasterId, "25");

		rule(new RuleBuilder(ruleId, defaultTemplateId, "Single Criteria", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId, ComponentRuleOperator.EQUAL, "25")
			.build());
		outcome(ruleId, targetComponentId, defaultMasterId, 1, REQUIRED);
		return this;
	}

	public TestComponentData ruleSingleGroupMultipleCriteria(int ruleId, int targetComponentId, int criteriaComponentId1, int criteriaComponentId2)
	{
		masterText(criteriaComponentId1, defaultMasterId, "Criteria Component 1", EDITABLE, true);
		masterNumber(criteriaComponentId2, defaultMasterId, "Criteria Component 2", EDITABLE, true);
		masterText(targetComponentId, defaultMasterId, "Target Component", VISIBLE, true);

		//These values will make the rule succeed. Change them if you want it to fail.
		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId1, defaultMasterId, "FOOBAR");
		user(criteriaComponentId2, defaultMasterId, "30");

		rule(new RuleBuilder(ruleId, defaultTemplateId, "Single Group, Multiple Criteria", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId1, ComponentRuleOperator.EQUAL, "FOOBAR")
			.addCriteria(1, criteriaComponentId2, ComponentRuleOperator.GREATER_THAN, "25")
			.build());
		outcome(ruleId, targetComponentId, defaultMasterId, 1, REQUIRED);
		return this;
	}

	public TestComponentData ruleMultipleGroupsMultipleCriteria(int ruleId, int targetComponentId, int criteriaComponentId1, int criteriaComponentId2, int criteriaComponentId3)
	{
		masterText(criteriaComponentId1, defaultMasterId, "Criteria Component 1", EDITABLE, true);
		masterNumber(criteriaComponentId2, defaultMasterId, "Criteria Component 2", EDITABLE, true);
		masterNumber(criteriaComponentId3, defaultMasterId, "Criteria Component 3", EDITABLE, true);
		masterText(targetComponentId, defaultMasterId, "Target Component", VISIBLE, true);

		//These values will make the rule succeed. Change them if you want it to fail.
		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId1, defaultMasterId, "FOOBAR");
		user(criteriaComponentId2, defaultMasterId, "21");
		user(criteriaComponentId3, defaultMasterId, "12");

		rule(new RuleBuilder(ruleId, defaultTemplateId, "Multiple Groups, Multiple Criteria", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId1, ComponentRuleOperator.EQUAL, "FOOBAR")
			.addCriteria(1, criteriaComponentId2, ComponentRuleOperator.LESS_THAN, "25")
			.addCriteria(2, criteriaComponentId3, ComponentRuleOperator.EQUAL, "12")
			.build());
		outcome(ruleId, targetComponentId, defaultMasterId, 1, REQUIRED);
		return this;
	}

	public TestComponentData ruleCriteriaComponentNotVisible(int ruleId, int targetComponentId, int criteriaComponentId)
	{
		//The criteria component is not visible, and hence the rule will make any criteria dependent on it false
		masterNumber(criteriaComponentId, defaultMasterId, "Criteria Component", NOT_VISIBLE, true);
		masterText(targetComponentId, defaultMasterId, "Target Component", VISIBLE, true);

		//The rule will fail, but not because of the values rather, because the criteria component is not visible
		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId, defaultMasterId, "25");

		rule(new RuleBuilder(ruleId, defaultTemplateId, "Single Criteria", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId, ComponentRuleOperator.EQUAL, "25")
			.build());
		outcome(ruleId, targetComponentId, defaultMasterId, 1, REQUIRED);
		return this;
	}

	public TestComponentData ruleCircularReference(int ruleId1, int ruleId2, int componentId1, int componentId2)
	{
		masterText(componentId1, defaultMasterId, "Component 1", NOT_VISIBLE, true);
		masterText(componentId2, defaultMasterId, "Component 2", EDITABLE, true);

		user(componentId1, defaultMasterId, "FOO");
		user(componentId2, defaultMasterId, "BAR");

		rule(new RuleBuilder(ruleId1, defaultTemplateId, "First Cyclic Rule", RuleType.LOADSHEET)
			.addCriteria(1, componentId1, ComponentRuleOperator.EQUAL, "FOO")
			.build());
		rule(new RuleBuilder(ruleId2, defaultTemplateId, "Second Cyclic Rule", RuleType.LOADSHEET)
			.addCriteria(1, componentId2, ComponentRuleOperator.EQUAL, "BAR")
			.build());

		//These outcomes will cause component 2's initial presence to make component 1 available,
		// which will then cause component 1's presence to make component 2 unavailable,
		// which will then cause component 2's absence to make component 1 revert back to being unavailable,
		// which will then cause component 1's absence to make component 2 revert back to being available
		//This cycle will repeat indefinitely unless stopped.
		outcome(ruleId1, componentId2, defaultMasterId, 1, NOT_VISIBLE);
		outcome(ruleId2, componentId1, defaultMasterId, 1, EDITABLE);

		return this;
	}

	public TestComponentData ruleCascadingOutcomes(int primaryRuleId, int secondaryRuleId, int parentComponentId, int intermediateComponentId, int targetComponentId)
	{
		masterText(parentComponentId, defaultMasterId, "Parent Component", EDITABLE, true);
		masterText(intermediateComponentId, defaultMasterId, "Intermediate Component", NOT_VISIBLE, true);
		masterText(targetComponentId, defaultMasterId, "Target Component", NOT_VISIBLE, true);

		user(parentComponentId, defaultMasterId, "FOO");
		user(intermediateComponentId, defaultMasterId, "BAR");
		user(targetComponentId, defaultMasterId, "TARGET");

		rule(new RuleBuilder(primaryRuleId, defaultTemplateId, "Primary Rule", RuleType.LOADSHEET)
			.addCriteria(1, parentComponentId, ComponentRuleOperator.EQUAL, "FOO")
			.build());
		rule(new RuleBuilder(secondaryRuleId, defaultTemplateId, "Primary Rule", RuleType.LOADSHEET)
			.addCriteria(1, intermediateComponentId, ComponentRuleOperator.EQUAL, "BAR")
			.build());

		//This should result in the target component being available, since the primary rule makes the secondary rule fire.
		//This should take 2 iterations to accomplish.
		outcome(primaryRuleId, intermediateComponentId, defaultMasterId, 1, REQUIRED);
		outcome(secondaryRuleId, targetComponentId, defaultMasterId, 1, REQUIRED);

		return this;
	}

	public TestComponentData ruleMultipleOutcomePriorities(int ruleIdPriority1, int ruleIdPriority2, int targetComponentId, int criteriaComponentId1, int criteriaComponentId2)
	{
		masterText(targetComponentId, defaultMasterId, "Target Component", NOT_VISIBLE, true);
		masterText(criteriaComponentId1, defaultMasterId, "Criteria Component 1", EDITABLE, true);
		masterText(criteriaComponentId2, defaultMasterId, "Criteria Component 2", EDITABLE, true);

		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId1, defaultMasterId, "NOTFOO");
		user(criteriaComponentId2, defaultMasterId, "BAR");

		rule(new RuleBuilder(ruleIdPriority1, defaultTemplateId, "Priority 1 Rule", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId1, ComponentRuleOperator.EQUAL, "FOO")
			.build());
		rule(new RuleBuilder(ruleIdPriority2, defaultTemplateId, "Priority 2 Rule", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId2, ComponentRuleOperator.EQUAL, "BAR")
			.build());

		//These outcomes will trigger the second rule (since the first rule fails)
		// If the first criteria component value is changed to FOO, the first rule will fire instead
		outcome(ruleIdPriority1, targetComponentId, defaultMasterId, 1, REQUIRED);
		outcome(ruleIdPriority2, targetComponentId, defaultMasterId, 2, VISIBLE);

		return this;
	}

	public TestComponentData ruleMultipleUnitMasters(int ruleId, int masterId1, int masterId2, int targetComponentId, int criteriaComponentId)
	{
		unitMaster(masterId1, 1);
		unitMaster(masterId2, 2);
		
		masterText(criteriaComponentId, masterId1, "Criteria Component (Template 1)", EDITABLE, true);
		masterText(criteriaComponentId, masterId2, "Criteria Component (Template 2)", EDITABLE, true);
		masterText(targetComponentId, masterId1, "Target Component (Template 1)", VISIBLE, true);
		masterText(targetComponentId, masterId2, "Target Component (Template 2)", VISIBLE, true);
		
		user(criteriaComponentId, masterId1, "Y");
		user(criteriaComponentId, masterId2, "N");
		user(targetComponentId, masterId1, "FOO");
		user(targetComponentId, masterId2, "FOO");
		
		//Sets up a rule that should succeed on the first template template and fail on the second template
		rule(new RuleBuilder(ruleId, defaultTemplateId, "Multiple Template Rule", RuleType.LOADSHEET)
			.addCriteria(1, criteriaComponentId, ComponentRuleOperator.EQUAL, "Y")
			.build());
		outcome(ruleId, targetComponentId, masterId1, 1, Visibility.EDITABLE);
		outcome(ruleId, targetComponentId, masterId2, 1, Visibility.REQUIRED);
		
		return this;
	}
	
	public TestComponentData ruleOnGroundBody(int ruleId, int ruleId2, int onGroundCompId, int bodyInstalledCompId, int bodyMakeCompId)
	{
		ComponentMaster onGround = masterText(onGroundCompId, defaultMasterId, "On Ground Unit", EDITABLE, true);
		ComponentMaster bodyInstalled = masterText(bodyInstalledCompId, defaultMasterId, "Body Installed", NOT_VISIBLE, true);
		ComponentMaster bodyMake = masterText(bodyMakeCompId, defaultMasterId, "Body Make", NOT_VISIBLE, true);
		
		value(onGround, "N");
		value(bodyInstalled, "Y");
		value(bodyMake, "MOR");
		
		rule(new RuleBuilder(ruleId, defaultTemplateId, "ON GROUND = Y", RuleType.LOADSHEET)
			.addCriteria(1, onGroundCompId, ComponentRuleOperator.EQUAL, "Y")
			.build());
		outcome(ruleId, bodyInstalledCompId, defaultMasterId, 1, REQUIRED);
		
		rule(new RuleBuilder(ruleId2, defaultTemplateId, "BODY INSTALLED = Y", RuleType.LOADSHEET)
			.addCriteria(1, bodyInstalledCompId, ComponentRuleOperator.EQUAL, "Y")
			.build());
		outcome(ruleId2, bodyMakeCompId, defaultMasterId, 1, REQUIRED);
		
		return this;
	}
	
	//***** CONFLICT RESOLUTION SCENARIOS *****//
	public TestComponentData conflictOnlyOneComponent(int componentId, int masterId)
	{
		masterText(componentId, masterId, "SOLO COMPONENT", Visibility.REQUIRED, false);
		
		return this;
	}
	
	public TestComponentData conflictOnlyOneRequired(int componentId, int requiredMasterId, int optionalMasterId)
	{
		masterText(componentId, requiredMasterId, "REQ COMP", Visibility.REQUIRED, false);
		masterText(componentId, optionalMasterId, "OPTIONAL COMP", Visibility.EDITABLE, false);
		
		return this;
	}

	public TestComponentData conflictOnlyOneNotIgnored(int componentId, int requiredMasterId, int ignoredMasterId)
	{
		masterText(componentId, requiredMasterId, "REQ COMP", Visibility.REQUIRED, false);
		masterText(componentId, ignoredMasterId, "IGNORED COMP", Visibility.REQUIRED, true);
		
		return this;
	}
	
	public TestComponentData conflictNoResolution(int componentId, int requiredMasterId1, int requiredMasterId2, int optionalMasterId, int ignoredMasterId)
	{
		masterText(componentId, requiredMasterId1, "CONFLICT REQ COMP 1", Visibility.REQUIRED, false);
		masterText(componentId, requiredMasterId2, "CONFLICT REQ COMP 2", Visibility.REQUIRED, false);
		masterText(componentId, optionalMasterId, "CONFLICT OPTIONAL COMP", Visibility.EDITABLE, false);
		masterText(componentId, ignoredMasterId, "CONFLICT IGNORED COMP", Visibility.REQUIRED, true);
		
		return this;
	}
	
	public TestComponentData conflictGlobalResolution(int componentId, int resolvedMasterId, int unresolvedMasterId, int optionalMasterId, int ignoredMasterId)
	{
		masterText(componentId, resolvedMasterId, "GLOBAL REQ COMP 1", Visibility.REQUIRED, false);
		masterText(componentId, unresolvedMasterId, "GLOBAL REQ COMP 2", Visibility.REQUIRED, false);
		masterText(componentId, optionalMasterId, "GLOBAL OPTIONAL COMP", Visibility.EDITABLE, false);
		masterText(componentId, ignoredMasterId, "GLOBAL IGNORED COMP", Visibility.REQUIRED, true);
		
		resolution(componentId, resolvedMasterId, true);
		
		return this;
	}
	
	public TestComponentData conflictIndividualResolution(int componentId, int resolvedMasterId, int unresolvedMasterId, int optionalMasterId, int ignoredMasterId)
	{
		masterText(componentId, resolvedMasterId, "INDIV REQ COMP 1", Visibility.REQUIRED, false);
		masterText(componentId, unresolvedMasterId, "INDIV REQ COMP 2", Visibility.REQUIRED, false);
		masterText(componentId, optionalMasterId, "INDIV OPTIONAL COMP", Visibility.EDITABLE, false);
		masterText(componentId, ignoredMasterId, "INDIV IGNORED COMP", Visibility.REQUIRED, true);
		
		//Adds a global resolution that points to the wrong component ID.
		resolution(componentId, unresolvedMasterId, true);
		//Adds an individual resolution that points to the right component ID. Individual resolutions trump global resolutions.
		resolution(componentId, resolvedMasterId, false);
		
		return this;
	}
	
	//***** VISIBILITY SCENARIOS *****//
	public TestComponentData visibilityShown(int componentId)
	{
		masterText(componentId, defaultMasterId, "Visible Component (no rules)", VISIBLE, true);

		user(componentId, defaultMasterId, "Visible Value");

		return this;
	}

	public TestComponentData visibilityHidden(int componentId)
	{
		masterText(componentId, defaultMasterId, "Not Visible Component (no rules)", NOT_VISIBLE, true);

		user(componentId, defaultMasterId, "Not Visible Value");

		return this;
	}

	public TestComponentData visibilityHiddenDueToRules(int ruleId, int targetComponentId, int criteriaComponentId)
	{
		masterText(targetComponentId, defaultMasterId, "Not Visible Component (from rules)", VISIBLE, true);
		masterText(criteriaComponentId, defaultMasterId, "Criteria Component", EDITABLE, true);

		user(targetComponentId, defaultMasterId, "TARGET");
		user(criteriaComponentId, defaultMasterId, "FOO");

		rule(new RuleBuilder(ruleId, defaultTemplateId, "Basic Rule", RuleType.LOADSHEET)
		.addCriteria(1, criteriaComponentId, ComponentRuleOperator.EQUAL, "FOO")
		.build());

		//The target component will not be visible after running the rules
		outcome(ruleId, targetComponentId, defaultMasterId, 1, Visibility.NOT_VISIBLE);

		return this;
	}

	//***** SEQUENCING SCENARIOS *****//
	public TestComponentData sequencing(int componentId1, String subgroupName1, int componentId2, String subgroupName2, int componentId3, String subgroupName3)
	{
		masterText(componentId1, defaultMasterId, subgroupName1, VISIBLE, true);
		masterText(componentId2, defaultMasterId, subgroupName2, VISIBLE, true);
		masterText(componentId3, defaultMasterId, subgroupName3, VISIBLE, true);
		
		user(componentId1, defaultMasterId, "First Value");
		user(componentId2, defaultMasterId, "Second Value");
		user(componentId3, defaultMasterId, "Third Value");
		
		//Since there can only be one component sequence in a given run of the rules, we need to build the different sequences outside of this test data object.
		
		return this;
	}
	
	//***** BUILDER METHODS *****//
	private void unitMaster(int masterId, int templateId)
	{
		if(templateIdsByMasterId.containsKey(masterId))
			throw new IllegalStateException("Unit master ID " + masterId + " already exists (template " + templateIdsByMasterId.get(masterId) + " vs. " + templateId + ")");
		templateIdsByMasterId.put(masterId, templateId);
	}
	
	private ComponentMaster masterNumber(int componentId, int masterId, String subComponentName, Visibility visibility, boolean ignoredInConflicts)
	{
		if(masters.containsKey(masterId, componentId))
			throw new IllegalStateException("Component master " + componentId + " already exists in template " + masterId);
		ComponentMaster master = new TestComponentMaster(componentId, masterId, "G", "SG", subComponentName, ComponentType.NUMERIC, visibility, ignoredInConflicts);
		masters.put(masterId, componentId, master);
		return master;
	}

	private TestComponentMaster masterText(int componentId, int masterId, String subComponentName, Visibility visibility, boolean ignoredInConflicts)
	{
		if(masters.containsKey(masterId, componentId))
			throw new IllegalStateException("Component master " + componentId + " already exists in template " + masterId);
		TestComponentMaster master = new TestComponentMaster(componentId, masterId, "G", "SG", subComponentName, ComponentType.TEXT, visibility, ignoredInConflicts);
		masters.put(masterId, componentId, master);
		return master;
	}

	private void defaultValue(int componentId, int masterId, String value)
	{
		if(defaultComponentValues.containsKey(masterId, componentId))
			throw new IllegalStateException("Default value for component " + componentId + " already exists");
		defaultComponentValues.put(masterId, componentId, value);
	}

	private void value(ComponentMaster master, String value)
	{
		int componentId = master.getComponentId();
		int masterId = master.getMasterId();
		if(componentValues.containsKey(masterId, componentId))
			throw new IllegalStateException("Component value for ID " + componentId + " already exists");
		ComponentValue val = new ComponentValue(master, value);
		componentValues.put(masterId, componentId, val);
	}

	private void user(int componentId, int masterId, String userValue)
	{
		if(userValues.containsKey(masterId, componentId))
			throw new IllegalStateException("User value " + componentId + " already exists for template " + masterId);
		
		userValues.put(masterId, componentId, userValue);
	}

	private void rule(Rule rule)
	{
		int ruleId = rule.getRuleId();
		if(rules.containsKey(ruleId))
			throw new IllegalStateException("Rule " + ruleId + " already exists");
		rules.put(ruleId, rule);
	}

	private void outcome(int ruleId, int componentId, int masterId, int priority, Visibility visibility)
	{ 
		Integer templateId = templateIdsByMasterId.get(masterId);
		if(templateId == null)
			throw new IllegalStateException("Unit master " + masterId + " has not been associated with a rule template. Can not create outcome for component " + componentId + ", rule " + ruleId);
		
		if(outcomes.containsKey(masterId) && outcomes.get(masterId).containsKey(ruleId) && outcomes.get(masterId).get(ruleId).containsKey(componentId))
			throw new IllegalStateException("Rule " + ruleId + " already assigned to component " + componentId);

		if(outcomes.get(masterId) == null)
			outcomes.put(masterId, new HashMap<Integer, Map<Integer, RuleOutcome>>());
		Map<Integer, Map<Integer, RuleOutcome>> outcomesForTemplate = outcomes.get(masterId);
		if(outcomesForTemplate.get(ruleId) == null)
			outcomesForTemplate.put(ruleId, new HashMap<Integer, RuleOutcome>());

		outcomesForTemplate.get(ruleId).put(componentId, CoreTestUtil.createRuleOutcome(ruleId, componentId, templateId, priority, visibility));
	}

	private void resolution(int componentId, int masterId, boolean isGlobal)
	{
		if(isGlobal)
			resolver.addGlobalResolution(componentId, masterId);
		else
			resolver.addIndividualResolution(componentId, masterId);
	}
	
	//***** MODIFIED ACCESSORS *****//
	public TestComponentData setUserValue(int masterId, int componentId, String userValue)
	{
		userValues.put(masterId, componentId, userValue);
		return this;
	}
	
	public TestComponentData setComponentValue(int masterId, int componentId, String value)
	{
		ComponentMaster master = getMasters().get(masterId, componentId);
		if(master == null)
			throw new IllegalStateException("No component master for master ID " + masterId + ", component ID " + componentId);
		ComponentValue componentValue = new ComponentValue(master, value);
		this.componentValues.put(masterId, componentId, componentValue);
		return this;
	}
	
	public TestComponentData clearUserValues()
	{
		Map<Integer, Map<Integer, String>> allUserValues = this.userValues.getAllComponents();
		allUserValues.entrySet().forEach(e -> {
			e.getValue().keySet().forEach(compId -> {
				this.userValues.remove(e.getKey(), compId);
			});
		});
		return this;
	}
	
	public TestComponentData clearComponentValues()
	{
		Map<Integer, Map<Integer, ComponentValue>> allUserValues = this.componentValues.getAllComponents();
		allUserValues.entrySet().forEach(e -> {
			e.getValue().keySet().forEach(compId -> {
				this.componentValues.remove(e.getKey(), compId);
			});
		});
		return this;
	}
	
	public NestedComponentMap<ComponentMaster> getMasters()
	{
		return masters;
	}

	public NestedComponentMap<ComponentValue> getComponentValues()
	{
		return componentValues;
	}

	public NestedComponentMap<String> getDefaultComponentValues()
	{
		return defaultComponentValues;
	}

	public NestedComponentMap<String> getUserValues()
	{
		return userValues;
	}
	
	public Map<Integer, String> getUserValues(int masterId)
	{
		return userValues.getComponentsForMasterId(masterId);
	}

	public List<Rule> getRules()
	{
		return new ArrayList<Rule>(rules.values());
	}

	public Map<Integer, List<RuleOutcome>> getOutcomes()
	{
		Map<Integer, List<RuleOutcome>> result = new HashMap<Integer, List<RuleOutcome>>();
		for(Entry<Integer, Map<Integer, Map<Integer, RuleOutcome>>> entry : outcomes.entrySet())
		{
			int masterId = entry.getKey();
			
			List<RuleOutcome> outcomesForMasterId = new ArrayList<RuleOutcome>();
			for(Map<Integer, RuleOutcome> outcomesByComponentId : entry.getValue().values())
				outcomesForMasterId.addAll(outcomesByComponentId.values());
			
			result.put(masterId, outcomesForMasterId);
		}
		return result;
	}
	
	public List<RuleOutcome> getOutcomesAsList()
	{
		Map<Integer, List<RuleOutcome>> outcomesByMasterId = this.getOutcomes();
		
		List<RuleOutcome> result = new ArrayList<RuleOutcome>();
		for(List<RuleOutcome> outcomes : outcomesByMasterId.values())
			result.addAll(outcomes);
		return result;
	}

	public ConflictResolver getConflictResolver()
	{
		return resolver;
	}
}
