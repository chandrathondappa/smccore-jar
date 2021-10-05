/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import com.penske.apps.smccore.base.domain.enums.CorpCode;

/**
 * A read-only view of a vendor in SMC. Not intended for editing or creating vendors.
 */
public class SmcVendorView
{
	/** Database ID in SMC of the vendor */
	private int vendorId;
	/** What company code this vendor record apples to */
	private CorpCode corp;
	/** The vendor's identifying number within systems outside SMC. Must be combined with corp to be unique. */
	private int vendorNumber;
	/** The human-readable name of the vendor */
	private String vendorName;

	/** Null constructor - MyBatis only */
	protected SmcVendorView() {}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{SmcVendorView (ID: " + vendorId + ") - Vendor #: " + vendorNumber + ", Corp: " + corp + ", Name: " + vendorName + "}";
	}
	
	//***** DEFAULT ACCESSORS *****//

	public int getVendorId()
	{
		return vendorId;
	}

	public CorpCode getCorp()
	{
		return corp;
	}

	public int getVendorNumber()
	{
		return vendorNumber;
	}

	public String getVendorName()
	{
		return vendorName;
	}
}