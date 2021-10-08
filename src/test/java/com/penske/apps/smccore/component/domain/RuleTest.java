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
 * Class under test: {@link Rule}
 */
public class RuleTest
{
	private final int masterId = 9876;
	private final TestComponentData data = new TestComponentData(masterId);

	@Test
	public void shouldEvalauteSingleCriteriaFalse()
	{
		data.ruleSingleCriteria(1010, 10, 11)
			.setUserValue(masterId, 11, "30");

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because single criteria and group are false
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		false,		null);
		assertCriteria(result,	 1,				null,		true,		false,		null);
		assertCriteria(result,	 null,			null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateSingleCriteriaTrue()
	{
		data.ruleSingleCriteria(1010, 10, 11);

		RuleResult result = evaluateRule(1010);

		//Overall rule is true, because single criteria and group are true
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		true,		null);
		assertCriteria(result,	 1,				null,		true,		true,		null);
		assertCriteria(result,	 null,			null,		true,		true,		null);
	}

	@Test
	public void shouldEvaluateSingleGroupTwoCriteriaFirstFalse()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12)
			.setUserValue(masterId, 11, "BAZ");

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because one criteria is false, and hence group is false
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		false,		null);
		assertCriteria(result,	 1,				1,			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 1,				null,		true,		false,		null);
		assertCriteria(result,	 null,			null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateSingleGroupTwoCriteriaBothTrue()
	{
		data.ruleSingleGroupMultipleCriteria(1010, 10, 11, 12);

		RuleResult result = evaluateRule(1010);

		//Overall rule is true, because all criteria are true, and hence group is true
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		true,		null);
		assertCriteria(result,	 1,				1,			true,		true,		null);
		assertCriteria(result,	 1,				null,		true,		true,		null);
		assertCriteria(result,	 null,			null,		true,		true,		null);
	}

	@Test
	public void shouldEvaluateTwoGroupsBothFalse()
	{
		data.ruleMultipleGroupsMultipleCriteria(1010, 10, 11, 12, 13)
			.setUserValue(masterId, 12, "26")
			.setUserValue(masterId, 13, "15");

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because both criteria groups are false.
		//First group is false because one of its criteria is false.
		//Second group is false because its only criteria is false.
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		true,		null);
		assertCriteria(result,	 1,				1,			true,		false,		null);
		assertCriteria(result,	 1,				null,		true,		false,		null);
		assertCriteria(result,	 2,				0,			true,		false,		null);
		assertCriteria(result,	 2,				null,		true,		false,		null);
		assertCriteria(result,	 null,			null,		true,		false,		null);
	}

	@Test
	public void shouldEvaluateTwoGroupsFirstTrue()
	{
		data.ruleMultipleGroupsMultipleCriteria(1010, 10, 11, 12, 13)
			.setUserValue(1, 13, "15");

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because both criteria groups are false.
		//First group is false because one of its criteria is false.
		//Second group is false because its only criteria is false.
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		true,		null);
		assertCriteria(result,	 1,				1,			true,		true,		null);
		assertCriteria(result,	 1,				null,		true,		true,		null);
		assertCriteria(result,	 2,				0,			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 2,				null,		false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 null,			null,		true,		true,		null);
	}

	@Test
	public void shouldEvaluateTwoGroupsSecondTrue()
	{
		data.ruleMultipleGroupsMultipleCriteria(1010, 10, 11, 12, 13)
			.setUserValue(masterId, 11, "BAZ");

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because both criteria groups are false.
		//First group is false because one of its criteria is false.
		//Second group is false because its only criteria is false.
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		false,		null);
		assertCriteria(result,	 1,				1,			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 1,				null,		true,		false,		null);
		assertCriteria(result,	 2,				0,			true,		true,		null);
		assertCriteria(result,	 2,				null,		true,		true,		null);
		assertCriteria(result,	 null,			null,		true,		true,		null);
	}

	@Test
	public void shouldEvaluateTwoGroupsBothTrue()
	{
		data.ruleMultipleGroupsMultipleCriteria(1010, 10, 11, 12, 13);

		RuleResult result = evaluateRule(1010);

		//Overall rule is false, because both criteria groups are false.
		//First group is false because one of its criteria is false.
		//Second group is false because its only criteria is false.
		//					Criteria Grp	Criteria #		Evaluated	Satisfied	Not Satisfied Reason
		assertCriteria(result,	 1,				0,			true,		true,		null);
		assertCriteria(result,	 1,				1,			true,		true,		null);
		assertCriteria(result,	 1,				null,		true,		true,		null);
		assertCriteria(result,	 2,				0,			false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 2,				null,		false,		false,		RuleResult.SKIPPED);
		assertCriteria(result,	 null,			null,		true,		true,		null);
	}

	private RuleResult evaluateRule(int ruleId)
	{
		Rule rule = null;
		for(Rule r : data.getRules())
		{
			if(r.getRuleId() == ruleId)
			{
				rule = r;
				break;
			}
		}
		assertThat("Rule " + ruleId + " not found.", rule, is(notNullValue()));

		ComponentVisibilityFilter filter = new ComponentVisibilityFilter();
		ComponentVisibilityFilterData components = filter.mergeComponentValues(1, data.getMasters(), data.getComponentValues(), data.getDefaultComponentValues(), data.getUserValues(), NotVisibleBehavior.REMOVE);

		return rule.evaluate(components, masterId);
	}

	private void assertCriteria(RuleResult result, Integer criteriaGroup, Integer criteriaIndex, boolean evaluated, boolean satisfied, String notSatisfiedReason)
	{
		if(criteriaGroup == null)
		{
			assertThat("Evaluated - Rule", result.isEvaluated(), is(evaluated));
			assertThat("Satisfied - Rule", result.isSatisfied(), is(satisfied));
			assertThat("Not Satisfied Reason - Rule", result.getNotSatisfiedReason(), is(notSatisfiedReason));
			return;
		}

		RuleCriteriaGroupResult groupResult = null;
		for(RuleCriteriaGroupResult gr : result.getGroupResults())
		{
			if(gr.getCriteriaGroup().getCriteriaGroup() == criteriaGroup)
			{
				groupResult = gr;
				break;
			}
		}
		assertThat("Result not found - Group " + criteriaGroup, groupResult, is(notNullValue()));

		if(criteriaIndex == null)
		{
			assertThat("Evaluated - Group " + criteriaGroup, groupResult.isEvaluated(), is(evaluated));
			assertThat("Satisfied - Group " + criteriaGroup, groupResult.isSatisfied(), is(satisfied));
			assertThat("Not Satisfied Reason - Group " + criteriaGroup, groupResult.getNotSatisfiedReason(), is(notSatisfiedReason));
			return;
		}

		RuleCriteriaResult criteriaResult = groupResult.getCriteriaResults().get(criteriaIndex);
		assertThat("Result not found - Group " + criteriaGroup + " Criteria " + criteriaIndex, criteriaResult, is(notNullValue()));
		assertThat("Evaluated Criteria - Group " + criteriaGroup + " Criteria "+ criteriaIndex, criteriaResult.isEvaluated(), is(evaluated));
		assertThat("Satisfied Criteria - Group " + criteriaGroup + " Criteria " + criteriaIndex, criteriaResult.isSatisfied(), is(satisfied));
		assertThat("Not Satisfied Reason - Group " + criteriaGroup + " Criteria " + criteriaIndex, criteriaResult.getNotSatisfiedReason(), is(notSatisfiedReason));
	}
}
