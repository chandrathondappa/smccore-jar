/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum representing the different PO Categories that the loadsheet application recognizes.
 * This is <b>not</b> an exhaustive list, since PO Categories are data-driven and user-maintainable.
 * This is just a list of the ones that the Loadsheet application cares about.
 * 
 * Again, having a null value for this enum does not mean the thing in question has no PO Category, just
 * that, if it does have one, it isn't in this list.
 */

public enum PoCategoryType implements MappedEnum
{
				//SMC Name		//Salesnet Name	//MFR Code	//Default FET Rate
	TRUCK(		"TRUCK",		"TRUCK",		"C",		new BigDecimal("0.1248")),
	TRACTOR(	"TRACTOR",		"TRACTOR",		"C",		new BigDecimal("0.1248")),
	TRAILER(	"TRAILER",		"TRAILER",		"C",		new BigDecimal("0.12")),
	CAR(		"CAR", 			null,			"C",		new BigDecimal("0.1248")),
	DOLLY(		"DOLLY", 		"DOLLY",		"C",		new BigDecimal("0.1248")),
	REEFER(		"REEFER",		"COOLING-UNIT",	"R",		new BigDecimal("0.1248")),
	BODY(		"BODY",			"BODY",			"B",		new BigDecimal("0.1248")),
	DEMOUNT(	"DEMOUNT",		null,			"B",		new BigDecimal("0.1248")),
	LIFTGATE(	"LIFTGATE",		"LIFTGATE",		"L",		new BigDecimal("0.1248")),
	FIVE_WHEEL(	"5WHEEL",		null,			"5",		new BigDecimal("0.1248")),
	MISC(		"MISC",			null,			"M",		new BigDecimal("0.1248")),
	TIRES(		"TIRES",		null,			"X",		new BigDecimal("0.1248")),
	DECAL(		"DECAL", 		null,			null,		new BigDecimal("0.1248")),
	OTHER(		"OTHER", 		null,			null,		new BigDecimal("0.1248")),
	NONE(		"NONE",			null,			null,		new BigDecimal("0.1248"));
	
	/**
	 * The PO Categories that should be checked for freight costs.
	 * Loadsheets that don't have one of these categories will not have their freight amount validated.
	 */
	private static final List<PoCategoryType> FREIGHT_PO_CATEGORIES = Arrays.asList(TRUCK, TRACTOR, TRAILER, BODY);
	
	/**
     * The PO categpries where the PO Category on the loadsheet needs to match that of the vehicle master file.
     */
    private static final List<PoCategoryType> VEHICLE_MASTER_CATEGORIES = Arrays.asList(TRUCK, TRACTOR, TRAILER, CAR, DOLLY, DEMOUNT);
	
    /** The PO Categories that count as "chasses" in the SalseNet system */
    public static final Set<PoCategoryType> SALESNET_CHASSIS_CATEGORIES = Collections.unmodifiableSet(new HashSet<PoCategoryType>(Arrays.asList(
    	TRUCK, TRACTOR
    )));
    
    /** The PO Categories that count as "bodies" or "trailers" in the SalseNet system */
    public static final Set<PoCategoryType> SALESNET_BODY_TRAILER_CATEGORIES = Collections.unmodifiableSet(new HashSet<PoCategoryType>(Arrays.asList(
    	BODY, TRAILER, DOLLY
    )));
    
    /** The PO Categories that are counted as having wheels */
    public static final Set<PoCategoryType> CATEGORIES_WITH_WHEELS = Collections.unmodifiableSet(new HashSet<PoCategoryType>(Arrays.asList(
    	TRUCK, TRACTOR, TRAILER, DOLLY
    )));
    
    /**
     * A list of PO categories for which specs and loadsheets may not be created, and for which POs may not be cut.
     * They are still here in the application so that historical data can show the correct values.
     */
    private static final List<PoCategoryType> DISABLED_CATEGORIES = Arrays.asList(NONE, OTHER);
    
	/**
	 * Static map to speed lookups by the PO Category's name.
	 */
	private static final Map<String, PoCategoryType> typesByCategoryName = new HashMap<String, PoCategoryType>();
	/**
	 * Static map to speed lookups by the PO Category's name in SalesNet.
	 */
	private static final Map<String, PoCategoryType> typesBySalesnetName = new HashMap<String, PoCategoryType>();
	/**
	 * The name of the PO Category, as stored in the database.
	 * We need a separate name because some of the names are not valid Java identifiers.
	 */
	private final String poCategoryName;
	/**
	 * The vehicle category as it shows up in the SalesNet system.
	 */
	private final String salesnetVehicleCategory;
	/**
	 * The one-letter code that will be used in the manufacturer table if a vendor is able to produce specs of this PO Category.
	 * This is also the one-letter suffix of the the field name itself. For example, a manufacturer that can produce Chassis, will have
	 * a "C" in the field "MFTYPC".
	 */
	private final String mfrFieldCode;
	/** The FET rate that should be used in the event the spec isn't from the configurator. */
	private final BigDecimal defaultFETRate;
	
	static {
		//Initialize the lookup map at class load time
		for(PoCategoryType type : values())
		{
			typesByCategoryName.put(type.getPoCategoryName(), type);
			if(type.getSalesnetVehicleCategory() != null)
				typesBySalesnetName.put(type.getSalesnetVehicleCategory(), type);
		}
	}
	
	/**
	 * Looks up a PoCategoryType by its database name. <b>This method can return null.</b>
	 * @param poCategoryName The database name of the PoCategoryType to lookup.
	 * @return The PoCategoryType that matches the given name.
	 * 	{@code null} will be returned if the given name is null, or if no enum constant matches the given name.
	 * 	NOTE: just because null is returned does not mean there is no such PoCategory or that the thing in question has no PoCategory.
	 * 	It just means the Loadsheet application doesn't have logic pertaining to such a PO Category. 
	 */
	public static PoCategoryType findTypeByName(String poCategoryName)
	{
		if(poCategoryName == null)
			return null;
		return typesByCategoryName.get(poCategoryName);
	}
	
	/**
	 * Looks up a PoCategoryType by its name in the salesnet database. <b>This method can return null.</b>
	 * @param salesnetName The name from the salesnet database to look for.
	 * @return The PoCategoryType that matches the given name.
	 * 	{@code null} will be returned if the given name is null, or if no enum constant matches the given name.
	 * 	NOTE: just because null is returned does not mean there is no such PoCategory or that the thing in question has no PoCategory.
	 * 	It just means the SMC application doesn't have logic pertaining to such a PO Category.
	 */
	public static PoCategoryType findTypeBySalesnetName(String salesnetName)
	{
		return typesBySalesnetName.get(StringUtils.upperCase(salesnetName));
	}
	
	private PoCategoryType(String poCategoryName, String salesnetVehicleCategory, String mfrFieldCode, BigDecimal defaultFETRate)
	{
		this.poCategoryName = poCategoryName;
		this.salesnetVehicleCategory = salesnetVehicleCategory;
		this.mfrFieldCode = mfrFieldCode;
		this.defaultFETRate = defaultFETRate;
	}

	//***** MODIFIED ACCESSORS *****//
	/**
	 * Checks if a loadsheet with the given PO Category requires a non-zero freight cost or not.
	 * @param categoryType The PO Category Type to check
	 * @return True if loadsheets with the PO Category require freight; false otherwise.
	 */
	public static boolean isFreightRequired(PoCategoryType categoryType)
	{
		return PoCategoryType.FREIGHT_PO_CATEGORIES.contains(categoryType);
	}
	
	/**
     * Checks if a category type requires a matching vehicle file category in its loadsheets.
     * @param categoryType The PO Category Type to check
     * @return True if loadsheets with the PO Category require the PO category to match between vehicle file and loadsheet; false otherwise.
     */
    public static boolean isVehicleMasterCategoryMatchRequired(PoCategoryType categoryType)
    {
        return PoCategoryType.VEHICLE_MASTER_CATEGORIES.contains(categoryType);
    }
	
    /**
     * Checks if a given category type should not be able to be used to create specs or loadsheets, or to issue POs.
     * @param categoryType The PO Category type to check.
     * @return True if the category type is disabled (i.e. not allowed). False if it is allowed.
     */
    public static boolean isCategoryDisabled(PoCategoryType categoryType)
    {
    	return DISABLED_CATEGORIES.contains(categoryType);
    }
    
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the poCategoryName
	 */
	public String getPoCategoryName()
	{
		return poCategoryName;
	}

	/**
	 * @return the mfrFieldCode
	 */
	public String getMfrFieldCode()
	{
		return mfrFieldCode;
	}

	public String getSalesnetVehicleCategory()
	{
		return salesnetVehicleCategory;
	}

    @Override
    public String getMappedValue() {
        return poCategoryName;
    }

	public BigDecimal getDefaultFETRate()
	{
		return defaultFETRate;
	}
}
