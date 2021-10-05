/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.dao.unittemplate;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.annotation.NonVendorQuery;
import com.penske.apps.smccore.base.annotation.SkipQueryTest;
import com.penske.apps.smccore.base.domain.VehicleIdentifier;
import com.penske.apps.smccore.component.domain.GlobalConflictResolution;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.CorpComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.GlobalComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.SmcComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponent;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;

/**
 * Contains queries for the component rules engine, for use with the unit template module.
 */
//FIXME: document
//FIXME: determine which ones are non-vendor queries
public interface UnitComponentDAO
{
	//***** SELECT QUERIES *****//
	/**
	 * Looks up header information from the unit master records for use by the rules engine.
	 * @param unitNumbers The unit numbers to look up data for.
	 * @param fetchMissingCount True to include an accurate count of missing information in the result set; false to set the missing count to 0.
	 * 	This also does not return accurate information for the template hash or actual delivery date, so things that depend on those will be wrong.
	 * 	IMPORTANT: this should be set to "true" in most cases. If this is set to "false", inaccurate information may result, including pulling the wrong set of CompnentMasters for the template.
	 * 		If you plan to use this UnitMasterInfo to run rules, update SMC_UNIT_COMPONENT, or fetch ComponentMaster objects, this must be set to true.
	 * 		Setting it to false can improve performance, though, in cases where the missing info count is not needed.
	 * @return The unit master header information for the given units.
	 */
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<UnitMasterInfo> getUnitMasterInfo(@Param("unitNumbers") Collection<String> unitNumbers, @Param("fetchMissingCount") boolean fetchMissingCount);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<UnitComponentMaster> getComponentMastersFromUnitComponent(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<UnitComponentMaster> getComponentMastersFromTemplate(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<GlobalComponentMaster> getGlobalComponents(@Param("componentIds") Collection<Integer> componentIds, @Param("notVisibleConstant") Visibility notVisibleConstant);

	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<CorpComponentValue> getCorpComponentValues(@Param("vehicleIds") Collection<? extends VehicleIdentifier> vehicleIds);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	@SkipQueryTest("Uses a stored procedure, which HSQLDB doesn't support")
	public List<SmcComponentValue> getSmcComponentValues(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<UnitComponent> getSavedUnitComponents(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<Rule> getRules(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters, @Param("unitComponentRuleType") RuleType unitComponentRuleType);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<RuleOutcome> getOutcomes(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public List<GlobalConflictResolution> getGlobalConflictResolutions(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters);
	
	//***** UPDATE / INSERT QUERIES *****//
	//FIXME: test that the rules engine only updates signatures for ones that are allowed to be updated
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public void updateUnitMaster(@Param("unitMasters") Collection<UnitMasterInfo> unitMasters, @Param("ssoId") String ssoId);

	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public void markUnitMastersAsError(@Param("unitNumbers") Collection<String> unitNumbers, @Param("ssoId") String ssoId);
	
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public void deleteOutdatedUnitComponents(@Param("unitComponents") Collection<UnitComponent> unitComponents);
	
	//FIXME: test - with missing componentValue, too
	@NonVendorQuery("The rules engine has to consider information from all POs on the unit, even from other vendors, in order to run rules correctly.")
	public void upsertUnitComponents(@Param("components") Collection<UnitComponent> components, @Param("ssoId") String ssoId);
}
