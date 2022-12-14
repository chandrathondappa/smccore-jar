package com.penske.apps.smccore.base.domain.enums;

/**
 * The different statuses an order can go through between the time it is created and the time it is confirmed by the vendor.
 * Corresponds to the status in the SMC_PURCHASING_DETAILS table.
 */
public enum PoStatus implements MappedEnum {
	
	/** The PO has been issued and confirmed in SMC by the vendor. */
	CONFIRM,
	/** The PO has been issued, but has not been confirmed by the vendor yet. */
	ISSUED,
	/** There has been a request to generate the given PO and is waiting to be generated by the PDF batch. */
	REQUEST;
	
	/**
	 * Looks up the PO status by the name of the enum constant.
	 * This works similar to {@link #valueOf(String)}, except it returns null
	 * instead of throwing an exception if no match is found.
	 * @param name The name of the enum constant to search for.
	 * @return The enum constant with the given name, or null if no matching constant is found.
	 */
	public static PoStatus findByName(String name)
	{
		for(PoStatus source : values())
			if(source.name().equals(name)) return source;

		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return name();
	}

}
