/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * The different types of email templates used by the SMC applications
 */
public enum EmailTemplateType implements MappedEnum
{
	/** The deal was pended from SMCOF or SMC daily batch */
	DEAL_PEND("DEALPEND"),
	/** A deal was prioritized and assigned to a buyer */
	DEAL_PRIORITIZED("NOTIFYSALESPERSON"),
	/** Email sent to someone that has actionable items when daily batch runs */
	DAILY_BATCH("DAILYBATCH"),
	/** Email sent to summarize the alerts a person has when daily batch runs */
	DAILY_SUMMARY_BATCH("DAILY_SUMMARY_BATCH"),
	/** Batch unit updates were processed successfully */
	UNIT_UPDATES_PASS("UNIT_UPDATES_NO_ERR"),
	/** Batch unit updates ran into errors and were not processed */
	UNIT_UPDATES_FAIL("UNIT_UPDATES_ERR"),
	/** A deal was pended because it was in a certain status when the deal was resubmitted. */
	LOADER_PEND_STATUS("LOADER_PEND_STATUS"),
	/** A deal was pended because it had a cost difference when it was resubmitted. */
	LOADER_PEND_COST("LOADER_PEND_COST"),
	/** Notification to a salea associate that spec confirmation has been enabled on a given deal. */
	SPEC_CONFIRMATION("SPEC_CONFIRMATION"),
	/** A new user was added to the system. */
	NEW_USER("NEW_USER"),
	/** Vendor User created. */
	EXISTING_USER("EXISTING_USER"),
	/** E-mail to Analyst that a new Vendor is assigned. */
	VENDOR_ASSIGNED("VENDOR_ASSIGNED"),
	/** MassUpload used in SMCNOTIFY. */
	MASS_UPLOAD("MASS_UPLOAD"),
	/** Loadsheet created with a Category/Subcatergory combination not within the Unit Templates. */
	UNIT_TEMPLATE("UNIT_TEMPLATE"),
	/** It is for using in JUnit tests. */
	UNKNOWN("UNKNOWN"),
	;
	
	private final String code;
	
	private EmailTemplateType(String code)
	{
		this.code = code;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return code;
	}

	public String getCode()
	{
		return code;
	}
}
