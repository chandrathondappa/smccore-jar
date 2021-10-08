/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * The means by which a deliverable will get transported to its destination.
 */
public enum TransportationTypeEnum implements MappedEnum
{
	/** The OEM will move the vehicle to the next step (freight will be charged). */
	OEM("O"),
	/** A separate transporter will be employed to ship the vehicle. */
	TRANSPORTER("T"),
	/** The local Penske district will transport the vehicle. */
	LOCAL("L");
	
	/** Single-letter code used to represent this transportation type in the database. */
	private String code;

	/**
	 * Looks up an enum constant by its one-letter code.
	 * @param code The code to look up.
	 * @return The enum constant that has the given one-letter code, or null if no matching constant is found.
	 */
	public static TransportationTypeEnum findByCode(String code)
	{
		for(TransportationTypeEnum type : values())
		{
			if(type.code.equals(code))
				return type;
		}
		
		return null;
	}

	private TransportationTypeEnum(String code)
	{
		this.code = code;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return code;
	}
	
	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}
}
