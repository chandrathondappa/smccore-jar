/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.engine;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.domain.enums.Visibility;

/**
 * A data model containing a version of the component data represented in a structure that is optimal for querying by the rules engine.
 * This just consolidates many of the arguments the visibility rules engine would otherwise take into one object that can be passed around the various rules engine methods.
 */
public class ComponentVisibilityFilterData
{
	/**
	 * An integer that can be used to identify the results of this run of the rules, or null if no such ID is desired.
	 * 	For example, when running loadsheet rules, this might be the ID of the loadsheet the components belong to, to allow for caching rule runs within a single request.
	 */
	private final Integer markerId;
	/** The components to be operated on by the rules engine, keyed by their component ID. */
	private final Map<Integer, List<ComponentModel>> componentsById;
	/** How the rules engine should handle components whose visibilities are not visible during evaluation of the rules */
	private final NotVisibleBehavior notVisibleBehavior;
	/** The results of evaluating all the necessary rules, and the effects those rules will have on various components. This will be null if rules haven't been run yet. */
	private final EvaluationResult evaluationResult;
	/** The number of times the rules needed to be run to settle on a stable visibility result. This will be 0 if rules haven't been run yet. */
	private final int ruleIterations;
	/** A list of debugging outputs, one for each iteration of the rules that was run. This will be null if rules haven't been run yet. */
	private final List<String> debugInfo;
	
	/**
	 * The component values and visibilities as they would be if there were no user changes.
	 * This is used to determine if the user's changes impacted the visibility of any components.
	 */
	private final Map<Integer, List<ComponentModel>> componentsWithoutUserChanges;
	
	/** The original set of component values from before the rules engine was run. */
	private final NestedComponentMap<? extends ComponentValue> originalValues;
	
	/**
	 * Creates the data object for use by the component visibility rules engine.
	 * 	This constructor has package-private visibility since only the rules engine should be creating instances of it.
	 * 	This is because the internal map is not completely isolated, for performance reasons (we don't want to make a copy of the map on every construction).
	 * 	So, the constructor being package-private means only the rules engine can create instances of this object, so the map will not be reused. 
	 * @param componentsById The components the rules engine will operate on, keyed by their component ID.
	 * 	There may be more than one component for a given component ID, which would be the case where a component appears on multiple component templates.
	 */
	private ComponentVisibilityFilterData(Integer markerId, NestedComponentMap<? extends ComponentValue> originalValues, Map<Integer, List<ComponentModel>> componentsById, NotVisibleBehavior notVisibleComponentBehavior,
		Map<Integer, List<ComponentModel>> componentsWithoutUserChanges, EvaluationResult evaluationResult, int ruleIterations, List<String> debugInfo)
	{
		if(originalValues == null)
			originalValues = NestedComponentMap.emptyMap();
		if(componentsById == null)
			componentsById = Collections.emptyMap();
		if(componentsWithoutUserChanges == null)
			componentsWithoutUserChanges = Collections.emptyMap();
		if(notVisibleComponentBehavior == null)
			throw new IllegalArgumentException("Missing behavior for what to do if a component is not visible. Can not run rules.");
		
		this.markerId = markerId;
		this.originalValues = originalValues;
		this.componentsById = Collections.unmodifiableMap(componentsById);
		this.componentsWithoutUserChanges = componentsWithoutUserChanges;
		this.notVisibleBehavior = notVisibleComponentBehavior;
		
		this.evaluationResult = evaluationResult;
		this.ruleIterations = ruleIterations;
		this.debugInfo = debugInfo == null ? Collections.<String>emptyList() : Collections.unmodifiableList(debugInfo);
	}
	
	/**
	 * Creates component data to return if there are no components and no activity was done.
	 * @param markerId Optional. An integer that can be used to identify the results of this run of the rules, or null if no such ID is desired.
	 * @return The component data
	 */
	static ComponentVisibilityFilterData emptyInstance(Integer markerId)
	{
		return new ComponentVisibilityFilterData(markerId, null, null, NotVisibleBehavior.KEEP, null, null, 0, null);
	}

	/**
	 * Creates a brand new component data object to hold information about rules as they are being run and after the rules have finished.
	 * @param markerId Optional. An integer that can be used to identify the results of this run of the rules, or null if no such ID is desired.
	 * @param originalValues The values that the components had before the rules were run or any changes were made.
	 * @param componentsById The components with their values, after merging in user input, but before running the rules.
	 * @param notVisibleBehavior How the rules engine should behave if a component is not visible. What value is passed in here should reflect what will actually be done with the components
	 * 	after the rules engine runs. (ex: if not visible components should always be removed after running rules, then use {@link NotVisibleBehavior#REMOVE}
	 * @param componentsWithoutUserChanges The components with their values, but without taking into account user input. This is essentially what {@code componentsById} would be if the user didn't
	 * 	provide any component values. If null, then the engine will not be able to detect whether components were influenced by user changes or not for the {@link NotVisibleBehavior#REMOVE_IF_USER_INFLUENCED} behavior.
	 * @return The component data
	 */
	static ComponentVisibilityFilterData newInstance(Integer markerId, NestedComponentMap<? extends ComponentValue> originalValues, Map<Integer, List<ComponentModel>> componentsById, 
		NotVisibleBehavior notVisibleBehavior, Map<Integer, List<ComponentModel>> componentsWithoutUserChanges)
	{
		return new ComponentVisibilityFilterData(markerId, originalValues, componentsById, notVisibleBehavior, componentsWithoutUserChanges, null, 0, null);
	}

	/**
	 * Creates a new component data object that operates on the components that don't have user changes from the {@code sourceData} argument.
	 * This component data object is suitable for running rules on the components without user changes.
	 * @param sourceData The component data from which to pull the components to operate on.
	 * @return The component data
	 */
	static ComponentVisibilityFilterData withoutUserChanges(ComponentVisibilityFilterData sourceData)
	{
		return new ComponentVisibilityFilterData(sourceData.markerId, sourceData.originalValues, sourceData.componentsWithoutUserChanges, sourceData.notVisibleBehavior, null, null, 0, null);
	}
	
	/**
	 * Creates a new component data object that merges the main component data object with one representing rules being run on the set of components without user changes.
	 * 	The resulting object will then contain information about what component visibilities would be if the rules were run on components without user changes,
	 * 	so that the components that do have user changes can behave appropriately if {@link NotVisibleBehavior#REMOVE_IF_USER_INFLUENCED} has been selected as the NotVisibleBehavior.
	 * @param sourceData The main component data for this run of the rules, containing the components with user changes.
	 * @param dataWithoutUserChanges The component data object that was split off from the main one via {@link #withoutUserChanges(ComponentVisibilityFilterData)}, and has been run through the rules.
	 * @return The new component data object with information from both {@code sourceData} and {@code dataWithoutUserChanges}
	 */
	static ComponentVisibilityFilterData reintegrateUserUnchangedComponentsAfterRules(ComponentVisibilityFilterData sourceData, ComponentVisibilityFilterData dataWithoutUserChanges)
	{
		return new ComponentVisibilityFilterData(sourceData.markerId, sourceData.originalValues, sourceData.componentsById, sourceData.notVisibleBehavior,
			dataWithoutUserChanges.componentsById, null, 0, null);
	}
	
	/**
	 * Creates a new component data object that has a new set of component values and visibilities, presumably after some of them have changed while rules were being run or conflicts were being resolved.
	 * @param sourceData The component data to base the new one off of. All fields are preserved except the component collection itself.
	 * @param components The new component values and visibilities.
	 * @return The component data
	 */
	static ComponentVisibilityFilterData withComponents(ComponentVisibilityFilterData sourceData, Map<Integer, List<ComponentModel>> components)
	{
		return new ComponentVisibilityFilterData(sourceData.markerId, sourceData.originalValues, components,
			sourceData.notVisibleBehavior, sourceData.componentsWithoutUserChanges, sourceData.evaluationResult, sourceData.ruleIterations, sourceData.debugInfo);
	}
	
	/**
	 * Creates a new component data object that represents the final results of running the rules on these components.
	 * @param sourceData The component data to base the new one off of.
	 * @param evaluationResult The results of evaluating each rule and criteria, and applying outcomes.
	 * @param ruleIterations The number of iterations over the rules it took for component visibilities to stop changing.
	 * @param debugInfo Optional. If debug information was generated when running the rules, this contains formatted strings to represent that.
	 * @return The component data
	 */
	static ComponentVisibilityFilterData withFinalRuleResults(ComponentVisibilityFilterData sourceData, EvaluationResult evaluationResult, int ruleIterations, List<String> debugInfo)
	{
		return new ComponentVisibilityFilterData(sourceData.markerId, sourceData.originalValues, sourceData.componentsById,
			sourceData.notVisibleBehavior, sourceData.componentsWithoutUserChanges, evaluationResult, ruleIterations, debugInfo);
	}
	
	/**
	 * Static factory method to create a new ComponentVisibilityFilterData object.
	 * This means of creating the object is slower than the package-private constructor, since it has to sort the components into a map first, to avoid leaked references.
	 * Generally speaking, these objects should be created by the rules engine alone, so this method is provided primarily for unit testing.
	 * @param components A collection of the components that belong in this object.
	 * 	There may be more than one component for a given component ID, which would be the case where a component appears on multiple component templates.
	 * @param componentsWithoutUserChanges TODO
	 * @param notVisibleBehavior How the rules engine should handle components that are not visible after running the rules.
	 * @return The newly-created data object.
	 */
	public static ComponentVisibilityFilterData newInstanceForTesting(Integer markerId, Collection<ComponentModel> components, Collection<ComponentModel> componentsWithoutUserChanges, NotVisibleBehavior notVisibleBehavior)
	{
		if(components == null)
			components = Collections.emptyList();
		if(componentsWithoutUserChanges == null)
			componentsWithoutUserChanges = Collections.emptyList();
		
		NestedComponentMap<ComponentValue> originalValues = NestedComponentMap.newInstance();
		Map<Integer, List<ComponentModel>> componentsMap = new HashMap<Integer, List<ComponentModel>>();
		for(ComponentModel component : components)
		{
			int componentId = component.getComponentMaster().getComponentId();
			
			if(!componentsMap.containsKey(componentId))
				componentsMap.put(componentId, new ArrayList<ComponentModel>());
			componentsMap.get(componentId).add(component);
			
			originalValues.put(component.getMasterId(), componentId, component.getComponentValue());
		}
		
		Map<Integer, List<ComponentModel>> componentsWithoutUserChangesMap = componentsWithoutUserChanges.stream()
			.collect(groupingBy(ComponentModel::getComponentId));
		
		return new ComponentVisibilityFilterData(markerId, originalValues, componentsMap, notVisibleBehavior, componentsWithoutUserChangesMap, null, 0, null);
	}
	
	//***** MODIFIED ACCESSORS *****//
	/**
	 * @return the component IDs of all components stored in this object.
	 */
	public Set<Integer> getAllComponentIds()
	{
		return Collections.unmodifiableSet(componentsById.keySet());
	}

	//FIXME: document
	public Set<Integer> getAllMasterIds()
	{
		Set<Integer> result = new HashSet<Integer>();
		for(List<ComponentModel> components : componentsById.values())
		{
			for(ComponentModel component : components)
				result.add(component.getMasterId());
		}
		
		return result;
	}
	
	/**
	 * Gets all the component data stored in this object.
	 * @return The component data in this object.
	 */
	public NestedComponentMap<ComponentModel> getAllComponents()
	{
		NestedComponentMap<ComponentModel> result = NestedComponentMap.newInstance();
		for(List<ComponentModel> components : componentsById.values())
		{
			for(ComponentModel component : components)
			{
				int masterId = component.getMasterId();
				int componentId = component.getComponentMaster().getComponentId();
				
				result.put(masterId, componentId, component);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets all the component data in this object as a flat list.
	 * @return  All the component data in the object.
	 */
	public List<ComponentModel> getAllComponentsAsList()
	{
		List<ComponentModel> result = new ArrayList<ComponentModel>();
		for(List<ComponentModel> components : componentsById.values())
			result.addAll(components);
		
		return result;
	}

	/**
	 * Gets only the components for a given unit master or loadsheet sequence.
	 * @param masterId The ID of the unit master or loadsheet for which to get components.
	 * @return Only the components for the given unit master, keyed by component ID.
	 */
	public Map<Integer, ComponentModel> getComponentsForMasterId(int masterId)
	{
		Map<Integer, ComponentModel> result = new HashMap<Integer, ComponentModel>();
		for(List<ComponentModel> components : componentsById.values())
		{
			for(ComponentModel component : components)
			{
				int componentId = component.getComponentMaster().getComponentId();
				if(component.getMasterId() == masterId)
					result.put(componentId, component);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets all the components being processed by the rules engine that have the given component ID.
	 * @param componentId The component ID for which to get components.
	 * @return The list of all components being processed that have the given component ID.
	 * 	This method will return an empty list if there are no components with the given ID being processed.
	 */
	public List<ComponentModel> getComponentsForComponentId(int componentId)
	{
		List<ComponentModel> results = componentsById.get(componentId);
		return results == null ? Collections.<ComponentModel>emptyList() : Collections.unmodifiableList(results);
	}

	/**
	 * Gets a single component for a given unit master and component ID.
	 * @param componentId The component ID of the component to find.
	 * @param masterId The master ID of the component to find.
	 * @return The component with matching master ID and component ID, if one exists, or null if no such component exists.
	 */
	public ComponentModel getSingleComponent(int componentId, int masterId)
	{
		List<ComponentModel> components = getComponentsForComponentId(componentId);
		if(components == null)
			return null;
		for(ComponentModel component : components)
		{
			if(component.getMasterId() == masterId)
				return component;
		}
		
		return null;
	}
	
	/**
	 * Gets a component with the given component ID and master ID that is available to the rules engine.
	 * 	The results of this method are dependent on whether or not this object was configured so that rules should ignore invisible components.
	 * @param componentId The component ID to look up a model for.
	 * @param masterId The master ID of the component to find.
	 * @return The component available to the rules engine with the given component ID and master ID.
	 */
	public ComponentModel getSingleComponentForRules(int componentId, int masterId)
	{
		ComponentModel component = getSingleComponent(componentId, masterId);
		if(component == null)
			return null;

		//If the component is not visible, then it may or may not be ignored by the rules. Use the notVisibleBehavior to decide what to do
		if(!component.isVisible())
		{
			switch(notVisibleBehavior)
			{
			case KEEP:
				//If not visible components are going to be kept around, then this method should return a value
				break;
			case REMOVE:
				//If not visible components are going to be removed, then treat this as though it has no value
				return null;
			case REMOVE_IF_USER_INFLUENCED:
				//If the component's visibility was actually affected by the user changes, then treat it as though it's been removed.
				if(this.isComponentVisibilityChanged(masterId, componentId))
					return null;
				break;
			default: throw new IllegalStateException("Unrecognized behavior if components are not visible: " + notVisibleBehavior);
			}
		}
		
		//If the component isn't editable, then rules should use the original value, not the new one
		if(!component.isEditable())
		{
			//FIXME: test this behavior - unit template rules engine, when someone uploads a changed vehicle make for example
			ComponentValue originalValue = originalValues.get(masterId, componentId);
			return new ComponentModel(component.getComponentMaster(), originalValue, component.getRuleVisibility());
		}
		//If the component is editable, then rules should use the new value
		else
			return component;
	}

	/**
	 * Checks if a component's visibility before user changes were made is different from after user changes were made.
	 * @param masterId The master ID of the component to find.
	 * @param componentId The component ID to look up.
	 * @return True if the component's visibility after user changes are applied is different from the visibility before user changes are applied.
	 * 	False if the visibility was not affected by user changes.
	 */
	public boolean isComponentVisibilityChanged(int masterId, int componentId)
	{
		ComponentModel compWithoutUserChanges = componentsWithoutUserChanges.getOrDefault(componentId, Collections.emptyList()).stream()
			.filter(comp -> comp.getMasterId() == masterId)
			.findAny()
			.orElse(null);
		
		if(compWithoutUserChanges == null)
			return false;
		
		ComponentModel compWithUserChanges = getSingleComponent(componentId, masterId);
		if(compWithUserChanges == null)
			return false;
		
		//Treat both not visible statuses as the same
		Visibility withoutUserChanges = compWithoutUserChanges.getFinalVisibility() == Visibility.NOT_VISIBLE_2 ? Visibility.NOT_VISIBLE : compWithoutUserChanges.getFinalVisibility();
		Visibility withUserChanges = compWithUserChanges.getFinalVisibility() == Visibility.NOT_VISIBLE_2 ? Visibility.NOT_VISIBLE : compWithUserChanges.getFinalVisibility();
		
		return withoutUserChanges != withUserChanges;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Integer getMarkerId()
	{
		return markerId;
	}

	public EvaluationResult getEvaluationResult()
	{
		return evaluationResult;
	}

	public int getRuleIterations()
	{
		return ruleIterations;
	}

	public List<String> getDebugInfo()
	{
		return debugInfo;
	}

	public NestedComponentMap<? extends ComponentValue> getOriginalValues()
	{
		return originalValues;
	}

	public NotVisibleBehavior getNotVisibleComponentBehavior()
	{
		return notVisibleBehavior;
	}

}
