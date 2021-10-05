package com.penske.apps.smccore.base.domain.enums;

import com.penske.apps.smccore.base.domain.ConfirmationAlertData;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.ProductionAlertData;

public enum AlertType implements MappedEnum {
	
	// FULFILLMENT
	OF_WORKING("ALRT_OF_WORKING"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getWorkingCount();
		}
	},
	OF_PENDED("ALRT_OF_PENDED"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getPendingCount();
		}
	},
	OF_READY_TO_ORDER("ALRT_OF_READY_TO_ORDER"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getReadyToOrderCount();
		}
	},
	OF_CONTRACT_RVW("ALRT_OF_CONTRACT_RVW"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getContractReviewCount();
		}
	},
	OF_VEND_ANLYST_ASSG_REQ("ALRT_OF_VEND_ANLYST_ASSG_REQ"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData,
				ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getVendorAnalystAssignmentRequiredCount();
		}
	},
	OF_NEW_VEND_SETUP_REQ("ALRT_OF_NEW_VEND_SETUP_REQ"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData,
				ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getNewVendorSetupRequiredCount();
		}
	},
	OF_VEND_USER_SETUP_REQ("ALRT_OF_VEND_USER_SETUP_REQ"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData,
				ProductionAlertData productionAlertData) {
			return fulfillmentAlertData == null ? 0 : fulfillmentAlertData.getVendorUserSetupRequiredCount();
		}
	},
	
	//CONFIRMATION
	OC_PURCHASE_ORDER("ALRT_OC_PURCHASE_ORDER"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return confirmationAlertData == null ? 0 : confirmationAlertData.getPurchaseOrderCount();
		}
	},
	OC_CHANGE_ORDER("ALRT_OC_CHANGE_ORDER"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return confirmationAlertData == null ? 0 : confirmationAlertData.getChangeOrderCount();
		}
	},
	OC_CANC_ORDER("ALRT_OC_CANC_ORDER"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return confirmationAlertData == null ? 0 : confirmationAlertData.getCancellationCount();
		}
	},
	
	//PRODUCTION
	PROD_EST_PROD_PAST_DUE("ALRT_PROD_EST_PROD_PAST_DUE"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getEstProductionDatePastDueCount();
		}
	},
	PROD_EST_DELV_PAST_DUE("ALRT_PROD_EST_DELV_PAST_DUE"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getEstDeliveryDatePastDueCount();
		}
	},
	PROD_PROD_HOLDS("ALRT_PROD_PROD_HOLDS"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getProdHoldsCount();
		}
	},
	ALL_MISSING_INFO("ALRT_ALL_MISSING_INFO"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getMissingInfoCount();
		}
	},
	PROD_DATA_CONFLICT("ALRT_PROD_DATA_CONFLICT"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getDataConflictCount();
		}
	},
	PROD_DELAY_COMM_REQ("ALRT_PROD_DELAY_COMM_REQ"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getDelayCommReqCount();
		}
	},
	PROD_PROD_DT_EARLY("ALRT_PROD_PROD_DT_EARLY"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getProdDateEarlyCount();
		}
	},
	PROD_PROD_DT_LATE("ALRT_PROD_PROD_DT_LATE"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getProdDateLateCount();
		}
	},
	PROD_DELV_DT_EARLY("ALRT_PROD_DELV_DT_EARLY"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getDeliveryDateEarlyCount();
		}
	},
	PROD_DELV_DT_LATE("ALRT_PROD_DELV_DT_LATE"){
		@Override
		public int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData) {
			return productionAlertData == null ? 0 : productionAlertData.getDeliveryDateLateCount();
		}
	},
	
	;
	
	private final String alertKey;
	
	public abstract int extractCount(FulfillmentAlertData fulfillmentAlertData, ConfirmationAlertData confirmationAlertData, ProductionAlertData productionAlertData);

	private AlertType(String alertKey)
	{
		this.alertKey = alertKey;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue() {
		return getAlertKey();
	}
	
	//***** MODIFED ACCESSORS *****//
	public static AlertType findByAlertKey(String alertKey)
	{
		for(AlertType alert : values())
		{
			if(alert.alertKey.equals(alertKey))
				return alert;
		}
		
		return null;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getAlertKey() {
		return alertKey;
	}
	
}
