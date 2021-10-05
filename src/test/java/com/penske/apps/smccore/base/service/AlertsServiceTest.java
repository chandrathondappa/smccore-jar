/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.TestData;
import com.penske.apps.smccore.base.dao.AlertsDAO;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.User;

/**
 * Class under test: {@link AlertsService}
 */
public class AlertsServiceTest
{
	private final AlertsDAO alertsDAO = mock(AlertsDAO.class);
	private final UserService userService = mock(UserService.class);
	
	private final AlertsService service = new AlertsService(alertsDAO, userService);
	
	private final TestData testData = new TestData();
	
	@Test
	public void shouldGetFulfillmentAlertData()
	{
		User user = testData.userPenske;
		
		when(userService.getExistingBuddiesList(user.getSso())).thenReturn(Arrays.asList("555222222"));
		when(alertsDAO.getFullfillmentAlertData(Arrays.asList("555222222"))).thenReturn(CoreTestUtil.createFulfillmentAlertData(1, 2, 3));
		//Mimic the fact that passing an empty list results in an SQL error
		when(alertsDAO.getFullfillmentAlertData(Collections.emptyList())).thenThrow(BadSqlGrammarException.class);
		
		FulfillmentAlertData data = service.getFulfillmentAlertData(testData.userPenske);
		
		assertThat(data, is(notNullValue()));
		assertThat(data.getWorkingCount(), is(1));
		assertThat(data.getPendingCount(), is(2));
		assertThat(data.getReadyToOrderCount(), is(3));
	}
	
	@Test
	public void shouldGetFulfillmentAlertDataEmptyIfNoBuddies()
	{
		User user = testData.userVendor;
	
		//Vendor users should not have any buddies
		when(userService.getExistingBuddiesList(user.getSso())).thenReturn(Collections.emptyList());
		when(alertsDAO.getFullfillmentAlertData(Arrays.asList("555222222"))).thenReturn(CoreTestUtil.createFulfillmentAlertData(1, 2, 3));
		//Mimic the fact that passing an empty list results in an SQL error
		when(alertsDAO.getFullfillmentAlertData(Collections.emptyList())).thenThrow(BadSqlGrammarException.class);
		
		FulfillmentAlertData data = service.getFulfillmentAlertData(testData.userPenske);
		
		assertThat(data, is(nullValue()));
	}
}
