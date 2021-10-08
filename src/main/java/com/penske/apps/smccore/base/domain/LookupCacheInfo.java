/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import java.util.Date;

/**
 * Container to hold information about the last time the various tables in the LookupContainer were modified, and the record counts in those tables
 */
public class LookupCacheInfo
{
	/** The last time the lookup table was modified */
	private Date lookupModified;
	/** The last time the doctype table was modified */
	private Date doctypeMasterModified;
	/** The current count of records in the lookup table */
	private int lookupCount;
	/** The current count of records in the doctype table */
	private int doctypeCount;
	
	/** Null constructor - MyBatis only */
	protected LookupCacheInfo() {}
	
	//***** MODIFIED ACCESSORS *****//
	public boolean isOlderThan(LookupCacheInfo other)
	{
		if(other == null)
			return false;
		
		if(lookupModified.before(other.getLookupModified()))
			return true;
		if(doctypeMasterModified.before(other.getDoctypeMasterModified()))
			return true;
		if(lookupCount != other.getLookupCount())
			return true;
		if(doctypeCount != other.getDoctypeCount())
			return true;
		
		return false;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Date getLookupModified()
	{
		return lookupModified;
	}
	public Date getDoctypeMasterModified()
	{
		return doctypeMasterModified;
	}
	public int getDoctypeCount()
	{
		return doctypeCount;
	}

	public int getLookupCount()
	{
		return lookupCount;
	}
}