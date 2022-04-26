/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.MyBatisDaoTest;
import com.penske.apps.smccore.base.beans.LookupManager;
import com.penske.apps.smccore.base.configuration.CoreConfiguration;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.configuration.EmbeddedDataSourceConfiguration;

/**
 * Class under test: {@link LookupDAO}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CoreConfiguration.class, EmbeddedDataSourceConfiguration.class})
@ActiveProfiles(ProfileType.TEST)
@Transactional
public class LookupDAOTest extends MyBatisDaoTest
{
	@Autowired
	private LookupDAO dao;
	
	@Before
	public void setup()
	{
		this.dao = trackMethodCalls(dao, LookupDAO.class);
	}
	
	@Test
	public void shouldGetLookupItems()
	{
		dao.getLookupItems();
	}
	
	@Test
	public void shouldGetSalesnetDocTypeMasters()
	{
		dao.getSalesnetDocTypeMasters(LookupManager.SALESNET_DOCUMENT_GROUP);
	}
	
	@Test
	public void shouldGetUserDocTypeMasters()
	{
		dao.getUserDocTypeMasters(LookupManager.USERS_DOCUMENT_GROUP);
	}
}
