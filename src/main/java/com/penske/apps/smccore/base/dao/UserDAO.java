/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.annotation.NonVendorQuery;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.UserLogin;
import com.penske.apps.smccore.base.domain.UserSecurity;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * DAO containing queries related to users and logging in
 */
public interface UserDAO
{
	//***** Users *****//
	@NonVendorQuery
	public List<User> getUsers(@Param("sso") String sso, @Param("userId") Integer userId, @Param("userType") UserType userType, @Param("userDepartment") UserDepartment userDepartment,
		@Param("loadSecurityFunctions") boolean loadSecurityFunctions, @Param("loadVendors") boolean loadVendors);

	@NonVendorQuery
	public UserSecurity getUserSecurity(@Param("user") User user);
	
	@NonVendorQuery
	public List<UserLogin> getUserLogins(@Param("user") User user);
	
	@NonVendorQuery
	public void insertUserSecurity(@Param("sec") UserSecurity userSecurity);
	
	@NonVendorQuery
	public void updateUserSecurity(@Param("sec") UserSecurity userSecurity);
	
	@NonVendorQuery
	public void recordUserLogin(@Param("loginId") Integer loginId, @Param("user") User user, @Param("serverLocation") String serverLocation);
	
	//***** Buddies for Users *****//
	@NonVendorQuery
	public List<String> getExistingBuddiesList(@Param("sso") String sso);

	@NonVendorQuery
    public BuddySelectionType getSelectionType(@Param("sso") String sso);

	@NonVendorQuery
    public List<String> getExistingBuddiesListFromUserMaster(@Param("selectionType") BuddySelectionType selectionType, @Param("sso") String sso);

	//***** Vendor IDs for Users *****//
	public List<Integer> getVendorIdsFromVendorFilter(@Param("user") User user);

	public List<Integer> getVendorIdsFromBuddies(@Param("ssoList") List<String> ssoList);
}