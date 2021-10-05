/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.domain.VehicleIdentifier;
import com.penske.apps.smccore.base.domain.enums.CorpCode;
import com.penske.apps.smccore.base.domain.enums.PayableStatus;

/**
 * Provides header and summary information about the given PO / Unit combination. Used by the component visibility rules engine.
 * This class does not have any public construtors, since it is just intended to summarize existing unit master information,
 * 	not allow creation of new unit masters. The AS400 PO generation process is responsible for creating unit master records.
 */
//FIXME: test
public class UnitMasterInfo implements VehicleIdentifier
{
	/**
	 * The master ID of the given unit / PO combination.
	 * 	This ID is a Penske internal number that uniquely identifies the combination of Unit number and PO number.
	 */
	private int masterId;
	/** The PO number of this unit master. */
	private int poNumber;
	/** The unit number (left-padded with spaces to 10 characters) for this unit master. */
	private String unitNumber;
	/** The corp code that the unit master represents. This may not always be the primary unit on paried units, depending on how the paring was done. */
	private CorpCode corpCode;
	/**
	 * The ID of the vendor this PO was issued to.
	 * 	This is a Penske internal number uniquely identifying the vendor.
	 * 	It is different from the vendor number, which is a human-readable number assigned to each vendor.
	 */
	private int vendorId;
	
	/**
	 * The ID of the PO Category / Subcategory association that this PO was issued against.
	 * 	These associations are part of a many-to-many relationship between categories and subcategories.
	 */
	private int poCategoryAssociationId;
	/**
	 * A signature identifying which kinds of POs have been issued for this unit. 
	 * This version of the signature is the one generated the last time the rules were run for this unit.
	 */
	private String previousUnitSignature;
	/**
	 * A signature identifying which kinds of POs have been issued for this unit.
	 * This version of the signature is the one generated in this run of the rules
	 */
	private String calculatedUnitSignature;
	/** The template ID associated with this unit / PO combination. */
	private int templateId;
	/**
	 * The template hash from the last time this unit master's template components were regenerated.
	 * 	This is used to determine if the template component data is up to date.
	 */
	private String previousTemplateHash;
	/** The up-to-date template hash from the current template master. */
	private String masterTemplateHash;
	/** The last date the rules engine was run and allowed to update data in SMC_UNIT_COMPONENT for this unit master ID */
	private Date lastTemplateRefresh;
	
	/** The status of the unit master with respect to the A/P system. */
	private PayableStatus payableStatus;
	/** The date the unit master was actually delivered to its destination. Null if it hasn't yet been delivered. */
	private Date actualDeliveryDate;
	/** The number of components in this unit master that were missing the last time the rules were run, based on its unit component data. */
	private int missingInfoCount;
	
	/** Null constructor - MyBatis only */
	protected UnitMasterInfo() {}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{UnitMaster " + masterId + " (PO " + poNumber + ", Unit " + unitNumber + "), Template " + templateId + "}";
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * Checks if this unit master record should have its template rebuilt or updated if and when the rules engine runs.
	 *	If a unit master has already been paid for, or if it is delivered an no longer has any missing information, we stop updating the unit template information.
	 * @return True if the unit template information should continue to be updated when rules run. False if it should not.
	 */
	public boolean isUnitComponentUpdateAllowed()
	{
		///We don't rebuild templates or save rule results for UnitMasters that have already been delivered and have no missing information. They are effectively closed.
		if(actualDeliveryDate != null && missingInfoCount == 0)
			return false;
		else
			return true;
	}
	
	//FIXME: document
	public boolean isTemplateOutOfDate()
	{
		//We don't rebuild templates for UnitMasters that are already closed, but we do save rule results for them, because additional missing information may be provided even after they're closed.
		if(payableStatus == PayableStatus.CLOSED)
			return false;
		
		//Don't register the template as out of date if the template is not allowed to be rebuilt (because the unit master has already been closed, for instance)
		if(!isUnitComponentUpdateAllowed())
			return false;
		
		return StringUtils.isBlank(previousTemplateHash) || !StringUtils.equals(previousTemplateHash, masterTemplateHash);
	}
	
	//FIXME: document
	public boolean isUnitSignatureOutOfDate()
	{
		return StringUtils.isBlank(previousUnitSignature) || !StringUtils.equals(previousUnitSignature, calculatedUnitSignature);
	}

	//FIXME: document
	public String getCalculatedTemplateHash()
	{
		if(!isUnitComponentUpdateAllowed())
			return previousTemplateHash;
		else
			return masterTemplateHash;
	}
	
	/**
	 * Allows the program to update the unit signature for this unit when the rules are run.
	 * @param newSignature The newly-calculated unit signature for the unit.
	 */
	public void updateCalculatedUnitSignature(String newSignature)
	{
		this.calculatedUnitSignature = newSignature;
	}
	
	/**
	 * Marks the unit master as having had the rules data run on it and able to be written to SMC_UNIT_COMPONENT.
	 * This method will do nothing if the unit components aren't allowed to be updated anymore (ex: for a delivered unit master with no missing info)
	 */
	public void updateLastTemplateRefresh()
	{
		//Don't allow the template refresh date to be updated if no components can be updated on the unit master either.
		if(isUnitComponentUpdateAllowed())
			this.lastTemplateRefresh = new Date();
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getMasterId()
	{
		return masterId;
	}

	public int getPoNumber()
	{
		return poNumber;
	}

	public String getUnitNumber()
	{
		return unitNumber;
	}
	
	public CorpCode getCorpCode()
	{
		return corpCode;
	}

	public int getVendorId()
	{
		return vendorId;
	}

	public int getPoCategoryAssociationId()
	{
		return poCategoryAssociationId;
	}

	public String getCalculatedUnitSignature()
	{
		return calculatedUnitSignature;
	}

	public int getTemplateId()
	{
		return templateId;
	}

	public String getPreviousTemplateHash()
	{
		return previousTemplateHash;
	}

	public PayableStatus getPayableStatus()
	{
		return payableStatus;
	}

	public Date getActualDeliveryDate()
	{
		return actualDeliveryDate;
	}

	public int getMissingInfoCount()
	{
		return missingInfoCount;
	}

	public String getPreviousUnitSignature()
	{
		return previousUnitSignature;
	}

	public String getMasterTemplateHash()
	{
		return masterTemplateHash;
	}

	public Date getLastTemplateRefresh()
	{
		return lastTemplateRefresh;
	}
}
