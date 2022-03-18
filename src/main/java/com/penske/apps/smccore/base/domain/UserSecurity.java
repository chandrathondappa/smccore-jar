/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.exception.BusinessRuleException;

/**
 * Security information related to a user (as of 2022-03-03, this is only for vendor users,
 * since Penske users don't currently have one-time passwords or require access codes to login).
 * 
 * This is a separate object from {@link User}, since we don't want passwords and security tokens getting serialized in the session or sent to logs.
 */
public class UserSecurity
{
	/** How many minutes a two-factor authentication access code is valid for before it expires */
	public static final int ACCESS_CODE_EXPIRATION_MINUTES = 60;
	
	/** Internal database ID */
	private Integer userSecurityId;
	/** The ID of the user that this security object pertains to */
	private int userId;
	/**
	 * The temporary SSO password the user receives and which is only good until they first log in and setup their account.
	 * This is cleared upon first login to SMC, since the user has to have set up their SSO account in order to get into SMC in the first place.
	 */
	private String oneTimePassword;
	/** The first date this user was sent a "welcome to SMC" email */
	private LocalDateTime newUserEmailDate;
	/** A two-factor authentication access code that the user must enter once a day. Access codes are only valid for a limited amount of time. */
	private String accessCode;
	/** The timestamp that the access code was created. Used to determine when it expires */
	private LocalDateTime accessCodeCreatedDate;
	
	/** Null constructor - MyBatis only */
	protected UserSecurity() {}
	
	/**
	 * Creates a new security object for a given user.
	 * @param userId The user for which to create the security object
	 * @param oneTimePassword Optional. If the user was not in LDAP, this should be the one-time password from the user creation service. If the user was in LDAP, this should be null, since
	 * 	they don't have a one-time password anymore if they've already logged in.
	 */
	public UserSecurity(User user, String oneTimePassword)
	{
		if(user == null)
			throw new IllegalArgumentException("User is required to create a UserSecurity");
		
		this.userId = user.getUserId();
		//One-time password is optional, since a user might be new to SMC, but might already have an SSO account, which means they won't have a one-time password anymore
		this.oneTimePassword = (StringUtils.isBlank(oneTimePassword) ? null : oneTimePassword);
		//User starts in a state representing an invalid access code, since they've never logged into SMC before
		this.accessCodeCreatedDate = LocalDateTime.of(1970, 1, 1, 0, 0);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{UserSecurity (User ID: " + userId + ")}";
	}

	//***** MODIFIED ACCESSORS *****//
	/**
	 * Checks if the user ought to be challenged to provide a 2-factor authentication access code upon login.
	 * Users are challenged each calendar day until they have successfully logged in that day
	 * @return True if the user should be challenged for a code on login attempt. False if they should not.
	 */
	public boolean isAccessTokenRequired()
	{
		boolean hasAccessCode = StringUtils.isNotBlank(this.accessCode);
		boolean codeWasGeneratedToday = !this.accessCodeCreatedDate.isBefore(LocalDate.now().atStartOfDay());
		
		//No access code means they correctly answered the challenge at some time in the past
		//Code generated today means the time they correctly answered it was today, so don't challenge them
		if(!hasAccessCode && codeWasGeneratedToday)
			return false;
		else
			return true;
	}
	
	/**
	 * Checks if the access code the user typed in matches the access code we generated, and that the access code is not expired.
	 * @param userAccessCode The access code the user typed in
	 * @return True if the user's access code matches the one we have, and the access code is not expired
	 */
	public boolean isAccessCodeValid(String userAccessCode)
	{
		boolean hasAccessCode = StringUtils.isNotBlank(this.accessCode);
		boolean codeWasGeneratedRecently = isAccessCodeGeneratedRecently();
		
		if(hasAccessCode && codeWasGeneratedRecently)
		{
			return StringUtils.equals(userAccessCode, this.accessCode);
		}
		else
			return false;
	}
	
	/**
	 * Checks if the access code was generated recently and has not expired
	 * @param userAccessCode The access code the user typed in
	 * @return True if the user's access code is not expired. False if it is expired and they will need a new one.
	 */
	public boolean isAccessCodeGeneratedRecently()
	{
		return this.accessCodeCreatedDate.isAfter(LocalDateTime.now().minusMinutes(ACCESS_CODE_EXPIRATION_MINUTES));
	}
	
	/**
	 * Registers the fact that a welcome email was sent to this user already
	 */
	public void markNewUserEmailSent()
	{
		if(this.newUserEmailDate != null)
			throw new BusinessRuleException("New user email was already sent for this user (ID " + userId + ")");
		
		this.newUserEmailDate = LocalDateTime.now();
	}
	
	/**
	 * Resets the 2-factor authentication access code for this user.
	 * @param accessCode The new access code.
	 */
	public void setNewAccessCode(String accessCode)
	{
		if(StringUtils.isBlank(accessCode))
			throw new IllegalArgumentException("Access code is required");
		if(accessCode.length() != 6)
			throw new IllegalArgumentException("Access code must be six digits");
		
		this.accessCode = accessCode;
		this.accessCodeCreatedDate = LocalDateTime.now();
	}
	
	/**
	 * Records that the user successfully provided an access code, and is cleared to access the system.
	 */
	public void clearAccessCode()
	{
		this.accessCode = null;
	}
	
	/**
	 * Records that the user has set up their account successfully and no longer needs the one-time password.
	 */
	public void clearOneTimePassword()
	{
		this.oneTimePassword = null;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Integer getUserSecurityId()
	{
		return userSecurityId;
	}

	public int getUserId()
	{
		return userId;
	}

	public String getOneTimePassword()
	{
		return oneTimePassword;
	}

	public LocalDateTime getNewUserEmailDate()
	{
		return newUserEmailDate;
	}
	
	public LocalDateTime getAccessCodeCreatedDate() {
		return accessCodeCreatedDate;
	}
}	