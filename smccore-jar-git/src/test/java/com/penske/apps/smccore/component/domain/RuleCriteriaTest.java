/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.penske.apps.smccore.component.TestComponentMaster;
import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.RuleResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaResult;

/**
 * Class under test: {@link RuleCriteria}
 */
public class RuleCriteriaTest
{
	private final int componentId = 555555555;
	private final String criteriaValue = "FOOBAR";
	private final int masterId = 1;
	private final int templateId = 1;
	private final ComponentMaster master = new TestComponentMaster(componentId, masterId, "G", "SG", "Test Comp", ComponentType.TEXT, Visibility.VISIBLE, false);
	private final ComponentValue matchingComponent = new ComponentValue(master, criteriaValue);
	private final ComponentValue nonMatchingComponent = new ComponentValue(master, "BAZ");
	private final List<ComponentModel> components = new ArrayList<ComponentModel>();
	private final List<ComponentModel> componentsWithoutUserChanges = new ArrayList<ComponentModel>();
	
	//Creates a rule with a single criteria, and then extracts that criteria from the rule.
	private final RuleCriteria criteria = new RuleBuilder(1, templateId, "Test Rule", RuleType.LOADSHEET)
		.addCriteria(1, componentId, ComponentRuleOperator.EQUAL, criteriaValue)
		.build()
		.getCriteriaGroups().get(0)
		.getCriteria().get(0);
	
	@Test
	public void shouldEvaluateWithMissingComponentModel()
	{
		//Components map is empty - missing component model for component
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE);
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.getCriteria(), is(criteria));
		assertThat(result.isEvaluated(), is(true));
		assertThat(result.isSatisfied(), is(false));
		assertThat(result.getNotSatisfiedReason(), is(RuleResult.COMPONENT_NOT_FOUND));
	}
	
	@Test
	public void shouldEvaluateWithNonMatchingValue()
	{
		components.add(new ComponentModel(master, nonMatchingComponent, Visibility.VISIBLE));
		
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE);
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.getCriteria(), is(criteria));
		assertThat(result.isEvaluated(), is(true));
		assertThat(result.isSatisfied(), is(false));
		assertThat(result.getNotSatisfiedReason(), is(nullValue()));
	}
	
	@Test
	public void shouldEvaluateWithMatchingValue()
	{
		components.add(new ComponentModel(master, matchingComponent, Visibility.VISIBLE));
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE);
		
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.getCriteria(), is(criteria));
		assertThat(result.isEvaluated(), is(true));
		assertThat(result.isSatisfied(), is(true));
		assertThat(result.getNotSatisfiedReason(), is(nullValue()));
	}

	@Test
	public void shouldEvaluateWithNotVisibleComponent()
	{
		//Components map has a component, but it isn't visible - should always evaluate to false, even if value matches.
		components.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE);
		
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.getCriteria(), is(criteria));
		assertThat(result.isEvaluated(), is(true));
		assertThat(result.isSatisfied(), is(false));
		assertThat(result.getNotSatisfiedReason(), is(RuleResult.COMPONENT_NOT_VISIBLE));
	}
	
	@Test
	public void shouldEvaluateWithNotVisibleComponentKeep()
	{
		//Components has a component that's not visible, but the rules engine is set up to keep non-visible components, so the criteria should evaluate to true
		components.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.KEEP);
		
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.isEvaluated(), is(true));
		assertThat(result.isSatisfied(), is(true));
	}

	@Test
	public void shouldEvaluateWithNotVisibleUserChanged()
	{
		//This component is not visible, but would be visible if the user hadn't made changes
		components.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		componentsWithoutUserChanges.add(new ComponentModel(master, matchingComponent, Visibility.EDITABLE));
		
		//This run of the rules is set up remove not visible components if the user has made them not visible by their actions
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		
		//So, the component should act as though it's no longer there, and the criteria should not be satisfied
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.isSatisfied(), is(false));
	}

	@Test
	public void shouldEvaluateWithNotVisibleUserUnchanged()
	{
		//This component is not visible, and still would not be visible if the user hadn't made changes
		components.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		componentsWithoutUserChanges.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		
		//This run of the rules is set up remove not visible components if the user has made them not visible by their actions
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		
		//So, the component should act as though it's still there, so the criteria should be satisfied
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.isSatisfied(), is(true));
	}
	
	@Test
	public void shouldEvaluateWithNotVisible2()
	{
		//In theory, this test case should be part of a test for the ComponentVisibilityFilterData class, but we don't have one of those as of 2020-04-01
		
		//This component is not visible, and still would not be visible if the user hadn't made changes
		components.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE_2));
		componentsWithoutUserChanges.add(new ComponentModel(master, matchingComponent, Visibility.NOT_VISIBLE));
		
		//This run of the rules is set up remove not visible components if the user has made them not visible by their actions
		ComponentVisibilityFilterData componentData = ComponentVisibilityFilterData.newInstanceForTesting(null, components, componentsWithoutUserChanges, NotVisibleBehavior.REMOVE_IF_USER_INFLUENCED);
		
		//The component should act the same way as it would if both visibilities were NOT_VISIBLE
		RuleCriteriaResult result = criteria.evaluate(componentData, masterId);
		assertThat(result.isSatisfied(), is(true));
	}
}
