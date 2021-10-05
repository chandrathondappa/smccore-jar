/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.engine.NestableComponentData;

/**
 * Adds the concept of a master ID to the {@link ComponentValue} class.
 * <br/><br/>
 * IMPORTANT NOTE: As of 2018-05-17, the only two SMC components that are of ComponentType NUMERIC are PO Number and Vendor Number, and both of those have 0 decimal places.
 * 	Therefore, we are setting the SMC_GET_UNIT_VEHICLE_COMPONENTS procedure so that NUM_VALUE is a type NUMERIC(15,0) column, not a NUMERIC(15,4). This should allow those two components
 * 	to default to having zero decimal places. However, this will only work as long as we don't have any numeric SMC components with decimal places. If that should happen, the correct way to
 * 	fix this would be to add a DECIMAL_PLACES column to SMC_COMPONENT_INFO_DETAILS that would act as a fallback if there were no VEHCMPSG2F record for a given component (as is the case for
 * 	most of the SMC components). 
 */
public class SmcComponentValue extends ComponentValue implements NestableComponentData
{
	private int masterId;

	/** Null constructor - MyBatis only */
	protected SmcComponentValue() {}
	
	/** {@inheritDoc} */
	@Override
	public int getMasterId()
	{
		return masterId;
	}
}