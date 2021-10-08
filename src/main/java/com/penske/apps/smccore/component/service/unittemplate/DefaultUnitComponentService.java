/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.service.unittemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.base.util.BatchRunnable;
import com.penske.apps.smccore.base.util.Util;
import com.penske.apps.smccore.component.dao.unittemplate.UnitComponentDAO;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.GlobalConflictResolution;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleBuilder;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.domain.enums.ProgramComponent;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.CorpComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.GlobalComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.OptionalComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.SmcComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponent;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.UnitConflictResolver;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;
import com.penske.apps.smccore.component.engine.ComponentModel;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilter;
import com.penske.apps.smccore.component.engine.ComponentVisibilityFilterData;
import com.penske.apps.smccore.component.engine.NestedComponentMap;

/**
 * Implementation of UnitComponentService service.
 */
//FIXME: test (including with bogus unit number)
@Service
public class DefaultUnitComponentService implements UnitComponentService
{
	private static final int COMMENT_REQUIRED_RULE_ID = -1;
	private static final int MAX_UNIT_COMPONENT_SAVE_SIZE = 250;
	private static final int MAX_UNIT_COMPONENT_DELETE_SIZE = 300;
	
	private final UnitComponentDAO unitComponentDAO;
	
	@Autowired
	public DefaultUnitComponentService(UnitComponentDAO unitComponentDAO)
	{
		this.unitComponentDAO = unitComponentDAO;
	}
	
	//***** Lookup Methods *****//
	/** {@inheritDoc} */
	@Override
	public List<UnitMasterInfo> getUnitMasterInfo(String unitNumber)
	{
		if(StringUtils.isBlank(unitNumber))
			return Collections.emptyList();
		
		Map<String, List<UnitMasterInfo>> unitMasters = this.getUnitMasterInfoForMultipleUnitNumbers(Arrays.asList(unitNumber));
		if(unitMasters.size() > 1)
			throw new IllegalArgumentException("Can not lookup unit masters for more than one unit at a time with this method (unit number: " + unitNumber + ")");
			
		for(List<UnitMasterInfo> unitMastersForUnitNumber : unitMasters.values())
			return unitMastersForUnitNumber;
		
		return Collections.emptyList();
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, List<UnitMasterInfo>> getUnitMasterInfoForMultipleUnitNumbers(Collection<String> unitNumbers)
	{
		return getUnitMasterInfoInternal(unitNumbers, true);
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, List<UnitMasterInfo>> getUnitMasterInfoWithoutMissingCount(Collection<String> unitNumbers)
	{
		return getUnitMasterInfoInternal(unitNumbers, false);
	}

	/** {@inheritDoc} */
	@Override
	public NestedComponentMap<UnitComponentMaster> getUnitComponents(Collection<UnitMasterInfo> unitMasters, Map<Integer, GlobalComponentMaster> globalComponentAlreadyLoaded)
	{
		if(unitMasters == null)
			return NestedComponentMap.emptyMap();
		
		String unitNumber = null;
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			if(unitNumber == null)
				unitNumber = unitMaster.getUnitNumber();
			else if(!unitNumber.equals(unitMaster.getUnitNumber()))
				throw new IllegalArgumentException("Can not load unit components from more than one unit at a time (" + unitNumber + " vs. " + unitMaster.getUnitNumber() + ")");
		}
		
		//Since the logic for this is complex, we just delegate to the main method and pull the result out for a single unit number
		Map<String, Collection<UnitMasterInfo>> unitMasterMap = Collections.singletonMap(unitNumber, unitMasters);
		Map<String, NestedComponentMap<UnitComponentMaster>> componentMasters = getUnitComponentsForMultipleUnits(unitMasterMap, globalComponentAlreadyLoaded);
		
		NestedComponentMap<UnitComponentMaster> result = componentMasters.get(unitNumber);
		if(result == null)
			return NestedComponentMap.emptyMap();
		else
			return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, NestedComponentMap<UnitComponentMaster>> getUnitComponentsForMultipleUnits(Map<String, ? extends Collection<UnitMasterInfo>> unitMasters, Map<Integer, GlobalComponentMaster> globalComponentAlreadyLoaded)
	{
		if(unitMasters == null)
			return Collections.emptyMap();
		
		Map<Integer, UnitMasterInfo> unitMastersByMasterId = new HashMap<Integer, UnitMasterInfo>();
		List<UnitMasterInfo> unitMastersToLoad = new ArrayList<UnitMasterInfo>();
		List<UnitMasterInfo> unitMastersToRebuild = new ArrayList<UnitMasterInfo>();
		for(Collection<UnitMasterInfo> unitMastersForUnit : unitMasters.values())
		{
			for(UnitMasterInfo unitMaster : unitMastersForUnit)
			{
				//If template out of date and should allow updates, fetch from template master
				if(unitMaster.isTemplateOutOfDate())
					unitMastersToRebuild.add(unitMaster);
				//If up to date or template should not be updated, fetch from existing unit component template (from last run of the rules)
				else
					unitMastersToLoad.add(unitMaster);
				
				unitMastersByMasterId.put(unitMaster.getMasterId(), unitMaster);
			}
		}
		
		//Actually load the component master records
		List<UnitComponentMaster> masters = new ArrayList<UnitComponentMaster>();
		if(!unitMastersToLoad.isEmpty())
			masters.addAll(unitComponentDAO.getComponentMastersFromUnitComponent(unitMastersToLoad));
		if(!unitMastersToRebuild.isEmpty())
			masters.addAll(unitComponentDAO.getComponentMastersFromTemplate(unitMastersToRebuild));
		
		//Early exit, for optimization
		if(masters.isEmpty())
			return Collections.emptyMap();
		
		Map<String, NestedComponentMap<UnitComponentMaster>> result = new HashMap<String, NestedComponentMap<UnitComponentMaster>>();
		for(UnitComponentMaster master : masters)
		{
			int masterId = master.getMasterId();
			int componentId = master.getComponentId();
			UnitMasterInfo unitMaster = unitMastersByMasterId.get(masterId);
			if(unitMaster == null)
				continue;
			
			//Ensure that disabled components are never shown on any template, even the ones that are old and shouldn't be rebuilt.
			ProgramComponent programComponent = ProgramComponent.findByComponentId(master.getComponentId());
			if(ProgramComponent.getDisabledComponents().contains(programComponent))
				continue;
			
			String unitNumber = unitMaster.getUnitNumber();
			NestedComponentMap<UnitComponentMaster> componentsForUnitNumber = result.get(unitNumber);
			if(componentsForUnitNumber == null)
			{
				componentsForUnitNumber = NestedComponentMap.newInstance();
				result.put(unitNumber, componentsForUnitNumber);
			}
			
			componentsForUnitNumber.put(masterId, componentId, master);
		}
		
		//Ensure all the global components are in the template, too. If they aren't there already, then add them with NOT_VISIBLE visibility
		Map<Integer, GlobalComponentMaster> globalComponentMasters = getGlobalComponentMasters(globalComponentAlreadyLoaded);
		for(Entry<Integer, UnitMasterInfo> entry : unitMastersByMasterId.entrySet())
		{
			int masterId = entry.getKey();
			String unitNumber = entry.getValue().getUnitNumber();
			
			NestedComponentMap<UnitComponentMaster> componentsForUnitNumber = result.get(unitNumber);
			if(componentsForUnitNumber == null)
				continue;
			
			for(GlobalComponentMaster globalComponent : globalComponentMasters.values())
			{
				int componentId = globalComponent.getComponentId();
				if(componentsForUnitNumber.get(masterId, componentId) == null)
				{
					//Since the global component masters are pulled from the database with an incorrect master ID,
					//	we have to clone them and make one with the right template ID, or we'll get exceptions when running the rules engine.
					GlobalComponentMaster globalComponentForMasterId = new GlobalComponentMaster(globalComponent, masterId);
					componentsForUnitNumber.put(masterId, componentId, globalComponentForMasterId);
				}
			}
		}
		
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<Integer, GlobalComponentMaster> getGlobalComponentMasters(Map<Integer, GlobalComponentMaster> globalComponentsAlreadyLoaded)
	{
		if(globalComponentsAlreadyLoaded == null)
			globalComponentsAlreadyLoaded = Collections.emptyMap();
		
		Map<Integer, GlobalComponentMaster> result = new HashMap<Integer, GlobalComponentMaster>();
		List<Integer> componentsToLoad = new ArrayList<Integer>();
		for(ProgramComponent globalComponent : ProgramComponent.getGlobalComponents())
		{
			int componentId = globalComponent.getComponentId();
			GlobalComponentMaster component = globalComponentsAlreadyLoaded.get(componentId);
			if(component == null)
				componentsToLoad.add(componentId);
			else
				result.put(componentId, component);
		}
		
		if(!componentsToLoad.isEmpty())
		{
			List<GlobalComponentMaster> componentsLoaded = unitComponentDAO.getGlobalComponents(componentsToLoad, Visibility.NOT_VISIBLE);
			for(GlobalComponentMaster component : componentsLoaded)
				result.put(component.getComponentId(), component);
		}
		
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public NestedComponentMap<ComponentValue> getAllComponentValues(Collection<UnitMasterInfo> unitMasters)
	{
		if(unitMasters == null)
			return NestedComponentMap.emptyMap();
		
		//Ensure all passed-in unit masters have the same unit number, and pull out that unit number.
		UnitMasterInfo sampleUnitMaster = null;
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			if(sampleUnitMaster == null)
				sampleUnitMaster = unitMaster;
			else if(!sampleUnitMaster.getUnitNumber().equals(unitMaster.getUnitNumber()))
				throw new IllegalArgumentException("Can not load component values for multiple unit numbers at a time: " + sampleUnitMaster.getUnitNumber() + " vs. " + unitMaster.getUnitNumber() + ". Master ID: " + unitMaster.getMasterId());
		}
		String sampleUnitNumber = sampleUnitMaster.getUnitNumber();
		
		//Since the logic for this is complex, we just delegate to the main method and pull the result out for a single unit number
		Map<String, NestedComponentMap<ComponentValue>> valuesMap = getAllComponentValuesForMultipleUnits(Collections.singletonMap(sampleUnitNumber, unitMasters));
		
		NestedComponentMap<ComponentValue> result = valuesMap.get(sampleUnitNumber);
		if(result == null)
			return NestedComponentMap.emptyMap();
		else
			return result;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, NestedComponentMap<ComponentValue>> getAllComponentValuesForMultipleUnits(Map<String, ? extends Collection<UnitMasterInfo>> unitMasters)
	{
		if(unitMasters == null)
			return Collections.emptyMap();
		
		Map<Integer, UnitMasterInfo> unitMastersByMasterId = new HashMap<Integer, UnitMasterInfo>();
		for(Collection<UnitMasterInfo> unitMastersForUnitNumber : unitMasters.values())
		{
			if(unitMastersForUnitNumber == null)
				continue;
			
			for(UnitMasterInfo unitMaster : unitMastersForUnitNumber)
				unitMastersByMasterId.put(unitMaster.getMasterId(), unitMaster);
		}
		
		Map<String, NestedComponentMap<ComponentValue>> result = new HashMap<String, NestedComponentMap<ComponentValue>>();
		
		//Get SMC component values for all unit masters together
		List<SmcComponentValue> smcComponentValues = unitComponentDAO.getSmcComponentValues(unitMastersByMasterId.values());
		for(SmcComponentValue value : smcComponentValues)
		{
			int masterId = value.getMasterId();
			int componentId = value.getComponentId();
			UnitMasterInfo unitMaster = unitMastersByMasterId.get(masterId);
			if(unitMaster == null)
				continue;
			
			String unitNumber = unitMaster.getUnitNumber();
			NestedComponentMap<ComponentValue> valuesForUnitNumber = result.get(unitNumber);
			if(valuesForUnitNumber == null)
			{
				valuesForUnitNumber = NestedComponentMap.newInstance();
				result.put(unitNumber, valuesForUnitNumber);
			}
			
			valuesForUnitNumber.put(masterId, componentId, value);
		}
		
		//Get vehicle component values for each unit and corp once, and then add them to each unit master for their unit number
		List<CorpComponentValue> corpComponentValues = unitComponentDAO.getCorpComponentValues(unitMastersByMasterId.values());
		for(CorpComponentValue value : corpComponentValues)
		{
			int componentId = value.getComponentId();
			String unitNumber = value.getUnitNumber();
			Collection<UnitMasterInfo> unitMastersForUnitNumber = unitMasters.get(unitNumber);
			if(unitMastersForUnitNumber == null)
				continue;
			
			NestedComponentMap<ComponentValue> componentsForUnitNumber = result.get(unitNumber);
			if(componentsForUnitNumber == null)
			{
				componentsForUnitNumber = NestedComponentMap.newInstance();
				result.put(unitNumber, componentsForUnitNumber);
			}
			
			for(UnitMasterInfo unitMaster : unitMastersForUnitNumber)
			{
				int masterId = unitMaster.getMasterId();
				componentsForUnitNumber.put(masterId, componentId, value);
			}
		}
		
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Rule> getRules(Collection<UnitMasterInfo> unitMasters)
	{
		if(unitMasters == null)
			return Collections.emptyList();
		
		Set<Integer> templateIds = new HashSet<Integer>();
		for(UnitMasterInfo unitMaster : unitMasters)
			templateIds.add(unitMaster.getTemplateId());
		
		List<Rule> rules = unitComponentDAO.getRules(unitMasters, RuleType.UNIT_TEMPLATE);

		//Add rules that should apply to every template
		rules.addAll(getGlobalRules(templateIds));
		
		return rules;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, List<Rule>> getAllRulesByTemplateId()
	{
		List<Rule> rules = unitComponentDAO.getRules(null, RuleType.UNIT_TEMPLATE);
		
		Map<Integer, List<Rule>> result = new HashMap<Integer, List<Rule>>();
		for(Rule rule : rules)
		{
			int templateId = rule.getTemplateId();
			if(!result.containsKey(templateId))
				result.put(templateId, new ArrayList<Rule>());
			result.get(templateId).add(rule);
		}
		
		List<Rule> globalRules = getGlobalRules(result.keySet());
		for(Rule rule : globalRules)
		{
			int templateId = rule.getTemplateId();
			result.get(templateId).add(rule);
		}
		
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<Integer, List<RuleOutcome>> getOutcomesByMasterId(Collection<UnitMasterInfo> unitMasters)
	{
		if(unitMasters == null || unitMasters.isEmpty())
			return Collections.emptyMap();
		
		Map<Integer, List<Integer>> masterIdsByTemplateId = new HashMap<Integer, List<Integer>>();
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			int templateId = unitMaster.getTemplateId();
			if(!masterIdsByTemplateId.containsKey(templateId))
				masterIdsByTemplateId.put(templateId, new ArrayList<Integer>());
			masterIdsByTemplateId.get(templateId).add(unitMaster.getMasterId());
		}
		
		//Get the list of outcomes for the various unit masters
		List<RuleOutcome> outcomes = unitComponentDAO.getOutcomes(unitMasters);
		
		//Add a global outcome to each template to set comments to visibility R if the comment required rule fires
		outcomes.addAll(getGlobalOutcomes(masterIdsByTemplateId.keySet()));
		
		//Add each outcome to all of the unit masters that it pertains to (i.e. all the unit masters that use its template)
		Map<Integer, List<RuleOutcome>> result = new HashMap<Integer, List<RuleOutcome>>();
		for(RuleOutcome outcome : outcomes)
		{
			int templateId = outcome.getTemplateId();
			List<Integer> masterIds = masterIdsByTemplateId.get(templateId);
			if(masterIds == null)
				continue;
			for(Integer masterId : masterIds)
			{
				List<RuleOutcome> outcomesForMasterId = result.get(masterId);
				if(outcomesForMasterId == null)
				{
					outcomesForMasterId = new ArrayList<RuleOutcome>();
					result.put(masterId, outcomesForMasterId);
				}
				outcomesForMasterId.add(outcome);
			}
		}
		
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, List<RuleOutcome>> getAllOutcomesByTemplateId()
	{
		//Get all outcomes
		List<RuleOutcome> outcomes = unitComponentDAO.getOutcomes(null);
	
		//Index the outcomes by template ID
		Map<Integer, List<RuleOutcome>> result = new HashMap<Integer, List<RuleOutcome>>();
		for(RuleOutcome outcome : outcomes)
		{
			int templateId = outcome.getTemplateId();
			if(!result.containsKey(templateId))
				result.put(templateId, new ArrayList<RuleOutcome>());
			result.get(templateId).add(outcome);
		}
		
		//Add a set of global outcomes to each template
		List<RuleOutcome> globalOutcomes = getGlobalOutcomes(result.keySet());
		for(RuleOutcome globalOutcome : globalOutcomes)
		{
			int templateId = globalOutcome.getTemplateId();
			result.get(templateId).add(globalOutcome);
		}
		
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, List<GlobalConflictResolution>> getAllGlobalConflictResolutions()
	{
		List<GlobalConflictResolution> resolutions = unitComponentDAO.getGlobalConflictResolutions(null);
		Map<String, List<GlobalConflictResolution>> result = new HashMap<String, List<GlobalConflictResolution>>();
		for(GlobalConflictResolution resolution : resolutions)
		{
			String unitSignature = resolution.getUnitSignature();
			if(!result.containsKey(unitSignature))
				result.put(unitSignature, new ArrayList<GlobalConflictResolution>());
			result.get(unitSignature).add(resolution);
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<GlobalConflictResolution> getGlobalConflictResolutions(Collection<UnitMasterInfo> unitMasters)
	{
		if(unitMasters == null || unitMasters.isEmpty())
			return Collections.emptyList();
		
		return unitComponentDAO.getGlobalConflictResolutions(unitMasters);
	}

	//***** Update Methods *****//
	/** {@inheritDoc} */
	@Override
	@Transactional
	public void updateUnitComponents(Collection<UnitMasterInfo> unitMasters, ComponentVisibilityFilterData componentData, NestedComponentMap<? extends OptionalComponentValue> valuesAfterSave, final String ssoId)
	{
		//Determine which ones need to be rebuilt from template master, and which ones are up to date.
		String unitNumber = null;
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			if(unitNumber == null)
				unitNumber = unitMaster.getUnitNumber();
			else if(!unitNumber.equals(unitMaster.getUnitNumber()))
				throw new IllegalArgumentException("Can not save component data from more than one unit at a time (" + unitNumber + " vs. " + unitMaster.getUnitNumber() + ")");
		}
		
		Map<String, Collection<UnitMasterInfo>> unitMasterMap = Collections.singletonMap(unitNumber, unitMasters);
		Map<String, ComponentVisibilityFilterData> componentDataMap = Collections.singletonMap(unitNumber, componentData);
		Map<String, NestedComponentMap<? extends OptionalComponentValue>> valuesAfterSaveMap = new HashMap<String, NestedComponentMap<? extends OptionalComponentValue>>();
		valuesAfterSaveMap.put(unitNumber, valuesAfterSave);
		
		this.updateUnitComponentsForMultipleUnits(unitMasterMap, componentDataMap, valuesAfterSaveMap, null, ssoId);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Future<?>> updateUnitComponentsForMultipleUnits(
			Map<String, ? extends Collection<UnitMasterInfo>> unitMasters,
			Map<String, ComponentVisibilityFilterData> componentData,
			Map<String, NestedComponentMap<? extends OptionalComponentValue>> valuesAfterSave,
			ThreadPoolExecutor executor,
			final String ssoId)
	{
		if(valuesAfterSave == null)
			valuesAfterSave = Collections.emptyMap();
		
		Map<Integer, UnitMasterInfo> allUnitMasters = new HashMap<Integer, UnitMasterInfo>();
		for(Entry<String, ? extends Collection<UnitMasterInfo>> entry : unitMasters.entrySet())
		{
			for(UnitMasterInfo unitMaster : entry.getValue())
				allUnitMasters.put(unitMaster.getMasterId(), unitMaster);
		}
		
		//Grab previous saved unit component values.
		//For templates not being rebuilt, saved components come from component masters
		//For templates being rebuilt, query for saved components from DB
		Map<String, NestedComponentMap<UnitComponent>> savedUnitComponents = getSavedValues(allUnitMasters, componentData);
		
		List<UnitComponent> componentsToUpsert = new ArrayList<UnitComponent>();
		List<UnitComponent> componentsToDelete = new ArrayList<UnitComponent>();
		for(Entry<String, ? extends Collection<UnitMasterInfo>> entry : unitMasters.entrySet())
		{
			String unitNumber = entry.getKey();
			ComponentVisibilityFilterData componentDataForUnitNumber = componentData.get(unitNumber);
			NestedComponentMap<UnitComponent> savedUnitComponentsForUnitNumber = savedUnitComponents.get(unitNumber);
			if(savedUnitComponentsForUnitNumber == null)
				savedUnitComponentsForUnitNumber = NestedComponentMap.emptyMap();
			
			//If we don't have component data, don't continue any further
			if(componentDataForUnitNumber == null)
				continue;
			
			for(UnitMasterInfo unitMaster : entry.getValue())
			{
				int masterId = unitMaster.getMasterId();
				NestedComponentMap<? extends OptionalComponentValue> changedValues = valuesAfterSave.get(unitNumber);
				NestedComponentMap<? extends ComponentValue> originalValues = componentDataForUnitNumber.getOriginalValues();
				
				//Only send component information for unit masters that should be allowed to have their components updated.
				if(unitMaster.isUnitComponentUpdateAllowed())
				{
					//Mark the unit master as having had the rules run on it, so we can tell what time data was last written for it
					unitMaster.updateLastTemplateRefresh();
					
					Map<Integer, ComponentModel> componentsForMasterId = componentDataForUnitNumber.getComponentsForMasterId(masterId);
					for(ComponentModel component : componentsForMasterId.values())
					{
						if(component.getBaseVisibility() == null)
							throw new IllegalArgumentException("Base visibility may not be null: " + component);
						
						int componentId = component.getComponentId();
						boolean valueProvided = isValueProvided(component, originalValues, changedValues);
						
						UnitComponent savedUnitComponent = savedUnitComponentsForUnitNumber.get(masterId, componentId);
						UnitComponent unitComponent = new UnitComponent(component, valueProvided);
						if(unitComponent.isDifferentfrom(savedUnitComponent))
							componentsToUpsert.add(unitComponent);
					}
					
					for(UnitComponent unitComponent : savedUnitComponentsForUnitNumber.getComponentsForMasterId(masterId).values())
					{
						int componentId = unitComponent.getComponentId();
						if(componentsForMasterId.get(componentId) == null)
							componentsToDelete.add(unitComponent);
					}
				}
			}
		}
		
		//We always save the unit masters, since we have to mark the isTemplateProcessed flag as Y in the daily batch.
		//	Other applications don't need to update the unit master unless either the template hash or the unit signature are out of date. 
		unitComponentDAO.updateUnitMaster(allUnitMasters.values(), ssoId);

		//Delete the templates that need to be rebuilt
		if(!componentsToDelete.isEmpty())
		{
			new BatchRunnable<UnitComponent>(componentsToDelete, MAX_UNIT_COMPONENT_DELETE_SIZE) {
				@Override protected void runBatch(List<UnitComponent> items){
					unitComponentDAO.deleteOutdatedUnitComponents(items);
				}
			}.run();
		}
		
		List<UnitComponentSaveRunnable> saveRunnables = createSaveRunnables(unitMasters.keySet(), componentsToUpsert, ssoId);
		List<Future<?>> result = new ArrayList<Future<?>>();
		//Send merge statement in chunks for SMC_UNIT_COMPONENT records
		for(UnitComponentSaveRunnable runnable : saveRunnables)
		{
			if(executor == null)
				runnable.run();
			else
				result.add(executor.submit(runnable));
		}
		
		return result;
	}

	//***** Rules Engine Methods *****//
	/** {@inheritDoc} */
	@Override
	public ComponentVisibilityFilterData runRulesAndResolveConflicts(Collection<UnitMasterInfo> unitMasters, NestedComponentMap<String> userValues)
	{
		String unitNumber = null;
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			if(unitNumber == null)
				unitNumber = unitMaster.getUnitNumber();
			else if(!unitNumber.equals(unitMaster.getUnitNumber()))
				throw new IllegalArgumentException("Can not save component data from more than one unit at a time (" + unitNumber + " vs. " + unitMaster.getUnitNumber() + ")");
		}
		
		//Get lots of data out of the DB.
		NestedComponentMap<UnitComponentMaster> componentMasters = this.getUnitComponents(unitMasters, null);
		NestedComponentMap<ComponentValue> existingComponents = this.getAllComponentValues(unitMasters);
		List<Rule> rules = this.getRules(unitMasters);
		Map<Integer, List<RuleOutcome>> outcomes = this.getOutcomesByMasterId(unitMasters);
		List<GlobalConflictResolution> globalResolutions = this.getGlobalConflictResolutions(unitMasters);
		
		//Build a conflict resolver
		UnitConflictResolver resolver = new UnitConflictResolver(unitMasters, componentMasters, globalResolutions);
	
		//Actually run the rules and resolve conflicts
		ComponentVisibilityFilter filter = new ComponentVisibilityFilter();
		ComponentVisibilityFilterData componentData = filter.mergeComponentValues(null, componentMasters, existingComponents, null, userValues, NotVisibleBehavior.KEEP);
		componentData = filter.runRules(componentData, rules, outcomes);
		componentData = filter.resolveConflicts(componentData, resolver);
		
		return componentData;
	}

	/** {@inheritDoc} */
	@Override
	public String computeUnitSignature(Collection<UnitMasterInfo> unitMasters)
	{
		List<String> signatureParts = new ArrayList<String>();
		String unitNumber = null;
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			if(unitNumber == null)
				unitNumber = unitMaster.getUnitNumber();
			else if(!unitNumber.equals(unitMaster.getUnitNumber()))
				throw new IllegalArgumentException("Can not generate unit signature for multiple units at a time (" + unitNumber + " vs. " + unitMaster.getUnitNumber() + ")");
			
			//Each piece of information for a unit master is joined with pipes
			String signaturePart = new StringBuilder().append(unitMaster.getPoCategoryAssociationId()).append("|").append(unitMaster.getVendorId()).toString();
			signatureParts.add(signaturePart);
		}
	
		//All the parts of the unit signature are sorted so we always get the same signature for the same set of unit masters.
		Collections.sort(signatureParts);
		
		//The parts corresponding to individual unit masters are joined with commas
		return StringUtils.join(signatureParts, ",");
	}

	//***** HELPER METHODS *****//
	private List<Rule> getGlobalRules(Collection<Integer> templateIds)
	{
		if(templateIds == null)
			return Collections.emptyList();
		
		List<Rule> result = new ArrayList<Rule>();
		
		for(Integer templateId : templateIds)
		{
			//Add a global rule that fires if comment_required = "Y"
			result.add(new RuleBuilder(COMMENT_REQUIRED_RULE_ID, templateId, "Comment Required", RuleType.UNIT_TEMPLATE)
				.addCriteria(1, ProgramComponent.COMMENT_REQUIRED, ComponentRuleOperator.EQUAL, "Y")
				.build());
		}
		
		return result;
	}
	
	private List<RuleOutcome> getGlobalOutcomes(Collection<Integer> templateIds)
	{
		if(templateIds == null)
			return Collections.emptyList();
		
		List<RuleOutcome> result = new ArrayList<RuleOutcome>();
		for(Integer templateId : templateIds)
		{
			result.add(new RuleOutcome(templateId, COMMENT_REQUIRED_RULE_ID, ProgramComponent.COMMENTS, -1, Visibility.REQUIRED));
		}
		return result;
	}

	private Map<String, List<UnitMasterInfo>> splitUnitMastersByUnitNumber(List<UnitMasterInfo> unitMasters)
	{
		Map<String, List<UnitMasterInfo>> result = new HashMap<String, List<UnitMasterInfo>>();
		for(UnitMasterInfo unitMaster : unitMasters)
		{
			String unitNumber = unitMaster.getUnitNumber();
			if(!result.containsKey(unitNumber))
				result.put(unitNumber, new ArrayList<UnitMasterInfo>());
			result.get(unitNumber).add(unitMaster);
		}
		
		//Update the unit signature on each of the unit masters we have, to make sure we take
		//	into account any POs that were issued or voided since the last time rules was run.
		for(List<UnitMasterInfo> unitMastersForUnitNumber : result.values())
		{
			String newUnitSignature = computeUnitSignature(unitMastersForUnitNumber);
			for(UnitMasterInfo unitMaster : unitMastersForUnitNumber)
				unitMaster.updateCalculatedUnitSignature(newUnitSignature);
		}
		
		return result;
	}

	private List<UnitComponentSaveRunnable> createSaveRunnables(Collection<String> unitNumbers, List<UnitComponent> componentsToUpsert, String ssoId)
	{
		if(componentsToUpsert == null || componentsToUpsert.isEmpty())
			return Collections.emptyList();
		
		List<UnitComponentSaveRunnable> runnables = new ArrayList<UnitComponentSaveRunnable>();
		for(int i = 0 ; i < componentsToUpsert.size(); i += MAX_UNIT_COMPONENT_SAVE_SIZE)
		{
			int maxIndex = Math.min(componentsToUpsert.size(), i + MAX_UNIT_COMPONENT_SAVE_SIZE);
			List<UnitComponent> components = componentsToUpsert.subList(i, maxIndex);
			UnitComponentSaveRunnable runnable = new UnitComponentSaveRunnable(unitComponentDAO, components, ssoId, unitNumbers);
			
			runnables.add(runnable);
		}
		return runnables;
	}
	
	private boolean isValueProvided(ComponentModel componentModel, NestedComponentMap<? extends ComponentValue> originalValues, NestedComponentMap<? extends OptionalComponentValue> changedValues)
	{
		if(componentModel == null)
			throw new IllegalArgumentException("Component model is required");
		if(originalValues == null)
			originalValues = NestedComponentMap.emptyMap();
		if(changedValues == null)
			changedValues = NestedComponentMap.emptyMap();
		
		int masterId = componentModel.getMasterId();
		int componentId = componentModel.getComponentId();
		
		//First check in the changed component values. Then, if it's not there, check in the original component values
		OptionalComponentValue value = changedValues.get(masterId, componentId);
		if(value == null)
			value = originalValues.get(masterId, componentId);
		
		if(value == null || value.isValueEmpty())
			return false;
		else
			return true;
	}
	
	private Map<String, NestedComponentMap<UnitComponent>> getSavedValues(Map<Integer, UnitMasterInfo> allUnitMasters, Map<String, ComponentVisibilityFilterData> componentData)
	{
		Map<String, NestedComponentMap<UnitComponent>> result = new HashMap<String, NestedComponentMap<UnitComponent>>();
		
		Map<Integer, UnitMasterInfo> masterIdsToRebuild = new HashMap<Integer, UnitMasterInfo>();
		for(UnitMasterInfo unitMaster : allUnitMasters.values())
		{
			if(unitMaster.isTemplateOutOfDate())
				masterIdsToRebuild.put(unitMaster.getMasterId(), unitMaster);
		}
		
		//If a template is being rebuilt, the rules engine data is looking at the new template, not the old one, so we have to pull information about the old one from the
		// previously-saved unit component records.
		List<UnitComponent> savedComponents;
		if(masterIdsToRebuild.isEmpty())
			savedComponents = Collections.emptyList();
		else
			savedComponents = unitComponentDAO.getSavedUnitComponents(masterIdsToRebuild.values());
		for(UnitComponent savedComponent : savedComponents)
		{
			int componentId = savedComponent.getComponentId();
			int masterId = savedComponent.getMasterId();
			UnitMasterInfo unitMaster = allUnitMasters.get(masterId);
			if(unitMaster == null)
				continue;
			
			String unitNumber = unitMaster.getUnitNumber();
			if(result.get(unitNumber) == null)
				result.put(unitNumber, NestedComponentMap.<UnitComponent>newInstance());
			
			result.get(unitNumber).put(masterId, componentId, savedComponent);
		}
		
		//For templates not being rebuilt, the rules engine data is loaded from the existing template, which means we can just use the component masters that have already been loaded.
		for(Entry<String, ComponentVisibilityFilterData> entry : componentData.entrySet())
		{
			String unitNumber = entry.getKey();
			ComponentVisibilityFilterData componentDataForUnitNumber = entry.getValue();
			
			for(int componentId : componentDataForUnitNumber.getAllComponentIds())
			{
				for(ComponentModel componentModel : componentDataForUnitNumber.getComponentsForComponentId(componentId))
				{
					int masterId = componentModel.getMasterId();
					//If the template is being rebuilt, then the saved UnitComponents should have already come from the DB, not from the component masters, so skip those components
					if(masterIdsToRebuild.containsKey(masterId))
						continue;
					
					UnitComponentMaster componentMaster = componentModel.unwrapComponentMaster();
					UnitComponent savedComponent = componentMaster.getPreviousValues();
					
					if(savedComponent == null)
						continue;
					
					if(result.get(unitNumber) == null)
						result.put(unitNumber, NestedComponentMap.<UnitComponent>newInstance());
					
					result.get(unitNumber).put(masterId, componentId, savedComponent);
				}
			}
		}
		
		return result;
	}

	private Map<String, List<UnitMasterInfo>> getUnitMasterInfoInternal(Collection<String> unitNumbers, boolean fetchMissingCount)
	{
		if(unitNumbers == null || unitNumbers.isEmpty())
			return Collections.emptyMap();
		
		List<String> paddedUnitNumbers = new ArrayList<String>();
		for(String unitNumber : unitNumbers)
		{
			if(StringUtils.isBlank(unitNumber))
				continue;
			paddedUnitNumbers.add(Util.getPaddedUnitNumber(unitNumber));
		}
		
		if(paddedUnitNumbers.isEmpty())
			return Collections.emptyMap();

		List<UnitMasterInfo> unitMasters = unitComponentDAO.getUnitMasterInfo(paddedUnitNumbers, fetchMissingCount);
		Map<String, List<UnitMasterInfo>> result = splitUnitMastersByUnitNumber(unitMasters);
		
		return result;
	}
	
	//***** HELPER CLASSES *****//
	private static class UnitComponentSaveRunnable implements Runnable
	{
		private final UnitComponentDAO unitComponentDAO;
		private final List<UnitComponent> components;
		private final String ssoId;
		private final List<String> unitNumbers = new ArrayList<String>();

		public UnitComponentSaveRunnable(UnitComponentDAO unitComponentDAO, List<UnitComponent> components, String ssoId, Collection<String> unitNumbers)
		{
			this.unitComponentDAO = unitComponentDAO;
			this.components = components;
			this.ssoId = ssoId;
			this.unitNumbers.addAll(unitNumbers);
		}
		
		/** {@inheritDoc} */
		@Override
		public void run()
		{
			try {
				unitComponentDAO.upsertUnitComponents(components, ssoId);
			} catch(RuntimeException ex) {
				String message = "Error while saving unit component information: " + ex.getMessage() + ".\nUnit numbers: " + StringUtils.join(unitNumbers, ", ") + ".";
				throw new RuntimeException(message, ex);
			}
		}
	}
}