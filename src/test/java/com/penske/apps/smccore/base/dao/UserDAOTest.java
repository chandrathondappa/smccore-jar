/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.MyBatisDaoTest;
import com.penske.apps.smccore.TestData;
import com.penske.apps.smccore.base.configuration.CoreConfiguration;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.configuration.EmbeddedDataSourceConfiguration;

/**
 * Class under test: {@link UserDAO}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CoreConfiguration.class, EmbeddedDataSourceConfiguration.class})
@SqlGroup({
	@Sql(scripts = "/setup/create-corp-schema.sql"),
	@Sql(scripts = "/setup/create-smc-schema.sql"),
	@Sql(scripts = "/setup/drop-corp-schema.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
	@Sql(scripts = "/setup/drop-smc-schema.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
@ActiveProfiles(ProfileType.TEST)
@Transactional
public class UserDAOTest extends MyBatisDaoTest
{
	@Autowired
	private UserDAO dao;
	
	private final TestData data = new TestData();
	
	@Before
	public void setup()
	{
		this.dao = this.trackMethodCalls(dao, UserDAO.class);
	}

	@Test
    public void shouldGetUsers()
	{
        dao.getUsers("600127842", null, null, true, false);
        dao.getUsers("600127842", null, null, true, true);
        dao.getUsers("600127842", null, null, false, true);
        dao.getUsers(null, UserType.PENSKE, UserDepartment.SUPPLY_SPECIALIST, false, false);
    }
	
	@Test
	public void shouldGetExistingBuddiesList()
	{
		dao.getExistingBuddiesList("600222222");
	}
	
	@Test
	public void shouldGetSelectionType()
	{
		dao.getSelectionType("600222222");
	}
	
	@Test
	public void shouldGetExistingBuddiesListFromUserMaster()
	{
		dao.getExistingBuddiesListFromUserMaster(BuddySelectionType.ALL, "600166698");
	}
	
	@Test
	public void shouldGetVendorIdsFromBuddies()
	{
		dao.getVendorIdsFromBuddies(Arrays.asList("600166698"));
	}
	
	@Test
	public void shouldGetVendorIdsFromVendorFilter()
	{
		dao.getVendorIdsFromVendorFilter(data.userPenske);
	}
}
