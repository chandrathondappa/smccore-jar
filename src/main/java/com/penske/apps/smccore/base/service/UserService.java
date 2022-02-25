/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.penske.apps.smccore.base.dao.UserDAO;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.business.ldap.sso.CPBGESSOUser;
import com.penske.util.security.priv.CPTSso;

/**
 * Service related to looking up users, security functions, and buddies.
 */
@Service
public class UserService
{
	private static final Logger logger = LogManager.getLogger(UserService.class);
	
	private final UserDAO userDAO;
	
	@Autowired
	public UserService(UserDAO userDAO)
	{
		this.userDAO = userDAO;
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
		
		List<User> users = userDAO.getUsers(sso, null, null, loadSecurityFunctions, loadAssociatedVendors);
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
    	return userDAO.getUsers(null, userType, userDepartment, false, false);
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