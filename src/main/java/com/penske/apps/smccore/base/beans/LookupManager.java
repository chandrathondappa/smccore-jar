/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.beans;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.penske.apps.smccore.base.dao.LookupDAO;
import com.penske.apps.smccore.base.domain.DocTypeMaster;
import com.penske.apps.smccore.base.domain.LookupCacheInfo;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.LookupItem;

/**
 * Loads and holds the lookup data for the server. Also, allows reloading of the lookup data.
 * This bean is initialized by the spring configuration.
 */
public class LookupManager
{
	/** The document group name indicating a document type comes from the SalesNet system, as opposed to a user-added one. */
	public static final String SALESNET_DOCUMENT_GROUP = "SALESNET";
	public static final String USERS_DOCUMENT_GROUP = "USERDOCS";
	private static final Logger logger = LogManager.getLogger(LookupManager.class);
	
	private final LookupDAO lookupDAO;
	
	/**
	 * The most recent load of the lookup information from the database.
	 * This is in an AtomicReference so that it can be reloaded without interfering with someone else's reading of the data.
	 */
	private final AtomicReference<LookupContainer> containerRef;
	/**
	 * Information about the max modified dates and record counts of the lookup tables the last time they were updated.
	 * Used to determine whether the current lookup information is out of date and needs to be updated again.
	 */
	private final AtomicReference<LookupCacheInfo> cacheInfoRef;
	
	/** This method is intended only to help with testing. Calling it in normal circumstances will result in a broken LookupManager, so it should be reserved only for testing. */
	protected LookupManager()
	{
		this.lookupDAO = null;
		this.containerRef = null;
		this.cacheInfoRef = null;
	}
	
	@Autowired
	public LookupManager(LookupDAO lookupDAO)
	{
		this.lookupDAO = lookupDAO;
		
		LookupContainer lookupContainer = loadLookupContainer();
		LookupCacheInfo lookupCacheInfo = lookupDAO.getLookupCacheInfo();
		
		if(lookupCacheInfo == null)
			throw new IllegalArgumentException("Unable to get max modified dates from DB lookup tables.");
		
		this.cacheInfoRef = new AtomicReference<LookupCacheInfo>(lookupCacheInfo);
		this.containerRef = new AtomicReference<LookupContainer>(lookupContainer);
		logger.info("Initial load of lookup data completed - " + lookupContainer.getLookupCount() + " items");
	}
	
	/**
	 * Checks whether the current lookup data is out of date, and refreshes it if necessary
	 * @return True if the lookup data was refreshed. False if it was not.
	 */
	public boolean checkAndRefreshLookups()
	{
		LookupCacheInfo previousCacheInfo = this.cacheInfoRef.get();
		LookupCacheInfo currentCacheInfo = lookupDAO.getLookupCacheInfo();
		
		//JPS - 2019-01-10 - I realize this implementation isn't completely threadsafe, since the method lacks "synchronized",
		// but since the only way the lookup container and cache data is updated is with a newer (more correct) copy, I think this type of checking will be sufficient.
		// If two threads enter this method, and both detect the data is old before either replaces it, then it will still end up with the newer data before either of them exits.
		// The data in these tables doesn't change often enough, and we don't have high enough user concurrency, to worry about this, honestly.
		if(previousCacheInfo.isOlderThan(currentCacheInfo))
		{
			LookupContainer lookupContainer = loadLookupContainer();
			this.containerRef.set(lookupContainer);
			this.cacheInfoRef.set(currentCacheInfo);
			logger.info("Lookup data reloaded - " + lookupContainer.getLookupCount() + " items ");
			return true;
		}
		
		logger.debug("Lookup data checked for old data, but reload was not needed.");
		return false;
	}
	
	/**
	 * Gets information about application-wide lookups.
	 * @return The lookup informaion.
	 */
	public LookupContainer getLookupContainer()
	{
		return containerRef.get();
	}
	
	/**
	 * Gets the global configuration data needed for the loader
	 * @return A container with global configuration data
	 */
	private LookupContainer loadLookupContainer()
	{
		List<LookupItem> lookupItems = lookupDAO.getLookupItems();
		List<DocTypeMaster> salesnetDocTypes = lookupDAO.getSalesnetDocTypeMasters(SALESNET_DOCUMENT_GROUP);
		return new LookupContainer(lookupItems, salesnetDocTypes);
	}
}
