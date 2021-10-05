/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import com.penske.apps.smccore.base.domain.enums.CorpCode;
import com.penske.apps.smccore.base.util.Util;

/**
 * An object that contains a unit number and corp code. This combination of information uniquely identifies a vehicle in the vehicle file.
 */
public interface VehicleIdentifier
{
	/** Gets the unit number, as it exists in the object (this might be left-padded and it might not, depending on how the object wants to treat it) */
	public String getUnitNumber();
	/** Gets the unit number, left-padded with enough spaces to match the standard AS400 formatting of unit numbers (10 characters as of 2020-01-20 */
	public default String getPaddedUnitNumber()
	{
		return Util.getPaddedUnitNumber(getUnitNumber());
	}
	/** Gets the corp code the unit belongs to - this can matter for paired units */
	public CorpCode getCorpCode();
}
