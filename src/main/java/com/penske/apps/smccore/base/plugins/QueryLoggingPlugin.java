/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanCreationException;

/**
 * MyBatis plugin that logs performance details of all queries run by the {@code SqlSessionFactory} it belongs to.
 * Each MyBatis plugin is associated with a single MyBatis {@code Configuration}, and each {@code SqlSessionFactory} has a
 * {@code Configuration}, given by the {@code configLocation} property of the SqlSessionFactory bean.
 * 
 * IMPORTANT NOTE: this plugin assumes you are not calling MyBatis {@link Configuration#addMappedStatement(MappedStatement)} or the like, since
 * it relies upon the mappings not being removed after application startup.
 */
@Intercepts({
	@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
	@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class QueryLoggingPlugin implements Interceptor
{
	private static final Logger logger = LogManager.getLogger(QueryLoggingPlugin.class);

	private final ConcurrentHashMap<String, Method> queriesById = new ConcurrentHashMap<String, Method>();

	private final TimingBean timingBean;

	public QueryLoggingPlugin(TimingBean timingBean)
	{
		this.timingBean = timingBean;
	}

	/** {@inheritDoc} */
	@Override
	public Object intercept(Invocation invocation) throws Throwable
	{
		long start = System.currentTimeMillis();

		Object result = invocation.proceed();

		long duration = System.currentTimeMillis() - start;
		
		Object[] args = invocation.getArgs();
		MappedStatement statement = (MappedStatement) args[0];
		Method queryMethod = getInvokedQueryMethod(statement);
		Class<?> queryClass = queryMethod.getDeclaringClass();
		String queryName = queryClass.getSimpleName() + "." + queryMethod.getName();
		
		if(timingBean != null)
		{
			try{
				timingBean.logTiming(CoreTimingType.SMC_CORE_QUERY, duration, queryName);
			} catch(BeanCreationException ex) {
				//If we get an exception trying to log the timing info, it means we are trying to run a query with a request or session-scoped timing bean, but we're not in that scope.
				// For example, during application startup
				ex.toString();
			}
		}
		logAdditionalTimingInformation(invocation, queryMethod, timingBean, duration);

		if(logger.isTraceEnabled())
			logger.trace(new StringBuilder("==> Runtime: ").append(duration));

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	/** {@inheritDoc} */
	@Override
	public void setProperties(Properties props) {}

	/**
	 * Locate the actual DAO method that was called, based on the ID of the mapped statement.
	 * This relies on the fact that each mapped statement ID is unique, so method overloading in DAOs is not allowed for MyBatis mappers.
	 * @param statement The mapped statement object from MyBatis, which contains the query metadata.
	 * @return The actual DAO method that got invoked to run the query.
	 */
	private Method getInvokedQueryMethod(MappedStatement statement)
	{
		String queryId = statement.getId();

		//Attempt to look the method up in the cache, in case it's already been looked up before, so we don't need to parse it again.
		// This works because once a method gets in the cache, it will always remain there, since MyBatis mappings don't typically change midway through an application.
		Method result = queriesById.get(queryId);
		if(result != null)
			return result;

		String className = StringUtils.substringBeforeLast(queryId, ".");
		String methodName = StringUtils.substringAfterLast(queryId, ".");
		methodName=StringUtils.substringBefore(methodName, "!");	//removing the selectKey from method name
		Class<?> daoClass;
		try {
			daoClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			logger.info(e);
			throw new IllegalStateException("Could not find DAO class for query " + queryId);
		}

		Method[] methods = daoClass.getMethods();
		if(methods == null)
			return null;

		for(Method m : methods)
		{
			//For a normal class, which could have overloaded methods, this would not work.
			// In this particular case, because each method in a MyBatis-Spring DAO maps to a query with the same ID as the method name,
			// and because the IDs in a MyBatis mapper have to be unique, it is not possible to have overloaded methods within a single mapper.
			// If there were such, MyBatis would generate an error on startup, so looking at just the method names and not the parameters will work in this case.
			if(m.getName().equals(methodName))
			{
				result = m;
				break;
			}
		}

		if(result != null)
			queriesById.putIfAbsent(queryId, result);

		return result;
	}

	/**
	 * Allows a subclass to perform additional logging of timing information, perhaps broken down more granularly by the method invoked or the class it was invoked on.
	 * @param invocation The invocation object provided to the plugin. Generally, you won't need to use this directly, but you might need it, for instance, to get the arguments passed to the query.
	 * @param queryMethod An object representing the actual DAO method that was called to invoke this query.
	 * @param timingBean A place where timing information can be logged.
	 * 	IMPORTANT NOTE: this may be null, if the application doesn't provide one, so any implementations of this method need to deal with this possibility without throwing a NullPointerException
	 * @param elapsedTime The total number of milliseconds the query took to complete
	 */
	@SuppressWarnings("unused")
	protected void logAdditionalTimingInformation(Invocation invocation, Method queryMethod, TimingBean timingBean, long elapsedTime)
	{
		//This method is a no-op in this class, but can be overridden by subclasses in individual applications to provide additional application-specific logging.
	}
}
