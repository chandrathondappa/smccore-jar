/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.domain.enums.SecurityFunction;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * A user of the system 
 */
public class User implements Serializable
{
	private static final long serialVersionUID = -7571478025581844162L;
	
	/** Internal system-generated user ID */
	private int userId;
	/** The user's SSO ID */
	private String sso;
	/** The user's first name */
	private String firstName;
	/** The user's last name */
	private String lastName;
	/** The user's email address */
	private String emailAddress;
	/** The user's phone number */
	private String phone;
	/** The type of user this person is - broadly determines what they can see */
	private UserType userType;
	/** The department within Vehicle Supply that the user belongs to, or null if they aren't a Penske employee. */
	private UserDepartment userDepartment;
	
	/** The organization the user belongs to */
	private int orgId;
	/** The human-readable name of the org the user belongs to */
	private String orgName;
	/** The user's role - this determines their security functions */
	private int roleId;

	/** All the user's security functions, regardless of tab */
	private Set<SecurityFunction> securityFunctions;
	
	/** All the vendors the user has permission to see data for. Based on the user's org. */
	private Set<Integer> associatedVendorIds;
	
	/** Null constructor - MyBatis only */
	protected User() {}
	
	@Override
    public String toString() 
    {
        return "{User (" + sso + ") - Name: " + firstName + " " + lastName + ", Type: " + userType + ", Dept: " + userDepartment + "}";
    }
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Format the user's first and last names together with a space.
	 * @return The user's full name.
	 */
	public String getFullName()
	{
		if(StringUtils.isBlank(firstName))
			return lastName;
		if(StringUtils.isBlank(lastName))
			return firstName;
		return firstName + " " + lastName;
	}
	
	/**
	 * Gets the user's full name, but only truncated to 14 characters.
	 * @return The first 14 characters of the user's full name
	 */
	public String getTruncatedFullName()
    {
        if(StringUtils.isBlank(firstName))
            return StringUtils.substring(lastName, 0, 14);
        if(StringUtils.isBlank(lastName))
            return StringUtils.substring(firstName, 0, 14);
        
        return StringUtils.substring(firstName + " " + lastName, 0, 14);
    }

	/**
	 * Checks if this user is an employee of one of Penske's vendors
	 * @return True if this user is a vendor user; false if they are some other type of user
	 */
	public boolean isVendorUser()
	{
		return userType == UserType.VENDOR;
	}
	
	/**
	 * Checks if this user is a Penske employee or not.
	 * @return True if this user is a penske user; false if they are some other type of user
	 */
	public boolean isPenskeUser()
	{
		return userType == UserType.PENSKE;
	}
	
	/**
	 * Gets the list of all the security functions the user is allowed to access
	 * @return All the user's security functions, regardless of tab
	 */
	public Set<SecurityFunction> getSecurityFunctions()
	{
		if(this.securityFunctions == null)
			throw new IllegalStateException("Can not access security functions for user " + sso + ". The user was not loaded from the database with security functions.");
		
		return Collections.unmodifiableSet(securityFunctions);
	}

	/**
	 * Checks if the user has a given security permission or not
	 * @param secFunction The permission to check for
	 * @return True if the user has the permission; false if they do not.
	 */
	public boolean hasSecurityFunction(SecurityFunction secFunction)
	{
		return getSecurityFunctions().contains(secFunction);
	}
	
	/**
	 * Gets the SMC IDs of all the vendors the user should be allowed to see data for (if they are a vendor user - Penske users can see data for all vendors)
	 * @return The IDs of the user's vendors they have permission to
	 */
	public Set<Integer> getAssociatedVendorIds()
	{
		if(this.associatedVendorIds == null)
			throw new IllegalStateException("Can not access associated vendors for user " + sso + ". The user was not loaded from the database with associated vendors.");
		return Collections.unmodifiableSet(associatedVendorIds);
	}
	
	/**
	 * Package-private setter for MyBatis to use to set security functions as strings, so the class turns them into enum constants
	 */
	void setSecurityFunctionViews(List<SecurityFunctionView> secFunctionViews)
	{
		if(secFunctionViews == null)
			secFunctionViews = Collections.emptyList();
		
		//Extract the enum constant and tab name from the objects, and build a set from them
		Set<SecurityFunction> allSecFunctions = secFunctionViews.stream()
			.map(SecurityFunctionView::getSecurityFunction)
			.filter(Objects::nonNull)
			.collect(toSet());
		
		this.securityFunctions = allSecFunctions;
	}

	/**
	 * Package-private setter for MyBatis to use to set associated vendors, so that the vendor object doesn't need to be stored in the user.
	 */
	void setVendorViews(List<SmcVendorView> vendorViews)
	{
		if(vendorViews == null)
			vendorViews = Collections.emptyList();
		
		Set<Integer> userVendorIds = new HashSet<>();
		Set<Integer> userVendorNumbers = new HashSet<>();
		for(SmcVendorView vend : vendorViews)
		{
			userVendorIds.add(vend.getVendorId());
			userVendorNumbers.add(vend.getVendorNumber());
		}
		
		this.associatedVendorIds = userVendorIds;
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the userId
	 */
	public int getUserId()
	{
		return userId;
	}

	/**
	 * @return the sso
	 */
	public String getSso()
	{
		return sso;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName()
	{
		return lastName;
	}
	
	/**
	 * @return the email address
	 */
	public String getEmailAddress()
	{
		return emailAddress;
	}

	public UserType getUserType()
	{
		return userType;
	}

	public String getPhone()
	{
		return phone;
	}

	public UserDepartment getUserDepartment()
	{
		return userDepartment;
	}

	public int getOrgId()
	{
		return orgId;
	}

	public int getRoleId()
	{
		return roleId;
	}

	public String getOrgName()
	{
		return orgName;
	}
}