/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

/**
 * Ensures that the ApplicationName client property is set on all queries that get called from the application.
 * This is used by some triggers in the database to check whether the table is being modified by SMC or something else, and to run differently (or not run) if it's being called from SMC.
 */
@Intercepts({
	@Signature(type = StatementHandler.class, method="prepare", args= {Connection.class, Integer.class})
})
public class ClientInfoPlugin implements Interceptor
{
	/** {@inheritDoc} */
	@Override
	public Object intercept(Invocation invocation) throws Throwable
	{
		Object[] args = invocation.getArgs();
		Connection conn = (Connection) args[0];
		
		conn.setClientInfo("ApplicationName", "SMC");
		
		Object result = invocation.proceed();
		
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Object plugin(Object target)
	{
		return Plugin.wrap(target, this);
	}

	/** {@inheritDoc} */
	@Override
	public void setProperties(Properties props) {}

}
