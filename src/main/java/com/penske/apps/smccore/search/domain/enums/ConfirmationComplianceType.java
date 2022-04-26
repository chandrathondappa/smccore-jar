/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.search.domain.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;

import com.penske.apps.smccore.search.domain.ConfirmationSearch;

/**
 * The different ways that orders can be out of compliance in Order Confirmation
 */
public enum ConfirmationComplianceType
{
	/** PO has been unconfirmed too many business days */
	UNCONFIRMED_PO(AlertType.OC_UNCONFIRMED_PO) {
		@Override public ConfirmationSearch createComplianceSearch(Map<AlertType, Integer> measureValues) {
			Integer measureValue = measureValues.get(this.getAlertType());
			if(measureValue == null)
				throw new IllegalArgumentException("Unable to find measurement value for compliance " + this.name());
			
			ConfirmationSearch search = this.getAlertType().createConfirmationSearch()
				.setDaysUnconfirmedOperatorString("greater")
				.setDaysUnconfirmed(measureValue)
				.setSentViaEdi(null);	//We evaluate compliance for all types of orders, even those sent by EDI
			
			return search;
		}
	},
	/** CO has been unconfirmed too many business days */
	UNCONFIRMED_CO(AlertType.OC_UNCONFIRMED_CO) {
		@Override public ConfirmationSearch createComplianceSearch(Map<AlertType, Integer> measureValues) {
			Integer measureValue = measureValues.get(this.getAlertType());
			if(measureValue == null)
				throw new IllegalArgumentException("Unable to find measurement value for compliance " + this.name());
			
			ConfirmationSearch search = this.getAlertType().createConfirmationSearch()
				.setDaysUnconfirmedOperatorString("greater")
				.setDaysUnconfirmed(measureValue)
				.setSentViaEdi(null);	//We evaluate compliance for all types of orders, even those sent by EDI
			
			return search;
		}
	},
	/** Cancellation has been unconfirmed too many business days */
	UNCONFIRMED_CANCELLATION(AlertType.OC_UNCONFIRMED_CANCELLATION) {
		@Override public ConfirmationSearch createComplianceSearch(Map<AlertType, Integer> measureValues) {
			Integer measureValue = measureValues.get(this.getAlertType());
			if(measureValue == null)
				throw new IllegalArgumentException("Unable to find measurement value for compliance " + this.name());
			
			ConfirmationSearch search = this.getAlertType().createConfirmationSearch()
				.setDaysUnconfirmedOperatorString("greater")
				.setDaysUnconfirmed(measureValue)
				.setSentViaEdi(null);	//We evaluate compliance for all types of orders, even those sent by EDI
			
			return search;
		}
	},
	
	;

	private static final Map<String, ConfirmationComplianceType> ALL_VALUES = Stream.of(values())
		.collect(toMap(ConfirmationComplianceType::name, x -> x));
	
	/**
	 * The alert that houses the human-readable message that shown to the user if an item is out of compliance.
	 * For most compliance items, this alert is also the basis for the search that defines which items get flagged with this compliance type.
	 */
	private final AlertType alertType;

	private ConfirmationComplianceType(AlertType alertType)
	{
		this.alertType = alertType;
	}

	public static ConfirmationComplianceType findByName(String name)
	{
		return ALL_VALUES.get(name);
	}
	
	/**
	 * Creates a new search object with the criteria to determine whether an item should be flagged as out of compliance.
	 * @param measureValues A map of the measurement values used in comparisons to determine compliance violations. Keyed by alert type, this information generally comes from SMC_ALERTS.
	 * @return A search that captures what it means to be in violation of this compliance type
	 */
	public abstract ConfirmationSearch createComplianceSearch(Map<AlertType, Integer> measureValues);

	/**
	 * Gets the alert that corresponds to the human-readable message shown to the user in the event an item violates this compliance type.
	 * @return The type of alert used to get the human-readable message
	 */
	public AlertType getAlertType()
	{
		return alertType;
	}
}