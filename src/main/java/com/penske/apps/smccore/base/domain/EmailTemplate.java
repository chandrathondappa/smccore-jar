/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;

/**
 * Represents the text of an email and its subject line, without all the details that make it complete. It may contain placeholders.
 */
//FIXME: test
public class EmailTemplate
{
	/** Internal database ID */
	private int emailTemplateId;
	/** The name of the email template */
	private EmailTemplateType emailType;
	/** The subject line of the email - potentially with placeholders */
	private String subjectTemplate;
	/** The body of the email - potentially with placeholders */
	private String bodyTemplate;
	
	/** Null constructor - MyBatis only */
	protected EmailTemplate() {}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{Email Template (ID " + emailTemplateId + ") - Type: " + emailType + "}";
	}

	//***** HELPER METHODS *****//
	private String replacePlaceholders(String str, List<Pair<String, String>> placeholderReplacements)
	{
		if(StringUtils.isBlank(str))
			return "";
		if(placeholderReplacements == null)
			return str;
		
		//Perform the desired replacements on the string sequentially, replacing a single token each time
		for(Pair<String, String> replacement : placeholderReplacements)
			str = StringUtils.replace(str, replacement.getLeft(), replacement.getRight(), 1);
		
		return str;
	}
	
	//***** MODIFIED ACCESSORS *****//
	public String getActualSubject(List<Pair<String, String>> placeholderReplacements)
	{
		return replacePlaceholders(subjectTemplate, placeholderReplacements);
	}
	
	public String getActualBody(List<Pair<String, String>> placeholderReplacements)
	{
		return replacePlaceholders(bodyTemplate, placeholderReplacements);
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getEmailTemplateId()
	{
		return emailTemplateId;
	}

	public EmailTemplateType getEmailType()
	{
		return emailType;
	}

	public String getSubjectTemplate()
	{
		return subjectTemplate;
	}

	public String getBodyTemplate()
	{
		return bodyTemplate;
	}
}