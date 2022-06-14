/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.MyBatisDaoTest;
import com.penske.apps.smccore.TestData;
import com.penske.apps.smccore.base.configuration.CoreConfiguration;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.SmcTab;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.configuration.EmbeddedDataSourceConfiguration;
import com.penske.apps.smccore.search.dao.AlertsDAO;
import com.penske.apps.smccore.search.domain.ConfirmationAlertData;
import com.penske.apps.smccore.search.domain.ConfirmationSearch;
import com.penske.apps.smccore.search.domain.FulfillmentAlertData;
import com.penske.apps.smccore.search.domain.ProductionAlertData;
import com.penske.apps.smccore.search.domain.enums.AlertType;

/**
 * Class under test: {@link AlertsDAO}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CoreConfiguration.class, EmbeddedDataSourceConfiguration.class})
@ActiveProfiles(ProfileType.TEST)
@Sql(scripts = "/setup/test-case-scripts/alert-counts-data.sql")
@Transactional
public class AlertsDAOTest extends MyBatisDaoTest
{
	@Autowired
	private AlertsDAO dao;
	
	private final TestData data = new TestData();
	private final User userPenske = data.userPenske;
	private final User userVendor = data.userVendor;
	
	@Before
	public void setup()
	{
		this.dao = this.trackMethodCalls(dao, AlertsDAO.class);
	}

	@Test
	public void shouldGetAlertsForTab()
	{
		dao.getAlertsForTab(SmcTab.ORDER_FULFILLMENT, 1, UserType.PENSKE, UserType.PENSKE);
	}
	
	@Test
	public void shouldGetAllVendorIds()
	{
		dao.getAllVendorIds();
	}
	
	@Test
	public void shouldGetConfirmationAlertDataForVendor()
	{
		ConfirmationSearch poCountSearch = AlertType.OC_UNCONFIRMED_PO.createConfirmationSearch().asUser(userVendor, null);
		ConfirmationSearch coCountSearch = AlertType.OC_UNCONFIRMED_CO.createConfirmationSearch().asUser(userVendor, null);
		ConfirmationSearch cancellationSearch = AlertType.OC_UNCONFIRMED_CANCELLATION.createConfirmationSearch().asUser(userVendor, null);
		ConfirmationSearch whereClauseSearch = new ConfirmationSearch().asUser(userVendor, null);
		
		ConfirmationAlertData transwestData = dao.getConfirmationAlertData(Arrays.asList(1536), poCountSearch, coCountSearch, cancellationSearch, whereClauseSearch);
		assertThat(transwestData.getPurchaseOrderCount(), is(2));
		assertThat(transwestData.getChangeOrderCount(), is(0));
		assertThat(transwestData.getCancellationCount(), is(0));
		
		ConfirmationAlertData truckLiteData = dao.getConfirmationAlertData(Arrays.asList(2085), poCountSearch, coCountSearch, cancellationSearch, whereClauseSearch);
		assertThat(truckLiteData.getPurchaseOrderCount(), is(1));
		assertThat(truckLiteData.getChangeOrderCount(), is(0));
		assertThat(truckLiteData.getCancellationCount(), is(0));
	}

	@Test
	public void shouldGetConfirmationAlertDataForUser()
	{
		ConfirmationSearch poCountSearch = AlertType.OC_UNCONFIRMED_PO.createConfirmationSearch().asUser(userPenske, null);
		ConfirmationSearch coCountSearch = AlertType.OC_UNCONFIRMED_CO.createConfirmationSearch().asUser(userPenske, null);
		ConfirmationSearch cancellationSearch = AlertType.OC_UNCONFIRMED_CANCELLATION.createConfirmationSearch().asUser(userPenske, null);
		ConfirmationSearch whereClauseSearch = new ConfirmationSearch().asUser(userPenske, null);
		
		ConfirmationAlertData transwestData = dao.getConfirmationAlertData(Arrays.asList(1536), poCountSearch, coCountSearch, cancellationSearch, whereClauseSearch);
		assertThat(transwestData.getPurchaseOrderCount(), is(2));
		assertThat(transwestData.getChangeOrderCount(), is(0));
		assertThat(transwestData.getCancellationCount(), is(0));
		
		ConfirmationAlertData truckLiteData = dao.getConfirmationAlertData(Arrays.asList(2085), poCountSearch, coCountSearch, cancellationSearch, whereClauseSearch);
		assertThat(truckLiteData.getPurchaseOrderCount(), is(1));
		assertThat(truckLiteData.getChangeOrderCount(), is(0));
		assertThat(truckLiteData.getCancellationCount(), is(0));
	}
	
	@Test
	public void shouldGetFullfillmentAlertData()
	{
		FulfillmentAlertData allUserData = dao.getFullfillmentAlertData(Arrays.asList("600003280","600162037","600000095","600136705","600013219","600147785","600155653"));
		assertThat(allUserData.getWorkingCount(), is(1));
		assertThat(allUserData.getPendingCount(), is(16));
		assertThat(allUserData.getReadyToOrderCount(), is(8));
		assertThat(allUserData.getContractReviewCount(), is(23));
		assertThat(allUserData.getVendorAnalystAssignmentRequiredCount(), is(0));
		assertThat(allUserData.getNewVendorSetupRequiredCount(), is(17));
		assertThat(allUserData.getVendorUserSetupRequiredCount(), is(0));
		
		FulfillmentAlertData oneUserData = dao.getFullfillmentAlertData(Arrays.asList("600003280"));
		assertThat(oneUserData.getWorkingCount(), is(1));
		assertThat(oneUserData.getPendingCount(), is(0));
		assertThat(oneUserData.getReadyToOrderCount(), is(0));
		assertThat(oneUserData.getContractReviewCount(), is(1));
		assertThat(oneUserData.getVendorAnalystAssignmentRequiredCount(), is(0));
		assertThat(oneUserData.getNewVendorSetupRequiredCount(), is(17));
		assertThat(oneUserData.getVendorUserSetupRequiredCount(), is(0));
	}
	
	@Test
	public void shouldGetProductionAlertDataHideDates()
	{
		ProductionAlertData allData = dao.getProductionAlertData(Arrays.asList(1044, 1070), true);
		assertThat(allData.getDataConflictCount(), is(17));
		assertThat(allData.getDelayCommReqCount(), is(11));
		assertThat(allData.getDeliveryDateEarlyCount(), is(16));
		assertThat(allData.getDeliveryDateLateCount(), is(19));
		assertThat(allData.getDeliveryDateOutOfRangeCount(), is(11));
		assertThat(allData.getEstDeliveryDatePastDueCount(), is(9));
		assertThat(allData.getEstProductionDatePastDueCount(), is(71));
		assertThat(allData.getMissingInfoCount(), is(20));
		assertThat(allData.getProdDateEarlyCount(), is(39));
		assertThat(allData.getProdDateLateCount(), is(66));
		assertThat(allData.getProdDateOutOfRangeCount(), is(10));
		assertThat(allData.getProdHoldsCount(), is(6));
		
		ProductionAlertData esocData = dao.getProductionAlertData(Arrays.asList(1044), true);
		assertThat(esocData.getDataConflictCount(), is(0));
		assertThat(esocData.getDelayCommReqCount(), is(4));
		assertThat(esocData.getDeliveryDateEarlyCount(), is(0));
		assertThat(esocData.getDeliveryDateLateCount(), is(10));
		assertThat(esocData.getDeliveryDateOutOfRangeCount(), is(0));
		assertThat(esocData.getEstDeliveryDatePastDueCount(), is(0));
		assertThat(esocData.getEstProductionDatePastDueCount(), is(16));
		assertThat(esocData.getMissingInfoCount(), is(16));
		assertThat(esocData.getProdDateEarlyCount(), is(16));
		assertThat(esocData.getProdDateLateCount(), is(15));
		assertThat(esocData.getProdDateOutOfRangeCount(), is(0));
		assertThat(esocData.getProdHoldsCount(), is(0));
		
		ProductionAlertData fontaineData = dao.getProductionAlertData(Arrays.asList(1070), true);
		assertThat(fontaineData.getDataConflictCount(), is(17));
		assertThat(fontaineData.getDelayCommReqCount(), is(7));
		assertThat(fontaineData.getDeliveryDateEarlyCount(), is(16));
		assertThat(fontaineData.getDeliveryDateLateCount(), is(9));
		assertThat(fontaineData.getDeliveryDateOutOfRangeCount(), is(11));
		assertThat(fontaineData.getEstDeliveryDatePastDueCount(), is(9));
		assertThat(fontaineData.getEstProductionDatePastDueCount(), is(55));
		assertThat(fontaineData.getMissingInfoCount(), is(4));
		assertThat(fontaineData.getProdDateEarlyCount(), is(23));
		assertThat(fontaineData.getProdDateLateCount(), is(51));
		assertThat(fontaineData.getProdDateOutOfRangeCount(), is(10));
		assertThat(fontaineData.getProdHoldsCount(), is(6));
	}
	
	@Test
	public void shouldGetProductionAlertDataDontHideDates()
	{
		ProductionAlertData allData = dao.getProductionAlertData(Arrays.asList(1044, 1070), false);
		assertThat(allData.getDataConflictCount(), is(17));
		assertThat(allData.getDelayCommReqCount(), is(11));
		assertThat(allData.getDeliveryDateEarlyCount(), is(19));
		assertThat(allData.getDeliveryDateLateCount(), is(20));
		assertThat(allData.getDeliveryDateOutOfRangeCount(), is(11));
		assertThat(allData.getEstDeliveryDatePastDueCount(), is(17));
		assertThat(allData.getEstProductionDatePastDueCount(), is(71));
		assertThat(allData.getMissingInfoCount(), is(20));
		assertThat(allData.getProdDateEarlyCount(), is(39));
		assertThat(allData.getProdDateLateCount(), is(66));
		assertThat(allData.getProdDateOutOfRangeCount(), is(10));
		assertThat(allData.getProdHoldsCount(), is(15));
		
		ProductionAlertData esocData = dao.getProductionAlertData(Arrays.asList(1044), false);
		assertThat(esocData.getDataConflictCount(), is(0));
		assertThat(esocData.getDelayCommReqCount(), is(4));
		assertThat(esocData.getDeliveryDateEarlyCount(), is(0));
		assertThat(esocData.getDeliveryDateLateCount(), is(10));
		assertThat(esocData.getDeliveryDateOutOfRangeCount(), is(0));
		assertThat(esocData.getEstDeliveryDatePastDueCount(), is(0));
		assertThat(esocData.getEstProductionDatePastDueCount(), is(16));
		assertThat(esocData.getMissingInfoCount(), is(16));
		assertThat(esocData.getProdDateEarlyCount(), is(16));
		assertThat(esocData.getProdDateLateCount(), is(15));
		assertThat(esocData.getProdDateOutOfRangeCount(), is(0));
		assertThat(esocData.getProdHoldsCount(), is(0));
		
		ProductionAlertData fontaineData = dao.getProductionAlertData(Arrays.asList(1070), false);
		assertThat(fontaineData.getDataConflictCount(), is(17));
		assertThat(fontaineData.getDelayCommReqCount(), is(7));
		assertThat(fontaineData.getDeliveryDateEarlyCount(), is(19));
		assertThat(fontaineData.getDeliveryDateLateCount(), is(10));
		assertThat(fontaineData.getDeliveryDateOutOfRangeCount(), is(11));
		assertThat(fontaineData.getEstDeliveryDatePastDueCount(), is(17));
		assertThat(fontaineData.getEstProductionDatePastDueCount(), is(55));
		assertThat(fontaineData.getMissingInfoCount(), is(4));
		assertThat(fontaineData.getProdDateEarlyCount(), is(23));
		assertThat(fontaineData.getProdDateLateCount(), is(51));
		assertThat(fontaineData.getProdDateOutOfRangeCount(), is(10));
		assertThat(fontaineData.getProdHoldsCount(), is(15));
	}

	@Test
	public void shouldGetSearchTemplates()
	{
		dao.getSearchTemplates(null, null, UserType.PENSKE);
		dao.getSearchTemplates(SmcTab.ORDER_FULFILLMENT, UserType.PENSKE, UserType.PENSKE);
		dao.getSearchTemplates(SmcTab.ORDER_CONFIRMATION, UserType.VENDOR, UserType.PENSKE);
	}
}
