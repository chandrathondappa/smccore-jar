/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.service.unittemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.GlobalConflictResolution;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.unittemplate.GlobalComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.OptionalComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.NestedComponentMap;

/**
 * A service for performing common operations related to the component rules engine in the unit templates module.
 */
public interface UnitComponentService
{
	//***** Lookup Methods *****//
	/**
	 * Looks up the basic information about the unit masters for the given unit number for use by the rules engine.
	 * @param unitNumber The unit number to get all unit masters for.
	 * @return All the unit masters that should be visible to the rules engine (i.e. that have templates and are not voided or replaced)
	 */
	public List<UnitMasterInfo> getUnitMasterInfo(String unitNumber);
	
	public Map<String, List<UnitMasterInfo>> getUnitMasterInfoForMultipleUnitNumbers(Collection<String> unitNumbers);

	/**
	 * Looks up the basic information about the unit masters for the given unit number for use by the rules engine, but each one has a missing info count set to 0.
	 * 	This also does not return accurate information for the template hash or actual delivery date, so things that depend on those will be wrong.
	 * 	IMPORTANT: this method should not be called in most cases. Usually, you want to use {@link #getUnitMasterInfoForMultipleUnitNumbers(Collection)} or {@link #getUnitMasterInfo(String)}.
	 * 		If this method is used when running rules or pulling up templates, inaccurate information may result, including pulling the wrong set of CompnentMasters for the template.
	 * 		If you plan to use this UnitMasterInfo to run rules, update SMC_UNIT_COMPONENT, or fetch ComponentMaster objects, do not use this method.
	 * 		This method does have faster performance, though, in cases where the missing info count is not needed.
	 * @param unitNumbers The unit numbers to get all unit masters for.
	 * @return All the unit masters that should be visible to the rules engine, with their missing count set to 0.
	 */
	public Map<String, List<UnitMasterInfo>> getUnitMasterInfoWithoutMissingCount(Collection<String> unitNumbers);
	
	/**
	 * Looks up the component master data for each of the given unit masters.
	 * @param unitMasters The unit master records for the templates to load from the existing template data.
	 * @param globalComponentAlreadyLoaded Optional. Component master information for all the components that need to be on every template.
	 * 	If this is not provided, or if there is a global component that isn't in here, then it will be looked up from the database.
	 * 	This parameter is just provided to allow the program to optimize by looking up global component masters ahead of time and not doing it over and over
	 * 	When invoking this method for a single unit, this doesn't generally need to be provided.
	 * @return All the component masters that define what is on the given unit masters' templates.
	 */
	public NestedComponentMap<UnitComponentMaster> getUnitComponents(Collection<UnitMasterInfo> unitMasters, Map<Integer, GlobalComponentMaster> globalComponentAlreadyLoaded);
	
	//FIXME: document
	public Map<String, NestedComponentMap<UnitComponentMaster>> getUnitComponentsForMultipleUnits(Map<String, ? extends Collection<UnitMasterInfo>> unitMasters, Map<Integer, GlobalComponentMaster> globalComponentAlreadyLoaded);
	
	/**
	 * Gets the full set of global component masters records from the database.
	 * Global components are ones that are included in every template, regardless of how the business configures them.
	 * @param globalComponentsAlreadyLoaded An already-loaded set of component masters for the global components. If this already contains an entry for every global component,
	 * 	then no database calls will be made. Keyed by component ID
	 * @return The components that should be available on every template, keyed by component ID.
	 */
	public Map<Integer, GlobalComponentMaster> getGlobalComponentMasters(Map<Integer, GlobalComponentMaster> globalComponentsAlreadyLoaded);
	
	/**
	 * Get all the component values for a given set of unit masters, all from the same unit.
	 * 	This includes both the SMC-specific component values, as well as the unit-wide values from the vehicle component file (CORP.VEHCMPF).
	 * 	The SMC-specific components may have different values for each unit master, so the results are categorized by both template ID and component ID.
	 * @param unitMasters The unit masters for which to look up SMC component values. These must all be for the same unit, or the results will be incorrect.
	 * @return The component values, keyed by template ID and then by component ID.
	 */
	public NestedComponentMap<ComponentValue> getAllComponentValues(Collection<UnitMasterInfo> unitMasters);
	
	//FIXME: test
	public Map<String, NestedComponentMap<ComponentValue>> getAllComponentValuesForMultipleUnits(Map<String, ? extends Collection<UnitMasterInfo>> unitMasters);
	
	/**
	 * Get the component rules that apply to the templates for the given set of unit masters.
	 * @param unitMasters The unit masters for which to look up rules. The rules returned are based on the template ID of each unit master.
	 * @return The list of applicable rules for the given unit masters.
	 */
	public List<Rule> getRules(Collection<UnitMasterInfo> unitMasters);
	
	/**
	 * Get all the unit component rules in the system, keyed by the ID of the template they are attached to.
	 * @return All the unit component rules in the system, keyed by template ID.
	 */
	public Map<Integer, List<Rule>> getAllRulesByTemplateId();
	
	/**
	 * Get the rule outcomes that apply to the templates for the given set of unit masters.
	 * @param unitMasters The unit masters for which to look up rule outcomes. The outcomes returned are based on the template ID of each unit master.
	 * @return The list of applicable rule outcomes for the given unit masters.
	 */
	public Map<Integer, List<RuleOutcome>> getOutcomesByMasterId(Collection<UnitMasterInfo> unitMasters);

	/**
	 * Gets all the unit component rule outcomes in the system, keyed by the ID of the template they apply to.
	 * @return All the unit component rule outcomes in the system, keyed by template ID.
	 */
	public Map<Integer, List<RuleOutcome>> getAllOutcomesByTemplateId();
	
	/**
	 * Gets all the global conflict resolutions in the system.
	 * @return All global conflict resolutions, keyed by unit signature.
	 */
	public Map<String, List<GlobalConflictResolution>> getAllGlobalConflictResolutions();
	
	/**
	 * Looks up global conflict resolutions that could potentially apply to the given unit masters because they have the same unit signature.
	 * @param unitMasters If this is empty or null, no resolutions will be fetched.
	 * 	Only resolutions that could apply to the given unit masters will be fetched.
	 * @return Only the resolutions that could apply to the given unit masters (i.e. that have the same unit signature)
	 */
	public List<GlobalConflictResolution> getGlobalConflictResolutions(Collection<UnitMasterInfo> unitMasters);
	
	//***** Update Methods *****//
	/**
	 * Save the unit components after running the rules, and update unit master records to have the correct template hashes and unit signatures
	 * @param unitMasters The unit masters that should have their unit components updated, but not completely overwritten.
	 * @param componentData The component data that results from running the rules engine.
	 * @param valuesAfterSave Optional. The component values that were changed after the save. This is used to determine whether a value has been provided for a given component.
	 * 	If this is null (or if a component can't be located in this object for a given master ID and component ID), the save routine will look for the value in the original set of component values contained in {@literal componentData}.
	 * 	If no values changed during the operation, it is appropriate to pass null into this argument.
	 * @param ssoId The person doing the action
	 */
	public void updateUnitComponents(Collection<UnitMasterInfo> unitMasters, ComponentVisibilityFilterData componentData, NestedComponentMap<? extends OptionalComponentValue> valuesAfterSave, String ssoId);
	
	//FIXME: document
	public List<Future<?>> updateUnitComponentsForMultipleUnits(
			Map<String, ? extends Collection<UnitMasterInfo>> unitMasters,
			Map<String, ComponentVisibilityFilterData> componentData,
			Map<String, NestedComponentMap<? extends OptionalComponentValue>> valuesAfterSave,
			ThreadPoolExecutor executor, String ssoId
	);
	
	//***** Rules Engine Methods *****//
	/**
	 * Loads component data for the given unit masters, runs the rules, and resolves conflicts.
	 * Since this runs a bunch of queries to get all the data, it is suitable for processes operating on a single unit at a time,
	 * 	but is pretty inefficient for running rules on multiple units in batch.
	 * Better performance can be achieved by using the more granular methods in this service and keeping results that can be reused between steps.
	 * This method does not save anything to the database.
	 * @param unitMasters The units the components belong to.
	 * @param userValues Optional. The user-submitted values for the components.
	 * @return A pair whose left side is the results of running the rules engine and resolving conflicts,
	 * 	and whose right side is the set of component values the unit originally had in the database before the rules were run.
	 */
	public ComponentVisibilityFilterData runRulesAndResolveConflicts(Collection<UnitMasterInfo> unitMasters, NestedComponentMap<String> userValues);

	
	/**
	 * Generates a unit signature based on a given set of unit masters. Does not update those unit masters.
	 * @param unitMasters The unit masters which go to comprise the unit signature.
	 * @return The computed unit signature (a combination of PO Category/Subcategory Association ID and Vendor ID)
	 */
	public String computeUnitSignature(Collection<UnitMasterInfo> unitMasters);
}
