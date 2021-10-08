/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.beans;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.dao.LookupDAO;
import com.penske.apps.smccore.base.domain.DocTypeMaster;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.LookupItem;
import com.penske.apps.smccore.base.util.DateUtil;

/**
 * Class under test: {@link LookupManager}
 */
public class LookupManagerTest
{
	private final int originalCount = 10;
	private final Date originalLoadDate = DateUtil.parseDate("2018-01-01");
	private final Date newerDate = DateUtil.parseDate("2018-06-01");
	
	private final LookupDAO lookupDAO = mock(LookupDAO.class);
	
	private LookupManager lookupManager;
	
	@Before
	public void setup()
	{
		//Set up initial info to look like there are no lookups yet
		when(lookupDAO.getLookupCacheInfo()).thenReturn(CoreTestUtil.createLookupCacheInfo(originalLoadDate, originalCount));
		when(lookupDAO.getLookupItems()).thenReturn(Collections.<LookupItem>emptyList());
		when(lookupDAO.getSalesnetDocTypeMasters(anyString())).thenReturn(Collections.<DocTypeMaster>emptyList());
		
		lookupManager = new LookupManager(lookupDAO);
	}
	
	@Test
	public void shouldNotReload()
	{
		LookupContainer originalContainer = lookupManager.getLookupContainer();
		when(lookupDAO.getLookupCacheInfo()).thenReturn(CoreTestUtil.createLookupCacheInfo(originalLoadDate, originalCount));
		
		boolean reloaded = lookupManager.checkAndRefreshLookups();
		LookupContainer newContainer = lookupManager.getLookupContainer();
		
		assertThat(reloaded, is(false));
		assertThat(newContainer, is(originalContainer));
	}
	
	@Test
	public void shouldReloadOnNewerDates()
	{
		LookupContainer originalContainer = lookupManager.getLookupContainer();
		when(lookupDAO.getLookupCacheInfo()).thenReturn(CoreTestUtil.createLookupCacheInfo(newerDate, originalCount));
		
		boolean reloaded = lookupManager.checkAndRefreshLookups();
		LookupContainer newContainer = lookupManager.getLookupContainer();
		
		assertThat(reloaded, is(true));
		assertThat(newContainer, not(originalContainer));
	}
	
	@Test
	public void shouldReloadOnDifferentCounts()
	{
		LookupContainer originalContainer = lookupManager.getLookupContainer();
		when(lookupDAO.getLookupCacheInfo()).thenReturn(CoreTestUtil.createLookupCacheInfo(originalLoadDate, 50));
		
		boolean reloaded = lookupManager.checkAndRefreshLookups();
		LookupContainer newContainer = lookupManager.getLookupContainer();
		
		assertThat(reloaded, is(true));
		assertThat(newContainer, not(originalContainer));
	}
}
