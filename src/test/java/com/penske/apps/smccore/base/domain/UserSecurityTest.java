/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.TestData;

/**
 * Class under test: {@link UserSecurity}
 */
public class UserSecurityTest
{
	private final TestData data = new TestData();
	
	private final User user = data.userVendor;
	//Represents a user that hasn't set up their SSO account yet
	private final UserSecurity sec = new UserSecurity(user, "temppw");
	//Represents a user that has set up their SSO account, and so doesn't have a one-time password
	private final UserSecurity existingSec = new UserSecurity(user, null);
	
	@Rule
	public final ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void shouldCreate()
	{
		assertThat(sec.getUserId(), is(user.getUserId()));
		assertThat(sec.getNewUserEmailDate(), is(nullValue()));
		assertThat(sec.getOneTimePassword(), is("temppw"));
		
		assertThat(existingSec.getUserId(), is(user.getUserId()));
		assertThat(existingSec.getNewUserEmailDate(), is(nullValue()));
		assertThat(existingSec.getOneTimePassword(), is(nullValue()));
		
		UserSecurity emptyPwSec = new UserSecurity(user, "");
		assertThat(emptyPwSec.getOneTimePassword(), is(nullValue()));
	}
	
	@Test
	public void shouldNotCreateWithoutUser()
	{
		thrown.expectMessage("User is required to create a UserSecurity");
		new UserSecurity(null, "temppw");
	}
	
	@Test
	public void shouldMarkNewUserEmailSent()
	{
		sec.markNewUserEmailSent();
		assertThat(sec.getNewUserEmailDate(), is(not(nullValue())));
	}
	
	@Test
	public void shouldMarkNewUserEmailSentTwice() throws InterruptedException
	{
		sec.markNewUserEmailSent();
		LocalDateTime emailSentDate = sec.getNewUserEmailDate();
		
		//Have to wait 5 milliseconds so that the dates are different from each other
		Thread.sleep(5);
		
		sec.markNewUserEmailSent();
		LocalDateTime emailSentDate2 = sec.getNewUserEmailDate();
		
		assertThat(emailSentDate, is(not(emailSentDate2)));
	}
	
	@Test
	public void shouldSetNewAccessCode()
	{
		assertThat(sec.isAccessCodeValid("876543"), is(false));
		sec.setNewAccessCode("876543");
		assertThat(sec.isAccessCodeValid("876543"), is(true));
	}
	
	@Test
	public void shouldNotSetNewAccessCodeTooLong()
	{
		thrown.expectMessage("Access code must be six digits");
		sec.setNewAccessCode("0876532");
	}
	
	@Test
	public void shouldNotSetNewAccessCodeTooShort()
	{
		thrown.expectMessage("Access code must be six digits");
		sec.setNewAccessCode("25876");
	}
	
	@Test
	public void shouldNotSetNewAccessCodeMissing()
	{
		thrown.expectMessage("Access code is required");
		sec.setNewAccessCode(null);
	}
	
	@Test
	public void shouldClearAccessCode()
	{
		sec.setNewAccessCode("555555");
		assertThat(sec.isAccessCodeValid("555555"), is(true));
		
		sec.clearAccessCode();
		assertThat(sec.isAccessCodeValid("555555"), is(false));
	}
	
	@Test
	public void shouldCheckIfAccessCodeRequired()
	{
		//has access code, generated today, generated < 60 minutes ago
		//Y					Y					Y
		sec.setNewAccessCode("555555");
		assertThat(sec.isAccessTokenRequired(), is(true));
		
		//Y					Y					N
		//Can't test this one either - can't guarantee it's being run today, but more than an hour ago
		
		//Y					N					Y
		//Can't test this one - can't set the current time to be between 12:00 AM and 12:59 AM
		
		//Y					N					N
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusDays(1));
		assertThat(sec.isAccessTokenRequired(), is(true));
		
		//N					Y					Y
		sec.setNewAccessCode("444444");
		sec.clearAccessCode();
		assertThat(sec.isAccessTokenRequired(), is(false));
		
		//N					Y					N
		//Can't test this one either - can't guarantee it's being run today, but more than an hour ago
		
		//N					N					Y
		//Can't test this one - can't set the current time to be between 12:00 AM and 12:59 AM
		
		//N					N					N
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusDays(1));
		assertThat(sec.isAccessTokenRequired(), is(true));
	}
	
	@Test
	public void shouldCheckIfAccessCodeIsValid()
	{
		String accessCode = "555555";
		
		//has access code, generated today, generated < 60 minutes ago
		//Y					*					Y
		sec.setNewAccessCode(accessCode);
		assertThat(sec.isAccessCodeValid(accessCode), is(true));
		assertThat(sec.isAccessCodeValid("333333"), is(false));
		
		//Y					*					N
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusMinutes(80));
		assertThat(sec.isAccessCodeValid(accessCode), is(false));
		
		//N					*					Y
		sec.setNewAccessCode(accessCode);
		sec.clearAccessCode();
		assertThat(sec.isAccessCodeValid(accessCode), is(false));
		
		//N					*					N
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusMinutes(80));
		assertThat(sec.isAccessCodeValid(accessCode), is(false));
	}
	
	@Test
	public void shouldCheckIfAccessCodeGeneratedRecently()
	{
		//generated < 60 minutes ago
		sec.setNewAccessCode("555555");
		assertThat(sec.isAccessCodeGeneratedRecently(), is(true));
		
		//generated > 1 day ago
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusDays(1));
		assertThat(sec.isAccessCodeGeneratedRecently(), is(false));
		
		//generated > 60 minutes ago
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusMinutes(61));
		assertThat(sec.isAccessCodeGeneratedRecently(), is(false));
		
		//generated < 60 minutes ago
		CoreTestUtil.set(sec, "accessCodeCreatedDate", LocalDateTime.now().minusMinutes(49));
		assertThat(sec.isAccessCodeGeneratedRecently(), is(true));
	}
}
