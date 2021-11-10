/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.penske.apps.smccore.base.dao.AlertsDAOTest;
import com.penske.apps.smccore.base.dao.EmailDAOTest;
import com.penske.apps.smccore.base.dao.LookupDAOTest;
import com.penske.apps.smccore.base.dao.UserDAOTest;
import com.penske.apps.smccore.component.dao.unittemplate.UnitComponentDAOTest;

/**
 * Contains all the DAO tests (which require a Spring context)
 */
@RunWith(Suite.class)
@SuiteClasses({
	AlertsDAOTest.class,
	EmailDAOTest.class,
	LookupDAOTest.class,
	UserDAOTest.class,
	UnitComponentDAOTest.class,
})
public class DAOTests {}
