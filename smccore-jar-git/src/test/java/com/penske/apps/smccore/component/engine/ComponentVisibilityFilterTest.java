/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import static com.penske.apps.smccore.component.domain.enums.Visibility.EDITABLE;
import static com.penske.apps.smccore.component.domain.enums.Visibility.NOT_VISIBLE;
import static com.penske.apps.smccore.component.domain.enums.Visibility.REQUIRED;
import static com.penske.apps.smccore.component.domain.enums.Visibility.VISIBLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.penske.apps.smccore.component.TestComponentData;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;

/**
 * Class under test: {@link ComponentVisibilityFilter}
 */
public class ComponentVisibilityFilterTest
{
	private final int masterId = 9876;
	private final TestComponentData data = new TestComponentData(masterId);
	private final ComponentVisibilityFilter filter = new ComponentVisibilityFilter();

	@Test
	public void shouldMergeComponentValues()
	{
		data.componentMasterOnly(1)
			.componentDefaultValueOnly(2)
			.componentDefaultValueAndExisting(3)
			.componentUserOnly(4)
			.componentExistingDefaultAndUser(5)
			.componentNoMaster(6)
			.componentDefaultAndBlankExisting(7);
		
		ComponentVisibilityFilterData componentData = filter.mergeComponentValues(1, data.getMasters(), data.getComponentValues(), data.getDefaultComponentValues(), data.getUserValues(), NotVisibleBehavior.REMOVE);
		Map<Integer, ComponentModel> components = componentData.getComponentsForMasterId(masterId);

		ComponentModel masterOnly = components.get(1);
		ComponentModel defaultOnly = components.get(2);
		ComponentModel defaultAndExisting = components.get(3);
		ComponentModel userOnly = components.get(4);
		ComponentModel existingDefaultAndUser = components.get(5);
		ComponentModel componentNoMaster = components.get(6);
		ComponentModel componentDefaultAndBlankExisting = components.get(7);

		assertThat(masterOnly.getComponentValueAsString(), is(""));
		assertThat(defaultOnly.getComponentValueAsString(), is("DEFAULT VALUE"));
		assertThat(defaultAndExisting.getComponentValueAsString(), is("EXISTING VALUE"));
		assertThat(userOnly.getComponentValueAsString(), is("USER VALUE"));
		assertThat(existingDefaultAndUser.getComponentValueAsString(), is("USER VALUE"));
		assertThat(componentNoMaster, is(nullValue()));
		assertThat(componentDefaultAndBlankExisting.getComponentValueAsString(), is(""));

		assertThat(components.size(), is(6));
	}

	@Test
	public void shouldRunRules()
	{
		//										Rule IDs	Target Comp IDs		Criteria Comp IDs
		data.ruleSingleCriteria(				1010, 		10, 				110				)
			.ruleSingleGroupMultipleCriteria(	1020, 		20,					120, 121		)
			.ruleMultipleGroupsMultipleCriteria(1030, 		30,					130, 131, 132	)
			.ruleCriteriaComponentNotVisible(	1040, 		40,					140				)
			.ruleMultipleOutcomePriorities(		1060, 1061, 60,					160, 161		);

		data.setUserValue(masterId, 160, "FOO");
		
		Map<Integer, RuleResult> ruleResults = runRules(NotVisibleBehavior.REMOVE).getEvaluationResult().getRuleResults().get(masterId);
		
		//To see the reasoning behind these asserts, look at the methods called on the TestComponentData object above.
		assertRuleResult(ruleResults.get(1010), true, true, null);
		assertRuleResult(ruleResults.get(1020), true, true, null);
		assertRuleResult(ruleResults.get(1030), true, true, null);
		assertRuleResult(ruleResults.get(1040), true, false, null);
		assertRuleResult(ruleResults.get(1060), true, true, null);
		assertRuleResult(ruleResults.get(1061), false, false, RuleResult.SKIPPED);

		//Scenarios for when some rules fail
		data.setUserValue(masterId, 110, "10")
			.setUserValue(masterId, 120, "BAZ")
			.setUserValue(masterId, 130, "BAZ")
			.setUserValue(masterId, 132, "15")
			.setUserValue(masterId, 160, "NOTFOO");

		ruleResults = runRules(NotVisibleBehavior.REMOVE).getEvaluationResult().getRuleResults().get(masterId);

		assertRuleResult(ruleResults.get(1010), true, false, null);
		assertRuleResult(ruleResults.get(1020), true, false, null);
		assertRuleResult(ruleResults.get(1030), true, false, null);
		assertRuleResult(ruleResults.get(1060), true, false, null);
		assertRuleResult(ruleResults.get(1061), true, true, null);

		//Scenario for when both priorities of rules fail
		data.setUserValue(masterId, 160, "BLAH")
			.setUserValue(masterId, 161, "BLAH");

		ruleResults = runRules(NotVisibleBehavior.REMOVE).getEvaluationResult().getRuleResults().get(masterId);

		assertRuleResult(ruleResults.get(1060), true, false, null);
		assertRuleResult(ruleResults.get(1061), true, false, null);
	}

	@Test
	public void shouldRunRulesMultipleIterations()
	{
		//										Rule IDs	Target Comp IDs		Criteria Comp IDs
		data.ruleCascadingOutcomes(				1050, 1051, 50, 51, 52							);

		ComponentVisibilityFilterData componentData = runRules(NotVisibleBehavior.REMOVE);
		Map<Integer, ComponentModel> components = componentData.getComponentsForMasterId(masterId);
		Map<Integer, RuleResult> ruleResults = componentData.getEvaluationResult().getRuleResults().get(masterId);
		
		assertRuleResult(ruleResults.get(1050), true, true, null);
		assertRuleResult(ruleResults.get(1051), true, true, null);
		assertThat(components.get(51).getRuleVisibility(), is(REQUIRED));
		assertThat(components.get(52).getRuleVisibility(), is(REQUIRED));
		assertThat(componentData.getRuleIterations(), is(3));
		
		//Scenarios for when first rule fails
		data.setUserValue(masterId, 50, "BAZ");

		componentData = runRules(NotVisibleBehavior.REMOVE);
		components = componentData.getComponentsForMasterId(masterId);
		ruleResults = componentData.getEvaluationResult().getRuleResults().get(masterId);

		assertRuleResult(ruleResults.get(1050), true, false, null);
		assertRuleResult(ruleResults.get(1051), true, false, null);
		assertThat(components.get(51).getRuleVisibility(), is(NOT_VISIBLE));
		assertThat(components.get(52).getRuleVisibility(), is(NOT_VISIBLE));
		assertThat(componentData.getRuleIterations(), is(1));
		
		//Scenarios for when only second rule fails
		data.setUserValue(masterId, 50, "FOO")
			.setUserValue(masterId, 51, "BAZ");
		
		componentData = runRules(NotVisibleBehavior.REMOVE);
		components = componentData.getComponentsForMasterId(masterId);
		ruleResults = componentData.getEvaluationResult().getRuleResults().get(masterId);
		assertThat(componentData.getRuleIterations(), is(2));

		assertRuleResult(ruleResults.get(1050), true, true, null);
		assertRuleResult(ruleResults.get(1051), true, false, null);
		assertThat(components.get(51).getRuleVisibility(), is(REQUIRED));
		assertThat(components.get(52).getRuleVisibility(), is(NOT_VISIBLE));
	}

	@Test
	public void shouldApplyRules()
	{
		//Criteria components have IDs at least 100
		//Rules have IDs at least 1000
		
												//Rule IDs	Target Comp IDs		Criteria Comp IDs
		data.ruleSingleCriteria(				1010, 		10, 				110				)
			.ruleSingleGroupMultipleCriteria(	1020, 		20,					120, 121		)
			.ruleMultipleGroupsMultipleCriteria(1030, 		30,					130, 131, 132	)
			.ruleCriteriaComponentNotVisible(	1040, 		40,					140				)
			.ruleMultipleOutcomePriorities(		1060, 1061, 60,					160, 161		);
		
		Map<Integer, ComponentModel> components = runRules(NotVisibleBehavior.REMOVE).getComponentsForMasterId(masterId);
		
		//To see the reasoning behind these asserts, look at the methods called on the TestComponentData object above.
		assertThat(components.get(10).getRuleVisibility(), is(REQUIRED));
		assertThat(components.get(20).getRuleVisibility(), is(REQUIRED));
		assertThat(components.get(30).getRuleVisibility(), is(REQUIRED));
		assertThat(components.get(40).getRuleVisibility(), is(VISIBLE));
		assertThat(components.get(60).getRuleVisibility(), is(VISIBLE));
		
		//Scenarios for when rules fail
		data.setUserValue(masterId, 110, "10")
			.setUserValue(masterId, 120, "BAZ")
			.setUserValue(masterId, 130, "BAZ")
			.setUserValue(masterId, 132, "50")
			.setUserValue(masterId, 160, "FOO");
		
		components = runRules(NotVisibleBehavior.REMOVE).getComponentsForMasterId(masterId);
		
		assertThat(components.get(10).getRuleVisibility(), is(VISIBLE));
		assertThat(components.get(20).getRuleVisibility(), is(VISIBLE));
		assertThat(components.get(30).getRuleVisibility(), is(VISIBLE));
		assertThat(components.get(60).getRuleVisibility(), is(REQUIRED));
		
		//Scenario for when both priorities of rules fail
		data.setUserValue(masterId, 160, "BLAH")
			.setUserValue(masterId, 161, "BLAH");
		components = runRules(NotVisibleBehavior.REMOVE).getComponentsForMasterId(masterId);
		assertThat(components.get(60).getRuleVisibility(), is(NOT_VISIBLE));
	}
	
	@Test
	public void shouldApplyRulesDifferentlyAcrossTemplates()
	{
										//Rule ID	//Master 1		//Master 2	//Target Comp ID	//Criteria Comp ID
		data.ruleMultipleUnitMasters(	1010, 		2,				3,				10,					110);
		
		ComponentVisibilityFilterData componentData = runRules(NotVisibleBehavior.REMOVE);
		Map<Integer, ComponentModel> components1 = componentData.getComponentsForMasterId(2);
		assertThat(components1.get(10).getFinalVisibility(), is(EDITABLE));
		
		Map<Integer, ComponentModel> components2 = componentData.getComponentsForMasterId(3);
		assertThat(components2.get(10).getFinalVisibility(), is(VISIBLE));
	}
	
	@Test
	public void shouldApplyRulesWithUserChangesButNoVisibilityChange()
	{
		//Scenario with walkramp, but user change did not affect visibility of components (change N -> N)
		int onGroundId = 110;
		int bodyInstalledId = 10;
		int bodyMakeId = 11;

		//Sets up a scenario where there is a truck PO and a body PO, so "On Ground" is set to "N", but there is still body information from another PO
		// Further, the body installed component is set to "Y", so the body make should show up by default even though on-ground is "N"
		// (this is not a situation that would normally occur, but is here to test that the rule making body make visible still fires)
		data.ruleOnGroundBody(1010, 1011, onGroundId, bodyInstalledId, bodyMakeId);
		
		ComponentVisibilityFilterData componentData;

		componentData = runRules(NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		//Check that body make is visible, but installed is not (i.e. that installed is still available to rules, even though the rule says it should be not visible.
		assertThat(componentData.getSingleComponent(bodyInstalledId, masterId)	.getFinalVisibility(), is(NOT_VISIBLE));
		assertThat(componentData.getSingleComponent(bodyMakeId, masterId)		.getFinalVisibility(), is(REQUIRED));
		//Check that body make and body installed visibility did not change
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyInstalledId),	is(false));
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyMakeId),		is(false));
		
		//----------------------//
		
		//Next, simulate the user changing the on-ground from "N" to "N" (effectively, no visibility change)
		data.setUserValue(masterId, onGroundId, "N");

		//Check that everything is still the same
		componentData = runRules(NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		assertThat(componentData.getSingleComponent(bodyInstalledId, masterId)	.getFinalVisibility(), is(NOT_VISIBLE));
		assertThat(componentData.getSingleComponent(bodyMakeId, masterId)		.getFinalVisibility(), is(REQUIRED));
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyInstalledId),	is(false));
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyMakeId),		is(false));
		
		//Now, make the scenario where the truck is already an on-ground unit (so both body installed and body make will start out visible),
		// and the user is changing on grond from "Y" to "N" (so they will not be visible anymore after the user's changes are applied)
		data.setComponentValue(masterId, onGroundId, "Y")
			.setUserValue(masterId, onGroundId, "N");
		
		componentData = runRules(NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		//Check that body installed is not visible
		assertThat(componentData.getSingleComponent(bodyInstalledId, masterId)	.getFinalVisibility(), is(NOT_VISIBLE));
		//Check that body make is now no longer visible (this will indicate that the rules couldn't see the value of body installed)
		assertThat(componentData.getSingleComponent(bodyMakeId, masterId)		.getFinalVisibility(), is(NOT_VISIBLE));
		//Check that body installed and body make's visibilities did change (so they would in theory be removed)
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyInstalledId),	is(true));
		assertThat(componentData.isComponentVisibilityChanged(masterId, bodyMakeId),		is(true));
	}

	@Test
	public void shouldResolveConflicts()
	{
											//Comp ID	//Required Master IDs	//Optional Master ID	//Ignored Master ID
		data.conflictOnlyOneComponent(		10, 		110)
			.conflictOnlyOneRequired(		20, 		110, 					120)
			.conflictOnlyOneNotIgnored(		30, 		110, 											130)
			.conflictNoResolution(			40,			110, 111, 				120, 					130)
			.conflictGlobalResolution(		50,			110, 111, 				120, 					130)
			.conflictIndividualResolution(	60, 		110, 111, 				120,					130);
		
		ComponentVisibilityFilterData componentData = resolveConflicts();
		Map<Integer, ComponentModel> components;
		
		components = getComponentsByMasterId(componentData, 10);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(nullValue()));
		
		components = getComponentsByMasterId(componentData, 20);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(nullValue()));
		assertThat(components.get(120).getFinalVisibility(), is(EDITABLE));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		
		components = getComponentsByMasterId(componentData, 30);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(nullValue()));
		assertThat(components.get(130).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(130).getConflictStatus(), is(nullValue()));
		
		components = getComponentsByMasterId(componentData, 40);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(ConflictStatus.CONFLICT));
		assertThat(components.get(111).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(111).getConflictStatus(), is(ConflictStatus.CONFLICT));
		assertThat(components.get(120).getFinalVisibility(), is(EDITABLE));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		assertThat(components.get(130).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		
		components = getComponentsByMasterId(componentData, 50);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(ConflictStatus.GLOBAL_RESOLUTION));
		assertThat(components.get(111).getFinalVisibility(), is(VISIBLE));
		assertThat(components.get(111).getConflictStatus(), is(ConflictStatus.GLOBAL_RESOLUTION));
		assertThat(components.get(120).getFinalVisibility(), is(EDITABLE));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		assertThat(components.get(130).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		
		components = getComponentsByMasterId(componentData, 60);
		assertThat(components.get(110).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(110).getConflictStatus(), is(ConflictStatus.INDIVIDUAL_RESOLUTION_REQUIRED));
		assertThat(components.get(111).getFinalVisibility(), is(VISIBLE));
		assertThat(components.get(111).getConflictStatus(), is(ConflictStatus.INDIVIDUAL_RESOLUTION_VISIBLE));
		assertThat(components.get(120).getFinalVisibility(), is(EDITABLE));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		assertThat(components.get(130).getFinalVisibility(), is(REQUIRED));
		assertThat(components.get(120).getConflictStatus(), is(nullValue()));
		
		//component ID with size 1
		//multiple components, but only one required
		//multiple required, but only one non-ignored component
		//conflict, individual resolution (with one non-required, one ignored)
		//conflict, global resolution (with one non-required, one ignored)
	}
	
	//***** HELPER METHODS *****//
	private ComponentVisibilityFilterData runRules(NotVisibleBehavior notVisibleBehavior)
	{
		ComponentVisibilityFilterData mergedComponents = filter.mergeComponentValues(1, data.getMasters(), data.getComponentValues(), data.getDefaultComponentValues(), data.getUserValues(), notVisibleBehavior);
		ComponentVisibilityFilterData resultContainer = filter.runRules(mergedComponents, data.getRules(), data.getOutcomes());
		return resultContainer;
	}
	
	private ComponentVisibilityFilterData resolveConflicts()
	{
		ComponentVisibilityFilterData mergedComponents = filter.mergeComponentValues(1, data.getMasters(), data.getComponentValues(), data.getDefaultComponentValues(), data.getUserValues(), NotVisibleBehavior.REMOVE);
		ComponentVisibilityFilterData resultContainer = filter.resolveConflicts(mergedComponents, data.getConflictResolver());
		return resultContainer;
	}
	
	private Map<Integer, ComponentModel> getComponentsByMasterId(ComponentVisibilityFilterData componentData, int componentId)
	{
		List<ComponentModel> components = componentData.getComponentsForComponentId(componentId);
		if(components == null)
			return Collections.emptyMap();
		
		Map<Integer, ComponentModel> results = new HashMap<Integer, ComponentModel>();
		for(ComponentModel component : components)
			results.put(component.getMasterId(), component);
		
		return results;
	}
	
	private void assertRuleResult(RuleResult ruleResult, boolean evaluated, boolean satisfied, String notSatisfiedReason)
	{
		assertThat("Evaluated " + ruleResult + ": ", ruleResult.isEvaluated(), is(evaluated));
		assertThat("Satisfied " + ruleResult + ": ", ruleResult.isSatisfied(), is(satisfied));
		assertThat("Not Satisfied Reason " + ruleResult + ": ", ruleResult.getNotSatisfiedReason(), is(notSatisfiedReason));
	}
}
