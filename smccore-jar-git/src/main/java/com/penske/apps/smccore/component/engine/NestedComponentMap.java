/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container that indexes component information first by master ID and then by component ID.
 * This object is primarily intended for use by the component visibility rules engine, as it represents a structure
 * 	commonly seen throughout the engine and associated service: Map<Integer, Map<Integer, E>>.
 * @param <T> The type of data contained in this map. This does not necessarily have to implement {@link NestableComponentData}, but some
 * 	of the methods on this class work better if it does.
 */
//FIXME: test
public class NestedComponentMap<T>
{
	//We use Collections.emptyMap() to force the internal map to be unmodifiable.
	private static final NestedComponentMap<?> EMPTY_INSTANCE = new NestedComponentMap<Object>(Collections.<Integer, Map<Integer, Object>>emptyMap());

	private final Map<Integer, Map<Integer, T>> components;
	
	@SuppressWarnings("unchecked")	//This is safe because the internal map will never contain anything, so we won't ever get a cast exception trying to coerce things in and out of it.
	public static <T> NestedComponentMap<T> emptyMap()
	{
		return (NestedComponentMap<T>) EMPTY_INSTANCE;
	}
	
	/** Empty constructor - creates an empty nested map */
	private NestedComponentMap()
	{
		this.components = new HashMap<Integer, Map<Integer, T>>();
	}
	
	/** Creates a nested map using the given underlying map directly. Do not use this publicly because of exposing leaked references to class internals. */
	private NestedComponentMap(Map<Integer, Map<Integer, T>> components)
	{
		if(components == null)
			components = new HashMap<Integer, Map<Integer, T>>();
		
		this.components = components;
	}
	
	public static <T> NestedComponentMap<T> newInstance()
	{
		return new NestedComponentMap<T>();
	}
	
	/**
	 * Creates a map of component information keyed by master ID and then by component ID, with data already in it.
	 * @param components A collection of component information that the map should contain.
	 * 	If this argument has more than one object for the same master ID and component ID, one of them will be chosen arbitrarily
	 * 	for inclusion in the map, and the rest will be ignored.
	 */
	public static <N extends NestableComponentData> NestedComponentMap<N> of(Collection<N> components)
	{
		if(components == null)
			return new NestedComponentMap<N>();
		
		NestedComponentMap<N> result = new NestedComponentMap<N>();
		for(N component : components)
		{
			int masterId = component.getMasterId();
			int componentId = component.getComponentId();
			result.put(masterId, componentId, component);
		}
		
		return result;
	}

	/**
	 * Creates a map of component information keyed by master ID and then by component ID, by pulling data from an existing map.
	 * The original map remains unchanged. Makes a copy of the original map's entries, but the values in the original map are the same as the values in this map.
	 * @param source The source map of component information to pull data from.
	 */
	public static <T> NestedComponentMap<T> of(NestedComponentMap<T> source)
	{
		if(source == null)
			return new NestedComponentMap<T>();
		
		NestedComponentMap<T> result = new NestedComponentMap<T>();
		for(Entry<Integer, Map<Integer, T>> entry : source.components.entrySet())
		{
			Map<Integer, T> componentsForMasterId = new HashMap<Integer, T>(entry.getValue());
			result.components.put(entry.getKey(), componentsForMasterId);
		}
		return result;
	}
	
	/**
	 * Creates a map of component information keyed by master ID and then by component ID, with data already in it.
	 * 	NOTE: this method does not make use of the keys in the given map, but instead re-indexes the collection of component data by their IDs.
	 * 	This is done to ensure accuracy of the map entries, but is slower than it might otherwise be.
	 * 	Thus, this method has no speed advantages over the constructor version that takes a Collection<E>, but is just here for convenience.
	 * @param components A map of component information that the map should contain, indexed by master ID and then by component ID.
	 */
	public static <T> NestedComponentMap<T> of(Map<Integer, Map<Integer, T>> components)
	{
		if(components == null)
			return new NestedComponentMap<T>();
		
		NestedComponentMap<T> result = new NestedComponentMap<T>();
		for(Entry<Integer, Map<Integer, T>> componentsForMasterIdEntry : components.entrySet())
		{
			if(componentsForMasterIdEntry == null)
				continue;
			
			int masterId = componentsForMasterIdEntry.getKey();
			Map<Integer, T> componentsForMasterId = componentsForMasterIdEntry.getValue();
			if(componentsForMasterId == null)
				continue;
			
			for(Entry<Integer, T> componentEntry : componentsForMasterId.entrySet())
				result.put(masterId, componentEntry.getKey(), componentEntry.getValue());
		}
		
		return result;
	}
	
	/**
	 * Package-private factory method to directly set the component data in this map.
	 * 	This method is not public because it results in a leaked reference to the internal map, but it is faster.
	 * 	Thus, this method is intended only for use within the rules engine itself, where leaked references can be properly managed
	 * @param components A map of component information that this map should contain, indexed by master ID and then by component ID. 
	 * @return The newly-created map.
	 */
	static <T> NestedComponentMap<T> newInstanceInternal(Map<Integer, Map<Integer, T>> components)
	{
		return new NestedComponentMap<T>(components);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return components.toString();
	}
	
	//***** COLLECTION MANIPULATION METHODS *****//
	/**
	 * Adds a piece of component information to the map.
	 * @param component The component that should be added and indexed by master ID and component ID. May not be null.
	 * 	If the map already contains a value with the same master ID and component ID, this one will replace the one already there.
	 */
	public void put(int masterId, int componentId, T component)
	{
		if(component == null)
			throw new NullPointerException("NestedComponentMap does not allow nulls");
		
		Map<Integer, T> componentsForMasterId = components.get(masterId);
		if(componentsForMasterId == null)
		{
			componentsForMasterId = new HashMap<Integer, T>();
			components.put(masterId, componentsForMasterId);
		}
		
		componentsForMasterId.put(componentId, component);
	}
	
	/**
	 * Removes an entry from the map, based on its master ID and component ID.
	 * @param component A NestableComponentData with the same master ID and component ID as the one to be removed. Does not have to be identically the same object,
	 * 	nor does it have to be equal according to {@link Object#equals(Object)}. It just has to have the same keys.
	 * 	If this is null, this method does nothing.
	 * @return The component information removed from the map, or null if no component with matching master ID and component ID was found.
	 */
	public <N extends NestableComponentData> T remove(N component)
	{
		if(component == null)
			return null;
		
		return remove(component.getMasterId(), component.getComponentId());
	}
	
	/**
	 * Removes an entry from the map, based on its master ID and component ID.
	 * @param masterId The master ID of the component information to remove.
	 * @param componentId The component ID of the component information to remove.
	 * @return The component information removed from the map, or null if no component with matching master ID and component ID was found.
	 */
	public T remove(int masterId, int componentId)
	{
		Map<Integer, T> componentsForMasterId = components.get(masterId);
		if(componentsForMasterId == null)
			return null;
		
		T valueRemoved = componentsForMasterId.remove(componentId);
		
		//Have to clean up empty maps after removing the last element for a master ID.
		if(componentsForMasterId.isEmpty())
			components.remove(masterId);
		
		return valueRemoved;
	}

	public boolean isEmpty()
	{
		return components.isEmpty();
	}

	//***** COLLECTION INQURY METHODS *****//
	public T get(int masterId, int componentId)
	{
		Map<Integer, T> componentsForMasterId = components.get(masterId);
		if(componentsForMasterId == null)
			return null;
		return componentsForMasterId.get(componentId);
	}
	
	public boolean containsKey(int masterId, int componentId)
	{
		return components.containsKey(masterId) && components.get(masterId).containsKey(componentId);
	}
	
	public <N extends NestableComponentData> boolean containsKey(N component)
	{
		if(component == null)
			return false;
		return this.containsKey(component.getMasterId(), component.getComponentId());
	}
	
	/**
	 * Returns an unmodifiable view of the master IDs for which there are components contained in this map.
	 * @return A view of all the master IDs represented by this map.
	 */
	public Set<Integer> getAllMasterIds()
	{
		return Collections.unmodifiableSet(components.keySet());
	}
	
	/**
	 * Gets a map of all the component information that belongs to a particular master ID.
	 * @param masterId The master ID to look up.
	 * @return An unmodifiable view of all the component information for the given master ID, keyed by component ID.
	 * 	If there are no components for this master ID, returns an empty map.
	 */
	public Map<Integer, T> getComponentsForMasterId(int masterId)
	{
		Map<Integer, T> result = components.get(masterId);
		return result == null ? Collections.<Integer, T>emptyMap() : Collections.unmodifiableMap(result);
	}
	
	/**
	 * Gets a new copy of the data contained in this map. The values are the same objects in this map,
	 * 	but the resulting maps are not tied to the internal map in this object, so changes to one map do not affect the other.
	 * @return A copy of the data contained in this map, keyed by master ID and then by component ID. 
	 */
	public Map<Integer, Map<Integer, T>> getAllComponents()
	{
		Map<Integer, Map<Integer, T>> result = new HashMap<Integer, Map<Integer, T>>();
		for(Entry<Integer, Map<Integer, T>> entry : components.entrySet())
		{
			Map<Integer, T> componentsForMasterId = new HashMap<Integer, T>(entry.getValue());
			result.put(entry.getKey(), componentsForMasterId);
		}
		return result;
	}
}