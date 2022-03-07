/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import java.time.LocalDateTime;

/**
 * A single successful login for a single user in SMC
 */
public class UserLogin
{
	/** Internal database ID */
	private int loginId;
	/** The SSO of the user who logged in */
	private String sso;
	/** The server the user logged into */
	private String serverLocation;
	/** The time of the user's login */
	private LocalDateTime loginTime;
	
	/** Null constructor - MyBatis only */
	protected UserLogin() {}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{UserLoginItem (SSO: " + sso + ") - " + loginTime.toString() + "}";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getLoginId()
	{
		return loginId;
	}

	public String getSso()
	{
		return sso;
	}

	public String getServerLocation()
	{
		return serverLocation;
	}

	public LocalDateTime getLoginTime()
	{
		return loginTime;
	}
}
