/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ConflictResolver;

/**
 * Conflict resolver for unit tests that resolves conflicts based solely on a specified template ID.
 */
public class TestConflictResolver implements ConflictResolver
{
	private final Map<Integer, Integer> individualResolutions = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> globalResolutions = new HashMap<Integer, Integer>();

	/** {@inheritDoc} */
	@Override
	public ComponentModel getIndividualResolution(Collection<ComponentModel> componentsInConflict)
	{
		return resolveconflict(componentsInConflict, individualResolutions);
	}
	
	/** {@inheritDoc} */
	@Override
	public ComponentModel getGlobalResolution(Collection<ComponentModel> componentsInConflict)
	{
		return resolveconflict(componentsInConflict, globalResolutions);
	}

	public TestConflictResolver addIndividualResolution(Integer componentId, Integer masterId)
	{
		this.individualResolutions.put(componentId, masterId);
		return this;
	}
	
	public TestConflictResolver addGlobalResolution(Integer componentId, Integer masterId)
	{
		this.globalResolutions.put(componentId, masterId);
		return this;
	}
	
	private ComponentModel resolveconflict(Collection<ComponentModel> componentsInConflict, Map<Integer, Integer> resolutionSource)
	{
		if(componentsInConflict == null || componentsInConflict.isEmpty())
			return null;
		
		Integer resolutionMasterId = null;
		for(ComponentModel component : componentsInConflict)
		{
			if(resolutionMasterId == null)
			{
				resolutionMasterId = resolutionSource.get(component.getComponentMaster().getComponentId());
				if(resolutionMasterId == null)
					return null;
			}
			if(component.getMasterId() == resolutionMasterId)
				return component;
		}
		return null;
	}
}
