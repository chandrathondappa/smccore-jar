package com.penske.apps.smccore.search.domain;

/**
 * Information about how many vehicles have certain conditions that are highlighted in the Production module.
 */
public class ProductionAlertData
{
	/** How many units have data conflicts on them */
	private int dataConflictCount;
	/** How many PO/Units require delay comments to explain why they are late */
	private int delayCommReqCount;
	/** How many PO/Units are currently scheduled to be delivered early */
	private int deliveryDateEarlyCount;
	/** How many PO/Units are currently scheduled to be delivered late */
	private int deliveryDateLateCount;
	/** How many PO/Units have their delivery date outside the acceptable window */
	private int deliveryDateOutOfRangeCount;
	/** How many PO/Units whose Estimated Delivery Date is in the past */
	private int estDeliveryDatePastDueCount;
	/** How many PO/Units whose Estimated Production Date is in the past */
	private int estProductionDatePastDueCount;
	/** How many PO/Units are missing component information that Penske requires */
	private int missingInfoCount;
	/** How many PO/Units are currently scheduled to be produced early */
	private int prodDateEarlyCount;
	/** How many PO/Units are currently scheduled to be produced late */
	private int prodDateLateCount;
	/** How many PO/Units have their production date outside the acceptable window */
	private int prodDateOutOfRangeCount;
	/** How many PO/Units have been put on hold */
	private int prodHoldsCount;
	
	protected ProductionAlertData() {}

	// MODIFIED ACCESSORS
	
	// DEFAULT ACCESSORS
	public int getDataConflictCount() {
		return dataConflictCount;
	}

	public int getDelayCommReqCount() {
		return delayCommReqCount;
	}

	public int getDeliveryDateEarlyCount() {
		return deliveryDateEarlyCount;
	}

	public int getDeliveryDateLateCount() {
		return deliveryDateLateCount;
	}

	public int getDeliveryDateOutOfRangeCount() {
		return deliveryDateOutOfRangeCount;
	}

	public int getEstDeliveryDatePastDueCount() {
		return estDeliveryDatePastDueCount;
	}

	public int getEstProductionDatePastDueCount() {
		return estProductionDatePastDueCount;
	}

	public int getMissingInfoCount() {
		return missingInfoCount;
	}

	public int getProdDateEarlyCount() {
		return prodDateEarlyCount;
	}

	public int getProdDateLateCount() {
		return prodDateLateCount;
	}

	public int getProdDateOutOfRangeCount() {
		return prodDateOutOfRangeCount;
	}

	public int getProdHoldsCount() {
		return prodHoldsCount;
	};
	
}
