/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.penske.apps.smccore.base.beans.LookupManagerTest;
import com.penske.apps.smccore.base.domain.EmailTemplateTest;
import com.penske.apps.smccore.base.domain.LookupContainerTest;
import com.penske.apps.smccore.base.domain.UserSecurityTest;
import com.penske.apps.smccore.base.service.UserServiceTest;
import com.penske.apps.smccore.base.util.BatchCallableTest;
import com.penske.apps.smccore.base.util.DateUtilTest;
import com.penske.apps.smccore.base.util.UnitNumberUtilTest;
import com.penske.apps.smccore.base.util.UnitRangeBuilderTest;
import com.penske.apps.smccore.base.util.UtilTest;
import com.penske.apps.smccore.component.domain.RuleCriteriaGroupTest;
import com.penske.apps.smccore.component.domain.RuleCriteriaTest;
import com.penske.apps.smccore.component.domain.RuleTest;
import com.penske.apps.smccore.component.domain.UnitDatesTest;
import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperatorTest;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterTest;
import com.penske.apps.smccore.component.service.CalculatedDataServiceTest;

/**
 * Tests that are relatively simple and don't require a database connection.
 */
@RunWith(Suite.class)
@SuiteClasses({
	//Utility Tests
	UnitNumberUtilTest.class,
	UnitRangeBuilderTest.class,
	DateUtilTest.class,
	BatchCallableTest.class,
	UtilTest.class,
	
	//Domain tests
	LookupContainerTest.class,
	UnitDatesTest.class,
	RuleTest.class,
	RuleCriteriaGroupTest.class,
	RuleCriteriaTest.class,
	ComponentRuleOperatorTest.class,
	ComponentVisibilityFilterTest.class,
	EmailTemplateTest.class,
	UserSecurityTest.class,
	
	//Service Tests
	UserServiceTest.class,
	CalculatedDataServiceTest.class,
	
	//Misc Tests
	LookupManagerTest.class,
})
public class UnitTests {}
