/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.penske.apps.smccore.component.domain.GlobalConflictResolution;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ConflictResolver;
import com.penske.apps.smccore.component.engine.NestedComponentMap;

/**
 * Conflict Resolver for the unit templates module.
 */
//FIXME: test
public class UnitConflictResolver implements ConflictResolver
{
	/** The unit signature for the unit being processed. This conflict resolver only works on one unit signature at a time. */
	private final String unitSignature;
	/** All the template IDs in the current unit, keyed by vendor ID. Used to map a global resolution to the component that should be required. */
	private final Map<Integer, Integer> masterIdsByVendorId = new HashMap<Integer, Integer>();
	/** The set of global resolutions for this unit signature, keyed by the component ID that they resolve conflicts for. */
	private final Map<Integer, GlobalConflictResolution> globalResolutions = new HashMap<Integer, GlobalConflictResolution>();
	/** If the current unit has individual resolutions, this map contains the template ID on which the component should be required, keyed by the component ID the conflict is being resolved for. */
	private final Map<Integer, Integer> individualResolutions = new HashMap<Integer, Integer>();
	
	/**
	 * Create a resolver that can operate on unit template components.
	 * @param unitMasterInfo Some summary information about each unit master involved in this unit. Used for determining the mapping between vendor IDs and template IDs.
	 * @param componentMasters Objects containing information about individual conflict resolutions (i.e. ones specific to that PO, as opposed to global conflict resolution rules).
	 * @param globalResolutions All the global conflict resolution rules for this unit signature.
	 */
	public UnitConflictResolver(Collection<UnitMasterInfo> unitMasterInfo, NestedComponentMap<UnitComponentMaster> componentMasters, Collection<GlobalConflictResolution> globalResolutions)
	{
		if(globalResolutions == null)
			globalResolutions = Collections.emptyList();
		if(unitMasterInfo == null)
			unitMasterInfo = Collections.emptyList();
		if(componentMasters == null)
			componentMasters = NestedComponentMap.emptyMap();
		
		String unitSignature = null;
		for(UnitMasterInfo unitMaster : unitMasterInfo)
		{
			if(unitSignature == null)
				unitSignature = unitMaster.getCalculatedUnitSignature();
			else if(!unitSignature.equals(unitMaster.getCalculatedUnitSignature()))
				throw new IllegalArgumentException("Global conflict resolver only applicable for a single unit signature: " + unitSignature + " vs. " + unitMaster.getCalculatedUnitSignature());
			
			unitSignature = unitMaster.getCalculatedUnitSignature();
			
			//It is technically possible to have more than one unit master for the same vendor ID (ex: multiple DECAL POs to the same vendor on the same unit)
			//so the global conflict resolver will arbitrarily pick one of them to use when resolving conflicts.
			// In theory, if this happens, it doesn't matter which PO the component gets marked required on because they are all from the same vendor.
			masterIdsByVendorId.put(unitMaster.getVendorId(), unitMaster.getMasterId());
		}
		
		if(StringUtils.isBlank(unitSignature))
			throw new IllegalArgumentException("Global conflict resolver requires at lest one unit master with a unit signature.");
		
		this.unitSignature = unitSignature;
		
		//NOTE: this is a "last-one-wins" scenario. If more than one unit component is marked required
		//	for an individual conflict, the last one in the collection will actually end up being required.
		for(int masterId : componentMasters.getAllMasterIds())
		{
			for(Entry<Integer, UnitComponentMaster> entry : componentMasters.getComponentsForMasterId(masterId).entrySet())
			{
				int componentId = entry.getKey();
				UnitComponentMaster componentMaster = entry.getValue();
				ConflictStatus previousConflictStatus = componentMaster.getPreviousConflictStatus();
				if(previousConflictStatus == ConflictStatus.INDIVIDUAL_RESOLUTION_REQUIRED)
					this.individualResolutions.put(componentId, masterId);
			}
		}
		
		for(GlobalConflictResolution resolution : globalResolutions)
		{
			String resolutionSignature = resolution.getUnitSignature();
			if(!this.unitSignature.equals(resolutionSignature))
				throw new IllegalArgumentException("Global conflict resolutions with signature " + resolutionSignature + " are not applicable for global conflict resolver with signature " + this.unitSignature);
			
			int componentId = resolution.getComponentId();
			
			this.globalResolutions.put(componentId, resolution);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public ComponentModel getIndividualResolution(Collection<ComponentModel> componentsInConflict)
	{
		if(componentsInConflict == null || componentsInConflict.isEmpty())
			return null;
		
		//Make sure the objects in the conflict are actually in conflict and all refer to the same component
		Pair<Integer, Map<Integer, ComponentModel>> conflictData = getConflictData(componentsInConflict);
		Integer componentId = conflictData.getLeft();
		Map<Integer, ComponentModel> componentsByMasterId = conflictData.getRight();
		
		Integer masterId = individualResolutions.get(componentId);
		return componentsByMasterId.get(masterId);
	}
	
	/** {@inheritDoc} */
	@Override
	public ComponentModel getGlobalResolution(Collection<ComponentModel> componentsInConflict)
	{
		if(componentsInConflict == null || componentsInConflict.isEmpty())
			return null;
		
		//Make sure the objects in the conflict are actually in conflict and all refer to the same component
		Pair<Integer, Map<Integer, ComponentModel>> conflictData = getConflictData(componentsInConflict);
		Integer componentId = conflictData.getLeft();
		Map<Integer, ComponentModel> componentsByMasterId = conflictData.getRight();
		
		GlobalConflictResolution resolution = this.globalResolutions.get(componentId);
		if(resolution == null)
			return null;
		
		//Look up which component should be responsible for providing the information based on the vendor ID.
		Integer masterId = masterIdsByVendorId.get(resolution.getProviderVendorId());
		ComponentModel requiredComponent = componentsByMasterId.get(masterId);
		
		return requiredComponent;
	}
	
	private Pair<Integer, Map<Integer, ComponentModel>> getConflictData(Collection<ComponentModel> componentsInConflict)
	{
		Integer componentId = null;
		Map<Integer, ComponentModel> componentsByMasterId = new HashMap<Integer, ComponentModel>();
		for(ComponentModel component : componentsInConflict)
		{
			if(componentId == null)
				componentId = component.getComponentMaster().getComponentId();
			else if(componentId != component.getComponentMaster().getComponentId())
				throw new IllegalArgumentException("Can not resolve global conflict for a mixture of component IDs at once. (" + componentId + " vs. " + component.getComponentMaster().getComponentId() + "), signature " + unitSignature);
			
			int masterId = component.getMasterId();
			
			//This should never happen, since each template should only have one of each component ID, but just in case, throw an exception here if it does happen, because something is gravely wrong.
			if(componentsByMasterId.containsKey(masterId))
				throw new IllegalArgumentException("Found multiple components for the same component ID and template ID when trying to resolve global conflicts. Can not resolve conflict.");
			
			componentsByMasterId.put(masterId, component);
		}
		
		return Pair.of(componentId, componentsByMasterId);
	}
}
