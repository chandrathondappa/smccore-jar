/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * The types of vehicles that can be represented in the vehicle file.
 * This usually maps more-or-less to the PO category of the "Chassis" on a unit
 */
public enum VehicleCategory implements MappedEnum
{
	TRUCK(PoCategoryType.TRUCK),
	TRACTOR(PoCategoryType.TRACTOR),
	TRAILER(PoCategoryType.TRAILER),
	CAR(PoCategoryType.CAR),
	BODY(PoCategoryType.BODY),
	OTHER(PoCategoryType.REEFER),
	;
	
	/** The type of PO that would probably need to be issued for the chassis if this type of vehicle is selected */
	private final PoCategoryType poCategory;
	
	private VehicleCategory(PoCategoryType poCategory)
	{
		this.poCategory = poCategory;
	}
	
	//***** MODIFIED ACCESSORS *****//
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return name();
	}

	//***** DEFAULT ACCESSORS *****//
	public PoCategoryType getPoCategory()
	{
		return poCategory;
	}
}
