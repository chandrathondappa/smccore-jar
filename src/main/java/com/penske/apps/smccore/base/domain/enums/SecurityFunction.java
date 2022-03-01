/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Enum containing all the security functions recognized by the SMC system.
 * These are used to grant permissions to users to perform certain tasks
 */
public enum SecurityFunction
{
	//***** Order Fulfillment Security Functions *****//
	APPROVE_DCN,
	MANAGE_UNITS,
	ATTACH_FILES,
	DCN_ACTIONS,
	GET_NEXT_DEAL,
	IMMEDIATE_REVIEW,
	IMPORT_SPEC,
	MANAGE_LOADSHEET,
	MANAGE_LOADSHEET_TEMPLATES,
	ORDER_FULFILLMENT_TAB,
	PRIORITIZATION,
	SPEC_SEARCH,
	UPDATE_DEAL, 
	CHANGE_ORDERS,
	MASS_COMPONENT_UPDATE,
	APPROVE_COST_OVERAGE,	
	COMPLETE_TO_PEND,

	//***** Order Confirmation / Production Security Functions *****//
	ORDER_CONFIRMATION_TAB,
	EXPORT_DATA,
	DOWNLOAD_DOCUMENTS,
	DOWNLOAD_DOCUMENTS_PRODUCTION,
	CONFIRM_ORDER,
	ADD_CUSTOM_ORDER,
	ADD_CUSTOM_ORDER_PRODUCTION,
	UPDATE_DOCUMENTS,
	PRODUCTION_TAB,
	EDI_870_MENU,
	DATA_CONFLICT_MENU,
	UPDATE_DATES,
	UPDATE_HOLDS,
	UNIT_DETAIL_PAGE,
	MASS_UPDATE,
	ADD_UNIT_COMMENTS,
	DOWNLOAD_UNIT_TEMPLATES,
	MARK_REMOVE_HOT,
	PROVIDE_MISSING_INFORMATION,
	REMOVE_MISSING_INFO,
	REORDER_PROD_SUM_FIELDS,
	SHOW_NON_OEM_DATES,
	ATTACH_FILES_CONFIRMATION,
	VIEW_UNIT_RULES,

	//***** Home Screen / Admin Console Security Functions *****//
	ADMIN_CONSOLE_TAB,

	MANAGE_USERS,
	MANAGE_VENDOR_USERS,
	MANAGE_ROLES,
	MANAGE_VENDORS,
	MANAGE_ORG,

	MANAGE_CATEGORY_ASSOCIATION,
	MANAGE_CATEGORY,
	MANAGE_COMPONENTS,
	MANAGE_TEMPLATE,

	DYNAMIC_RULES_MANAGEMENT,
	SEARCH_TEMPLATES,
	ALERT_MANAGEMENT,
	GLOBAL_EXCEPTIONS_MANAGEMENT,
	MANAGE_TC,
	UPLOAD_EXCEL,
	VENDOR_FILTER,
	LOADSHEET_MANAGEMENT, 
	LOADSHEET_RULES, 
	LOADSHEET_SEQUENCES,
	COST_SHEET_ADJUSTMENT_OPTIONS,
	COST_SHEET_TOLERANCES,

	OEM_BUILD_MATRIX,
	OEM_BUILD_MATRIX_DEBUG,

	EXPORT_VENDOR_ACTIVITY;
	
	/** Reverse-lookup map to find a security function by its enum constant name */
	private static Map<String, SecurityFunction> functionsByName = Stream.of(SecurityFunction.values()).collect(toMap(SecurityFunction::name, x -> x));
	
	/**
	 * Looks up a security function by its enum constant name.
	 * @param name The name of the security function to look up
	 * @return The security function with the given name, if one exists. If no match found, returns null.
	 */
	public static SecurityFunction findByName(String name)
	{
		return functionsByName.get(name);
	}
}
