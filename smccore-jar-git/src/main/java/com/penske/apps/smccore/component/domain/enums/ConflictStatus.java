/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;


/**
 * Enum to indicate whether a component is in conflict with another component. If the conflict has been resolved, this indicates what the resolution was.
 * A null value for a field of this type indicates the component is not in conflict at all.
 */
//FIXME: test
public enum ConflictStatus implements MappedEnum
{
	/** Component is in conflict, and conflict has not been resolved yet */
	CONFLICT("C", "Unresolved"),
	/** Component is in conflict, and the conflict has been resolved via a global conflict resolution */
	GLOBAL_RESOLUTION("G", "Global Resolution"),
	/** Component is in conflict, and the conflict has been resolved via an individual resolution (this unit component is the required one) */
	INDIVIDUAL_RESOLUTION_REQUIRED("IR", "Individual Resolution - Required"),
	/** Component is in conflict, and the conflict has been resolved via an individual resolution (this unit component is not required, and is just visible instead) */
	INDIVIDUAL_RESOLUTION_VISIBLE("IV", "Individual Resolution - Visible"),
	;

	/** The one- or two-letter code used to represent this conflict status in the database. */
	private final String code;
	/** A human-readable description of this conflict status */
	private final String description;
	
	private ConflictStatus(String code, String description)
	{
		this.code = code;
		this.description = description;
	}
	
	/**
	 * Looks up a conflict status enum by its code in the database.
	 * @param code The database value to parse.
	 * @return The status with a matching code, or null if there is no matching status.
	 */
	public static ConflictStatus findByCode(String code)
	{
		for(ConflictStatus status : values())
		{
			if(status.getCode().equals(code))
				return status;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return code;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getCode()
	{
		return code;
	}

	public String getDescription()
	{
		return description;
	}
}
