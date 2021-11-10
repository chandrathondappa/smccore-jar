/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Orchestrates all the other jUnit tests
 */
@RunWith(Suite.class)
@SuiteClasses({
	DAOTests.class,
	UnitTests.class,
})
public class AllTests {}
