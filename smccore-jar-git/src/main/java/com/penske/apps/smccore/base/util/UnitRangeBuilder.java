/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import com.penske.apps.smccore.base.plugins.CoreTimingType;
import com.penske.apps.smccore.base.plugins.TimingBean;
import com.penske.apps.smccore.base.util.UnitNumberUtil.UnitNumberChunks;

/**
 * An object used to group units (or anything that behaves like a unit by implementing {@link SortableUnit}) into ranges and sets of ranges.
 * Depending on how complex the grouping and splitting needs to be, you can have multiple levels of grouping criteria. Each level added adds another Set<> to the result of the {@link #build(Collection)} method.
 * <br/>
 * NOTE: to group simple unit number strings into contiguous ranges, see {@link UnitNumberUtil#groupUnitRanges(Collection, TimingBean)}
 * 
 * <p><b>Examples below</b></p>
 * 
 * <p>
 * <u>Units:</u><br/>
 *	1 (from Morgan  to PA, SchA 17-01)<br/>
 *	2 (from Morgan  to AZ, SchA 17-01)<br/>
 * 	3 (from Supreme to AZ, SchA 17-02)<br/>
 *	4 (from Kidron  to PA, SchA 17-02)<br/>
 *	5 (from Kidron  to PA, SchA 17-03)<br/>
 * 	6 (from Kidron  to PA, SchA 17-03)
 * </p>
 * 
 * <u>Call:</u> {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).build()}<br/>
 * <u>Result:</u>
 * <pre>
{[1], [2-3], [4-6]}</pre>
 * 
 * <u>Call:</u> {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).andThen().by({poVendor-extracting-function}).build()}<br/>
 * <u>Result:</u>
 * <pre>
 {
  {[1], [2]}
  {[3]}
  {[4-6]}
 }</pre>
	
	<u>Call:</u> {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).andThen().by({poVendor-extracting-function}).andThen().by({scheduleA-extracting-function}).build()}<br/>
	<u>Result:</u>
	<pre>
 {
  {
   {[1], [2]}
  }
  {
   {[3]}
   {[4]}
  }
 {
   {[5-6]}
 }
}
	 </pre>
 */
public class UnitRangeBuilder<T extends SortableUnit>
{
	/** If this is supplied, the timing information for how long it took to sort units is reported here. */
	private final TimingBean timingBean;
	
	private boolean byUnitNumberCalled = false;

	private List<Function<T, String>> keyFunctions = new ArrayList<>();
	
	/**
	 * Creates a new builder for grouping unit ranges
	 */
	public UnitRangeBuilder()
	{
		this(null);
	}
	
	/**
	 * Creates a new builder for grouping unit ranges, which will report how long the operation took to the given timing bean.
	 * @param timingBean A bean to aggregate performance information for how long the grouping took.
	 */
	public UnitRangeBuilder(TimingBean timingBean)
	{
		this.timingBean = timingBean;
	}
	
	/**
	 * Causes units with non-contiguous or non-comparable unit numbers to be grouped in separate ranges.
	 * This method must be invoked before invoking {@link #build(Collection)}, or an exception will be thrown at runtime, since
	 * 	the concept of a "range" of units doesn't even make sense without this level of grouping.
	 * This method serves to make things like "new UnitRangeBuilder().byUnitNumber().andThen()..." read more clearly than if it were just "new UnitRangeBuilder().andThen()..."
	 * 
	 * 	Unit numbers are comparable if they are the same length and if they are the same except for the last numeric chunk.
	 * 		Ex: Units 1000 and 1001 are comparable, but units 999 and 1000 are not (different lengths)
	 * 		Ex: Units 1AU1000A and 1AU1001A are comparable, but units 1AU1000A and 1AU1000B are not comparable (difference outside the last numeric chunk)
	 * 		Ex: Units 1X1000 and 1X1001 are comparable, but units 1X1000 and 2X1000 are not comparable (difference outside the last numeric chunk) 
	 * 		Ex: units 201561, 201562, and 201563 are contiguous
	 * 	Unit numbers are contiguous if they are comparable, and if their last numeric chunks are adjacent integers
	 * 		Ex: Units A1000B and A1001B are contiguous, but units A999B and A1000B are not
	 * 
	 * @return The builder, for method chaining
	 */
	public UnitRangeBuilder<T> byUnitNumber()
	{
		this.byUnitNumberCalled = true;
		return this;
	}
	
	/**
	 * Causes units for which the key extractor function returns different values to be grouped into separate ranges.
	 * @param keyExtractor A function that takes a sortable unit and returns a String that does not contain the '|' character.
	 * @return The builder, for method chaining.
	 */
	public UnitRangeBuilder<T> by(Function<T, String> keyExtractor)
	{
		this.keyFunctions.add(keyExtractor);
		return this;
	}
	
	/**
	 * Adds another layer of grouping to the resulting sets of unit ranges. This adds another nested Set to the return type of {@link #build(Collection)}.
	 * For example, a caller might want to split units that have different delivery dates into different ranges, and then further split ranges that have different PO vendors into different groups of ranges,
	 * and then further split ranges with different loadsheets into groups of groups of ranges.
	 * 	To do that, they would call: {@code new UnitRangeBuilder().byUnitNumber().by({date-extractor-function}).andThen().by({vendor-extractor-function}).andThen().by({loadsheet-extractor-function}).build(units)}
	 * @return The builder, for method chaining
	 */
	public UnitRangeTwoLayerBuilder<T> andThen()
	{
		return new UnitRangeTwoLayerBuilder<>(this);
	}

	/**
	 * Groups the given units together into ranges based on the criteria this builder is configured for.
	 * 	For instance, calling {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).build()}
	 * 	on units:<p>
	 * 		1(to PA)<br/>
	 * 		2(to AZ)<br/>
	 * 		3(to AZ)<br/>
	 * 		4(to PA)<br/>
	 * 		5(to PA)<br/>
	 * 		6(to PA)
	 * </p>would result in the following ranges:<p>
	 * 		{[1], [2-3], [4-6]}
	 * </p>
	 * @param units The units to place into groups.
	 * @return A set of ranges, each of which contains one or more units. Each unit range will be sorted from smallest to largest unit number.
	 */
	public Set<List<T>> build(Collection<T> units)
	{
		if(units == null || units.isEmpty())
			return Collections.emptySet();
		//Early exit if we have only one unit - it's always going to be a single range
		if(units.size() == 1)
		{
			Set<List<T>> result = new HashSet<List<T>>();
			result.add(new ArrayList<T>(units));
			return result;
		}
		
		//Simply make the most complex builder we can, but don't add any further grouping criteria, so each resulting set will only have one element
		UnitRangeFourLayerBuilder<T> builder = this.andThen().andThen().andThen();
		
		//Actually perform the grouping
		Set<Set<Set<Set<List<T>>>>> groupedUnits = performGrouping(units, builder, timingBean);
		
		//Because the other two layers were artificially added, we can safely assume they won't be grouped any differently than the first layer, so the outer two sets
		// should have only one element each. Thus, it's sufficient to get a single sample element from each of the outer two sets and return that.
		Set<List<T>> result = Util.getSampleElement(Util.getSampleElement(Util.getSampleElement(groupedUnits)));
		
		return result;
	}
	
	private String getKey(T unit)
	{
		return getBuilderKey(unit, keyFunctions);
	}
	
	/**
	 * Static method to actually perform the low-level grouping of units.
	 * @param units The units to be grouped
	 * @param builder The builder that describes how to group the units
	 * @param timingBean Optional. Bean to report performance information about the operation.
	 * @return The units grouped into sets of ranges.
	 */
	private static <T extends SortableUnit> Set<Set<Set<Set<List<T>>>>> performGrouping(Collection<T> units, UnitRangeFourLayerBuilder<T> builder, TimingBean timingBean)
	{
		long startTime = System.currentTimeMillis();
		
		//Locate lowest-level grouping, and make sure contiguous units flag has been set
		if(builder.parent.parent.parent.byUnitNumberCalled == false)
			throw new IllegalArgumentException("Invalid unit grouping configuration. You must call byUnitNumber() on UnitRangeBuilder to group by contiguous units before calling .andThen() or .build()");
		
		/*
		 * First, we sort the unit numbers into different buckets, based on which ones are comparable with each other (the outer map represents the buckets).
		 * 
		 * This is similar to the way a hash table works (with buckets and keys), except here, we aren't using the hash code to determine bucket membership.
		 * A unit number gets put into a bucket if it is comparable every other unit number in that bucket.
		 * Because of the nature of unit numbers, comparability is transitive, which means it is sufficient to examine just a
		 *  single unit number in the bucket (the key of the outer map) to determine bucket membership.
		 *  
		 * The inner map is a TreeMap, which is used to sort comparable unit numbers smallest to greatest (based on their last numeric segment).
		 * 
		 * At the end of this loop, the outer map contains one bucket for each set of mutually-comparable unit numbers, with each bucket containing
		 * unit numbers sorted from smallest to largest. A bucket *may* contain unit numbers that are not contiguous, which will have to be broken out
		 * into separate lists in the next step.
		 */
		Map<String, T> unitsByNumber = new HashMap<String, T>();
		Map<String, UnitNumberChunks> chunksByNumber = new HashMap<String, UnitNumberChunks>();
		Map<UnitNumberChunks, Map<Integer, UnitNumberChunks>> bucketMap = new LinkedHashMap<UnitNumberChunks, Map<Integer, UnitNumberChunks>>();
		for(T unit : units)
		{
			String unitNumber = unit.getUnitNumber();
			UnitNumberChunks chunks = new UnitNumberChunks(unitNumber);
			
			unitsByNumber.put(unitNumber, unit);
			chunksByNumber.put(unitNumber, chunks);

			//Look for a key that is comparable with the unit number we are trying to sort.
			UnitNumberChunks matchingKey = null;
			for(UnitNumberChunks bucketKey : bucketMap.keySet())
			{
				if(!chunks.isComparable(bucketKey))
					continue;

				matchingKey = bucketKey;
				break;
			}

			//If we didn't find an existing bucket that we can put the unit number in, make a brand new one for it.
			if(matchingKey == null)
			{
				matchingKey = chunks;

				//TreeMap doesn't allow null keys, so use a HashMap instead if the comparison key is null.
				// This only works because, if the comparison key is null, the unit number can't be compared with any other unit number.
				// So, no other unit number will ever be put in this bucket, in which case the order of elements doesn't matter.
				if(chunks.getSortKey() != null)
					bucketMap.put(chunks, new TreeMap<Integer, UnitNumberChunks>());
				else
					bucketMap.put(chunks, new HashMap<Integer, UnitNumberChunks>());
			}

			//Actually put the unit number into the right bucket
			Integer comparisonKey = chunks.getSortKey();
			bucketMap.get(matchingKey).put(comparisonKey, chunks);
		}
		
		/*
		 * Next, once the units are sorted into groups whose unit numbers are comparable, they are further split based on the keys the user defined (ex: by address, by dates, by PO vendor, etc.)
		 * The units go into three levels of maps, one for each level of user-defined key
		 * Within those maps are two nested lists. The inner list represents units that all have the same values for every user-defined key, and which are also contiguous with each other (ex: "261584" and "261585" are contiguous)
		 * 	The outer list represents units that have the same values for all user-defined keys, but which are not contiguous
		 * 		(ex: "261584" and "261589" are not contiguous because there are units in between. "261584 amd "261584A" are not contiguous because they are not comparable either - they're of different lengths)
		 */
		Map<String, Map<String, Map<String, Map<String, List<List<T>>>>>> bucket1 = new LinkedHashMap<String, Map<String, Map<String, Map<String, List<List<T>>>>>>();
		for(Map<Integer, UnitNumberChunks> sortedMap : bucketMap.values())
		{
			for(UnitNumberChunks chunks : sortedMap.values())
			{
				T unit = unitsByNumber.get(chunks.getFullUnitNumber());
				//We should never end up hitting this exception, but just in case, make sure we don't lose unit numbers.
				if(unit == null)
					throw new IllegalStateException("Failed to find unit " + chunks.getFullUnitNumber() + " when grouping units: " + unitsByNumber.keySet());
				
				String key1 = builder.getKey(unit);
				String key2 = key1 + "|" + builder.parent.getKey(unit);
				String key3 = key2 + "|" + builder.parent.parent.getKey(unit);
				String key4 = key3 + "|" + builder.parent.parent.parent.getKey(unit);
				
				if(!bucket1.containsKey(key1))
					bucket1.put(key1, new LinkedHashMap<String, Map<String, Map<String, List<List<T>>>>>());
				Map<String, Map<String, Map<String, List<List<T>>>>> bucket2 = bucket1.get(key1);
				if(!bucket2.containsKey(key2))
					bucket2.put(key2, new LinkedHashMap<String, Map<String, List<List<T>>>>());
				Map<String, Map<String, List<List<T>>>> bucket3 = bucket2.get(key2);
				if(!bucket3.containsKey(key3))
					bucket3.put(key3, new LinkedHashMap<String, List<List<T>>>());
				Map<String, List<List<T>>> bucket4 = bucket3.get(key3);
				if(!bucket4.containsKey(key4))
					bucket4.put(key4, new ArrayList<List<T>>());
				List<List<T>> bottomBucket = bucket4.get(key4);
				if(bottomBucket.size() == 0)
					bottomBucket.add(new ArrayList<T>());
				
				//The active unit range is the last one in the list, and the unit to compare with for contiguity is the last one in the active range.
				// This only works because the units are sorted by unit number in the first loop above, so they are already in order
				List<T> range = bottomBucket.get(bottomBucket.size() - 1);
				T previousUnit = range.isEmpty() ? null : range.get(range.size() - 1);
				UnitNumberChunks previousChunks = previousUnit == null ? null : chunksByNumber.get(previousUnit.getUnitNumber());
				
				//Make new unit range if this unit is not contiguous with the last one in the working range
				boolean unitsAreContiguous = previousChunks == null || chunks.isContiguous(previousChunks);
				if(!unitsAreContiguous)
				{
					range = new ArrayList<T>();
					bottomBucket.add(range);
				}
				
				range.add(unit);
			}
		}
		
		/*
		 * Finally, once we've categorized each unit into an appropriate bucket, we loop over all the buckets to transform the maps into our final data types
		 * The innermost map and the non-contiguous list are folded together, since they both together represent the bottom-most layer of user-defined criteria
		 */
		Set<Set<Set<Set<List<T>>>>> result = new LinkedHashSet<Set<Set<Set<List<T>>>>>();
		for(Map<String, Map<String, Map<String, List<List<T>>>>> bucket2 : bucket1.values())
		{
			Set<Set<Set<List<T>>>> result2 = new LinkedHashSet<Set<Set<List<T>>>>();
			for(Map<String, Map<String, List<List<T>>>> bucket3 : bucket2.values())
			{
				Set<Set<List<T>>> result3 = new LinkedHashSet<Set<List<T>>>();
				for(Map<String, List<List<T>>> bucket4 : bucket3.values())
				{
					Set<List<T>> result4 = new LinkedHashSet<List<T>>();
					for(List<List<T>> bottomBucket : bucket4.values())
					{
						//We collapse the bottom two layers (contiguity and whatever the bottom set of criteria the user asked for is), since to the user, they are one set of criteria
						for(List<T> range : bottomBucket)
							result4.add(Collections.unmodifiableList(range));
					}
					result3.add(Collections.unmodifiableSet(result4));
				}
				result2.add(Collections.unmodifiableSet(result3));
			}
			result.add(Collections.unmodifiableSet(result2));
		}
		
		long endTime = System.currentTimeMillis();
		if(timingBean != null)
			timingBean.logTiming(CoreTimingType.GROUP_UNIT_RANGE, endTime - startTime);
		
		//Make bucket map into result sets (or lists)
		return result;
	}

	/**
	 * Utility method to build up a String key identifying a given unit based on which fields a given builder cares about.
	 * @param <T> The class that is going to have a key created to identify it
	 * @param unit The unit to generate an identifier for
	 * @param keyFunctions A list of functions to extract pieces for a key in order. 
	 * @return The compiled key, containing one piece for each function.
	 */
	private static <T extends SortableUnit> String getBuilderKey(T unit, List<Function<T, String>> keyFunctions)
	{
		if(keyFunctions.isEmpty())
			return "";
		
		String key = keyFunctions.stream()
			.map(f -> f.apply(unit))
			.collect(joining("|"));
		
		return key;
	}
	
	//***** HELPER CLASSES *****//
	public static class UnitRangeTwoLayerBuilder<T extends SortableUnit>
	{
		/** The one-level builder that defines the next lower level of grouping units together. */
		private final UnitRangeBuilder<T> parent;
		
		private List<Function<T, String>> keyFunctions = new ArrayList<>();
		
		/**
		 * Creates a new second-level builder for grouping unit ranges.
		 * @param parent The first-level builder that defines the next lower-level of grouping units together.
		 */
		private UnitRangeTwoLayerBuilder(UnitRangeBuilder<T> parent)
		{
			this.parent = parent;
		}
		
		public UnitRangeTwoLayerBuilder<T> by(Function<T, String> keyExtractor)
		{
			this.keyFunctions.add(keyExtractor);
			return this;
		}
		
		/**
		 * Adds another layer of grouping to the resulting sets of unit ranges. This adds another nested Set to the return type of {@link #build(Collection)}.
		 * For example, a caller might want to split units that have different delivery dates into different ranges, and then further split ranges that have different PO vendors into different groups of ranges,
		 * and then further split ranges with different loadsheets into groups of groups of ranges.
		 * 	To do that, they would call: {@code new UnitRangeBuilder().byUnitNumber().by({date-extractor-function}).andThen().by({vendor-extractor-function}).andThen().by({loadsheet-extractor-function}).build(units)}
		 * @return The builder, for method chaining
		 */
		public UnitRangeThreeLayerBuilder<T> andThen()
		{
			return new UnitRangeThreeLayerBuilder<>(this);
		}
	
		private String getKey(T unit)
		{
			return getBuilderKey(unit, keyFunctions);
		}
	
		/**
		 * Groups the given units together into ranges based on the criteria this builder is configured for.
		 * 	For instance, calling {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).andThen().by({poVendor-extracting-function}).build()}
		 * 	on units:<p>
		 * 		1(from Morgan  to PA)<br/>
		 * 		2(from Morgan  to AZ)<br/>
		 * 		3(from Supreme to AZ)<br/>
		 * 		4(from Kidron  to PA)<br/>
		 * 		5(from Kidron  to PA)<br/>
		 * 		6(from Kidron  to PA)
		 * </p>would result in the following ranges:<p>
		 * 	<pre>
		 		{
		 			{[1], [2]}
		 			{[3]}
		 			{[4-6]}
		 		}
		 	</pre>
		 * </p>
		 * @param units The units to place into groups.
		 * @return A set of ranges, each of which contains one or more units. Each unit range will be sorted from smallest to largest unit number.
		 */
		public Set<Set<List<T>>> build(Collection<T> units)
		{
			if(units == null || units.isEmpty())
				return Collections.emptySet();
			//Early exit if we have only one unit - it's always going to be a single range
			if(units.size() == 1)
			{
				Set<Set<List<T>>> result = new HashSet<Set<List<T>>>();
				Set<List<T>> inner = new HashSet<List<T>>();
				inner.add(new ArrayList<T>(units));
				result.add(inner);
				return result;
			}
			
			//Simply make the most complex builder we can, but don't add any further grouping criteria, so each resulting set will only have one element
			UnitRangeFourLayerBuilder<T> builder = this.andThen().andThen();
			
			//Actually perform the grouping
			Set<Set<Set<Set<List<T>>>>> groupedUnits = performGrouping(units, builder, parent.timingBean);
			
			//Because the other layer was artificially added, we can safely assume it won't be grouped any differently than the first two layers, so the outer set
			// should have only one element. Thus, it's sufficient to get a single sample element from the outer set and return that.
			Set<Set<List<T>>> result = Util.getSampleElement(Util.getSampleElement(groupedUnits));
			
			return result;
		}
	}
	
	public static class UnitRangeThreeLayerBuilder<T extends SortableUnit>
	{
		/** The one-level builder that defines the next lower level of grouping units together. */
		private final UnitRangeTwoLayerBuilder<T> parent;
		
		private List<Function<T, String>> keyFunctions = new ArrayList<>();
		
		/**
		 * Creates a new third-level builder for grouping unit ranges.
		 * @param parent The second-level builder that defines the next lower-level of grouping units together.
		 */
		private UnitRangeThreeLayerBuilder(UnitRangeTwoLayerBuilder<T> parent)
		{
			this.parent = parent;
		}
		
		public UnitRangeThreeLayerBuilder<T> by(Function<T, String> keyExtractor)
		{
			this.keyFunctions.add(keyExtractor);
			return this;
		}
		
		/**
		 * Adds another layer of grouping to the resulting sets of unit ranges. This adds another nested Set to the return type of {@link #build(Collection)}.
		 * For example, a caller might want to split units that have different delivery dates into different ranges, and then further split ranges that have different PO vendors into different groups of ranges,
		 * and then further split ranges with different loadsheets into groups of groups of ranges.
		 * 	To do that, they would call: {@code new UnitRangeBuilder().byUnitNumber().by({date-extractor-function}).andThen().by({vendor-extractor-function}).andThen().by({loadsheet-extractor-function}).build(units)}
		 * @return The builder, for method chaining
		 */
		public UnitRangeFourLayerBuilder<T> andThen()
		{
			return new UnitRangeFourLayerBuilder<>(this);
		}
	
		private String getKey(T unit)
		{
			return getBuilderKey(unit, keyFunctions);
		}
	
		/**
		 * Groups the given units together into ranges based on the criteria this builder is configured for.
		 * 	For instance, calling {@code new UnitRangeBuilder().byUnitNumber().by({address-extracting-function}).andThen().by({poVendor-extracting-function}).andThen().by({scheduleA-extracting-function}).build()}
		 * 	on units:<p>
				1(from Morgan  to PA, SchA 17-01)<br/>
		 * 		2(from Morgan  to AZ, SchA 17-01)<br/>
		 * 		3(from Supreme to AZ, SchA 17-02)<br/>
		 * 		4(from Kidron  to PA, SchA 17-02)<br/>
		 * 		5(from Kidron  to PA, SchA 17-03)<br/>
		 * 		6(from Kidron  to PA, SchA 17-03)
		 * </p>would result in the following ranges:<p>
		 * 	<pre>
		 		{
		 			{
		 				{[1], [2]}
			 		}
			 		{
			 			{[3]}
			 			{[4]}
			 		}
			 		{
			 			{[5-6]}
			 		}
			 	}
		 	</pre>
		 * </p>
		 * @param units The units to place into groups.
		 * @return A set of ranges, each of which contains one or more units. Each unit range will be sorted from smallest to largest unit number.
		 */
		public Set<Set<Set<List<T>>>> build(Collection<T> units)
		{
			if(units == null || units.isEmpty())
				return Collections.emptySet();
			//Early exit if we have only one unit - it's always going to be a single range
			if(units.size() == 1)
			{
				Set<Set<Set<List<T>>>> result = new HashSet<Set<Set<List<T>>>>();
				Set<Set<List<T>>> inner = new HashSet<Set<List<T>>>();
				Set<List<T>> inner2 = new HashSet<List<T>>();
				inner2.add(new ArrayList<T>(units));
				inner.add(inner2);
				result.add(inner);
				return result;
			}
			
			UnitRangeFourLayerBuilder<T> builder = this.andThen();
			
			Set<Set<Set<Set<List<T>>>>> groupedUnits = performGrouping(units, builder, parent.parent.timingBean);
			
			//Because the other layer was artificially added, we can safely assume it won't be grouped any differently than the first two layers, so the outer set
			// should have only one element. Thus, it's sufficient to get a single sample element from the outer set and return that.
			Set<Set<Set<List<T>>>> result = Util.getSampleElement(groupedUnits);
			
			return result;
		}
	}
	
	public static class UnitRangeFourLayerBuilder<T extends SortableUnit>
	{
		/** The one-level builder that defines the next lower level of grouping units together. */
		private final UnitRangeThreeLayerBuilder<T> parent;
		
		private List<Function<T, String>> keyFunctions = new ArrayList<>();
		
		/**
		 * Creates a new fourth-level builder for grouping unit ranges.
		 * @param parent The third-level builder that defines the next lower-level of grouping units together.
		 */
		private UnitRangeFourLayerBuilder(UnitRangeThreeLayerBuilder<T> parent)
		{
			this.parent = parent;
		}
		
		public UnitRangeFourLayerBuilder<T> by(Function<T, String> keyExtractor)
		{
			this.keyFunctions.add(keyExtractor);
			return this;
		}
	
		private String getKey(T unit)
		{
			return getBuilderKey(unit, keyFunctions);
		}

		/**
		 * Groups the given units together into ranges based on the criteria this builder is configured for.
		 * @param units The units to place into groups.
		 * @return A set of ranges, each of which contains one or more units. Each unit range will be sorted from smallest to largest unit number.
		 */
		public Set<Set<Set<Set<List<T>>>>> build(Collection<T> units)
		{
			if(units == null || units.isEmpty())
				return Collections.emptySet();
			//Early exit if we have only one unit - it's always going to be a single range
			if(units.size() == 1)
			{
				Set<Set<Set<Set<List<T>>>>> result = new HashSet<Set<Set<Set<List<T>>>>>();
				Set<Set<Set<List<T>>>> inner = new HashSet<Set<Set<List<T>>>>();
				Set<Set<List<T>>> inner2 = new HashSet<Set<List<T>>>();
				Set<List<T>> inner3 = new HashSet<List<T>>();
				inner3.add(new ArrayList<T>(units));
				inner2.add(inner3);
				inner.add(inner2);
				result.add(inner);
				return result;
			}
			
			Set<Set<Set<Set<List<T>>>>> groupedUnits = performGrouping(units, this, parent.parent.parent.timingBean);
			
			return groupedUnits;
		}
	}
}