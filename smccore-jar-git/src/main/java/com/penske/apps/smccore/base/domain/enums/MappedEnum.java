/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;


/**
 * Represents an enum that is mapped to database fields, but the value in the DB is not the enum constant's "name()".
 * The value from {@link #getMappedValue()} will be used for translation both to and from database values.
 */
public interface MappedEnum
{
	/**
	 * Fetches the String value used to represent this enum constant in the database.
	 * @return The DB value for this enum constant.
	 */
	public String getMappedValue();
}
