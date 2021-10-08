/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;
import com.penske.apps.smccore.base.util.DateUtil;
import com.penske.apps.smccore.base.util.Util;

/**
 * The different types of components that are defined within the system.
 */
public enum ComponentType implements MappedEnum
{
	/** The component's value is a text string. */
	TEXT("T") {
		@Override
		public String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull) {
			return StringUtils.defaultString(textValue);
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue)
		{
			return StringUtils.isBlank(textValue);
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue) {
			return StringUtils.equalsIgnoreCase(firstTextValue, secondTextValue);
		}
	},
	/** The component holds a comment (comments are just like text, but they can have a longer maximum field length) */
	COMMENT("C") {
		@Override
		public String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull) {
			return textValue;
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue)
		{
			return StringUtils.isBlank(textValue);
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue) {
			return StringUtils.equalsIgnoreCase(firstTextValue, secondTextValue);
		}
	},
	/** The component's value is a date - this is only used for SMC psuedo-components (derived from ojects other than actual components). It behaves very much like {@link #TEXT} */
	DATE("D") {
		@Override
		public String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull) {
			if(DateUtil.isDateIsTheAs400DefaultTime(dateValue))
				return null;
			else
				return DateUtil.formatDateUS(dateValue);
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue)
		{
			return dateValue == null || DateUtil.isDateIsTheAs400DefaultTime(dateValue);
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue) {
			//Treat 0001-01-01 as though it is null
			if(DateUtil.isDateIsTheAs400DefaultTime(firstDateValue))
				firstDateValue = null;
			if(DateUtil.isDateIsTheAs400DefaultTime(secondDateValue))
				secondDateValue = null;
			
			//To be equal, if one is null, they both have to be null
			if(firstDateValue == null && secondDateValue == null)
				return true;
			else if(firstDateValue == null || secondDateValue == null)
				return false;
			
			return DateUtils.isSameDay(firstDateValue, secondDateValue);
		}
	},
	/** The component's value is a number (possibly a decimal number) */
	NUMERIC("N")
	{
		@Override
		public String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull) {
			if(numericValue == null)
			    return allowNull? null : "";
			
			//Truncate the value we get out if it has more decimal digits than the permissible amount
			// We normally round most values, but the AS400 behaved this way for this case, and we are preserving that behavior here
			// because we're not sure what impact this will have on downstream systems or data correctness.
			if(decimalPositions != null)
				numericValue = numericValue.setScale(decimalPositions, RoundingMode.FLOOR);
			
			return numericValue.toPlainString();
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue)
		{
			return numericValue == null;
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue) {
			return Util.isEqualBigDecimals(firstNumericValue, secondNumericValue);
		}
	},
	/** The component's value is a year. If we get this one, we need to millennium-adjust it in some cases. */
	YEAR("Y")
	{
		@Override
		public String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull) {
			if(numericValue == null)
				return allowNull? null : "";
			
			//Years do not have decimal digits, so truncate them.
			return numericValue.setScale(0, RoundingMode.DOWN).toPlainString();
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue)
		{
			return numericValue == null;
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue) {
			return Util.isEqualBigDecimals(firstNumericValue, secondNumericValue);
		}
	},
	;

	/** The one-letter code used to represent this type in the database. */
	private final String code;

	/**
	 * Retrieves the component type based on its one-letter database code.
	 * @param code The one-letter code to find a component type for.
	 * @return The component type with the matching code; null if no such match found.
	 */
	public static ComponentType findByCode(String code)
	{
		for(ComponentType type : values())
		{
			if(type.code.equals(code))
				return type;
		}
		return null;
	}
	
	private ComponentType(String code)
	{
		this.code = code;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return getCode();
	}

	/**
	 * Computes the value of a component as a string, based on the component type.
	 * @param textValue The textual value of the component, if it is a text component.
	 * @param numericValue The numeric value of the component, if it is a numeric or year component.
	 * @param dateValue TODO
	 * @param decimalPositions The number of decimal places that the component value has, if this is a numeric component.
	 * @param allowNull flag that determines if the method is allowed to return a null or just and empty string ""
	 * @return The component's value, rendered as a string.
	 */
	public abstract String getComponentValueAsString(String textValue, BigDecimal numericValue, Date dateValue, Integer decimalPositions, boolean allowNull);
		
	public abstract boolean isValueEmpty(String textValue, BigDecimal numericValue, Date dateValue);
	
	public abstract boolean areValuesEqual(String firstTextValue, BigDecimal firstNumericValue, Date firstDateValue, String secondTextValue, BigDecimal secondNumericValue, Date secondDateValue);
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}
}
