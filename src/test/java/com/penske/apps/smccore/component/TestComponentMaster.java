/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.Visibility;

/**
 * Dummy implementation of the {@link ComponentMaster} interface, for unit testing
 */
public class TestComponentMaster implements ComponentMaster
{
    /** The component's nine-digit ID */
    private int componentId;
    /** The ID of the unit master this component belongs to */
    private int masterId;
	
    /** The name of the top-level component grouping. Corresponds to the first 3 digits in the component ID. */
    private String componentGroup;
    /** The name of the component sub-group. Corresponds to the middle 3 digits in the component ID. */
    private String subGroup;
    /** The name of the sub-component. Corresponds to the last 3 digits in the component ID. */
    private String subComponentName;
    
    /** The type of data contained in this component (text or numbers). */
    private ComponentType componentType;
    /** Indication for whether or not this component should be visible / required / not visible in the loadsheet. May get changed by visibility override rules */
    private Visibility visibility;
    
    private boolean ignoredInConflicts;

	public TestComponentMaster(int componentId, int masterId, String componentGroup, String subGroup, String subComponentName, ComponentType componentType, Visibility visibility, boolean ignoredInConflicts)
	{
		this.componentId = componentId;
		this.masterId = masterId;
		this.componentGroup = componentGroup;
		this.subGroup = subGroup;
		this.subComponentName = subComponentName;
		this.componentType = componentType;
		this.visibility = visibility;
		this.ignoredInConflicts = ignoredInConflicts;
	}

	/** {@inheritDoc} */
    @Override
    public String toString()
    {
    	return "{Comp " + componentId + " (" + getFullComponentName() + "), Vis: " + visibility + "}";
    }
    
	/** {@inheritDoc} */
    public String getFullComponentName()
    {
        List<String> parts = new ArrayList<String>(3);
        if(StringUtils.isNotBlank(componentGroup))
            parts.add(componentGroup);
        if(StringUtils.isNotBlank(subGroup))
            parts.add(subGroup);
        if(StringUtils.isNotBlank(subComponentName))
            parts.add(subComponentName);
        return StringUtils.join(parts, " - ");
    }

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
		return componentGroup;
	}

	/** {@inheritDoc} */
	@Override
	public String getSubGroup()
	{
		return subGroup;
	}

	/** {@inheritDoc} */
	@Override
	public String getSubComponentName()
	{
		return subComponentName;
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
		return 4;
	}

	/** {@inheritDoc} */
	@Override
	public int getMasterId()
	{
		return masterId;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isIgnoredInConflicts()
	{
		return ignoredInConflicts;
	}
}
