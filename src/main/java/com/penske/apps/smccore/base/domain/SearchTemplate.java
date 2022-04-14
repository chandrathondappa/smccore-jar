/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import com.penske.apps.smccore.base.domain.enums.SmcTab;

/**
 * A record representing a pre-configured search that the user can perform in one of the tabs of SMC.
 */
public class SearchTemplate
{
	/** Internal database ID */
	private int templateId;
	/** A more human-readable identifier for the template. */
	private String templateKey;
	/** The name of the search template, as displayed to the user on search screens. */
	private String templateName;
	/** The order in which the template appears on the list of templates */
	private int displaySequence;
	/** True if the template should be visible to Penske users; false if Penske users should not see the template. */
	private boolean visibleToPenske;
	/** True if the template should be visible to vendor users; false if vendor users should not see the template. */
	private boolean visibleToVendor;
	
	/** The tab this search template applies to. */
	private SmcTab tabKey;

	/** Null constructor - MyBatis only */
	protected SearchTemplate() {}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{SearchTemplate (" + templateKey + ") - Tab: " + tabKey + "}";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getTemplateId()
	{
		return templateId;
	}
	public String getTemplateKey()
	{
		return templateKey;
	}
	public String getTemplateName()
	{
		return templateName;
	}
	public int getDisplaySequence()
	{
		return displaySequence;
	}
	public boolean isVisibleToPenske()
	{
		return visibleToPenske;
	}
	public boolean isVisibleToVendor()
	{
		return visibleToVendor;
	}

	public SmcTab getTabKey()
	{
		return tabKey;
	}
}
