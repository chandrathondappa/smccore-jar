/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;

/**
 * A global rule for resolving component conflicts.
 * If two components are in conflict and no individual resolution has been set, then the list of applicable
 * global resolutions will be searched for one that is able to resolve the conflict.
 * 
 * There may only be one global conflict resolution per unit signature per component.
 * 
 * Individual resolutions trump global resolutions.
 */
public class GlobalConflictResolution
{
	/** Internal database ID for this global exception */
	private Integer globalExceptionId;
	
	/** A String that identifies the po category/subcategory combinations and vendor IDs involved in this unit. */
	private String unitSignature;
	/** The internal ID of the component that this object resolves conflicts for. */
	private int componentId;
	/** The PO Category/Subcategory combination of the PO on which the component should be required. */
	private int providerPoCategoryAssociationId;
	/** The vendor ID of the  */
	private int providerVendorId;
	
	/** Null constructor - MyBatis only */
	protected GlobalConflictResolution() {}
	
	/**
	 * Creates a global conflict resolution.
	 * @param componentId The ID of the component that this resolution will resolve conflicts for.
	 * @param provider Information about the vendor and unit signature that will be responsible for providing the information when this global resolution applies to a conflict.
	 */
	public GlobalConflictResolution(int componentId, UnitMasterInfo provider)
	{
		if(provider == null)
			throw new IllegalArgumentException("A global conflict resolution requires a vendor that will provide the component information. Component ID: " + componentId);
		
		this.componentId = componentId;
		this.unitSignature = provider.getCalculatedUnitSignature();
		this.providerPoCategoryAssociationId = provider.getPoCategoryAssociationId();
		this.providerVendorId = provider.getVendorId();
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{GlobalResolution (component " + componentId + ", signature " + unitSignature + "), Provider: (" + providerPoCategoryAssociationId + " / " + providerVendorId + ")}";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Integer getGlobalExceptionId()
	{
		return globalExceptionId;
	}

	public String getUnitSignature()
	{
		return unitSignature;
	}

	public int getComponentId()
	{
		return componentId;
	}

	public int getProviderPoCategoryAssociationId()
	{
		return providerPoCategoryAssociationId;
	}

	public int getProviderVendorId()
	{
		return providerVendorId;
	}
}
