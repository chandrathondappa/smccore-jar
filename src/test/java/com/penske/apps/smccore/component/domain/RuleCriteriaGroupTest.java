/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.penske.apps.smccore.component.TestComponentData;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilter;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.RuleResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaGroupResult;
import com.penske.apps.smccore.component.engine.RuleResult.RuleCriteriaResult;

/**
 * Class under test: {@link RuleCriteriaGroup}
 */
public class RuleCriteriaGroupTest
{
	private final int masterId = 9876;
	private final TestComponentData data = new TestComponentData(masterId);

	@Test
	public void shouldEvaluateSingleCriteriaFalse()
	{
		data.ruleSingleCriteria(1010, 10, 11)
			.setUserValue(masterId, 11, "50");

		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is false, because single criteria is false
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		false,		null);
		assertCriteria(result,	null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateSingleCriteriaTrue()
	{
		data.ruleSingleCriteria(1010, 10, 11);

		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is true, because single criteria is true
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		true,		null);
		assertCriteria(result,	null,		true,		true,		null);
	}

	@Test
	public void shouldEvaluateTwoCriteriaBothFalse()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12)
			.setUserValue(masterId, 11, "BAZ")
			.setUserValue(masterId, 12, "1");
		
		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is false, because first criteria is false,
		// Second criteria not evaluated, due to short-circuiting
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		false,		null);
		assertCriteria(result,	1, 			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateTwoCriteriaFirstFalse()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12)
			.setUserValue(masterId, 11, "BAZ");
		
		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is false, because first criteria is false
		// Second criteria not evaluated, due to short-circuiting
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		false,		null);
		assertCriteria(result,	1, 			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateTwoCriteriaSecondFalse()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12)
			.setUserValue(masterId, 12, "1");
		
		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is false, because second criteria is false
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		true,		null);
		assertCriteria(result,	1, 			true,		false,		null);
		assertCriteria(result,	null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateTwoCriteriaBothTrue()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12);
		
		RuleCriteriaGroupResult result = evaluateCriteriaGroup(1010, 1);
		
		//Overall criteria group is true, because both are true
		//					 Criteria #		Evaluated	Satisfied	Not Satisfied Reason	
		assertCriteria(result,	0, 			true,		true,		null);
		assertCriteria(result,	1, 			true,		true,		null);
		assertCriteria(result,	null,		true,		true,		null);
	}

	private RuleCriteriaGroupResult evaluateCriteriaGroup(int ruleId, int criteriaGroup)
	{
		RuleCriteriaGroup group = null;
		for(Rule r : data.getRules())
		{
			if(r.getRuleId() == ruleId)
			{
				for(RuleCriteriaGroup g : r.getCriteriaGroups())
				{
					if(g.getCriteriaGroup() == criteriaGroup)
					{
						group = g;
						break;
					}
				}
				break;
			}
		}
		assertThat("Group " + criteriaGroup + " in rule " + ruleId + " not found.", group, is(notNullValue()));

		ComponentVisibilityFilter filter = new ComponentVisibilityFilter();
		ComponentVisibilityFilterData components = filter.mergeComponentValues(1, data.getMasters(), data.getComponentValues(), data.getDefaultComponentValues(), data.getUserValues(), NotVisibleBehavior.REMOVE);

		return group.evaluate(components, masterId);
	}
	
	private void assertCriteria(RuleCriteriaGroupResult groupResult, Integer criteriaIndex, boolean evaluated, boolean satisfied, String notSatisfiedReason)
	{
		if(criteriaIndex == null)
		{
			assertThat("Evaluated - Group", groupResult.isEvaluated(), is(evaluated));
			assertThat("Satisfied - Group", groupResult.isSatisfied(), is(satisfied));
			assertThat("Not Satisfied Reason - Group", groupResult.getNotSatisfiedReason(), is(notSatisfiedReason));
		}
		else
		{
			RuleCriteriaResult result = groupResult.getCriteriaResults().get(criteriaIndex);
			assertThat("Result not found - " + criteriaIndex, result, is(notNullValue()));
			assertThat("Evaluated - " + criteriaIndex, result.isEvaluated(), is(evaluated));
			assertThat("Satisfied - " + criteriaIndex, result.isSatisfied(), is(satisfied));
			assertThat("Not Satisfied Reason - " + criteriaIndex, result.getNotSatisfiedReason(), is(notSatisfiedReason));
		}
	}
}
