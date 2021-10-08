/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

import com.penske.apps.smccore.base.domain.enums.CorpCode;
import com.penske.apps.smccore.base.util.SortableUnit;
import com.penske.apps.smccore.component.domain.ComponentValue;

/**
 * Adds the concept of a unit number and corp code to a component value. Used as a result from queries against the vehicle component file that fetch components for more than one unit number.
 */
public class CorpComponentValue extends ComponentValue implements SortableUnit
{
	/** The number of the unit this component belongs to */
	private String unitNumber;
	/** The corp this unit belongs to. Necessary for differentiating paired units. */
	private CorpCode corpCode;
	
	/** Null constructor - MyBatis only */
	protected CorpComponentValue() {}

	//***** DEFAULT ACCESSORS *****//
	@Override
	public String getUnitNumber()
	{
		return unitNumber;
	}

	public CorpCode getCorpCode()
	{
		return corpCode;
	}
}
