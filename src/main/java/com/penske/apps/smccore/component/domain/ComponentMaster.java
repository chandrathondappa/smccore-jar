/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.engine.NestableComponentData;

/**
 * A representation of a single vehicle component in the abstract, rather than a particular component on a particular vehicle.
 * Components describe various attributes about a unit, like it's engine model year, engine model, vehicle make, rear axle serial number, gross vehicle weight, etc.
 * Each unit has its own set of components that describe the attributes of that unit.
 * Objects that implement this interface contains base information about a component itself, not the component's value for a particular vehicle.
 */
public interface ComponentMaster extends NestableComponentData
{
	/**
	 * @return The name of the top-level component grouping. Corresponds to the first 3 digits in the component ID.
	 */
	public String getComponentGroup();
	
    /**
     * @return The name of the component sub-group. Corresponds to the middle 3 digits in the component ID.
     */
	public String getSubGroup();
	
    /**
     * @return The name of the sub-component. Corresponds to the last 3 digits in the component ID.
     */
	public String getSubComponentName();
 
	/**
     * Gets a human-readable name for the entire component. Composed of the group, subgroup, and sub component name joined by hyphens.
     * @return A human-readable name for the component.
     */
    public String getFullComponentName();
	
    /**
     * @return The type of data contained in this component (text or numbers).
     */
	public ComponentType getComponentType();
	
    /** 
     * Gets the base visibility for this component in its particular template, before any rules are applied.
     * Visibility refers to whether or not this component should be visible / editable / required / not visible for the given template
     * May get changed by visibility override rules.
     * @return the base visibility for the component, before any rules are applied.
     */
	public Visibility getVisibility();
	
	/**
	 * Gets the number of decimal positions permitted in this component if it represents a number; 0 or null if it is a text component. null is treated as "do not round"
	 * @return The number of decimal positions this component has.
	 */
	public Integer getDecimalPositions();

	/**
	 * Checks if this component should be excluded when considering whether components are in conflict.
	 * @return True if this component should not count when determining if there are multiple required components for the same ID. False if it should count.
	 */
	public boolean isIgnoredInConflicts();
}