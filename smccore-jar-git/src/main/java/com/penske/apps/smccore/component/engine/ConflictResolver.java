/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.util.Collection;

/**
 * An object capable of providing information about how to resolve component conflicts.
 * A component conflict is when the same component ID is required on multiple templates for the same unit.
 * 
 * Contrary to what its name might suggest, this interface is in no way related to mediating arguments, putting an end to wars, or achieving world peace.
 */
public interface ConflictResolver
{
	/**
	 * Attempt to find a resolution for a conflict discovered between the given components using individual conflcit status resolutions.
	 * @param componentsInConflict The set of components that all have the same component ID and are all required on the same unit.
	 * @return The component that should be required in the given conflict. It is assumed that the other components in the conflict are just visible, not required.
	 * 	This component must be from the collection of components passed in.
	 */
	public ComponentModel getIndividualResolution(Collection<ComponentModel> componentsInConflict);
	
	/**
	 * Attempt to find a resolution for a conflict discovered between the given components using the global conflict resolution rules.
	 * This method does not change the component models passed in in any way.
	 * @param componentsInConflict The set of components that all have the same component ID and are all required on the same unit.
	 * @return The component that should be required in the given conflict. It is assumed that the other components in the conflict are just visible, not required.
	 * 	This component must be from the list of components passed in.
	 */
	public ComponentModel getGlobalResolution(Collection<ComponentModel> componentsInConflict);
}