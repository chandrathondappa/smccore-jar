/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.annotation.NonVendorQuery;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * DAO containing queries related to users and logging in
 */
public interface UserDAO
{
	@NonVendorQuery
	public List<User> getUsers(@Param("sso") String sso, @Param("userType") UserType userType, @Param("userDepartment") UserDepartment userDepartment,
		@Param("loadSecurityFunctions") boolean loadSecurityFunctions, @Param("loadVendors") boolean loadVendors);
	
	@NonVendorQuery
	public List<String> getExistingBuddiesList(@Param("sso") String sso);

	@NonVendorQuery
    public BuddySelectionType getSelectionType(@Param("sso") String sso);

	@NonVendorQuery
    public List<String> getExistingBuddiesListFromUserMaster(@Param("selectionType") BuddySelectionType selectionType, @Param("sso") String sso);

	public List<Integer> getVendorIdsFromVendorFilter(@Param("user") User user);

	public List<Integer> getVendorIdsFromBuddies(@Param("ssoList") List<String> ssoList);
}