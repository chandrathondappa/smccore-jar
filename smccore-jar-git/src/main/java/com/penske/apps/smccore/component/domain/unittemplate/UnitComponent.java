/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.NestableComponentData;

/**
 * Represents rules engine information about a particular component for a particular master ID
 */
//FIXME: test
public class UnitComponent implements NestableComponentData
{
	private int masterId;
	private int componentId;
	
	private boolean availableOnOtherPO;
    private boolean exportToExcel;
    private Visibility baseVisibility;
    private Visibility ruleVisibility;
    private Visibility finalVisibility;
    private ConflictStatus conflictStatus;
    private boolean valueProvided;
    
    /** Null constructor - MyBatis only */
    protected UnitComponent() {}
    
    public UnitComponent(ComponentModel component, boolean valueProvided)
    {
    	if(component == null)
    		throw new IllegalArgumentException("Component model is required");
    	
    	this.masterId = component.getMasterId();
    	this.componentId = component.getComponentId();
    	
    	UnitComponentMaster master = component.unwrapComponentMaster();
    	this.availableOnOtherPO = master.isAvailableOnOtherPO();
    	this.exportToExcel = master.isExportToExcel();
    	this.baseVisibility = component.getBaseVisibility();
    	this.ruleVisibility = component.getRuleVisibility();
    	this.finalVisibility = component.getFinalVisibility();
    	this.conflictStatus = component.getConflictStatus();
    	this.valueProvided = valueProvided;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
    	return new StringBuilder("{UnitComponent ")
		.append("(")
			.append("ID ").append(componentId)
			.append(", Master ID ").append(masterId)
		.append(")")
		.append(", Visibility: ").append(finalVisibility == null ? "" : finalVisibility.getUnitTemplateCode())
		.append(", Conflict: ").append(conflictStatus == null ? "" : conflictStatus.getCode())
		.append(", Value Provided: ").append(valueProvided)
	.append("}").toString();
    }

    //***** MODIFIED ACCESSORS *****//
    public boolean isDifferentfrom(UnitComponent other)
    {
    	//If previous final visibility was null, that means there was no previous row, since final visibilityis non-nullable
    	if(other == null)
    		return true;
    	
    	//If any saved field is different, it counts as a difference
    	if(availableOnOtherPO != other.isAvailableOnOtherPO())
    		return true;
    	if(exportToExcel != other.isExportToExcel())
    		return true;
    	if(baseVisibility != other.getBaseVisibility())
    		return true;
    	if(ruleVisibility != other.getRuleVisibility())
    		return true;
    	if(finalVisibility != other.getFinalVisibility())
    		return true;
    	if(conflictStatus != other.getConflictStatus())
    		return true;
    	if(valueProvided != other.isValueProvided())
    		return true;
    	
    	//If no differences were detected, return that fact
    	return false;
    }
    
    public void setConflictStatus(ConflictStatus conflictStatus)
	{
		this.conflictStatus = conflictStatus;
	}
    
    //***** DEFAULT ACCESSORS *****//
	public int getMasterId()
	{
		return masterId;
	}

	public int getComponentId()
	{
		return componentId;
	}

	public boolean isAvailableOnOtherPO()
	{
		return availableOnOtherPO;
	}

	public boolean isExportToExcel()
	{
		return exportToExcel;
	}

	public Visibility getBaseVisibility()
	{
		return baseVisibility;
	}

	public Visibility getRuleVisibility()
	{
		return ruleVisibility;
	}

	public Visibility getFinalVisibility()
	{
		return finalVisibility;
	}

	public ConflictStatus getConflictStatus()
	{
		return conflictStatus;
	}

	public boolean isValueProvided()
	{
		return valueProvided;
	}
}
