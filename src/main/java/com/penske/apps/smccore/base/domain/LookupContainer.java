/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.penske.apps.smccore.base.domain.enums.LookupKey;
import com.penske.apps.smccore.base.util.Util;

/**
 * Contains global configuration data looked up from the SMC_LOOKUP table (and other tables).
 */
public class LookupContainer
{
	/** The actual objects from SMC_LOOKUP, keyed by lookup name, listed in sequence order */
	private final Map<LookupKey, List<LookupItem>> items = new HashMap<LookupKey, List<LookupItem>>();
	/** The string values of objects from SMC_LOOKUP, keyed by lookup name, in no particular order */
	private final Map<LookupKey, Set<String>> values = new HashMap<LookupKey, Set<String>>();
	
	/** The different types of SalesNet documents, keyed by their document type. */
	private final Map<String, DocTypeMaster> docTypeMastersByDocTypeName = new HashMap<String, DocTypeMaster>();
	
	public LookupContainer(List<LookupItem> lookups, List<DocTypeMaster> salesnetDocTypes)
	{
		if(lookups == null)
			lookups = Collections.emptyList();
		if(salesnetDocTypes == null)
			salesnetDocTypes = Collections.emptyList();
		
		Map<LookupKey, Integer> maxSequencesByKey = new HashMap<LookupKey, Integer>();
		for(LookupItem lookup : lookups)
		{
			String name = lookup.getName();
			LookupKey key = LookupKey.findByDbName(name);
			if(key == null)
				continue;
			
			String value = lookup.getValue();
			int sequence = lookup.getSequence();
			
			Integer maxSequence = maxSequencesByKey.get(key);
			if(maxSequence == null)
				maxSequence = sequence;
			if(sequence < maxSequence)
				throw new IllegalArgumentException("Lookups must be in ascending sequence order for each lookup name. Found one that was out of order: Name: " + name + ", Value: " + value + ", Sequence: " + sequence + " (was after " + maxSequence + ")");
			
			if(items.get(key) == null)
				items.put(key, new ArrayList<LookupItem>());
			items.get(key).add(lookup);
			if(values.get(key) == null)
				values.put(key, new LinkedHashSet<String>());
			values.get(key).add(value);
			maxSequencesByKey.put(key, maxSequence);
		}
		
		for(DocTypeMaster docTypeMaster : salesnetDocTypes)
			this.docTypeMastersByDocTypeName.put(docTypeMaster.getDocType(), docTypeMaster);
	}

	public int getLookupCount()
	{
		int total = 0;
		
		for(List<LookupItem> lookupList : items.values())
			total += lookupList.size();
		
		return total;
	}
	
	public Set<String> getLookupValues(LookupKey key)
	{
		if(key == null)
			return Collections.emptySet();
		
		Set<String> result = values.get(key);
		return result == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(result);
	}
	
	public String getSingleLookupValue(LookupKey key)
	{
		if(key == null)
			return null;
		
		Set<String> vals = values.get(key);
		if(vals == null || vals.isEmpty())
			return null;
		if(vals.size() > 1)
			throw new IllegalArgumentException("SMC Lookup contains more than one value for key: " + key);
		
		return Util.getSampleElement(vals);
	}
	
	public Integer getSingleLookupValueInt(LookupKey key)
	{
		String valueString = getSingleLookupValue(key);
		if(StringUtils.isBlank(valueString))
			return null;
		if(!NumberUtils.isNumber(valueString))
			throw new IllegalArgumentException("SMC Lookup value for key " + key + " is not a number, but the program expects it to be");
		
		Integer value = Integer.valueOf(valueString);
		
		return value;
	}
	
	public List<LookupItem> getLookupItems(LookupKey key)
	{
		if(key == null)
			return Collections.emptyList();
		
		List<LookupItem> result = items.get(key);
		return result == null ? Collections.<LookupItem>emptyList() : Collections.unmodifiableList(result);
	}

	public Map<String, DocTypeMaster> getDocTypeMastersByDocTypeName()
	{
		return Collections.unmodifiableMap(docTypeMastersByDocTypeName);
	}

	public DocTypeMaster getDocTypeMasterForDocumentName(String fileName)
	{
		String shortName = StringUtils.left(fileName, 50);
		DocTypeMaster docType = docTypeMastersByDocTypeName.get(shortName);
		//If we don't find the document type by its file name, fall back to "Sales Docs"
		if(docType == null)
			docType = docTypeMastersByDocTypeName.get("Sales Docs");
		
		if(docType == null)
			throw new IllegalStateException("Failed to look up doc type for " + fileName + ". Missing fallback Sales Docs document type.");
		
		return docType;
	}
}