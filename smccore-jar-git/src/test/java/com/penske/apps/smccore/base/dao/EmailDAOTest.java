/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.MyBatisDaoTest;
import com.penske.apps.smccore.base.configuration.CoreConfiguration;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.base.domain.SmcEmail;
import com.penske.apps.smccore.base.domain.SmcEmailDocument;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;
import com.penske.apps.smccore.configuration.EmbeddedDataSourceConfiguration;

/**
 * Class under test: {@link EmailDAO}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CoreConfiguration.class, EmbeddedDataSourceConfiguration.class})
@SqlGroup({
	@Sql(scripts = "/setup/create-corp-schema.sql"),
	@Sql(scripts = "/setup/create-smc-schema.sql"),
	@Sql(scripts = "/setup/drop-corp-schema.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
	@Sql(scripts = "/setup/drop-smc-schema.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
@ActiveProfiles(ProfileType.TEST)
@Transactional
public class EmailDAOTest extends MyBatisDaoTest
{
	@Autowired
	private EmailDAO dao;
	
	@Before
	public void before()
	{
		dao = this.trackMethodCalls(dao, EmailDAO.class);
	}
	
	@Test
	public void shouldGetEmailTemplate()
	{
		dao.getEmailTemplate(EmailTemplateType.LOADER_PEND_COST);
	}
	
	@Test
	public void shouldInsertSmcEmail()
	{
		SmcEmail email = new SmcEmail("600555555", "test@penske.com", "testcc@penske.com", "testbcc@penske.com", "Test Body", "Test Subject");
		dao.insertSmcEmail(email);
	}
	
	@Test
	public void shouldInsertEmailDocuments()
	{
		SmcEmail email = new SmcEmail("600555555", "test@penske.com", "testcc@penske.com", "testbcc@penske.com", "Test Body", "Test Subject");
		CoreTestUtil.set(email, "emailAuditId", 555);
		
		List<SmcEmailDocument> docs = Arrays.asList(
			new SmcEmailDocument(email, "Questionnaire.pdf", 9876),
			new SmcEmailDocument(email, "SomeEmail.msg", 5555)
		);
		
		dao.insertEmailDocuments(docs, "600555555");
		
		for(SmcEmailDocument doc : docs)
			assertThat(doc.getDocId(), is(notNullValue()));
	}
}