/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;


/**
 * An object that has a unit number and can be sorted by it.
 * This might refer to a particular deliverable in a loadsheet, or else possibly a whole unit encompassing multiple deliverables on multiple loadsheets.
 * @see UnitRangeBuilder
 */
public interface SortableUnit
{
	/**
	 * Fetch the unit number associated with this unit.
	 * @return The unit number
	 */
	public String getUnitNumber();
}