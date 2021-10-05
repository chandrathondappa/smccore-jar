/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes a type of document that can be uploaded to the SMC system and attached to a DCN, Schedule A, spec, PO, etc.
 */
public class DocTypeMaster
{
	/** Internal ID for the record */
	private Integer docTypeId;
	/** Which category of document types this belongs to. Examples are SALESNET, USERDOCS, ORDERDOCS, etc. This is used by business logic or queries for some things. */
	private String docGroupName;
	/** The name of the document type. Documents coming from SalesNet will be categorized by matching this value with the filename (minus the extension). */
	private String docType;
	/** True if Penske SMC users can delete or upload documents of this kind directly from SMC. */
	private boolean editable;
	
	/** MyBatis only */
	protected DocTypeMaster() {}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{DocType " + StringUtils.substring(docType, 0, 30) + " (" + docGroupName + ")}";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Integer getDocTypeId()
	{
		return docTypeId;
	}

	public String getDocGroupName()
	{
		return docGroupName;
	}

	public String getDocType()
	{
		return docType;
	}

	public boolean isEditable()
	{
		return editable;
	}
}
