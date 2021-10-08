package com.penske.apps.smccore.base.domain;

import com.penske.apps.smccore.base.domain.enums.AlertType;
import com.penske.apps.smccore.base.domain.enums.SmcTab;

/**
 * Information about a type of alert to notify the user that some part of the system is outside normal tolerances.
 */
public class SmcAlert
{
	/** Database ID of the type of alert */
	private int alertID;
	/** The type of alert this information is about */
	private AlertType alertType;
	/** The human-readable name of the alert */
	private String alertName;
	/** The ID of the search template that corresponds to the alert */
	private int templateId;
	/** The key of the template that corresponds to the alert */
	private String templateKey;
	/** The order in which this alert should be displayed, relative to other alerts under the same tab and alert header */
	private int displaySequence;
	/** The ID of the section the alert should be displaye under in the home screen */
	private int headerId;
	/** True if the alert should be visible to a vendor user. False if it should be hidden from them. */
	private boolean visibleToVendor;
	/** True if the alter should be visible to a Penske user. False if it should be hidden from them. */
	private boolean visibleToPenske;
	/** True if the user can click on the alert to take action on it from the home screen. False if they can not. */
	private boolean actionable;
	/** Tooltip text that should show up on the home screen when the user hovers over the alert */
	private String helpText;
	/** A limit used in the computation of some alerts */
	private int measureValue;
	/** The text to display if something is out of compliance with this alert */
	private String complianceText;
	
	private SmcTab tabKey;
	
	protected SmcAlert() {}
	
	// DEFAULT ACCESSORS
	public int getAlertID() {
		return alertID;
	}

	public AlertType getAlertType() {
		return alertType;
	}

	public String getAlertName() {
		return alertName;
	}

	public int getTemplateId() {
		return templateId;
	}
	
	public String getTemplateKey() {
		return templateKey;
	}

	public int getDisplaySequence() {
		return displaySequence;
	}

	public int getHeaderId() {
		return headerId;
	}

	public boolean isVisibleToVendor() {
		return visibleToVendor;
	}

	public boolean isVisibleToPenske() {
		return visibleToPenske;
	}

	public boolean isActionable() {
		return actionable;
	}

	public String getHelpText() {
		return helpText;
	}

	public int getMeasureValue() {
		return measureValue;
	}

	public String getComplianceText() {
		return complianceText;
	}

	public SmcTab getTabKey() {
		return tabKey;
	}                  
}
