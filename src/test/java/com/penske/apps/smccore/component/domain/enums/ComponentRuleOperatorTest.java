/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.penske.apps.smccore.component.TestComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.engine.ComponentModel;

/**
 * Class under test: {@link ComponentRuleOperator}
 */
public class ComponentRuleOperatorTest
{
	private final ComponentMaster textMaster = new TestComponentMaster(1234, 1, "G", "SG", "TEXT COMP", ComponentType.TEXT, Visibility.VISIBLE, false);
	private final ComponentMaster numberMaster = new TestComponentMaster(4567, 1, "G", "SG", "NUMERIC COMP", ComponentType.NUMERIC, Visibility.VISIBLE, false);
	private final ComponentMaster yearMaster = new TestComponentMaster(7890, 1, "G", "SG", "YEAR COMP", ComponentType.YEAR, Visibility.VISIBLE, false);
	
	private final ComponentModel textModel = new ComponentModel(textMaster, new ComponentValue(textMaster, "FOOBAR"), Visibility.VISIBLE);
	private final ComponentModel numberModel = new ComponentModel(numberMaster, new ComponentValue(numberMaster, "25.5"), Visibility.VISIBLE);
	private final ComponentModel yearModel = new ComponentModel(yearMaster, new ComponentValue(yearMaster, "2016"), Visibility.VISIBLE);
	
	private final ComponentModel emptyTextModel = new ComponentModel(textMaster, new ComponentValue(textMaster, (String) null), Visibility.VISIBLE);
	private final ComponentModel emptyNumberModel = new ComponentModel(numberMaster, new ComponentValue(numberMaster, (String) null), Visibility.VISIBLE);
	private final ComponentModel emptyYearModel = new ComponentModel(yearMaster, new ComponentValue(yearMaster, (String) null), Visibility.VISIBLE);
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	//FIXME: test with DATE type
	
	@Test
	public void lessThanText()
	{
		thrown.expect(UnsupportedOperationException.class);
		ComponentRuleOperator.LESS_THAN.evaluate(textModel, "100");
	}
	
	@Test
	public void lessThanNumber()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.LESS_THAN;
		
		assertThat(operator.evaluate(numberModel, "50"), is(true));
		assertThat(operator.evaluate(numberModel, "25.5"), is(false));
		assertThat(operator.evaluate(numberModel, "25.56"), is(true));
		assertThat(operator.evaluate(numberModel, "26"), is(true));
		assertThat(operator.evaluate(numberModel, "25"), is(false));
		           
		assertThat(operator.evaluate(yearModel, "2017"), is(true));
		assertThat(operator.evaluate(yearModel, "2016"), is(false));
		assertThat(operator.evaluate(yearModel, "2015"), is(false));
		           
		assertThat(operator.evaluate(emptyNumberModel,	null),		is(false));
		assertThat(operator.evaluate(emptyNumberModel,	"25.6"),	is(false));
		assertThat(operator.evaluate(numberModel,		null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	"2017"),	is(false));
		assertThat(operator.evaluate(yearModel,			null),		is(false));
	}
	
	@Test
	public void lessThanEqualToText()
	{
		thrown.expect(UnsupportedOperationException.class);
		ComponentRuleOperator.LESS_THAN_OR_EQUAL_TO.evaluate(textModel, "100");
	}
	
	@Test
	public void lessThanEqualToNumber()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.LESS_THAN_OR_EQUAL_TO;
		
		assertThat(operator.evaluate(numberModel, "50"), is(true));
		assertThat(operator.evaluate(numberModel, "25.5"), is(true));
		assertThat(operator.evaluate(numberModel, "25.56"), is(true));
		assertThat(operator.evaluate(numberModel, "26"), is(true));
		assertThat(operator.evaluate(numberModel, "25"), is(false));
		           
		assertThat(operator.evaluate(yearModel, "2017"), is(true));
		assertThat(operator.evaluate(yearModel, "2016"), is(true));
		assertThat(operator.evaluate(yearModel, "2015"), is(false));
		           
		assertThat(operator.evaluate(emptyNumberModel,	null),		is(false));
		assertThat(operator.evaluate(emptyNumberModel,	"25.6"),	is(false));
		assertThat(operator.evaluate(numberModel,		null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	"2017"),	is(false));
		assertThat(operator.evaluate(yearModel,			null),		is(false));
	}
	
	@Test
	public void equalToText()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.EQUAL;
		
		assertThat(operator.evaluate(textModel, "FOOBAR"), is(true));
		assertThat(operator.evaluate(textModel, "BAZ"), is(false));
		assertThat(operator.evaluate(textModel, ""), is(false));
		assertThat(operator.evaluate(textModel, null), is(false));
		assertThat(operator.evaluate(emptyTextModel, "FOOBAR"), is(false));
		assertThat(operator.evaluate(emptyTextModel, ""), is(true));
		//Empty text models will always at least return an empty string
		// There shouldn't ever actually be a criteria with a value of null, since the admin console requires they supply a value for rule criteria.
		// So, we shouldn't ever run into this situation
		assertThat(operator.evaluate(emptyTextModel, null), is(false));
	}
	
	@Test
	public void equalToNumber()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.EQUAL;
		
		assertThat(operator.evaluate(numberModel, "50"), is(false));
		assertThat(operator.evaluate(numberModel, "25.5"), is(true));
		assertThat(operator.evaluate(numberModel, "25.56"), is(false));
		assertThat(operator.evaluate(numberModel, "26"), is(false));
		assertThat(operator.evaluate(numberModel, "25"), is(false));
		           
		assertThat(operator.evaluate(yearModel, "2017"), is(false));
		assertThat(operator.evaluate(yearModel, "2016"), is(true));
		assertThat(operator.evaluate(yearModel, "2015"), is(false));
		           
		assertThat(operator.evaluate(emptyNumberModel,	null),		is(true));
		assertThat(operator.evaluate(emptyNumberModel,	"25.6"),	is(false));
		assertThat(operator.evaluate(numberModel,		null),		is(false));
		//This is a somewhat odd case, because we expect never to have this happen
		// The admin console requires the user to fill in a value for the rule criteria, so we won't actually need to compare against a null or empty value
		assertThat(operator.evaluate(emptyYearModel,	null),		is(true));
		assertThat(operator.evaluate(emptyYearModel,	"2017"),	is(false));
		assertThat(operator.evaluate(yearModel,			null),		is(false));
	}
	
	@Test
	public void greaterThanEqualToText()
	{
		thrown.expect(UnsupportedOperationException.class);
		ComponentRuleOperator.GREATER_THAN_OR_EQUAL_TO.evaluate(textModel, "100");
	}
	
	@Test
	public void greaterThanEqualToNumber()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.GREATER_THAN_OR_EQUAL_TO;
		
		assertThat(operator.evaluate(numberModel, "50"), is(false));
		assertThat(operator.evaluate(numberModel, "25.5"), is(true));
		assertThat(operator.evaluate(numberModel, "25.56"), is(false));
		assertThat(operator.evaluate(numberModel, "26"), is(false));
		assertThat(operator.evaluate(numberModel, "25"), is(true));
		           
		assertThat(operator.evaluate(yearModel, "2017"), is(false));
		assertThat(operator.evaluate(yearModel, "2016"), is(true));
		assertThat(operator.evaluate(yearModel, "2015"), is(true));
		           
		assertThat(operator.evaluate(emptyNumberModel,	null),		is(false));
		assertThat(operator.evaluate(emptyNumberModel,	"25.6"),	is(false));
		assertThat(operator.evaluate(numberModel,		null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	"2017"),	is(false));
		assertThat(operator.evaluate(yearModel,			null),		is(false));
	}
	
	@Test
	public void greaterThanText()
	{
		thrown.expect(UnsupportedOperationException.class);
		ComponentRuleOperator.GREATER_THAN.evaluate(textModel, "100");
	}
	
	@Test
	public void greaterThanNumber()
	{
		ComponentRuleOperator operator = ComponentRuleOperator.GREATER_THAN;
		
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(numberModel, "50"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(numberModel, "25.5"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(numberModel, "25.56"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(numberModel, "26"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(numberModel, "25"), is(true));
		                                      
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(yearModel, "2017"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(yearModel, "2016"), is(false));
		assertThat(ComponentRuleOperator.GREATER_THAN.evaluate(yearModel, "2015"), is(true));
		                                      
		assertThat(operator.evaluate(emptyNumberModel,	null),		is(false));
		assertThat(operator.evaluate(emptyNumberModel,	"25.6"),	is(false));
		assertThat(operator.evaluate(numberModel,		null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	null),		is(false));
		assertThat(operator.evaluate(emptyYearModel,	"2017"),	is(false));
		assertThat(operator.evaluate(yearModel,			null),		is(false));
	}
}
