package com.penske.apps.smccore.base.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.penske.apps.smccore.base.dao.AlertsDAO;
import com.penske.apps.smccore.base.domain.ConfirmationAlertData;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.ProductionAlertData;
import com.penske.apps.smccore.base.domain.SmcAlert;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.SecurityFunction;
import com.penske.apps.smccore.base.domain.enums.SmcTab;
import com.penske.apps.smccore.base.domain.enums.UserType;

@Service
public class AlertsService {
	
	private final AlertsDAO alertsDAO;
	private final UserService userService;
	
	@Autowired
	public AlertsService(AlertsDAO alertsDAO, UserService userService)
	{
		this.alertsDAO = alertsDAO;
		this.userService = userService;
	}
	
	public List<SmcAlert> getAlertsForTab(SmcTab tab, int headerId, UserType userType, UserType penskeUserType) {
		return alertsDAO.getAlertsForTab(tab, headerId, userType, penskeUserType);
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
		if(UserType.VENDOR == user.getUserType())
		{
			if(user.getAssociatedVendorIds().isEmpty())
				return null;
			else
				return alertsDAO.getConfirmationAlertData(user.getAssociatedVendorIds(), true);
		}
		else {
			List<Integer> vendorIdsFromFilter = userService.getVendorIdsFromVendorFilter(user);
			if(vendorIdsFromFilter == null || vendorIdsFromFilter.isEmpty())
				return alertsDAO.getConfirmationAlertData(user.getAssociatedVendorIds(), false);
			else
				return alertsDAO.getConfirmationAlertData(vendorIdsFromFilter, false);
		}
	}
}
