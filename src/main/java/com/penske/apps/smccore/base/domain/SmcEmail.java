package com.penske.apps.smccore.base.domain;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;

/**
 * An email message that should get sent from SMC to one or more users
 */
public class SmcEmail {
	/** Internal database ID of the email record */
	private Integer emailAuditId;
	/** The sso of the user sending the email. Can't be null*/
	private String sso;
	/** The To Address(es) for the email. Can be a semicolon seperated list of emails. Can't be null*/
	private String toAddress;
	/** The CC address(es) for the email. Can be a semicolon seperated list of emails. Nullable*/
	private String ccAddress;
	/** The BCC address(es) for the email. Can be a semicolon seperated list of emails. Nullable*/
	private String bccAddress;
	/** The body for the email. Nullable*/
	private String body;
	/** The subject for the email. Can't be null*/
	private String subject;
	/** The name of the email template. Can't be null*/
	private EmailTemplateType emailType;
	
	/** Constructor for MyBattis*/
	protected SmcEmail(){}
	
	/** Creates a user-generated email, Does some validation on internal state
	 * @param userSso The SSO of the user generating the email. The user's sso IS required for the email
	 * @param toAddress The To Address for the email. To Address IS required for the email
	 * @param ccAddress The CC Address for the email. CC Address is NOT required for the email
	 * @param bccAddress The BCC Address for the email. BCC Address is NOT required for the email
	 * @param body The body for the email. Body is NOT required for the email
	 * @param subject The subject for the email. Subject IS required for the email
	 * */
	public SmcEmail(EmailTemplateType emailType, String userSso, String toAddress, String ccAddress, String bccAddress, String body, String subject) {
		if(emailType == null)
			throw new IllegalArgumentException("Every email must have a email template type.");
		if(StringUtils.isBlank(userSso))
			throw new IllegalArgumentException("Every email must have a user that created it.");
		if(StringUtils.isBlank(toAddress))
			throw new IllegalArgumentException("Every email must have a To Address.");
		if(StringUtils.isBlank(subject))
			throw new IllegalArgumentException("Every email must have a Subject.");
		
		this.emailType = emailType;
		this.sso = userSso;
		this.toAddress = toAddress;
		this.ccAddress = ccAddress;
		this.bccAddress = bccAddress;
		this.body = body;
		this.subject = subject;
	}

	//***** GENERATED ACCESSORS *****//
	public String getSso() {
		return sso;
	}

	public String getToAddress() {
		return toAddress;
	}

	public String getCcAddress() {
		return ccAddress;
	}

	public String getBccAddress() {
		return bccAddress;
	}

	public String getBody() {
		return body;
	}

	public String getSubject() {
		return subject;
	}

	public Integer getEmailAuditId()
	{
		return emailAuditId;
	}

	public EmailTemplateType getEmailType() {
		return emailType;
	}
}
