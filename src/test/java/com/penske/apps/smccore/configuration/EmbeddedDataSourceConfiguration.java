/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.configuration;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

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
    public DataSource smcDataSource() throws NamingException {
    	
        EmbeddedDatabase datasource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build();

        return datasource;
    }
}
