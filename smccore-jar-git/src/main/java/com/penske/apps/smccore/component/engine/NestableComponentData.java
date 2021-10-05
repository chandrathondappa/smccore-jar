/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

/**
 * A piece of component information that has both a master ID and a component ID.
 * Things that implement this interface can be stored in a {@link NestedComponentMap}, so they can be indexed properly.
 * Each component ID should theoretically appear at most once on each template.
 */
public interface NestableComponentData
{
	/**
	 * Gets The ID of the object that this component data applies to, to differentiate it from other components with the same component ID.
	 * For example, two different templates may have the same component on them, and if both are required, then there is a component conflict,
	 * and the master ID is used to tell the difference between them when resolving the conflict.
	 * @return The ID of the object this data applies to.
	 */
	public int getMasterId();
	
	/**
	 * Gets the 9-digit ID for the component. This is what identifies the component within Penske's systems.
	 * SMC uses components with IDs less than 100 to map to attributes of the vehicle record, rather than mapping directly to the vehicle component file. 
	 * @return The component's internal ID.
	 */
	public int getComponentId();
}
