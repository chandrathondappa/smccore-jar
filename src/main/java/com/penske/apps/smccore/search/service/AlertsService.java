package com.penske.apps.smccore.search.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.SecurityFunction;
import com.penske.apps.smccore.base.domain.enums.SmcTab;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.base.service.UserService;
import com.penske.apps.smccore.search.dao.AlertsDAO;
import com.penske.apps.smccore.search.domain.ConfirmationAlertData;
import com.penske.apps.smccore.search.domain.ConfirmationSearch;
import com.penske.apps.smccore.search.domain.FulfillmentAlertData;
import com.penske.apps.smccore.search.domain.ProductionAlertData;
import com.penske.apps.smccore.search.domain.SearchTemplate;
import com.penske.apps.smccore.search.domain.SmcAlert;
import com.penske.apps.smccore.search.domain.enums.AlertType;

@Service
public class AlertsService {
	
	private final AlertsDAO alertsDAO;
	private final UserService userService;

	//***** Alerts *****//
	@Autowired
	public AlertsService(AlertsDAO alertsDAO, UserService userService)
	{
		this.alertsDAO = alertsDAO;
		this.userService = userService;
	}
	
	public List<SmcAlert> getAlertsForTab(SmcTab tab, Integer headerId, UserType userType) {
		return alertsDAO.getAlertsForTab(tab, headerId, userType, UserType.PENSKE);
	}
	
	public FulfillmentAlertData getFulfillmentAlertData(User user)
	{
		//Penske users have at least themselves as a buddy. If a user has no buddies at all, then they have no fulfillment counts
		List<String> buddiesList = userService.getExistingBuddiesList(user.getSso());
		if(buddiesList == null || buddiesList.isEmpty())
			return null;
		
		return alertsDAO.getFullfillmentAlertData(buddiesList);
	}
	
	public ProductionAlertData getProductionAlertDataByVendorId(User user) {
		List<Integer> associatedVendorIds = new ArrayList<>();
		boolean hideDates = false;
		// Vendor users use the vendor IDs associated with thier org
		if(UserType.VENDOR == user.getUserType()) {
			if(!user.hasSecurityFunction(SecurityFunction.SHOW_NON_OEM_DATES))
				hideDates = true;
			associatedVendorIds.addAll(user.getAssociatedVendorIds());
		}
		// Penske users use the vendor IDs gathered in the following order (if an option returns no results, it moves to the next one):
		//	1) The vendor IDs the user has selected in the vendor filter (if the filter is active)
		//	2) The vendor IDs associated with the user and their buddies
		//	3) All vendor IDs 
		else {
			List<Integer> vendorIdsFromFilter = userService.getVendorIdsFromVendorFilter(user);
			if(vendorIdsFromFilter == null)
				vendorIdsFromFilter = Collections.emptyList();
			
			if(vendorIdsFromFilter.isEmpty()) {
				List<Integer> vendorIdsFromBuddies = userService.getVendorIdsFromBuddies(userService.getExistingBuddiesList(user.getSso()));
				if(vendorIdsFromBuddies == null)
					vendorIdsFromBuddies = Collections.emptyList();
				
				if(vendorIdsFromBuddies.isEmpty()) {
					associatedVendorIds.addAll(alertsDAO.getAllVendorIds());
				}
				else
					associatedVendorIds.addAll(vendorIdsFromBuddies);
			}
			else
				associatedVendorIds.addAll(vendorIdsFromFilter);
		}
		
		if(associatedVendorIds.isEmpty())
			return null;
		else
			return alertsDAO.getProductionAlertData(associatedVendorIds, hideDates);
	}

	public ConfirmationAlertData getConfirmationAlertDataByVendorId(User user) {
		
		ConfirmationSearch poCountSearch = AlertType.OC_UNCONFIRMED_PO.createConfirmationSearch().asUser(user, null);
		ConfirmationSearch coCountSearch = AlertType.OC_UNCONFIRMED_CO.createConfirmationSearch().asUser(user, null);
		ConfirmationSearch cancellationSearch = AlertType.OC_UNCONFIRMED_CANCELLATION.createConfirmationSearch().asUser(user, null);
		
		ConfirmationSearch whereClauseSearch = new ConfirmationSearch().asUser(user, null);
		Collection<Integer> associatedVendorIds;
		if(UserType.VENDOR == user.getUserType())
		{
			if(user.getAssociatedVendorIds().isEmpty())
				return null;
			else
				associatedVendorIds = user.getAssociatedVendorIds();
		}
		else {
			List<Integer> vendorIdsFromFilter = userService.getVendorIdsFromVendorFilter(user);
			if(vendorIdsFromFilter == null || vendorIdsFromFilter.isEmpty())
				associatedVendorIds = user.getAssociatedVendorIds();
			else
				associatedVendorIds = vendorIdsFromFilter;
		}
		
		return alertsDAO.getConfirmationAlertData(associatedVendorIds, poCountSearch, coCountSearch, cancellationSearch, whereClauseSearch);
	}

	//***** Search Templates *****//
	public List<SearchTemplate> getSearchTemplatesForUser(SmcTab tab, User user)
	{
		return alertsDAO.getSearchTemplates(tab, user.getUserType(), UserType.PENSKE);
	}
	
	public List<SearchTemplate> getAllSearchTemplates()
	{
		return alertsDAO.getSearchTemplates(null, null, UserType.PENSKE);
	}
}
