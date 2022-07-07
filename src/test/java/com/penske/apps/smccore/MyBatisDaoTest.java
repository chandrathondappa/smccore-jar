// parasoft-begin-suppress SECURITY.WSC.APIBS "This is a class only intended for unit tests, not production code."
// parasoft-begin-suppress UC.UCATCH "This is a class only intended for unit tests, not production code."
/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.core.DecoratingProxy;

import com.penske.apps.smccore.base.annotation.SkipQueryTest;

/**
 * Common superclass for DAOs that asserts that all methods in the DAO were run after the test class completes.
 */
public abstract class MyBatisDaoTest
{
	private static final Set<String> ALWAYS_SKIP = new HashSet<String>(Arrays.asList("hashCode", "equals", "toString", "proxyClassLookup"));
	
	private static final Set<String> PROXY_SKIP_METHODS = new HashSet<String>();
	
	private static final Map<Class<?>, Set<Method>> METHODS_RUN = new HashMap<Class<?>, Set<Method>>();
	private static final Map<Class<?>, Set<Method>> METHODS_TO_RUN = new HashMap<Class<?>, Set<Method>>();
	
	static {
		Stream.of(AdvisedSupport.class.getMethods()).forEach(m -> PROXY_SKIP_METHODS.add(m.getName()));
		Stream.of(DecoratingProxy.class.getMethods()).forEach(m -> PROXY_SKIP_METHODS.add(m.getName()));
	}
	
	protected <T> T trackMethodCalls(T daoObject, Class<T> daoClass)
	{
		Set<Method> methodsForClass = METHODS_TO_RUN.get(daoClass);
		
		if(methodsForClass == null)
		{
			methodsForClass = new HashSet<Method>();
			METHODS_TO_RUN.put(daoClass, methodsForClass);
			METHODS_RUN.put(daoClass, new HashSet<Method>());
			
			for(Method method : daoObject.getClass().getDeclaredMethods())
			{
				Method realTargetMethod = findMethod(daoClass, method);
				if(realTargetMethod != null && realTargetMethod.isAnnotationPresent(SkipQueryTest.class))
					continue;
				
				if(ALWAYS_SKIP.contains(method.getName()) || PROXY_SKIP_METHODS.contains(method.getName()))
					continue;
				
				methodsForClass.add(method);
			}
		}
		
		MyBatisTestInvocationHandler testHandler = new MyBatisTestInvocationHandler(daoObject, METHODS_RUN.get(daoClass));
		Object proxy = Proxy.newProxyInstance(daoClass.getClassLoader(), new Class<?>[]{daoClass}, testHandler);
		return daoClass.cast(proxy);
	}

	@AfterClass
	public static void testAllRun()
	{
		//Make copies of the static maps and clear out the maps themselves, so that they are clean for the next test class, no matter what happens
		Map<Class<?>, Set<Method>> methodsToRun = new HashMap<Class<?>, Set<Method>>(METHODS_TO_RUN);
		Map<Class<?>, Set<Method>> methodsRun = new HashMap<Class<?>, Set<Method>>(METHODS_RUN);
		METHODS_TO_RUN.clear();
		METHODS_RUN.clear();
		
		//Check to make sure all targeted methods were run
		for(Class<?> daoClass : methodsToRun.keySet())
		{
			Set<Method> methodsToRunForClass = methodsToRun.get(daoClass);
			Set<Method> methodsRunForClass = methodsRun.get(daoClass);
			if(methodsRunForClass == null)
				methodsRunForClass = Collections.emptySet();

			String forgotMessage = "No tracked methods found for class " + daoClass.getName() + ". Did you remember to replace the DAO object with the proxy generated from trackMethodCalls()?";
			assertThat(forgotMessage, methodsRunForClass, is(not(empty())));
			
			for(Method query : methodsToRunForClass)
			{
				String methodName = query.toGenericString();
				String methodRun = methodsRunForClass.contains(query) ? methodName : null;
				assertThat("Missing test for DAO method", methodRun, is(methodName));
			}
		}
	}
	
	private static Method findMethod(Class<?> claz, Method method)
	{
		try {
			return claz.getDeclaredMethod(method.getName(), method.getParameterTypes());
		} catch(NoSuchMethodException e) {
			return null;
		}
	}
	
	private static class MyBatisTestInvocationHandler implements InvocationHandler
	{
		private final Object delegate;
		private final Set<Method> methodsRun;
		
		public MyBatisTestInvocationHandler(Object delegate, Set<Method> methodsRun)
		{
			this.delegate = delegate;
			this.methodsRun = methodsRun;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			Method m = findMethod(this.getClass(), method);
			if(m == null)
				m = findMethod(delegate.getClass(), method);
			
			if(m == null)
				return null;
			
			methodsRun.add(m);
			return m.invoke(delegate, args);
		}
	}
}
