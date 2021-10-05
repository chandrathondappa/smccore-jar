/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.penske.apps.smccore.base.annotation.NonVendorQuery;
import com.penske.apps.smccore.base.annotation.SkipQueryTest;
import com.penske.apps.smccore.base.domain.DocTypeMaster;
import com.penske.apps.smccore.base.domain.LookupCacheInfo;
import com.penske.apps.smccore.base.domain.LookupItem;

/**
 * Contains queries related to SMC_LOOKUP and related tables
 */
public interface LookupDAO
{
	@SkipQueryTest("HSQLDB gives a type error on the union even though everything works in DB2")
	@NonVendorQuery
	public LookupCacheInfo getLookupCacheInfo();
	
	/**
	 * Looks up all global configuration information from SMC_LOOKUP.
	 */
	@NonVendorQuery
	public List<LookupItem> getLookupItems();
	
	/**
	 * Gets all the types of documents that can be attached from the sales configurator system
	 * @param salesnetDocumentGroup The name of the document group for the configurator system
	 * @return The salesnet document types
	 */
	@NonVendorQuery
	public List<DocTypeMaster> getSalesnetDocTypeMasters(@Param("salesnetDocumentGroup") String salesnetDocumentGroup);
	
	/**
	 * Gets all the types of documents that can be attached by a user
	 * @param usersDocumentGroup The name of the document group for users
	 * @return The user document types
	 */
	@NonVendorQuery
	public List<DocTypeMaster> getUserDocTypeMasters(@Param("usersDocumentGroup") String usersDocumentGroup);
}
