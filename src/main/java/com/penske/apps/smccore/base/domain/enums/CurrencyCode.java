/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * The currency that a PO can be paid in.
 * 
 * This enum is mapped directly to values in database columns, using the {@link #name()}.
 */
public enum CurrencyCode implements MappedEnum
{
	USD("USD"),
	CAD("CAD"),
	;
	
	private final String code;
	    
    private CurrencyCode(String code) {
        this.code = code;
    }
	
	/**
	 * Looks up a currency code enum constant by its name. Returns null if no match is found.
	 * @param name The name of the currency code to find.
	 * @return The enum constant with the matching currency code, or null if no matching code is found.
	 * Just because this method returns null doesn't mean the currency code is invalid. It just means there is
	 * 	no logic attached to the currency code.
	 */
	public static CurrencyCode findByName(String name)
	{
		for(CurrencyCode code : values())
		{
			if(code.name().equals(name))
				return code;
		}
		
		return null;
	}

    @Override
    public String getMappedValue() {
        return getCode();
    }
    
    //***** DEFAULT ACCESSORS *****//
    /**
     * @return the code
     */
    public String getCode() {
        return this.code;
    }
}
