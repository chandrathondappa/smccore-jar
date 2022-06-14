/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import com.penske.apps.smccore.base.annotation.qualifier.CoreDataSourceQualifier;
import com.penske.apps.smccore.base.configuration.ProfileType;

/**
 * Contains Spring configuration necessary for setting up an in-memory database for unit testing.
 */
@Configuration
@Profile(ProfileType.TEST)
public class EmbeddedDataSourceConfiguration
{
    @Bean
    @CoreDataSourceQualifier
    public DataSource smcDataSource() {
    	
        EmbeddedDatabase datasource = new EmbeddedDatabaseBuilder()
        	.setType(EmbeddedDatabaseType.HSQL)
        	.addScript("/setup/create-corp-schema.sql")
        	.addScript("/setup/create-smc-schema.sql")
        	.build();
        
        //We have to use a different separator for files containing procedure or function declarations, since the procedure language statements each end with a semicolon also,
        // so we don't want Spring to think we're done with the definition after the first statement.
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        	new ClassPathResource("/setup/create-smc-functions.sql")
        );
        populator.setSeparator("/;");
        
        populator.execute(datasource);

        return datasource;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager()
    {
    	DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(smcDataSource());

        return dataSourceTransactionManager;
    }
}
