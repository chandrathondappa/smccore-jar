/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.penske.apps.smccore.base.beans.LookupManager;
import com.penske.apps.smccore.base.dao.LookupDAO;
import com.penske.apps.smccore.base.service.CoreServiceMarker;
import com.penske.apps.smccore.component.service.ComponentServiceMarker;
import com.penske.apps.smccore.component.service.unittemplate.UnitComponentServiceMarker;
import com.penske.apps.smccore.search.service.CoreSearchServiceMarker;

/**
 * Main configuration class for the SMC Core JAR
 */
@Configuration
@Import({CoreMapperConfiguration.class})
@ComponentScan(basePackageClasses={
	UnitComponentServiceMarker.class,
	ComponentServiceMarker.class,
	CoreServiceMarker.class,
	CoreSearchServiceMarker.class,
})
public class CoreConfiguration
{
	@Autowired
	private LookupDAO lookupDAO;
	
	@Bean
	@Profile(ProfileType.NOT_TEST)
	public LookupManager lookupManager()
	{
		return new LookupManager(lookupDAO);
	}
}
