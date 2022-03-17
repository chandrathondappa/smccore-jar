/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.service;

import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.base.dao.EmailDAO;
import com.penske.apps.smccore.base.dao.UserDAO;
import com.penske.apps.smccore.base.domain.EmailTemplate;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.SmcEmail;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.UserLogin;
import com.penske.apps.smccore.base.domain.UserSecurity;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;
import com.penske.apps.smccore.base.domain.enums.LookupKey;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.base.exception.BusinessRuleException;
import com.penske.business.ldap.sso.CPBGESSOUser;
import com.penske.util.security.priv.CPTSso;

/**
 * Service related to looking up users, security functions, and buddies.
 */
@Service
public class UserService
{
	private static final Logger logger = LogManager.getLogger(UserService.class);
	private static final SecureRandom randomGenerator = new SecureRandom();
	
	private final UserDAO userDAO;
	private final EmailDAO emailDAO;
	
	@Autowired
	public UserService(UserDAO userDAO, EmailDAO emailDAO)
	{
		this.userDAO = userDAO;
		this.emailDAO = emailDAO;
	}

	/**
	 * Gets a single user, equipped with all their security functions and associated vendors
	 * @param sso The SSO of the user to retrieve
	 * @param loadAssociatedVendors True to load the user's associated vendor IDs. False to skip loading those.
	 * @return The user, loaded with all of their security functions and associated vendors
	 */
	public User getUser(String sso, boolean loadAssociatedVendors) {
    	if(StringUtils.isBlank(sso))
    		return null;
    	
    	return getUser(sso, loadAssociatedVendors, true);
    }
	
	/**
	 * Gets basic information about a single user.
	 * @param sso The SSO of the user to look up
	 * @param loadAssociatedVendors True to load the user's associated vendor IDs. False to skip loading those. 
	 * @param loadSecurityFunctions True to load the user's security functions. False to skip loading those.
	 * @return The user, optionally loaded with vendors and security functions
	 */
	public User getUser(String sso, boolean loadAssociatedVendors, boolean loadSecurityFunctions)
	{
		if(StringUtils.isBlank(sso))
			return null;
		
		List<User> users = userDAO.getUsers(sso, null, null, null, loadSecurityFunctions, loadAssociatedVendors);
		if(users == null || users.isEmpty())
			return null;
		else
			return users.get(0);
	}

	/**
	 * Gets basic informationa bout a single user by their ID
	 * @param userId The row ID of the user to look up 
	 * @param loadAssociatedVendors True to load the user's associated vendor IDs. False to skip loading those. 
	 * @param loadSecurityFunctions True to load the user's security functions. False to skip loading those.
	 * @return The user, optionally loaded with vendors and security functions
	 */
	public User getUser(int userId, boolean loadAssociatedVendors, boolean loadSecurityFunctions)
	{
		List<User> users = userDAO.getUsers(null, userId, null, null, loadSecurityFunctions, loadAssociatedVendors);
		if(users == null || users.isEmpty())
			return null;
		else
			return users.get(0);
	}
	
    /**
     * Gets all users matching a given user type and department.
     * Does not look up security functions for those users, so calling {@link User#getSecurityFunctions()} on the returned users will throw an exception.
     * @param userType The type of user to look up
     * @param userDepartment The department within penske to look up users for.
     * @return The users of the given type and department
     */
    public List<User> getUsersWithoutSecurityFunctions(UserType userType, UserDepartment userDepartment)
    {
    	return userDAO.getUsers(null, null, userType, userDepartment, false, false);
    }

    /**
	 * Looks up a user from the central Penske user store (not the same as the SMC user master table), based on their SSO.
	 * @param userSso The sso ID of the user to look for.
	 * @return The user, if one with that SSO exists. Null if no match was found
	 */
	public CPBGESSOUser getUserFromUserStore(String userSso)
	{
		if(StringUtils.isBlank(userSso))
			return null;
		
		logger.info("Looking up user with SSO: " + userSso);
		
		CPTSso oSSO = new CPTSso();
        CPBGESSOUser result = oSSO.findUser(userSso);
        
        if(result == null)
        	logger.info("User is not available in LDAP");
        else
        	logger.info("User " + userSso + " found. Email: " + result.getEmailAddress());

        return result;
	}

	/**
	 * Gets a user security object for the given user
	 * @param user The user for whom to get security information
	 * @return The user security
	 */
	public UserSecurity getUserSecurity(User user)
	{
		return userDAO.getUserSecurity(user);
	}

	/**
	 * Creates a new security record for a newly-created user of the SMC system.
	 * @param currentUser The user who is doing the action of adding a new user
	 * @param createdUser The user that got added
	 * @param oneTimePassword Optional. If the {@code createdUser} just got an SSO account created for them as part of this process, then this is their
	 * 	temporary password to set up their SSO account. If the user was already in our LDAP SSO system, then this should be null, since they don't have a temporary password anymore.
	 * @param sendNewUserEmail True to send the user a "welcome to SMC" email. False to omit sending this email for now.
	 * @param lookups A lookup container that references SMC_LOOKUP
	 * @param commonStaticUrl The URL that hosts static content for Penske
	 */
	@Transactional
	public void insertUserSecurity(User currentUser, User createdUser, String oneTimePassword, boolean sendNewUserEmail, LookupContainer lookups, URL commonStaticUrl)
	{
		boolean isNewSsoAccount = StringUtils.isNotBlank(oneTimePassword);
		
		UserSecurity sec = new UserSecurity(createdUser, oneTimePassword);
		
		userDAO.insertUserSecurity(sec, currentUser);
		
		if(sendNewUserEmail)
			this.sendNewUserEmail(currentUser, createdUser, sec, isNewSsoAccount, lookups, commonStaticUrl);
	}
	
	/**
	 * Records the fact that a user logged into the system and successfully provided their daily access code (if applicable)
	 * @param user The user who logged in
	 * @param userSecurity The user's security record
	 * @param serverLocation The server name that the user logged into
	 * @return The most recent time that the user logged in before this time
	 */
	@Transactional
	public LocalDateTime recordUserLogin(User user, UserSecurity userSecurity, String serverLocation)
	{
		if(user == null)
			throw new IllegalArgumentException("User is required to record a login for them");
		if(userSecurity == null && user.isVendorUser())
			throw new IllegalArgumentException("User security info is required to record a login for a vendor user (SSO: " + user.getSso() + ")");
		
		//Vendor users have user security objects, but Penske users do not, so this may be null
		if(userSecurity != null)
		{
			if(user.getUserId() != userSecurity.getUserId())
				throw new BusinessRuleException("Can not record vendor user login for SSO " + user.getSso() + ". User security info was for a different user.");
			
			//This service method assumes the user has successfully passed whatever authentication checks they need to,
			// so we can record that they no longer have to pass an access code check
			userSecurity.clearAccessCode();
			
			//Also, if the user has logged in successfully, then they must have already set up their account
			// and provided their new password, so they don't need the one-time password anymore
			userSecurity.clearOneTimePassword();
		}
		
		//Only keep the last 30 logins. If we have more than 30, then pick the oldest one and update it
		List<UserLogin> previousLogins = userDAO.getUserLogins(user);
		UserLogin loginToUpdate;
		if(previousLogins.size() > 29)
			loginToUpdate = previousLogins.get(previousLogins.size() - 1);
		else
			loginToUpdate = null;
		
		//Actually do the database writes
		userDAO.recordUserLogin(loginToUpdate == null ? null : loginToUpdate.getLoginId(), user, serverLocation);
		if(userSecurity != null)
			userDAO.updateUserSecurity(userSecurity, user);
		
		//Return most recent login time before the current one
		if(previousLogins.isEmpty())
			return null;
		else
			return previousLogins.get(0).getLoginTime();
	}
	
	/**
	 * Records the fact that a user successfully provided their daily access code (if applicable)
	 * @param user The user who logged in
	 * @param userSecurity The user's security record
	 */
	@Transactional
	public void recordTwoFactorAuthSuccess(User user, UserSecurity userSecurity) {
		if(userSecurity == null && user.isVendorUser())
			throw new IllegalArgumentException("User security info is required to record a sucessful access code authentication for a vendor user (SSO: " + user.getSso() + ")");
		
		userSecurity.clearAccessCode();
		userDAO.updateUserSecurity(userSecurity, user);
	}
	
	/**
	 * Generates a new access code for the given user and sends them an email with the access code in it.
	 * @param user The user to generate an access code for
	 * @param userSecurity The user's security record
	 */
	@Transactional
	public void generateAndSendAccessCode(User user, UserSecurity userSecurity)
	{
		if(user == null)
			throw new IllegalArgumentException("User is required to send an access code");
		if(userSecurity == null)
			throw new IllegalArgumentException("Security info is required to send an access code for " + user.getSso());
		if(user.getUserId() != userSecurity.getUserId())
			throw new IllegalArgumentException("User and security info must match when sending an access code for " + user.getSso());
		
		//Get a six-digit non-negative integer
		int accessCodeInt = randomGenerator.nextInt(1000000);
		String accessCode = StringUtils.leftPad(String.valueOf(accessCodeInt), 6, "0");
		
		//Update the access code in the security object
		userSecurity.setNewAccessCode(accessCode);
		
		List<Pair<String, String>> replacements = Arrays.asList(
			Pair.of("[ACCESS_CODE]", accessCode),
			Pair.of("[VALID_MINUTES]", String.valueOf(UserSecurity.ACCESS_CODE_EXPIRATION_MINUTES))
		);
		
		//create email to send new code to the user
		EmailTemplate template = emailDAO.getEmailTemplate(EmailTemplateType.ACCESS_CODE);
		String subject = template.getActualSubject(null);
		String body = template.getActualBody(replacements);
		
		SmcEmail email = new SmcEmail(template.getEmailType(), user.getSso(), user.getEmailAddress(), null, null, body, subject);
		
		userDAO.updateUserSecurity(userSecurity, user);
		emailDAO.insertSmcEmail(email);
	}
	
	/**
	 * Sends a "welcome to SMC" email to the given user.
	 * @param currentUser The user who is doing the action of adding a new user
	 * @param createdUser The user to whom the welcome email should be sent
	 * @param createdUserSecurity The security record for the {@code createdUser}
	 * @param newSsoAccount True if the user is new to the Penske SSO system and needs to setup their account still. False if they were already in the SSO system and don't need to set up their account.
	 * @param lookups A lookup container that references SMC_LOOKUP
	 * @param commonStaticUrl The URL that hosts static content for Penske
	 */
	@Transactional
	public void sendNewUserEmail(User currentUser, User createdUser, UserSecurity createdUserSecurity, boolean newSsoAccount, LookupContainer lookups, URL commonStaticUrl)
	{
		if(currentUser == null)
			throw new IllegalArgumentException("Current user is required to send a welcome email");
		if(createdUser == null)
			throw new IllegalArgumentException("Target user is required to send a welcome email");
		if(createdUserSecurity == null)
			throw new IllegalArgumentException("User security record is required to send a welcome email to SSO " + createdUser.getSso());
		if(createdUser.getUserId() != createdUserSecurity.getUserId())
			throw new IllegalArgumentException("User security record does not match the target user when sending a welcome email to SSO " + createdUser.getSso());
		
		EmailTemplate template;
		String subject;
		String body;
		
		if(newSsoAccount)
		{
			if(StringUtils.isBlank(createdUserSecurity.getOneTimePassword()))
				throw new IllegalArgumentException("Cannot find OTP for user. SSO: " + createdUser.getSso());
			
			String smcURL = lookups.getSingleLookupValue(LookupKey.SMC_APP_LINK);
			
			List<Pair<String, String>> replacements = Arrays.asList(
				Pair.of("[SSO_ID]", createdUser.getSso()),
				Pair.of("[OTP]", createdUserSecurity.getOneTimePassword()),
				Pair.of("[PENSKE_SIGN_ON_URL]", lookups.getSingleLookupValue(LookupKey.PENSKE_SIGN_ON_URL)),
				Pair.of("[SMC_APP_LINK_HREF]", smcURL),
				Pair.of("[SMC_APP_LINK]", smcURL),
				Pair.of("[CUSTOMER_SERVICE_PHONE_NUM]", lookups.getSingleLookupValue(LookupKey.CUSTOMER_SERVICE_PHONE_NUM)),
				Pair.of("[COMMON_STATIC_URL]", commonStaticUrl.toString())
			);
			
			// New User
			template = emailDAO.getEmailTemplate(EmailTemplateType.NEW_VENDOR_USER);
			subject = template.getSubjectTemplate();
			body = template.getActualBody(replacements);
		}
		else
		{
			// Existing User
			List<Pair<String, String>> replacements = Arrays.asList(
				Pair.of("[USER_NAME]", createdUser.getFullName()),
				Pair.of("[SMC_APP_LINK]", lookups.getSingleLookupValue(LookupKey.SMC_APP_LINK)),
				Pair.of("[CUSTOMER_SERVICE_PHONE_NUM]", lookups.getSingleLookupValue(LookupKey.CUSTOMER_SERVICE_PHONE_NUM)),
				Pair.of("[CUSTOMER_SERVICE_EMAIL]", lookups.getSingleLookupValue(LookupKey.CUSTOMER_SERVICE_EMAIL)),
				Pair.of("[IT_SERVICE_PHONE_NUM]", lookups.getSingleLookupValue(LookupKey.IT_SERVICE_PHONE_NUM)),
				Pair.of("[IT_SERVICE_EMAIL]", lookups.getSingleLookupValue(LookupKey.IT_SERVICE_EMAIL))
			);
			
			template = emailDAO.getEmailTemplate(EmailTemplateType.EXISTING_VENDOR_USER);
			subject = template.getActualSubject(replacements);
			body = template.getActualBody(replacements);
		}

		//Mark that the user actually got a welcome email sent to them
		createdUserSecurity.markNewUserEmailSent();
		
		SmcEmail email = new SmcEmail(template.getEmailType(), currentUser.getSso(), createdUser.getEmailAddress(), null, null, body, subject);
		
		userDAO.updateUserSecurity(createdUserSecurity, currentUser);
		emailDAO.insertSmcEmail(email);
	}
	
	/**
	 * Gets the buddies list for a user from SMC_BUDDIES. If they have a buddies selection type selected, 
	 * it will search the user master table to find all users that match that selection type
	 * @param userSso The sso ID of the user
	 * @return The list of ssos the user is buddies with
	 */
    public List<String> getExistingBuddiesList(String userSSO){

        BuddySelectionType selectionType = getSelectionType(userSSO);
        List<String> existingBuddiesList = new ArrayList<>();

        if(selectionType != null){
            existingBuddiesList= userDAO.getExistingBuddiesListFromUserMaster(selectionType, userSSO);
        }
        existingBuddiesList.addAll(userDAO.getExistingBuddiesList(userSSO));


        return existingBuddiesList;
    }
	
   /**
	 * Gets the buddy selection type for a user (if it exists)
	 * @param userSso The sso ID of the user
	 * @return The selection type, if one for that SSO exists. Null if no match was found
	 */ 
   public BuddySelectionType getSelectionType(String userSSO) {
	   return userDAO.getSelectionType(userSSO);
   }

   public List<Integer> getVendorIdsFromVendorFilter(User user) {
	   return userDAO.getVendorIdsFromVendorFilter(user);
   }

   public List<Integer> getVendorIdsFromBuddies(List<String> existingBuddiesList) {
	   return userDAO.getVendorIdsFromBuddies(existingBuddiesList);
   }


}