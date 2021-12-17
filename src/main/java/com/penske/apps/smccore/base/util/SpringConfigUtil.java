/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.penske.apps.smccore.base.annotation.MappedEnumTypes;
import com.penske.apps.smccore.base.configuration.ProfileType;
import com.penske.apps.smccore.base.configuration.typeHandlers.SmcEnumTypeHandler;
import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * Provides utility methods to assist in common Spring configuration tasks
 */
public final class SpringConfigUtil
{
	/** Can't instantiate a utility class. */
	private SpringConfigUtil() {}

	//FIXME: document
	//FIXME: test
	public static Set<String> getPackageNames(Class<?>... classes)
	{
		if(classes == null)
			return Collections.emptySet();
		
		Set<String> result = new HashSet<String>();
		for(Class<?> claz : classes)
			result.add(claz.getPackage().getName());
		return result;
	}
	
	/**
	 * Attempts to look up MyBatis mapper resources from the classpath
	 * @param resolver A pattern matching resolver to use when looking things up
	 * @param basePath The initial part of the class path entry (including the "classpath:" or "classpath*:" prefix)
	 * 	Use "classpath" for files that are in the application source tree.
	 * 	Use "classpath*" for files that are in a JAR.
	 * @param mapperFileNames The names of each individual mapper file to load.
	 * 	These may contain path wildcards (ex: "*"), but be careful using wildcards with a basePath including "classpath*",
	 * 	as it will search every JAR on the classpath.
	 * @return The mapper files as Spring resources
	 * @throws IOException If there was a problem accessing one of the resource files.
	 */
	//FIXME: test
	public static List<Resource> getMapperFileResources(ResourcePatternResolver resolver, String basePath, Collection<String> mapperFileNames) throws IOException
	{
		List<Resource> mapperLocations =  new ArrayList<Resource>();
		for(String mapperFileName : mapperFileNames)
		{
			String fullName = basePath + mapperFileName;
			Resource[] mapperFiles = resolver.getResources(fullName);
			if(mapperFiles == null || mapperFiles.length == 0)
				throw new IllegalStateException("Expected to find MyBatis mapper files for " + fullName + ", but the file was missing.");
			mapperLocations.addAll(Arrays.asList(mapperFiles));
		}
		return mapperLocations;
	}
	
	//FIXME: document
	//FIXME: test
	public static List<Class<?>> getMappedEnumTypes(Class<?>... annotatedClasses)
	{
		if(annotatedClasses == null)
			return Collections.emptyList();
		
		List<Class<?>> result = new ArrayList<Class<?>>();
		for(Class<?> annotatedClass : annotatedClasses)
		{
			MappedEnumTypes annotation = annotatedClass.getAnnotation(MappedEnumTypes.class);
			if(annotation == null)
				continue;
			Class<? extends MappedEnum>[] mappedEnumClasses = annotation.value();
			if(mappedEnumClasses == null)
				continue;
			result.addAll(Arrays.asList(mappedEnumClasses));
		}
		
		return result;
	}
	
	/**
	 * Registers a type handler for the given enums that automatically translates between the enum constant and a mapped database value.
	 * @param registry A type handler registry obtained from the SqlSessionFactory being configured.
	 * @param mappedEnumTypes An array of MappedEnum classes that should have type handlers registered for them.
	 */
	//FIXME: test
	public static <E extends MappedEnum> void registerMappedEnumTypeHandlers(TypeHandlerRegistry registry, List<Class<?>> mappedEnumTypes)
	{
		for(Class<?> mappedType : mappedEnumTypes)
		{
			if(!mappedType.isEnum())
				throw new IllegalStateException("Error registering Enum type handlers: " + mappedType.getName() + " is either not an enum.");

			@SuppressWarnings("unchecked")
			Class<E> mappedEnumType = (Class<E>) mappedType;
			
			SmcEnumTypeHandler<E> handler = new SmcEnumTypeHandler<E>(mappedEnumType);
			registry.register(mappedEnumType, handler);

			for(Object enumConstant : mappedType.getEnumConstants())
			{
				@SuppressWarnings("unchecked")
				Class<E> actualClass = (Class<E>) enumConstant.getClass();
				registry.register(actualClass, handler);
			}
		}
	}

	/**
	 * Fetches the current active Spring profile, based on the system property "srvenv"
	 * @return The spring profile that is currently active
	 */
	public static String getSpringProfile()
	{
		String activeProfiles = System.getProperty("srvenv");
		
		String[] parts = StringUtils.split(activeProfiles, ",");
		
		if(parts == null || parts.length == 0 || StringUtils.isBlank(parts[0]))
			throw new IllegalStateException("No active spring profile found. Is srvenv missing from system properties?");
		if(parts.length > 1)
			throw new IllegalStateException("Only one active spring profile allowed");
		return parts[0];
	}

	/**
	 * Checks if the spring configuration is running in local mode or if it's not.
	 * @param env The Spring environment
	 * @return True if this application is running in local mode. False if not.
	 */
	public static boolean isLocal(Environment env)
	{
		boolean result = Arrays.stream(env.getActiveProfiles())
			.anyMatch(prof -> ProfileType.LOCAL.equals(prof) || ProfileType.DEVELOPMENT.equals(prof));
		return result;
	}
	
	/**
	 * Checks if the given profile matches any of the listed profiles
	 * @param activeProfile The currently-active profile
	 * @param profiles The profiles to check if the given profile matches.
	 * @return True if {@code activeProfile} is equal to at least one of the profiles in {@code profiles}
	 */
	public static boolean isProfile(String activeProfile, String... profiles)
	{
		if(profiles == null)
			return false;
		
		for(String profile : profiles)
		{
			if(profile.equals(activeProfile))
				return true;
		}
		return false;
	}

	/**
	 * Adds a RollingFileAppender to the given log configuration builder with some basic default parameters that match our usual setup for SMC log files.
	 * @param builder The log configuration builder being used to set up logging.
	 * @param appenderName The name of the appender (used in the generated appender ref). Must not conflict with any other appender name in the log configuration.
	 * @param maxFileSize The size that any one log file should be allowed to grow to before it rolls over. This should be in bytes with the suffix of "KB", "MB", or "GB".
	 * 	For more information, see https://logging.apache.org/log4j/2.x/manual/appenders.html#SizeBased_Triggering_Policy
	 * @param maxBackupIndex The maximum number of rolled-over log files that will be kept upon rollover.
	 * @param pattern The pattern to use to create log messages
	 * @param logFilePath The path to the log file. Typically, this is an absolute path, like "/logs/apps/smcof/log.txt"
	 * @return A reference to the appender, which can be passed to other parts of the log configuration (ex: adding the appender to a logger).
	 */
	public static AppenderRefComponentBuilder makeRollingFileAppender(ConfigurationBuilder<?> builder, String appenderName, String maxFileSize, int maxBackupIndex, String pattern, String logFilePath)
	{
		builder.add(builder.newAppender(appenderName, "RollingFile")
			.addAttribute("fileName", logFilePath)
			.addAttribute("filePattern", logFilePath + ".%i")
			.add(builder.newLayout("PatternLayout").addAttribute("pattern", pattern))
			.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", maxFileSize))
			.addComponent(builder.newComponent("DefaultRolloverStrategy")
				.addAttribute("max", maxBackupIndex)
				.addAttribute("fileIndex", "min")
			)
		);
		
		return builder.newAppenderRef(appenderName);
	}
	
	/**
	 * Gets a summary of the current log4j configuration to the info logs
	 * @param contextPath The portion of the URL path that gives what application the log configuration is being printed for
	 */
	public static String getLogConfiguration(String contextPath)
	{
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		
		Map<String, Appender> appenders = config.getAppenders();
		Map<String, LoggerConfig> loggers = new LinkedHashMap<>(config.getLoggers());
		
		StringBuilder sb = new StringBuilder("=== " + contextPath + " Log Configuration ===\n");
		sb.append("  == Appenders ==\n");
		for(Entry<String, Appender> entry : appenders.entrySet())
		{
			String name = entry.getKey();
			Appender appender = entry.getValue();
			sb.append("  * ").append(name).append(": ").append(appender.getClass().getName()).append("\n");
			
			Layout<?> layout = appender.getLayout();
			if(layout instanceof PatternLayout)
			{
				PatternLayout patternLayout = (PatternLayout) layout;
				sb.append("    Pattern: ").append(patternLayout.getConversionPattern()).append("\n");
			}
			else
				sb.append("    Pattern: Not a PatternLayout (").append(layout.getClass().getName()).append(")\n");
			
			if(appender instanceof FileAppender)
			{
				FileAppender fileAppender = (FileAppender) appender;
				sb.append("    Filename: ").append(fileAppender.getFileName()).append("\n");
			}
			else if(appender instanceof RollingFileAppender)
			{
				RollingFileAppender fileAppender = (RollingFileAppender) appender;
				sb.append("    Filename: ").append(fileAppender.getFileName()).append("\n");
			}
		}
		
		sb.append("\n  == Loggers ==\n");
		for(Entry<String, LoggerConfig> entry: loggers.entrySet())
		{
			String name = StringUtils.isBlank(entry.getKey()) ? "ROOT LOGGER" : entry.getKey();
			LoggerConfig logger = entry.getValue();
			
			sb.append("  * ").append(name).append(" (Level: ").append(logger.getLevel()).append(")");
			
			if(!logger.isAdditive())
				sb.append(" - Additivity: false");
			sb.append("\n");
		}
		
		return sb.toString();
	}
}