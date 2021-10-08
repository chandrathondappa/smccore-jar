/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.Visibility;

/**
 * A representation of a single component that can be on a vehicle, for use in the unit template components module.
 */
public class UnitComponentMaster implements ComponentMaster
{
    /** The component's nine-digit ID */
    private int componentId;
    /** The ID of the unit template that this component belongs to. */
    private int masterId;
    
    /** A human-readable name for the component. */
    private String componentName;
    
    /** The type of data contained in this component. */
    private ComponentType componentType;
    /** Indication for whether or not this component should be visible / editable / required / not visible in the unit template. May get changed by visibility override rules or conflict resolution. */
    private Visibility visibility;
    /** The number of decimal positions permitted in this component if it represents a number; 0 or null if it is a text component. null is treated as "do not round" */
    private Integer decimalPositions;
    /** The number of characters long that the component value can be. This is primarily used by the user interface and not the back-end. */
    private Integer fieldLength;
    
    /** Whether this component will be shown to vendors on the Unit Details screen if it's on a different PO than the vendor user clicked on. */
    private boolean availableOnOtherPO;
    /** True if this component should get exported to excel. False if it shouldn't. */
    private boolean exportToExcel;
    /** True if this component is from a vehicle header level (ex: UNIT_SHIPPING_INFO, UNIT_DATE_INFO, or the vehicle file). False if it is an actual component in the component file. */
    private boolean vehicleComponent;

    //***** PREVIOUSLY-SAVED VALUES *****//
    /**
     * This object stores the values that used to be present in the unit component the last time the rules were run.
     * Used to determine whether or not to write a row for this component.
     * This can be null if there was no previous saved data for this component
     */
    private UnitComponent previousValues;
    
    //***** CONSTRUCTORS *****//
    /** Null constructor - MyBatis only */
	protected UnitComponentMaster() {}
	
	//FIXME: document
	protected UnitComponentMaster(UnitComponentMaster source, int newMasterId)
	{
		this.componentId = source.componentId;
		this.componentName = source.componentName;
		this.componentType = source.componentType;
		this.visibility = source.visibility;
		this.decimalPositions = source.decimalPositions;
		this.fieldLength = source.fieldLength;
		this.previousValues = source.previousValues;
		this.availableOnOtherPO = source.availableOnOtherPO;
		this.exportToExcel = source.exportToExcel;
		this.vehicleComponent = source.vehicleComponent;
		
		//Create this one with a different template ID from the original
		this.masterId = newMasterId;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{" + getFullComponentName() + " (" + masterId + ", " + componentId + "), " + componentType + ", " + visibility + ", Conf: " + getPreviousConflictStatus() + "}";
	}
	
    //***** MODIFIED ACCESSORS *****//
	/** {@inheritDoc} */
	@Override
	public int getMasterId()
	{
		return masterId;
	}
	
    /** {@inheritDoc} */
    public String getFullComponentName()
    {
        return componentName;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isIgnoredInConflicts()
    {
    	return vehicleComponent;
    }
        
    //***** DEFAULT ACCESSORS *****//
	/** {@inheritDoc} */
	@Override
	public int getComponentId()
	{
		return componentId;
	}

	/** {@inheritDoc} */
	@Override
	public String getComponentGroup()
	{
		return componentName;
	}

	/** {@inheritDoc} */
	@Override
	public String getSubGroup()
	{
		return "";
	}

	/** {@inheritDoc} */
	@Override
	public String getSubComponentName()
	{
		return "";
	}

	/** {@inheritDoc} */
	@Override
	public ComponentType getComponentType()
	{
		return componentType;
	}

	/** {@inheritDoc} */
	@Override
	public Visibility getVisibility()
	{
		return visibility;
	}

	/** {@inheritDoc} */
	@Override
	public Integer getDecimalPositions()
	{
		return decimalPositions;
	}

	public boolean isAvailableOnOtherPO()
	{
		return availableOnOtherPO;
	}

	public boolean isExportToExcel()
	{
		return exportToExcel;
	}

	public ConflictStatus getPreviousConflictStatus()
	{
		return previousValues == null ? null : previousValues.getConflictStatus();
	}

	public boolean isVehicleComponent()
	{
		return vehicleComponent;
	}

	public Integer getFieldLength()
	{
		return fieldLength;
	}

	public UnitComponent getPreviousValues()
	{
		return previousValues;
	}
}
