/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * A document attached to a particular email
 */
public class SmcEmailDocument
{
	/** Internal database ID */
	private Integer docId;
	/** ID of the email this document is attached to */
	private int emailAuditId;
	/** The filename name of the document */
	private String documentName;
	/** The type of document attached */
	private String documentType;
	/** The ID in the Document Archive Service (DAS) system that points to the actual content of the file */
	private int dasId;
	
	/** Null constructor - MyBatis only */
	protected SmcEmailDocument() {}
	
	public SmcEmailDocument(SmcEmail email, String documentName, int dasId)
	{
		if(email == null)
			throw new IllegalArgumentException("Email is required to attach a document to it");
		if(email.getEmailAuditId() == null)
			throw new IllegalArgumentException("Email record must be saved before attachments can be added");
		if(StringUtils.isBlank(documentName))
			throw new IllegalArgumentException("Email attachment needs a name");
		
		this.emailAuditId = email.getEmailAuditId();
		this.documentName = documentName;
		this.documentType = "EMAIL_DOCS";
		this.dasId = dasId;
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{SmcEmailDocument - Name: " + documentName + ", DAS ID: " + dasId + "}";
	}
	
	//***** DEFAULT ACCESSORS *****//
	public Integer getDocId()
	{
		return docId;
	}

	public int getEmailAuditId()
	{
		return emailAuditId;
	}

	public String getDocumentName()
	{
		return documentName;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public int getDasId()
	{
		return dasId;
	}
}
