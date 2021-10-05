package com.penske.apps.smccore.base.domain.enums;

public enum BuddySelectionType implements MappedEnum {
	// 
	ALL_BUYERS("ALLBUYERS"),
	ALL_PLANNING("ALLPLANNING"),
	ALL("ALL");
	
	/** The actual code that is printed in the database for this selection type. */
	private final String code;
	
	/**
	 * Look up a selection type by it's DB code.
	 * @param code The DB code to look up.
	 * @return The enum constant with matching DB code, or null if there is no such matching enum constant.
	 */
	public static BuddySelectionType findByCode(String code)
	{
		for(BuddySelectionType selectionType : values())
		{
			if(selectionType.code.equals(code))
				return selectionType;
		}
		
		return null;
	}
	
	private BuddySelectionType(String code)
	{
		this.code = code;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return getCode();
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

}
