/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.dao.unittemplate;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.MyBatisDaoTest;
import com.penske.apps.smccore.base.configuration.CoreConfiguration;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponent;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;
import com.penske.apps.smccore.configuration.EmbeddedDataSourceConfiguration;

/**
 * Class under test: {@link UnitComponentDAO}
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
public class UnitComponentDAOTest extends MyBatisDaoTest
{
	private final String unitNumber = "    123456";
	private final String ssoId = "600555555";
	private final List<UnitMasterInfo> unitMasters = Arrays.asList(
		CoreTestUtil.createUnitMasterInfo(1, unitNumber, 101, 1001, 10001, "testUnitSignature"),
		CoreTestUtil.createUnitMasterInfo(2, unitNumber, 102, 1002, 10002, "testUnitSignature")
	);
	private final List<UnitComponent> unitComponents = Arrays.asList(
		CoreTestUtil.createUnitComponent(1, 16000024, Visibility.EDITABLE, Visibility.EDITABLE, Visibility.EDITABLE, null, true),
		CoreTestUtil.createUnitComponent(1, 12000018, Visibility.EDITABLE, Visibility.EDITABLE, Visibility.EDITABLE, null, true)
	);
	
	@Autowired
	private UnitComponentDAO unitComponentDAO;
	
	@Before
	public void setup()
	{
		unitComponentDAO = this.trackMethodCalls(unitComponentDAO, UnitComponentDAO.class);
	}
	
	@Test
	public void shouldGetUnitMasterInfo()
	{
		//Have to run both syntaxes
		unitComponentDAO.getUnitMasterInfo(Arrays.asList("1234", "5678"), false);
		unitComponentDAO.getUnitMasterInfo(Arrays.asList("1234", "5678"), true);
	}
	
	@Test
	public void shouldGetComponentMastersFromUnitComponent()
	{
		unitComponentDAO.getComponentMastersFromUnitComponent(unitMasters);
	}
	
	@Test
	public void shouldGetComponentMastersFromTemplate()
	{
		unitComponentDAO.getComponentMastersFromTemplate(unitMasters);
	}
	
	@Test
	public void shouldGetGlobalComponents()
	{
		unitComponentDAO.getGlobalComponents(Arrays.asList(1, 2, 3, 4), Visibility.NOT_VISIBLE);
	}
	
	@Test
	public void shouldGetCorpComponentValues()
	{
		unitComponentDAO.getCorpComponentValues(unitMasters);
	}
	
	@Test
	public void shouldGetSavedUnitComponents()
	{
		unitComponentDAO.getSavedUnitComponents(unitMasters);
	}
	
	@Test
	public void shouldGetRules()
	{
		unitComponentDAO.getRules(unitMasters, RuleType.UNIT_TEMPLATE);
	}
	
	@Test
	public void shouldGetOutcomes()
	{
		unitComponentDAO.getOutcomes(unitMasters);
	}
	
	@Test
	public void shouldGetGlobalConflictResolutions()
	{
		unitComponentDAO.getGlobalConflictResolutions(unitMasters);
	}
	
	@Test
	public void shouldUpdateUnitMaster()
	{
		unitComponentDAO.updateUnitMaster(unitMasters, ssoId);
	}
	
	@Test
	public void shouldMarkUnitMastersAsError()
	{
		unitComponentDAO.markUnitMastersAsError(Arrays.asList(unitNumber, "    987654"), ssoId);
	}
	
	@Test
	public void shouldDeleteOutdatedUnitComponents()
	{
		unitComponentDAO.deleteOutdatedUnitComponents(unitComponents);
	}
	
	@Test
	public void shouldUpsertUnitComponents()
	{
		unitComponentDAO.upsertUnitComponents(unitComponents, ssoId);
	}
}
