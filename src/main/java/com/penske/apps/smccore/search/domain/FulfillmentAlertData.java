package com.penske.apps.smccore.search.domain;

/**
 * Information about how many vehicles have certain conditions that are highlighted in the Order Confirmation module.
 */
public class FulfillmentAlertData
{
	/** How many deals are currently in the WORKING status */
	private int workingCount;
	/** How many deals are currently in the PEND status */
	private int pendingCount;
	/** How many deals are ready to order - that is, approved by Vehicle Supply and Contract Admin */
	private int readyToOrderCount;
	/** How many deals are approved by Vehicle Supply but pending Contract Admin approval */
	private int contractReviewCount;
	/** How many vendors still lack a Supply Specialist and Planning Analyst assignment */
	private int vendorAnalystAssignmentRequiredCount;
	/** How many new vendors require setup in the admin console */
	private int newVendorSetupRequiredCount;
	/** How many new vendor users require setup in the admin console */
	private int vendorUserSetupRequiredCount;
	
	// DEFAULT ACCESSORS
	public int getWorkingCount() {
		return workingCount;
	}
	public int getPendingCount() {
		return pendingCount;
	}
	public int getReadyToOrderCount() {
		return readyToOrderCount;
	}
	public int getContractReviewCount() {
		return contractReviewCount;
	}
	public int getNewVendorSetupRequiredCount() {
		return newVendorSetupRequiredCount;
	}
	public int getVendorAnalystAssignmentRequiredCount() {
		return vendorAnalystAssignmentRequiredCount;
	}
	public int getVendorUserSetupRequiredCount() {
		return vendorUserSetupRequiredCount;
	}
}
