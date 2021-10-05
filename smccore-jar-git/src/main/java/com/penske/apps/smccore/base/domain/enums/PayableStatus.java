package com.penske.apps.smccore.base.domain.enums;

/**
 * The status of a given unit on a given PO from the A/P (Accounts Payable) side of the house.
 * Used to determine whether a deliverable has been effectively closed and paid for.
 */
public enum PayableStatus implements MappedEnum
{
	/** The PO has been issued, but has not been confirmed by the vendor yet. */
	ISSUED,
	/** The PO has been confirmed in SMC by the vendor, but not paid for yet. */
	CONFIRM,
	/** The unit was canceled off the PO or the entire PO was canceled. Treat this deliverable as though it never existed. */
	VOID,
	/**
	 * The deliverable has been paid for and is completed from the A/P perspective.
	 * Deliverables in this status do not have their unit templates updated, nor does the results of running rules or resolving conflicts for them get saved on their template.
	 */
	CLOSED;

	/**
	 * Looks up the payable status by the name of the enum constant.
	 * This works similar to {@link #valueOf(String)}, except it returns null
	 * instead of throwing an exception if no match is found.
	 * @param name The name of the enum constant to search for.
	 * @return The enum constant with the given name, or null if no matching constant is found.
	 */
	public static PayableStatus findByName(String name)
	{
		for(PayableStatus source : values())
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