package com.penske.apps.smccore.base.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.annotation.NonVendorQuery;
import com.penske.apps.smccore.base.domain.ConfirmationAlertData;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.ProductionAlertData;
import com.penske.apps.smccore.base.domain.SearchTemplate;
import com.penske.apps.smccore.base.domain.SmcAlert;
import com.penske.apps.smccore.base.domain.enums.SmcTab;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * Contains queries for getting the alerts and associated counts for the Home Screen and Daily Emails
 */
public interface AlertsDAO
{
	//***** Alerts *****//
	@NonVendorQuery("While this is used by vendors, it is being restricted in the query by user type so no need to filter by vendor ID")
	public List<SmcAlert> getAlertsForTab(@Param("tab") SmcTab tab, @Param("headerId")int headerId, @Param("userType") UserType userType, @Param("penskeUserType") UserType penskeUserType);
	
	public FulfillmentAlertData getFullfillmentAlertData(@Param("ssoList") List<String> ssoList);
	
	@NonVendorQuery("While this is used by vendors, adding the vendor ID to the query would change the results. So we are resticting vendors by their associated vendor IDs directly in the query")
	public ProductionAlertData getProductionAlertData(@Param("associatedVendorIds") List<Integer> associatedVendorIds, 
			@Param("hideDates") boolean hideDates);
	
	@NonVendorQuery("While this is used by vendors, adding the vendor ID to the query would change the results. So we are resticting vendors by their associated vendor IDs directly in the query")
	public ConfirmationAlertData getConfirmationAlertData(@Param("associatedVendorIds") Collection<Integer> associatedVendorIds, 
			@Param("vendorUser") boolean vendorUser);

	@NonVendorQuery
	public List<Integer> getAllVendorIds();

	//***** Search Templates *****//
	@NonVendorQuery
	public List<SearchTemplate> getSearchTemplates(@Param("tab") SmcTab tab, @Param("userType") UserType userType, @Param("penskeUserType") UserType penskeUserType);
}
