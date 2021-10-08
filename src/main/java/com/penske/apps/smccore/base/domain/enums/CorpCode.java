/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * Roughly speaking, a corp code identifies a country in the system.
 * 
 * This enum is mapped directly to values in database columns, using the {@link #code} field.
 */
public enum CorpCode implements MappedEnum
{
	USA("USA", "HPTL", "US", CurrencyCode.USD),
	CANADA("CANADA", "2000", "CA", CurrencyCode.CAD),
	MEXICO("MEXICO", "1800", "MX", CurrencyCode.USD),
	LOGISTICS("LOGISTICS", "3000", "US", CurrencyCode.USD);
	
	/** A human-readable description for this corp code, for display on screen or in Excel downloads. */
	private final String description;

	/** The actual code that is printed in the database for this corp code. */
	private final String code;
	
	/** The ISO 3166-1 country code for the country this corp is based from. */
	private final String countryCode;
	
	/** The currency used by this country. */
	private final CurrencyCode currencyCode;
	
	/**
	 * Look up a corp code by it's DB code.
	 * @param code The DB code to look up.
	 * @return The enum constant with matching DB code, or null if there is no such matching enum constant.
	 */
	public static CorpCode findByCode(String code)
	{
		for(CorpCode corpCode : values())
		{
			if(corpCode.code.equals(code))
				return corpCode;
		}
		
		return null;
	}
	
	/**
	 * Look up a corp code by its human-readable description, for parsing things like Excel uploads.
	 * NOTE: this should be used sparingly, since human-readable descrptions may change from time to time.
	 * 	Using {@link #findByCode(String)} is generally better, if you can do so.
	 * @param description The human-readable description to parse.
	 * @return The CorpCode with that description, if one exists; null otherwise.
	 */
	public static CorpCode findByDescription(String description)
	{
		for(CorpCode corpCode : values())
		{
			if(corpCode.description.equals(description))
				return corpCode;
		}
		
		return null;
	}
	
	private CorpCode(String description, String code, String countryCode, CurrencyCode currencyCode)
	{
		this.description = description;
		this.code = code;
		this.countryCode = countryCode;
		this.currencyCode = currencyCode;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return getCode();
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @return the currencyCode
	 */
	public CurrencyCode getCurrencyCode()
	{
		return currencyCode;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode()
	{
		return countryCode;
	}

	public String getDescription()
	{
		return description;
	}
}
