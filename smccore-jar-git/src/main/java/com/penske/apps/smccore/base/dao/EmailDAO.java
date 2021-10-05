package com.penske.apps.smccore.base.dao;

import java.util.Collection;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.domain.EmailTemplate;
import com.penske.apps.smccore.base.domain.SmcEmail;
import com.penske.apps.smccore.base.domain.SmcEmailDocument;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;

/**
 * Contains queries for creating and sending email messages from SMC through the smcnotify batch application
 */
public interface EmailDAO
{
	/**
	 * Looks up an email template from the database
	 * @param emailType The name identifying the email template to look up
	 * @return The email template with the matching type
	 */
	public EmailTemplate getEmailTemplate(@Param("emailType") EmailTemplateType emailType);
	
	/**
	 * Inserts an email message to be sent through the smcnotify batch application.
	 * @param email The email to be sent
	 */
	public void insertSmcEmail(@Param("email") SmcEmail email);

	/**
	 * Inserts references to documents that should be attached to an email
	 * @param documents The document references to add
	 */
	public void insertEmailDocuments(@Param("list") Collection<SmcEmailDocument> documents, @Param("creatorSso") String creatorSso);
}
