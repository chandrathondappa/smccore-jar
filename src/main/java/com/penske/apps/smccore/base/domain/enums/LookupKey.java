/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A lookup name that the SMC Loader is interested in from SMC_LOOKUP
 */
public enum LookupKey
{
	//***** Emails / Phone Numbers / URLs *****//
	/** The phone number that internal users should call for assistance with the application. */
	SUPPORT_PHONE_NUM("SUPPORT_NUM"),
	/** The phone number that external vendor users should call for assistance with the application. */
	CUSTOMER_SERVICE_PHONE_NUM("PENSKE_CUST_SERVICE_TOLL_FREE"),
	/** The email address that external vendor users can send to for assistance with the application */
	CUSTOMER_SERVICE_EMAIL("PENSKE_CUST_SERVICE_EMAIL"),
	/** Phone number for the Penske IT service desk */
	IT_SERVICE_PHONE_NUM("PENSKE_CUST_SERVICE_TOLL_FREE"),
	/** Email address for eh Penske IT service desk */
	IT_SERVICE_EMAIL("PENSKE_IT_SERVICE_EMAIL"),
	/** The list of email addresses to send warning emails to if a loadsheet is created before the corresponding unit template */
	UNIT_TEMPLATE_EMAIL_LIST("UNIT_TEMPLATE_EMAIL_LIST"),
	/** The external entry point for the SMC application, for inclusion in vendor emails. */
	SMC_APP_LINK("SMC_APPLICATION_URL"),
	/** The link that external vendor users can go to in order to get to their SSO login page */
	PENSKE_SIGN_ON_URL("PENSKE_SIGN_ON_URL"),
	
	//***** Documents *****//
	/** The list of file types allowed to be uploaded into SMC. Anything else will get tagged with a "pdf" extension */
	RECOGNIZED_FILE_TYPES("FILETYPE"),
	/** The ID in DAS of the document that should be attached to every spec confirmation noficiation email */
	SPEC_CONFIRMATION_DAS_ID("SPEC_CONFIRMATION_DAS_ID"),
	
	//***** SMCOF - Deal Review *****//
	/** Key for the different types of workflows an SMC user can submit (ex: change order, misbuilt, etc.) */
	WORKFLOW_REQUEST_TYPE("WORKFLOW_REQUEST_TYPE"),
	/** The base URL for all SupportCentral workflows accessed from Deal Review */
	WORKFLOW_REQUEST_URL("WORKFLOW_REQUEST_URL"),
	/** A link to the freight rates for bodies and trailers, for use when reviewing cost sheets. */
	FREIGHT_RATE_URL("FRIGHT_RATE_SHEET"),
	/** Acceptable reasons that a deal could be reviewed immediately */
	REVIEW_IMMEDIATELY_REASONS("REASON_CODE"),
	/** The selection of valid vehicle uses when creating a DCN */
	DCN_VEHICLE_USE("VEHICLE_USE"),
	/** The possible corps that a newly-created DCN can belong to */
	DCN_CORP("CORP"),
	/** The PO categories that a newly-created DCN can have */
	DCN_PO_CATEGORY("PO_CATEGORY"),
	/** The different statuses that a DCN can be in for the Vehicle Supply department */
	VS_STATUS("VS_STATUS"),
	/** The different statuses that a DCN can be in for the Contract Admin department */
	CA_STATUS("CA_STATUS"),
	/** The line of business a unit belongs to (ADD, NEW, REPLACE) */
	UNIT_BUSINESS_TYPE("BUSINESS_TYPE"),
	/** The way a unit range is assigned to a deal */
	UNIT_ASSIGNMENT_TYPE("ASSIGNMENT_TYPE"),
	
	//***** SMCOF - Loadsheet *****//
	/** The codes that are used to classify the level of luxury of company cars. */
	SERVICE_VEHICLE_GROUP_CODE("SERVICE_VEHICLE_GROUP_CODE"),
	/** Whether the NONE and OTHER PO Categories should be disabled for loadsheet creation or not. */
	DISABLE_NONE_OTHER("DISABLE_NONE_OTHER"),
	/** True to turn off validation that checks that the loadsheet total matches the CATS total before issuing POs or approving cost sheets. */
	DISABLE_NCT_MATCHING("DISABLE_NCT_MATCHING"),
	
	//***** SMCOP *****//
	/** The default number of days before today that the "Confirmed Orders" template in Order Confirmation should show orders for */
	CONFIRMED_TEMPLATE_DAYS("CONFIRMED_TEMPLATE_DAYS"),
	/** How many days must elapse before vendor users are prompted to agree to the terms and conditions again */
	AGREEMENT_PERIOD("AGREEMENT_PERIOD"),
	
	//***** SMC Notify - Task Frequency *****//
	/** How many threads run in SMC Notify to send emails via EBS */
	EMAIL_PROC_POOL_SIZE("SMCNOTIFY_POOL_SIZE_EMAIL_PROC"),
	/** How many threads run in SMC Notify to process batch uploads via submission of the Unit Details page */
	UNIT_QUEUE_POOL_SIZE("SMCNOTIFY_POOL_SIZE_UNIT_QUEUE"),
	/** How many threads run in SMC Notify to process batch uploads via Excel upload*/
	XLS_QUEUE_POOL_SIZE("SMCNOTIFY_POOL_SIZE_XLS_QUEUE"),
	/** How long (milliseconds) SMC Notify waits to check if batch uploads are finished and generate summary emails for them. */
	XLS_MAIL_GENERATOR_SLEEP_TIME("SMCNOTIFY_SLEEP_TIME_MAIL_GENERATOR"),
	/** How long (milliseconds) SMC Notify waits to check for batch uploads from the Unit Details screen */
	UNIT_QUEUE_SLEEP_TIME("SMCNOTIFY_SLEEP_TIME_UNIT_QUEUE"),
	/** How long (milliseconds) SMC Notify waits to check for batch uploads from Excel uploads*/
	XLS_QUEUE_SLEEP_TIME("SMCNOTIFY_SLEEP_TIME_XLS_QUEUE"),
	/** How long (milliseconds) SMC Notify waits to check for new emails to send via EBS. */
	EMAIL_PROC_SLEEP_TIME("SMCNOTIFY_SLEEP_TIME_EMAIL_PROC"),
	/** How long (milliseconds) SMC Notify waits between refreshing the SMC_PRODUCTION_SUMMARY table */
	PROD_ACTION_SLEEP_TIME("SMCNOTIFY_SLEEP_TIME_PROD_ACTION"),
	/** How long (seconds) SMC Notify waits between logging detailed timing information to a log file */
	TIMING_LOG_INTERVAL("SMCNOTIFY_TIMING_LOG_INTERVAL"),
	
	//***** SMC Notify - EBS Configuration *****//
	/** The email address that SMC notification emails appear to come from. */
	EBS_FROM_ADDRESS("EBS_FROM_ADDRESS"),
	/** The priority in EBS with which SMC notification emails get sent */
	EBS_PRIORITY("EBS_PRIORITY"),
	/** The high priority in EBS with which important SMC notification emails get sent */
	EBS_HIGH_PRIORITY("EBS_HIGH_PRIORITY"),
	;

	private static final Map<String, LookupKey> valuesByDbName = new HashMap<String, LookupKey>();
	
	/** The lookup name in SMC_LOOKUP that this key corresponds to */
	private final String dbName;
	
	static
	{
		for(LookupKey lookupKey : values())
			valuesByDbName.put(lookupKey.dbName, lookupKey);
	}
	
	private LookupKey(String dbName)
	{
		this.dbName = dbName;
	}
	
	public static LookupKey findByDbName(String dbName)
	{
		return valuesByDbName.get(dbName);
	}

	public static Set<String> getAllDbNames()
	{
		return Collections.unmodifiableSet(valuesByDbName.keySet());
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the dbName
	 */
	public String getDbName()
	{
		return dbName;
	}
}
