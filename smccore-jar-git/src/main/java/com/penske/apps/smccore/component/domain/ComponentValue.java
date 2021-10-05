/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.util.DateUtil;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.unittemplate.OptionalComponentValue;

/**
 * A single component on a single unit (ex: front axle serial number, gross vehicle weight, engin make, etc.)
 * Each of these objects is a concrete instance, for a particular template, of a component described by {@link ComponentMaster}. 
 */
public class ComponentValue implements OptionalComponentValue
{
	/** The 9-digit ID for the component. This is what identifies the component within Penske's systems. */
	private int componentId;
	/** The type of data contained in this component (text, number, date, year). */
	private ComponentType componentType;
	/** If the component type is text, this contains the value. */
	private String textValue = "";
	/** If the component type is a number or a year, this contains the value */
	private BigDecimal numericValue = null;
	/** If the component type is a date, this contains the value. */
	private Date dateValue = null;
	
	/** Null constructor - MyBatis only */
	protected ComponentValue() {}
	
	/**
	 * Make a new component value for the rules engine.
	 * @param master The master record that describes what this component represents.
	 * @param value The value of the component.
	 * 	If the master's component type represents a numeric component, this will be parsed as a number (and an exception thrown if the value is not parseable).
	 * 	If the master's component type represents a text component, this will be set as the value.
	 */
	//FIXME: test with dates, comments
	public ComponentValue(ComponentMaster master, String value)
	{
		if(master == null)
			throw new IllegalArgumentException("Component master record not found for SMC component");
		if(master.getComponentType() == null)
			throw new IllegalArgumentException("Component type is required: (component ID " + componentId + ")");
		
		this.componentId = master.getComponentId();
		this.componentType = master.getComponentType();
		
		switch(componentType)
		{
		case TEXT:
			//Component text values can only be 30 characters long - chop off the remainder.
			this.textValue = StringUtils.left(StringUtils.defaultString(StringUtils.upperCase(value)), 30);
			this.numericValue = null;
			this.dateValue = null;
			break;
		case COMMENT:
			//Comment values can only be 1000 characters long - chop off the remainder.
			//Don't upper-case comments
			this.textValue = StringUtils.left(StringUtils.defaultString(value), 1000);
			this.numericValue = null;
			this.dateValue = null;
			break;
		case NUMERIC:
		case YEAR:
			this.textValue = "";
			this.dateValue = null;
			if(StringUtils.isBlank(value))
				this.numericValue = null;
			else
			{
				Integer decimalPositions = master.getDecimalPositions();
			    try {
                    Number uservalue = DecimalFormat.getNumberInstance(java.util.Locale.US).parse(value);
                    BigDecimal numericValue = new BigDecimal(uservalue.toString());
                    if(decimalPositions != null)
                    	numericValue = numericValue.setScale(decimalPositions, RoundingMode.FLOOR);
                    this.numericValue = numericValue;
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Unable to set a non-numeric value in a numeric component (component ID " + this.componentId +", value '" + value + "')", e);
                }
			}
			break;
		case DATE:
			this.textValue = "";
			this.numericValue = null;
			if(StringUtils.isBlank(value))
				this.dateValue = null;
			else
			{
				Date theDate = DateUtil.parseDate(value);
				if(theDate == null)
					throw new IllegalArgumentException("Unable to set a non-date value in a date component (component ID " + this.componentId + ", value '" + value + "')");
				
				//Treat 0001-01-01 as though it's a null value
				if(DateUtil.isDateIsTheAs400DefaultTime(theDate))
					this.dateValue = null;
				else
					this.dateValue = theDate;
			}
			break;
		}
	}

	/**
	 * Creates a new component whose values are based off a source component value.
	 * @param source The component value to base this one off of.
	 */
	protected ComponentValue(ComponentValue source)
	{
		this.componentId = source.componentId;
		this.componentType = source.componentType;
		this.textValue = source.textValue;
		this.numericValue = source.numericValue;
		this.dateValue = source.dateValue;
	}
	
    @Override
    public String toString() 
    {
    	String className = this.getClass().getSimpleName();
    	String value = componentType == null ? "Empty component type!!" : componentType.getComponentValueAsString(textValue, numericValue, dateValue, 4, true);
        return "{" + className + " " + componentId + " (" + componentType + "): " + StringUtils.defaultString(value) + "}";
    }
	
	//**** MODIFIED ACCESSORS *****//
	/**
	 * Checks if the value of this component is empty or not. The definition of "empty" depends on the component type.
	 * @return True if the component is considered "empty". False if it is not.
	 */
	//FIXME: test with dates, comments
    @Override
    public boolean isValueEmpty()
	{
		return componentType.isValueEmpty(textValue, numericValue, dateValue);
	}
	
	/**
	 * Checks if this component has a value equal to some other component
	 * @param other The other component whose value should be compared to this one.
	 * @return True if both components are non-null, both components have the same component type,
	 * 	and both components have equal values (according to {@link ComponentType#areValuesEqual(String, BigDecimal, Date, String, BigDecimal, Date)}).
	 * 	Returns false otherwise.
	 */
	//FIXME: test with dates, comments
	public boolean isValueEqual(ComponentValue other)
	{
		if(other == null)
			return false;
		if(other.getComponentType() != componentType)
			return false;
		
		return componentType.areValuesEqual(textValue, numericValue, dateValue, other.getTextValue(), other.getNumericValue(), other.getDateValue());
	}
	
	/**
	 * Updates both the component value and the component type (they must always be in sync), using the values from another SMC component.
	 * Individual subclasses can call this method if necessary, but it isn't intended for public calls to the API.
	 * @param source The component to pull the value and component type from.
	 */
	//FIXME: test with dates, comments
	protected void updateValue(ComponentValue source)
	{
		if(source.getComponentId() != this.getComponentId())
			throw new IllegalArgumentException("Can not update component " + this.getComponentId() + " with a value from component " + source.getComponentId() + ". Component IDs must match.");
		
		ComponentType componentType = source.getComponentType();
		
		switch(componentType)
		{
		case TEXT:
			this.textValue = StringUtils.defaultString(StringUtils.upperCase(source.getTextValue()));
			this.numericValue = null;
			this.dateValue = null;
			break;
		case COMMENT:
			this.textValue = StringUtils.defaultString(source.getTextValue());
			this.numericValue = null;
			this.dateValue = null;
		case NUMERIC:
		case YEAR:
			this.textValue = "";
			this.numericValue = source.getNumericValue();
			this.dateValue = null;
			break;
		case DATE:
			this.textValue = "";
			this.numericValue = null;
			this.dateValue = source.getDateValue();
			break;
		}
		
		this.componentType = componentType;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getComponentId()
	{
		return componentId;
	}

	public ComponentType getComponentType()
	{
		return componentType;
	}

	public String getTextValue()
	{
		return textValue;
	}

	public BigDecimal getNumericValue()
	{
		return numericValue;
	}

	public Date getDateValue()
	{
		return dateValue;
	}
}