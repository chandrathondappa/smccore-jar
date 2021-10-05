/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;

/**
 * Class under test: {@link EmailTemplate}
 */
public class EmailTemplateTest
{
	@Test
	public void shouldGetActualSubject()
	{
		EmailTemplate template = CoreTestUtil.createEmailTemplate(EmailTemplateType.DAILY_BATCH, "Hello, [USER_NAME], DCN [DCN_NUMBER] is ready", "Test body");
		String actualSubject = template.getActualSubject(Arrays.asList(
			Pair.of("[USER_NAME]", "Joe Smith"),
			Pair.of("[DCN_NUMBER]", "12345")
		));
		
		assertThat(actualSubject, is("Hello, Joe Smith, DCN 12345 is ready"));
	}
	
	@Test
	public void shouldgetActualBody()
	{
		EmailTemplate template = CoreTestUtil.createEmailTemplate(EmailTemplateType.DAILY_BATCH, "Test subject", "Hello, %, DCN % is ready");
		String actualBody = template.getActualBody(Arrays.asList(
			Pair.of("%", "Joe Smith"),
			Pair.of("%", "12345")
		));
		
		assertThat(actualBody, is("Hello, Joe Smith, DCN 12345 is ready"));
	}
}
