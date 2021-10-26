/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * Represents a component ID that we have programming logic attached to.
 * This list is not an exhaustive list of all components in the system, but is rather just the ones we have program behavior attached to in at least one SMC application.
 * This enum does not implement {@link MappedEnum} because it does not represent a separate database field, but rather is just an in-code mapping for the component ID field.
 */
public enum ProgramComponent
{
	/** The actual date the unit master was delivered to its destination. If this is filled in, the unit master was in fact delivered. */
	ACTUAL_DELIVERY_DATE(		1),
	/** The actual date the unit master was produced by the vendor. If this is filled in, the unit master was in fact produced, although it may not have been delivered yet. */
	ACTUAL_PRODUCTION_DATE(		2),
	/** The date the vendor estimates the unit master will be delivered to its destination. */
	ESTIMATED_DELIVERY_DATE(	9),
	/** The date the vendor estimates the unit master will be finished being produced. */
	ESTIMATED_PRODUCTION_DATE(	10),
	/** The date Penske requested the unit master be delivered. */
	ORIGINAL_REQUESTED_DELIVERY_DATE(42),
	/** The date Penske requested the unit master be produced by. */
	ORIGINAL_REQUESTED_PRODUCTION_DATE(21),
	/** The name of the location the unit master should be delivered to. */
	DESTINATION_NAME(			6),
	/** The first line of the delivery address. */
	DESTINATION_ADDRESS_1(		38),
	/** The second line of the delivery address, if there is one. */
	DESTINATION_ADDRESS_2(		39),
	/** The city of the delivery address. */
	DESTINATION_CITY(			5),
	/** The state of the delivery address. */
	DESTINATION_STATE(			7),
	/** The postal code of the delivery address. */
	DESTINATION_ZIP(			41),
	/** A code identifying the keys for this vehicle, so we can get replacement keys from the dealer if they are lost. */
	KEY_CODE(					11),
	/** The Vehicle Identification Number for the unit. Stored in the Vehicle File */
	VIN(						32),
	/** The date the PO was issued */
	PO_DATE(					15),
	/** The date the PO was confirmed by the vendor. */
	CONFIRMED_DATE(				17),
	/** The Penske internal value signifying what the vehicle will be used for. */
	VEHICLE_USE(				28),
	/** The Penske human-readable number of the vendor the PO is issued to. This is not the same as the vendor ID, which is a database-only key. */
	VENDOR_NUMBER(				31),
	/** Human readable name of the vendor in Penske's systems */
	VENDOR_NAME(				30),
	/** The vendor's own number for the given unit master, so they can track it in their own systems. */
	VENDOR_ORDER_NUMBER(		33),
	/** Y/N flag indicating whether production for this unit master is on hold or not. */
	HOLD_FLAG(					34),
	/** Comments indicating why this unit master is on hold. */
	HOLD_COMMENTS(				35),
	/** The most recent comment made on this unit master. */
	COMMENTS(					37),
	/** Y/N flag indicating whether or not the vendor has to provide a comment on this unit master to be considered "in compliance" */
	COMMENT_REQUIRED(			46),
	PDI_CENTER_COMPLETED_DATE(  50),
	COMMENT_DATE(				51),
	/** The total unit tare weight for the vehicle */
	TOTAL_UNIT_TARE_WEIGHT(24095085),
	/** The body tare weight for the vehicle */
	BODY_TARE_WEIGHT(4095085),
	/** The chassis tare weight for the vehicle */
	CHASSIS_TARE_WEIGHT(8095085),
	/** The liftgate tare weight for the vehicle */
	LIFTGATE_TARE_WEIGHT(14095085),
	/** The reefer tare weight for the vehicle */
	REEFER_TARE_WEIGHT(16095085),
	;
	
	private final int componentId;
	
	/** Reverse lookup map, so we can optimize access to the components with programming attached. */
	private static final Map<Integer, ProgramComponent> reverseLookupMap = new HashMap<Integer, ProgramComponent>();
	/** The components that need to be on every template for rules and criteria to target. If they aren't on the template when it's loaded, they will be added with visibility N. */
	private static final Set<ProgramComponent> globalComponents = Collections.unmodifiableSet(new HashSet<ProgramComponent>(Arrays.asList(
		COMMENTS,
		COMMENT_REQUIRED
	)));
	/** The components relate to a units weight. Used in Mass Component Update */
	private static final Set<ProgramComponent> weightComponents = Collections.unmodifiableSet(new HashSet<ProgramComponent>(Arrays.asList(
		TOTAL_UNIT_TARE_WEIGHT,
		BODY_TARE_WEIGHT,
		CHASSIS_TARE_WEIGHT,
		LIFTGATE_TARE_WEIGHT,
		REEFER_TARE_WEIGHT
	)));
	/** A components relate to a units deliveryDate. Used for date hiding */
	private static final Set<ProgramComponent> deliveryDateComponents = Collections.unmodifiableSet(new HashSet<ProgramComponent>(Arrays.asList(
		ACTUAL_DELIVERY_DATE,
		ESTIMATED_DELIVERY_DATE,
		ORIGINAL_REQUESTED_DELIVERY_DATE
	)));
	/** A set of components that should no longer show up on any template, no matter whether the template is old and not subject to being rebuilt anymore or not. */
	private static final Set<ProgramComponent> disabledComponents = Collections.unmodifiableSet(new HashSet<ProgramComponent>(
		//Can add components here. Nothing here right now.
	));
	static {
		for(ProgramComponent comp : values())
			reverseLookupMap.put(comp.getComponentId(), comp);
	}
	
	private ProgramComponent(int componentId)
	{
		this.componentId = componentId;
	}

	public static ProgramComponent findByComponentId(int componentId)
	{
		return reverseLookupMap.get(componentId);
	}
	
	//***** DEFAULT ACCESSORS *****//
	public static Set<ProgramComponent> getGlobalComponents()
	{
		return globalComponents;
	}
	
	public static Set<ProgramComponent> getDisabledComponents()
	{
		return disabledComponents;
	}
	
	public static Set<ProgramComponent> getWeightComponents() {
		return weightComponents;
	}
	
	public static Set<ProgramComponent> getDeliveryDateComponents() {
		return deliveryDateComponents;
	}
	
	public int getComponentId()
	{
		return componentId;
	}
}
