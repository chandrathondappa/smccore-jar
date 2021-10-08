/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * Differentiates between different roles within Penske's Vehicle Supply team
 */
public enum UserDepartment implements MappedEnum
{
	/** Penske user responsible for preparing and issuing purchase orders */
	SUPPLY_SPECIALIST(1, "SUPPLY SPECIALIST"),
	/** Penske user responsible for tracking orders through the production and delivery process */
	PLANNING_ANALYST(2, "PLANNING ANALYST"),
	;
	
	/** Internal Database ID */
	private final int departmentId;
	/** A human-readable name for the type of user that fills the given role */
	private final String departmentUserTitle;
	
	private UserDepartment(int departmentId, String departmentUserTitle)
	{
		this.departmentId = departmentId;
		this.departmentUserTitle = departmentUserTitle;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return String.valueOf(departmentId);
	}

	public int getDepartmentId()
	{
		return departmentId;
	}

	public String getDepartmentUserTitle()
	{
		return departmentUserTitle;
	}
}