/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.penske.apps.smccore.base.annotation.MappedEnumTypes;
import com.penske.apps.smccore.base.annotation.qualifier.CoreDataSourceQualifier;
import com.penske.apps.smccore.base.annotation.qualifier.VendorQueryWrappingPluginQualifier;
import com.penske.apps.smccore.base.dao.CoreMapperMarker;
import com.penske.apps.smccore.base.domain.ConfirmationAlertData;
import com.penske.apps.smccore.base.domain.CoreTypeAliasMarker;
import com.penske.apps.smccore.base.domain.DocTypeMaster;
import com.penske.apps.smccore.base.domain.EmailTemplate;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.LookupCacheInfo;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.LookupItem;
import com.penske.apps.smccore.base.domain.ProductionAlertData;
import com.penske.apps.smccore.base.domain.SecurityFunctionView;
import com.penske.apps.smccore.base.domain.SmcAlert;
import com.penske.apps.smccore.base.domain.SmcEmail;
import com.penske.apps.smccore.base.domain.SmcEmailDocument;
import com.penske.apps.smccore.base.domain.SmcVendorView;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.UserLogin;
import com.penske.apps.smccore.base.domain.UserSecurity;
import com.penske.apps.smccore.base.domain.VehicleIdentifier;
import com.penske.apps.smccore.base.domain.enums.AlertType;
import com.penske.apps.smccore.base.domain.enums.BuddySelectionType;
import com.penske.apps.smccore.base.domain.enums.CorpCode;
import com.penske.apps.smccore.base.domain.enums.CurrencyCode;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;
import com.penske.apps.smccore.base.domain.enums.LookupKey;
import com.penske.apps.smccore.base.domain.enums.MappedEnum;
import com.penske.apps.smccore.base.domain.enums.PayableStatus;
import com.penske.apps.smccore.base.domain.enums.PoCategoryType;
import com.penske.apps.smccore.base.domain.enums.PoStatus;
import com.penske.apps.smccore.base.domain.enums.SecurityFunction;
import com.penske.apps.smccore.base.domain.enums.SmcTab;
import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.base.domain.enums.VehicleCategory;
import com.penske.apps.smccore.base.plugins.ClientInfoPlugin;
import com.penske.apps.smccore.base.plugins.QueryLoggingPlugin;
import com.penske.apps.smccore.base.util.SpringConfigUtil;
import com.penske.apps.smccore.component.dao.unittemplate.UnitComponentMapperMarker;
import com.penske.apps.smccore.component.domain.ComponentMaster;
import com.penske.apps.smccore.component.domain.ComponentTypeAliasMarker;
import com.penske.apps.smccore.component.domain.ComponentValue;
import com.penske.apps.smccore.component.domain.GlobalConflictResolution;
import com.penske.apps.smccore.component.domain.Rule;
import com.penske.apps.smccore.component.domain.RuleBuilder;
import com.penske.apps.smccore.component.domain.RuleCriteria;
import com.penske.apps.smccore.component.domain.RuleCriteriaGroup;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.UnitDates;
import com.penske.apps.smccore.component.domain.enums.CommentRequiredComplianceType;
import com.penske.apps.smccore.component.domain.enums.ComponentRuleOperator;
import com.penske.apps.smccore.component.domain.enums.ComponentType;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.NotVisibleBehavior;
import com.penske.apps.smccore.component.domain.enums.ProgramComponent;
import com.penske.apps.smccore.component.domain.enums.RuleType;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.CorpComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.GlobalComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.OptionalComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.SmcComponentValue;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponent;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponentMaster;
import com.penske.apps.smccore.component.domain.unittemplate.UnitConflictResolver;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;
import com.penske.apps.smccore.typehandlers.CoreTypeHandlerMarker;

/**
 * Sets up MyBatis configuration for the SMC core JAR. Expects that there will be a DataSource bean available for autowiring.
 * Also, leaves the decision as to whether to enable transaction management up to the parent applications's configuration.
 */
@Configuration
@MapperScan(basePackageClasses={UnitComponentMapperMarker.class, CoreMapperMarker.class}, sqlSessionFactoryRef="coreSessionFactory")
@MappedEnumTypes({
	//Common enums for all of SMC
	CorpCode.class, CurrencyCode.class, EmailTemplateType.class, PayableStatus.class, PoStatus.class, TransportationTypeEnum.class, UserDepartment.class, UserType.class, BuddySelectionType.class,
	SmcTab.class, AlertType.class, VehicleCategory.class, PoCategoryType.class,
	//Component-specific enums
	ComponentType.class, Visibility.class, ComponentRuleOperator.class, RuleType.class, ConflictStatus.class,
	//Calculated values for unit masters
	CommentRequiredComplianceType.class,
})
public class CoreMapperConfiguration
{
	@Autowired
	@CoreDataSourceQualifier
	private DataSource dataSource;
	
	@Autowired(required=false)
	private QueryLoggingPlugin queryLoggingPlugin;
	
	@Autowired(required=false)
	@VendorQueryWrappingPluginQualifier
	private Interceptor vendorQueryWrappingPlugin;
	
	@Autowired(required=false)
	private ClientInfoPlugin clientInfoPlugin;
	
	//2019-01-15 - JPS - These are necessary to spell out here, but I'm not completely sure why.
	// the CoreTypeAliasMarker type alias package marker doesn't seem to work. It works on local development,
	// but it fails to find the classes in the corresponding package when deployed to QA. As of now, we still don't know why it does this.
	public static final List<Class<?>> ALIAS_CLASSES;
	
	static {
		Class<?>[] classes = {
			//Domain classes
			ConfirmationAlertData.class,
			DocTypeMaster.class,
			EmailTemplate.class,
			FulfillmentAlertData.class,
			LookupCacheInfo.class,
			LookupContainer.class,
			LookupItem.class,
			ProductionAlertData.class,
			SecurityFunctionView.class,
			SmcAlert.class,
			SmcEmail.class,
			SmcEmailDocument.class,
			SmcVendorView.class,
			User.class,
			UserSecurity.class,
			UserLogin.class,
			VehicleIdentifier.class,
			
			//Component Domain classes
			ComponentMaster.class,
			ComponentValue.class,
			GlobalConflictResolution.class,
			Rule.class,
			RuleBuilder.class,
			RuleCriteria.class,
			RuleCriteriaGroup.class,
			RuleOutcome.class,
			UnitDates.class,
			
			//Unit Template Domain classes
			CorpComponentValue.class,
			GlobalComponentMaster.class,
			OptionalComponentValue.class,
			SmcComponentValue.class,
			UnitComponent.class,
			UnitComponentMaster.class,
			UnitConflictResolver.class,
			UnitMasterInfo.class,
			
			//Enums
			AlertType.class,
			BuddySelectionType.class,
			CorpCode.class,
			CurrencyCode.class,
			EmailTemplateType.class,
			LookupKey.class,
			MappedEnum.class,
			PayableStatus.class,
			PoCategoryType.class,
			PoStatus.class,
			SecurityFunction.class,
			SmcTab.class,
			TransportationTypeEnum.class,
			UserDepartment.class,
			UserType.class,
			VehicleCategory.class,
			
			//Component Enums
			CommentRequiredComplianceType.class,
			ComponentRuleOperator.class,
			ComponentType.class,
			ConflictStatus.class,
			NotVisibleBehavior.class,
			ProgramComponent.class,
			RuleType.class,
			Visibility.class,
			
		};
		ALIAS_CLASSES = Arrays.asList(classes);
	}
	
	@Bean
	public SqlSessionFactory coreSessionFactory() throws Exception
	{
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		sessionFactory.setConfiguration(configuration);
		
		//***** TYPE ALIASES *****//
		Set<String> typeAliasPackages = SpringConfigUtil.getPackageNames(
			ComponentTypeAliasMarker.class,
			CoreTypeAliasMarker.class		//Currently, this does not find any type alias classes, but I'm not sure why. See comment above on ALIAS_CLASSES
		);
		
		//***** GLOBAL TYPE HANDLERS *****//
		Set<String> typeHandlerPackages = SpringConfigUtil.getPackageNames(
			CoreTypeHandlerMarker.class
		);
		
		//***** MYBATIS PLUGINS *****//
		List<Interceptor> interceptors = new ArrayList<Interceptor>();
		if(queryLoggingPlugin != null)			interceptors.add(queryLoggingPlugin);
		if(vendorQueryWrappingPlugin != null)	interceptors.add(vendorQueryWrappingPlugin);
		if(clientInfoPlugin != null)			interceptors.add(clientInfoPlugin);
		
		//***** MAPPER FILES *****//
		//We have to use "classpath*" here because these mapper files will be in JARs in most SMC applications
		PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		String basePath = "classpath*:conf/xml/mapper/smccore/";
		List<String> mapperFileNames = Arrays.asList(
			"unit-component-mapper.xml",
			"lookup-mapper.xml",
			"email-mapper.xml",
			"user-mapper.xml",
			"alerts-mapper.xml"
		);
		
		List<Resource> mapperLocations = SpringConfigUtil.getMapperFileResources(patternResolver, basePath, mapperFileNames);
		
		//***** ENUM TYPE HANDLER CONFIGURATION *****//
		//Register a custom enum type handler for all the enum types listed in the @MappedEnumTypes annotation on the following classes.
		List<Class<?>> mappedTypes = SpringConfigUtil.getMappedEnumTypes(
			CoreMapperConfiguration.class
		);
		
		//***** GENERAL CONFIG *****//
		sessionFactory.setTypeAliases(ALIAS_CLASSES.toArray(new Class<?>[]{}));
		sessionFactory.setTypeAliasesPackage(StringUtils.join(typeAliasPackages, ","));
		sessionFactory.setTypeHandlersPackage(StringUtils.join(typeHandlerPackages, ","));
		sessionFactory.setPlugins(interceptors.toArray(new Interceptor[interceptors.size()]));
		sessionFactory.setMapperLocations(mapperLocations.toArray(new Resource[mapperLocations.size()]));
		
		SpringConfigUtil.registerMappedEnumTypeHandlers(configuration.getTypeHandlerRegistry(), mappedTypes);
		
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setAggressiveLazyLoading(false);
		configuration.setLazyLoadTriggerMethods(new HashSet<String>());
		configuration.setMultipleResultSetsEnabled(true);
		configuration.setUseGeneratedKeys(false);
		configuration.setDefaultStatementTimeout(25000);
		
		SqlSessionFactory result = sessionFactory.getObject();
		return result;
	}
}
