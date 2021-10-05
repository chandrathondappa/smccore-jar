package com.penske.apps.smccore.base.domain.enums;

import java.util.Arrays;
import java.util.List;

public enum SmcTab implements MappedEnum {
	// 
	ORDER_FULFILLMENT("TAB_OF"),
	ORDER_CONFIRMATION("TAB_OC"),
	PRODUCTION("TAB_PROD"),
	ADMIN_CONSOLE("TAB_ADMIN");
	
	/** The key that is stored in the database for this tab. */
	private final String tabKey;
	
	/**
	 * The tabs that have alerts generated for them
	 */
	public static final List<SmcTab> ALERT_TABS = Arrays.asList(ORDER_FULFILLMENT, ORDER_CONFIRMATION, PRODUCTION);
	
	/**
	 * Look up a tab by it's key.
	 * @param code The tab key to look up.
	 * @return The enum constant with matching tab key, or null if there is no such matching enum constant.
	 */
	public static SmcTab findByTabKey(String tabKey)
	{
		for(SmcTab tab : values())
		{
			if(tab.tabKey.equals(tabKey))
				return tab;
		}
		
		return null;
	}
	
	private SmcTab(String tabKey)
	{
		this.tabKey = tabKey;
	}

	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return getTabKey();
	}
	
	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the code
	 */
	public String getTabKey()
	{
		return tabKey;
	}
}
