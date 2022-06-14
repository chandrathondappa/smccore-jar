package com.penske.apps.smccore.search.domain;

/**
 * Information about how many vehicles have certain conditions that are highlighted in the Order Confirmation module.
 */
public class ConfirmationAlertData
{
	private int myId;
	/** How many unconfirmed cancellations there are */
	private int cancellationCount;
	/** How many unconfirmed change orders there are */
	private int changeOrderCount;
	/** How many unconfirmed purchase orders there are */
	private int purchaseOrderCount;

	// DEFAULT ACCESSORS
	public int getCancellationCount() {
		return cancellationCount;
	}
	public int getChangeOrderCount() {
		return changeOrderCount;
	}
	public int getPurchaseOrderCount() {
		return purchaseOrderCount;
	}
	public int getMyId()
	{
		return myId;
	}
	
}
