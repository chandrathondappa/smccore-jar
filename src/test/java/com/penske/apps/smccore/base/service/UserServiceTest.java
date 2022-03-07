/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.service;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.TestData;
import com.penske.apps.smccore.base.dao.EmailDAO;
import com.penske.apps.smccore.base.dao.UserDAO;
import com.penske.apps.smccore.base.domain.EmailTemplate;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.SmcEmail;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.UserLogin;
import com.penske.apps.smccore.base.domain.UserSecurity;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * Class under test: {@link UserService}
 */
public class UserServiceTest
{
	private final UserDAO userDAO = mock(UserDAO.class);
	private final EmailDAO emailDAO = mock(EmailDAO.class);
	private final UserService service = new UserService(userDAO, emailDAO);
	
	private final TestData data = new TestData();
	private final User userPenske = data.userPenske;
	private final User userVendor = data.userVendor;
	private final UserSecurity userSecurity = new UserSecurity(userVendor, "temppw");
	
	private final EmailTemplate newVendorTemplate = CoreTestUtil.createEmailTemplate(EmailTemplateType.NEW_VENDOR_USER, "Welcome to SMC", "Your temporary password is [OTP]");
	private final EmailTemplate existingVendorTemplate = CoreTestUtil.createEmailTemplate(EmailTemplateType.EXISTING_VENDOR_USER, "Welcome to SMC", "You already have an SSO account");
	
	private final LookupContainer lookups = CoreTestUtil.getLookupContainer();
	private final URL commonStaticUrl;
	
	private final ArgumentCaptor<UserSecurity> userSecurityCaptor = ArgumentCaptor.forClass(UserSecurity.class);
	private final ArgumentCaptor<SmcEmail> smcEmailCaptor = ArgumentCaptor.forClass(SmcEmail.class);
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	public UserServiceTest() throws MalformedURLException
	{
		this.commonStaticUrl = new URL("http://penske.com");
	}
	
	@Test
	public void shouldGetUser()
	{
		User user = CoreTestUtil.createUser(1234, "600013219", "JOHN", "PAPAVASILION", "test@penske.com", UserType.PENSKE);
		when(userDAO.getUsers("600013219", null, null, null, true, false)).thenReturn(Arrays.asList(user));

		User actualUser = service.getUser("600013219", false);

		assertThat(actualUser, is(user));
	}
	
	@Test
	public void shouldInsertUserSecurity_NewLDAPUser_SendEmail()
	{
		when(emailDAO.getEmailTemplate(EmailTemplateType.NEW_VENDOR_USER)).thenReturn(newVendorTemplate);
		
		service.insertUserSecurity(userPenske, userVendor, "temppw", true, lookups, commonStaticUrl);
		
		verify(userDAO).insertUserSecurity(userSecurityCaptor.capture());
		verify(emailDAO).insertSmcEmail(smcEmailCaptor.capture());
		
		UserSecurity sec = userSecurityCaptor.getValue();
		SmcEmail email = smcEmailCaptor.getValue();
		
		verify(userDAO).updateUserSecurity(sec);
		
		assertThat(sec.getOneTimePassword(), is("temppw"));
		assertThat(sec.getNewUserEmailDate(), is(not(nullValue())));
		assertThat(email.getEmailType(), is(EmailTemplateType.NEW_VENDOR_USER));
		assertThat(email.getBody(), is("Your temporary password is temppw"));
	}
	
	@Test
	public void shouldInsertUserSecurity_NewLDAPUser_NotSendEmail()
	{
		service.insertUserSecurity(userPenske, userVendor, "temppw", false, lookups, commonStaticUrl);
		
		verify(userDAO).insertUserSecurity(userSecurityCaptor.capture());
		verify(emailDAO, times(0)).insertSmcEmail(any());
		
		UserSecurity sec = userSecurityCaptor.getValue();
		
		assertThat(sec.getOneTimePassword(), is("temppw"));
		assertThat(sec.getNewUserEmailDate(), is(nullValue()));
	}
	
	@Test
	public void shouldInsertUserSecurity_ExistingLDAPUser_SendEmail()
	{
		when(emailDAO.getEmailTemplate(EmailTemplateType.EXISTING_VENDOR_USER)).thenReturn(existingVendorTemplate);
		
		service.insertUserSecurity(userPenske, userVendor, null, true, lookups, commonStaticUrl);
		
		verify(userDAO).insertUserSecurity(userSecurityCaptor.capture());
		verify(emailDAO).insertSmcEmail(smcEmailCaptor.capture());
		
		UserSecurity sec = userSecurityCaptor.getValue();
		SmcEmail email = smcEmailCaptor.getValue();
		
		verify(userDAO).updateUserSecurity(sec);
		
		assertThat(sec.getOneTimePassword(), is(nullValue()));
		assertThat(sec.getNewUserEmailDate(), is(not(nullValue())));
		assertThat(email.getEmailType(), is(EmailTemplateType.EXISTING_VENDOR_USER));
		assertThat(email.getBody(), is("You already have an SSO account"));
	}
	
	@Test
	public void shouldInsertUserSecurity_ExistingLDAPUser_NotSendEmail()
	{
		service.insertUserSecurity(userPenske, userVendor, null, false, lookups, commonStaticUrl);
		
		verify(userDAO).insertUserSecurity(userSecurityCaptor.capture());
		verify(emailDAO, times(0)).insertSmcEmail(any());
		
		UserSecurity sec = userSecurityCaptor.getValue();
		
		assertThat(sec.getOneTimePassword(), is(nullValue()));
		assertThat(sec.getNewUserEmailDate(), is(nullValue()));
	}

	@Test
	public void shouldRecordPenskeUserLoginFirstTime()
	{
		//When the user logs in the first time, they don't have any previous logins
		when(userDAO.getUserLogins(userPenske)).thenReturn(Collections.emptyList());
		
		LocalDateTime lastLogin = service.recordUserLogin(userPenske, null, "someserver");
		
		//No previous logins means no last login date
		assertThat(lastLogin, is(nullValue()));
		
		verify(userDAO).recordUserLogin(null, userPenske, "someserver");
	}
	
	@Test
	public void shouldRecordPenskeUserLoginMoreThan30Times()
	{
		//User already has 30 logins, so we should update the oldest one
		List<UserLogin> previousLogins = IntStream.range(1, 31)
			.mapToObj(i -> CoreTestUtil.createUserLogin(i, userPenske))
			//Logins sorted with most recent first
			.sorted(Comparator.comparing(UserLogin::getLoginTime).reversed())
			.collect(toList()); 
		
		when(userDAO.getUserLogins(userPenske)).thenReturn(previousLogins);
		
		LocalDateTime lastLogin = service.recordUserLogin(userPenske, null, "someserver");
		
		//Method returns the most recent login time
		assertThat(lastLogin, is(previousLogins.get(0).getLoginTime()));
		
		//Oldest login gets replaced
		verify(userDAO).recordUserLogin(previousLogins.get(29).getLoginId(), userPenske, "someserver");
	}
	
	@Test
	public void shouldRecordUserLoginForVendorWithoutOTP()
	{
		//User still has a temp password because they haven't logged into SMC yet
		userSecurity.clearOneTimePassword();
		userSecurity.setNewAccessCode("563876");
		
		UserLogin oldLogin = CoreTestUtil.createUserLogin(55, userVendor);
		
		when(userDAO.getUserLogins(userVendor)).thenReturn(Arrays.asList(
			oldLogin
		));
		
		LocalDateTime lastLogin = service.recordUserLogin(userVendor, userSecurity, "someserver");
		
		//No previous logins means no last login date
		assertThat(lastLogin, is(oldLogin.getLoginTime()));
		
		//Each login, the access code and password should be cleared
		assertThat(userSecurity.getOneTimePassword(), is(nullValue()));
		assertThat(userSecurity.isAccessTokenRequired(), is(false));
		assertThat(userSecurity.getNewUserEmailDate(), is(nullValue()));
		
		verify(userDAO).recordUserLogin(null, userVendor, "someserver");
		verify(userDAO).updateUserSecurity(userSecurity);
	}
	
	@Test
	public void shouldRecordUserLoginForVendorWithOTP()
	{
		//User still has a temp password because they haven't logged into SMC yet
		userSecurity.markNewUserEmailSent();
		userSecurity.setNewAccessCode("563876");
		
		//When the user logs in the first time, they don't have any previous logins
		when(userDAO.getUserLogins(userVendor)).thenReturn(Collections.emptyList());
		
		LocalDateTime lastLogin = service.recordUserLogin(userVendor, userSecurity, "someserver");
		
		//No previous logins means no last login date
		assertThat(lastLogin, is(nullValue()));
		
		//Each login, the access code and password should be cleared
		assertThat(userSecurity.getOneTimePassword(), is(nullValue()));
		assertThat(userSecurity.isAccessTokenRequired(), is(false));
		assertThat(userSecurity.getNewUserEmailDate(), is(notNullValue()));
		
		verify(userDAO).recordUserLogin(null, userVendor, "someserver");
		verify(userDAO).updateUserSecurity(userSecurity);
	}
	
	@Test
	public void shouldNotRecordUserLoginWithoutUser()
	{
		thrown.expectMessage("User is required to record a login for them");
		service.recordUserLogin(null, null, null);
	}
	
	@Test
	public void shouldNotRecordUserLoginForVendorWithoutSecurity()
	{
		thrown.expectMessage("User security info is required to record a login for a vendor user");
		service.recordUserLogin(userVendor, null, null);
	}
	
	@Test
	public void shouldNotRecordUserLoginForVendorMismatchedSecurity()
	{
		User vendorUser2 = CoreTestUtil.createUser(98767, "test@testvendor.com", "Test", "Other Vendor", "test@testvendor.com", UserType.VENDOR);
		UserSecurity sec = new UserSecurity(vendorUser2, null);
		
		thrown.expectMessage("User security info was for a different user.");
		service.recordUserLogin(userVendor, sec, null);
	}

	@Test
	public void shouldSendAccessCode() throws IllegalAccessException
	{
		when(emailDAO.getEmailTemplate(EmailTemplateType.ACCESS_CODE)).thenReturn(CoreTestUtil.createEmailTemplate(EmailTemplateType.ACCESS_CODE, "Access Code", "Your access code is [ACCESS_CODE]"));
		
		//Generate one the first time, and then generate it again, and test that the access codes are different
		service.generateAndSendAccessCode(userVendor, userSecurity);
		
		String accessCode1 = (String) FieldUtils.readField(FieldUtils.getField(UserSecurity.class, "accessCode", true), userSecurity);
		
		service.generateAndSendAccessCode(userVendor, userSecurity);
		
		String accessCode2 = (String) FieldUtils.readField(FieldUtils.getField(UserSecurity.class, "accessCode", true), userSecurity);
		
		assertThat(accessCode1, is(notNullValue()));
		assertThat(accessCode2, is(notNullValue()));
		assertThat(accessCode1, is(not(accessCode2)));
		
		verify(userDAO, times(2)).updateUserSecurity(userSecurity);
		verify(emailDAO, atLeastOnce()).insertSmcEmail(smcEmailCaptor.capture());
		
		List<SmcEmail> emails = smcEmailCaptor.getAllValues();
		assertThat(emails.get(0).getToAddress(), is(userVendor.getEmailAddress()));
		assertThat(emails.get(1).getToAddress(), is(userVendor.getEmailAddress()));
		assertThat(emails.get(0).getBody(), is("Your access code is " + accessCode1));
		assertThat(emails.get(1).getBody(), is("Your access code is " + accessCode2));
	}
	
	@Test
	public void shouldNotSendAccessCodeWithoutUser()
	{
		thrown.expectMessage("User is required to send an access code");
		service.generateAndSendAccessCode(null, userSecurity);
	}
	
	@Test
	public void shouldNotSendAccessCodeWithoutSecurity()
	{
		thrown.expectMessage("Security info is required to send an access code for");
		service.generateAndSendAccessCode(userVendor, null);
	}
	
	@Test
	public void shouldNotSendAccessCodeWithMismatchedSecurity()
	{
		User vendorUser2 = CoreTestUtil.createUser(98767, "test@testvendor.com", "Test", "Other Vendor", "test@testvendor.com", UserType.VENDOR);
		UserSecurity sec = new UserSecurity(vendorUser2, null);

		thrown.expectMessage("User and security info must match when sending an access code for");
		service.generateAndSendAccessCode(userPenske, sec);
	}
	
	@Test
	public void shouldSendNewUserEmail_NewLDAPUser()
	{
		when(emailDAO.getEmailTemplate(EmailTemplateType.NEW_VENDOR_USER)).thenReturn(newVendorTemplate);
		
		service.sendNewUserEmail(userPenske, userVendor, userSecurity, true, lookups, commonStaticUrl);
		
		verify(emailDAO).insertSmcEmail(smcEmailCaptor.capture());
		
		SmcEmail email = smcEmailCaptor.getValue();
		
		assertThat(email.getSso(), is(userPenske.getSso()));
		assertThat(email.getToAddress(), is(userVendor.getEmailAddress()));
		assertThat(email.getBody(), is("Your temporary password is temppw"));
	}
	
	@Test
	public void shouldSendNewUserEmail_ExistingLDAPUser()
	{
		when(emailDAO.getEmailTemplate(EmailTemplateType.EXISTING_VENDOR_USER)).thenReturn(existingVendorTemplate);
		
		//Should not have a one-time password if they're an existing SSO user
		userSecurity.clearOneTimePassword();
		
		service.sendNewUserEmail(userPenske, userVendor, userSecurity, false, lookups, commonStaticUrl);
		
		verify(emailDAO).insertSmcEmail(smcEmailCaptor.capture());
		
		SmcEmail email = smcEmailCaptor.getValue();
		
		assertThat(email.getSso(), is(userPenske.getSso()));
		assertThat(email.getToAddress(), is(userVendor.getEmailAddress()));
		assertThat(email.getBody(), is("You already have an SSO account"));
	}
	
	@Test
	public void shouldNotSendNewUserEmailWithoutCurrentUser()
	{
		thrown.expectMessage("Current user is required to send a welcome email");
		service.sendNewUserEmail(null, userVendor, userSecurity, false, lookups, commonStaticUrl);
	}
	
	@Test
	public void shouldNotSendNewUserEmailWithoutTargetUser()
	{
		thrown.expectMessage("Target user is required to send a welcome email");
		service.sendNewUserEmail(userPenske, null, userSecurity, false, lookups, commonStaticUrl);
	}
	
	@Test
	public void shouldNotSendNewUserEmailWithoutSecurity()
	{
		thrown.expectMessage("User security record is required to send a welcome email");
		service.sendNewUserEmail(userPenske, userVendor, null, false, lookups, commonStaticUrl);
	}
	
	@Test
	public void shouldNotSendNewUserEmailMismatchedSecurity()
	{
		User vendorUser2 = CoreTestUtil.createUser(98767, "test@testvendor.com", "Test", "Other Vendor", "test@testvendor.com", UserType.VENDOR);
		UserSecurity sec = new UserSecurity(vendorUser2, null);
		
		thrown.expectMessage("User security record does not match the target user when sending a welcome email");
		service.sendNewUserEmail(userPenske, userVendor, sec, false, lookups, commonStaticUrl);
	}
}