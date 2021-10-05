/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.penske.apps.smccore.base.exception.BusinessRuleException;
import com.penske.apps.smccore.base.exception.HumanReadableException;
import com.penske.apps.smccore.base.plugins.TimingBean;

/**
 * Utility methods relating to unit numbers and unit ranges. Provides methods to group unit ranges and split them by a variety of criteria
 */
public final class UnitNumberUtil
{
	private static final int MAX_UNIT_RANGE_SIZE = 50000;
	
	//Can't instantiate a utility class
	private UnitNumberUtil() {}

	/**
	 * Fetches the next unit number (if any) after the given unit number.
	 * 
	 * <p>The "next" unit number is determined by incrementing the last group of digits in the given unit number by one,
	 * as long as such an operation does not cause the unit number to roll over (that is, as long as the "next" unit number
	 * is the same length as the given unit number).</p>
	 * 
	 * @param unitNumber The starting unit number.
	 * @return The unit number that immediately follows the given one, or null if there is no next unit number.
	 */
	public static String nextUnitNumber(String unitNumber)
	{
		if(unitNumber == null)
			return null;

		UnitNumberChunks chunks = new UnitNumberChunks(unitNumber);
		UnitNumberChunks nextChunks = chunks.getNextUnitNumber();

		return nextChunks == null ? null : nextChunks.getFullUnitNumber();
	}

	/**
	 * Given a start and an end unit number that are comparable, lists all the unit numbers in between them.
	 * Two unit numbers are comparable if you can get from one to the other by repeated calls to {@link #nextUnitNumber(String)}
	 * @param unitNumStart The lower-bound of the unit range.
	 * @param unitNumEnd The upper-bound of the unit range.
	 * @return All the unit numbers in the unit range, or null if no unit range can be formed from the given numbers.
	 */
	public static List<String> enumerateUnitRange(String unitNumStart, String unitNumEnd)
	{
		if(unitNumStart == null && unitNumEnd == null)
			throw new BusinessRuleException("Invalid unit range. Both start and end unit numbers are empty.");

		//Handle the case of a single-unit range
		if(unitNumStart == null)
			unitNumStart = unitNumEnd;
		if(unitNumEnd == null)
			unitNumEnd = unitNumStart;

		//If start and end are the same, it is a single-unit range
		if(unitNumStart.equals(unitNumEnd))
			return Arrays.asList(unitNumStart);

		//Unit numbers can not possibly be in the same range if they are different lengths.
		if(unitNumStart.length() != unitNumEnd.length())
			return null;

		UnitNumberChunks startChunks = new UnitNumberChunks(unitNumStart);
		UnitNumberChunks endChunks = new UnitNumberChunks(unitNumEnd);

		//To form a unit range, start and end have to be comparable, and the start has to be before the end.
		Integer comparison = startChunks.compareUnitNumbers(endChunks);
		if(comparison == null || comparison > 0)
			return null;

		//Always add the first unit number to the range (all ranges contain at least one unit number)
		List<String> results = new ArrayList<String>();
		results.add(unitNumStart);

		//Keep going until either we reach the end (see break statement below) or we exceed the max unit range size
		while(results.size() < MAX_UNIT_RANGE_SIZE)
		{
			startChunks = startChunks.getNextUnitNumber();

			//If there is no next unit number, no unit range can be formed.
			if(startChunks == null)
				return null;

			results.add(startChunks.getFullUnitNumber());
			if(startChunks.equals(endChunks))
				break;
		}
		if(results.size() == MAX_UNIT_RANGE_SIZE)
			throw new HumanReadableException("No more than " + MAX_UNIT_RANGE_SIZE + " units allowed in a single unit range", false);

		return results;
	}
	
	/**
	 * Given a list of unit numbers, splits them into ranges simply based on which ones are contiguous, without regard to any
	 * 	other criteria (such as which loadsheet they belong to or what their address or dates are).
	 * This method is just intended for use in code where it is not possible to fetch the actual Deliverable or Unit objects to sort them.
	 * If possible, prefer using the {@link #groupUnitRanges(Collection, boolean, boolean)} method instead of this one.
	 * @param unitNumbers The unit numbers to group.
	 * @param timingBean Optional. Bean holding runtimes of various parts of the application, for trace information
	 * @return A set of ranges, grouped based on which unit numbers are contiguous. Each range will be in order from smallest to largest unit number in that range.
	 */
	public static Set<List<String>> groupUnitRanges(Collection<String> unitNumbers, TimingBean timingBean)
	{
		if(unitNumbers == null || unitNumbers.isEmpty())
			return Collections.emptySet();

		List<UnitNumberSortableUnit> sortableUnits = new ArrayList<UnitNumberSortableUnit>(unitNumbers.size());
		for(String unitNumber : unitNumbers)
			sortableUnits.add(new UnitNumberSortableUnit(unitNumber));

		Set<List<UnitNumberSortableUnit>> groupedUnitNumbers = new UnitRangeBuilder<UnitNumberSortableUnit>(timingBean).byUnitNumber().build(sortableUnits);

		Set<List<String>> results = new LinkedHashSet<List<String>>();
		for(List<UnitNumberSortableUnit> groupOfNumbers : groupedUnitNumbers)
		{
			List<String> resultNumbers = new ArrayList<String>(groupOfNumbers.size());
			for(UnitNumberSortableUnit unitNumber : groupOfNumbers)
				resultNumbers.add(unitNumber.getUnitNumber());

			results.add(resultNumbers);
		}
		return results;
	}

	/**
	 * Takes a set of grouped sortable units and emits a list of human-readable formatted unit ranges (of the form "{start}-{end}")
	 * @param groupedUnits The sortable units to print, grouped into lists by range (such as might come out of {@link #groupUnitRanges(Collection, boolean, boolean)}).
	 * @return A list of unit range designations.
	 */
	public static <E extends SortableUnit> List<String> formatUnitRanges(Set<List<E>> groupedUnits)
	{
		return formatUnitRanges(groupedUnits, true);
	}
	
	/**
	 * Takes a set of grouped sortable units and emits a list of human-readable formatted unit ranges (of the form "{start}-{end}")
	 * @param groupedUnits The sortable units to print, grouped into lists by range (such as might come out of {@link #groupUnitRanges(Collection, boolean, boolean)}).
	 * @param showSingleUnitsAsRange If this is set to true, then a range with a single unit it will have that unit number displayed twice: once as the start of the range, and once as the end of the range.
	 * 	For example, a single unit number "1234" run through this method would emit a range of "1234-1234".
	 * 	If this is false, then such ranges will be represented by the unit number printed only once. So, for unit "1234", it will show as "1234".
	 * 	Ranges with more than one unit in them are unaffected by this parameter.
	 * @return A list of unit range designations.
	 */
	public static <E extends SortableUnit> List<String> formatUnitRanges(Set<List<E>> groupedUnits, boolean showSingleUnitsAsRange)
	{
		List<String> formattedUnitRanges = new ArrayList<String>();

		for(List<E> unitGroup : groupedUnits)
		{
			String formattedRange = formatSingleUnitRange(unitGroup, showSingleUnitsAsRange);
			if(formattedRange == null)
				continue;
			
			formattedUnitRanges.add(formattedRange);
		}

		return formattedUnitRanges;
	}
	
	/**
	 * Takes a single list of unit numbers (assumes all of them are contiguous), and emits a human-readable formatted unit range of the form "{start}-{end}")
	 * @param unitRange The units to group into a range. All of these should be contiguous and sorted by unit number.
	 * @param showSingleUnitsAsRange If this is set to true, then a range with a single unit it will have that unit number displayed twice: once as the start of the range, and once as the end of the range.
	 * 	For example, a single unit number "1234" run through this method would emit a range of "1234-1234".
	 * 	If this is false, then such ranges will be represented by the unit number printed only once. So, for unit "1234", it will show as "1234".
	 * 	Ranges with more than one unit in them are unaffected by this parameter.
	 * @return The unit range formatted as a string.
	 */
	public static <E extends SortableUnit> String formatSingleUnitRange(List<E> unitRange, boolean showSingleUnitsAsRange)
	{
		if(unitRange == null || unitRange.isEmpty())
			return null;

		E startUnit = unitRange.get(0);
		E endUnit;
		if(unitRange.size() == 1)
		{
			if(showSingleUnitsAsRange)
				endUnit = startUnit;
			else
				endUnit = null;
		}
		else
			endUnit = unitRange.get(unitRange.size() - 1);

		return startUnit.getUnitNumber() + (endUnit == null ? "" : "-" + endUnit.getUnitNumber());
	}
	
	/**
	 * Takes a set of grouped unit numbers and emits a list of human-readable formatted unit ranges (of the form "{start}-{end}")
	 * @param groupedUnits The unit numbers to print, grouped into lists by range, such as might come out of a {@link UnitRangeBuilder}
	 * @return A list of unit range designations.
	 */
	public static List<String> formatUnitRangesFromStrings(Set<List<String>> groupedUnits)
	{
		return formatUnitRangesFromStrings(groupedUnits, true);
	}
	
	/**
	 * Takes a set of grouped unit numbers and emits a list of human-readable formatted unit ranges (of the form "{start}-{end}")
	 * @param groupedUnits The unit numbers to print, grouped into lists by range, such as might come out of a {@link UnitRangeBuilder}
	 * @param showSingleUnitsAsRange If this is set to true, then a range with a single unit it will have that unit number displayed twice: once as the start of the range, and once as the end of the range.
	 * 	For example, a single unit number "1234" run through this method would emit a range of "1234-1234".
	 * 	If this is false, then such ranges will be represented by the unit number printed only once. So, for unit "1234", it will show as "1234".
	 * 	Ranges with more than one unit in them are unaffected by this parameter.
	 * @return A list of unit range designations.
	 */
	public static List<String> formatUnitRangesFromStrings(Set<List<String>> groupedUnits, boolean showSingleUnitsAsRange)
	{
		if(groupedUnits == null || groupedUnits.isEmpty())
			return Collections.emptyList();
		
		List<String> formattedUnitRanges = new ArrayList<String>();

		for(List<String> unitGroup : groupedUnits)
		{
			if(unitGroup.isEmpty())
				continue;

			String startUnit = unitGroup.get(0);
			String endUnit;
			if(unitGroup.size() == 1)
			{
				if(showSingleUnitsAsRange)
					endUnit = unitGroup.get(0);
				else
					endUnit = null;
			}
			else
				endUnit = unitGroup.get(unitGroup.size() - 1);

			formattedUnitRanges.add(startUnit + (endUnit == null ? "" : "-" + endUnit));
		}

		return formattedUnitRanges;
	}

	//***** HELPER CLASSES *****//
	/**
	 * Helper class to represent the different parts of a unit number. The parts are:
	 * <ul>
	 * <li>The numeric chunk (the last numeric segment in the number)</li>
	 * <li>The prefix (part that comes before the numeric chunk)</li>
	 * <li>The suffix (part that comes after the numeric chunk)</li>
	 * </ul>
	 * 
	 * Unit numbers make up a partially-ordered set, not a totally-ordered set, so they can not implement {@link Comparable}. However,
	 * this class contains some very similar methods to determine if two unit numbers can be compared, and to determine which is larger.
	 */
	public static class UnitNumberChunks
	{
		/** The complete unit number represented by this object. */
		private final String fullUnitNumber;
		/** The part of the unit number that comes before the numeric chunk. */
		private final String prefix;
		/** The part of the unit number that comes after the numeric chunk. */
		private final String suffix;
		/**
		 * The last numeric segment of the unit number, treated as a String. This may have leading zeroes.
		 * If the unit number has no digits, this will be null.
		 */
		private final String numericChunk;
		/** The numeric chunk, represented as an integer. If the unit number has no digits, this will be null. */
		private final Integer numericChunkInteger;

		/**
		 * Create a new representation of a unit number by separating it into its constituent chunks.
		 * @param unitNumber The unit number to represent.
		 */
		public UnitNumberChunks(String unitNumber)
		{
			String[] chunks = StringUtils.splitByCharacterType(unitNumber.toUpperCase());
			LinkedList<String> prefix = new LinkedList<String>();
			LinkedList<String> suffix = new LinkedList<String>();
			String numericChunk = null;
			Integer numericChunkInteger = null;

			//Work backwards through the unit number, and look for the last set of digits. This is the numeric chunk.
			for(int i = chunks.length - 1; i >= 0; i--)
			{
				String chunk = chunks[i];

				//If the numeric chunk is already found, all other chunks are part of the prefix.
				if(numericChunk != null)
					prefix.addFirst(chunk);
				//The numeric chunk is the first set of digits, working from the end of the unit number
				else if(NumberUtils.isDigits(chunk))
				{
					numericChunk = chunk;
					numericChunkInteger = Integer.parseInt(numericChunk);
				}
				//If the numeric chunk is not found yet, put non-numeric chunks into the suffix.
				else
					suffix.addFirst(chunk);
			}

			this.numericChunk = numericChunk;
			this.numericChunkInteger = numericChunkInteger;
			this.prefix = StringUtils.defaultString(StringUtils.join(prefix, ""));
			this.suffix = StringUtils.defaultString(StringUtils.join(suffix, ""));
			this.fullUnitNumber = StringUtils.join(this.prefix, StringUtils.defaultString(this.numericChunk), this.suffix);
		}

		/**
		 * Create a new representation of a unit number by basing on a different unit number, but providing a different numeric chunk for this one.
		 * @param parent The unit number from which to take the prefix and suffix for this one.
		 * @param numericChunk The numeric chunk that should come between the prefix and suffix.
		 * 	If this is not an integer, the numeric chunk of the resulting unit number will be null.
		 */
		private UnitNumberChunks(UnitNumberChunks parent, String numericChunk)
		{
			if(NumberUtils.isDigits(numericChunk))
			{
				this.prefix = parent.prefix;
				this.suffix = parent.suffix;
				this.numericChunk = numericChunk;
				this.numericChunkInteger = Integer.parseInt(numericChunk);
			}
			else
			{
				this.prefix = null;
				this.suffix = parent.prefix + parent.suffix;
				this.numericChunk = null;
				this.numericChunkInteger = null;
			}
			this.fullUnitNumber = StringUtils.join(this.prefix, StringUtils.defaultString(this.numericChunk), this.suffix);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return new StringBuilder("{Unit#: ")
			.append(prefix)
			.append("-")
			.append(StringUtils.defaultString(numericChunk))
			.append("-")
			.append(suffix)
			.append("}")
			.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((numericChunk == null) ? 0 : numericChunk.hashCode());
			result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
			result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 * <br/><br/>
		 * Two unit number representations are equal if they have the same prefix, suffix, and numeric chunk.
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UnitNumberChunks other = (UnitNumberChunks) obj;
			if (numericChunk == null)
			{
				if (other.numericChunk != null)
					return false;
			} else if (!numericChunk.equals(other.numericChunk))
				return false;
			if (prefix == null)
			{
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			if (suffix == null)
			{
				if (other.suffix != null)
					return false;
			} else if (!suffix.equals(other.suffix))
				return false;
			return true;
		}

		//***** MODIFIED ACCESSORS *****//
		/**
		 * Gets the unit number that comes immediately after this one.
		 * The result, if it is non-null, will have a numeric chunk one greater than the numeric chunk of this unit number.
		 * Not all unit numbers have a next unit number, though. If adding one to the numeric chunk would increase the
		 *  length of a unit number, there is no next unit number.
		 * So, for example, the unit number "A999B" has no next unit number, since "A1000B" would be a longer unit number, and a unit range
		 * can only contain unit numbers of the same length.
		 * @return The next unit number after this one, or null if no next unit number exists.
		 */
		public UnitNumberChunks getNextUnitNumber()
		{
			if(numericChunk == null)
				return null;

			int nextNumber = numericChunkInteger + 1;
			String nextChunk = StringUtils.leftPad(String.valueOf(nextNumber), numericChunk.length(), "0");
			if(nextChunk.length() != numericChunk.length())
				return null;

			return new UnitNumberChunks(this, nextChunk);
		}

		/**
		 * Checks if two unit numbers can be compared with each other meaningfully.
		 * Two unit numbers are comparable if and only if they have the same prefix and suffix, are of the same length, and both have a non-null numeric chunk.
		 * So, the unit number "1A2B" is comparable with "1A8B", but not with "1A10B" (different length), or "1A2C" (different suffix).
		 * The unit number "ABC" is not comparable with any other unit number (no numeric chunk).
		 * @param other The unit numer that will be checked against this one for comparability.
		 * @return True if this unit number can be compared with {@code other}; false if they can not be compared.
		 */
		public boolean isComparable(UnitNumberChunks other)
		{
			//Non-numeric unit numbers are only comparable to themselves, not to any other unit number
			if(numericChunk == null || other.numericChunk == null)
				return this.fullUnitNumber.equals(other.fullUnitNumber);

			//Unit numbers of different lengths can not be in the same range
			if(this.fullUnitNumber.length() != other.fullUnitNumber.length())
				return false;
			
			if(!StringUtils.equals(prefix, other.prefix))
				return false;
			if(!StringUtils.equals(suffix, other.suffix))
				return false;

			return true;
		}

		/**
		 * Checks if two unit numbers are contiguous. That is, they are comparable, and their numeric chunks differ by exactly one.
		 * @param other The unit number that will be checked against this one for contiguity.
		 * @return
		 */
		public boolean isContiguous(UnitNumberChunks other)
		{
			if(!this.isComparable(other))
				return false;

			int difference = this.numericChunkInteger - other.numericChunkInteger;
			return Math.abs(difference) == 1;
		}

		/**
		 * Compares two unit numbers, and returns a result indicating which one is larger.
		 * @param other The unit number to compare against this one.
		 * @return 0 if the two unit numbers are equal; 1 if this unit number is greater; -1 if the other unit number is greater.
		 * 	If the two unit numbers are not comparable, this method returns null.
		 */
		public Integer compareUnitNumbers(UnitNumberChunks other)
		{
			if(!this.isComparable(other))
				return null;

			int thisNumber = this.numericChunkInteger;
			int otherNumber = other.numericChunkInteger;
			return (thisNumber - otherNumber == 0) ? 0 : (thisNumber > otherNumber ? 1 : -1);
		}

		/**
		 * Retrieves the key used in the {@link #compareUnitNumbers(UnitNumberChunks)} method. This is the determining factor for comparisons,
		 * assuming two unit numbers are comparable. Thus, this number provides a means of sorting comparable unit numbers.
		 * @return A key that can be used to sort a list of comparable unit numbers. When looking at non-comparable unit numbers, this number means nothing.
		 */
		public Integer getSortKey()
		{
			return numericChunkInteger;
		}

		//***** DEFAULT ACCESSORS *****//
		/**
		 * @return The full unit number. That is, the combination of prefix, numeric chunk, and suffix.
		 */
		public String getFullUnitNumber()
		{
			return fullUnitNumber;
		}
	}

	/**
	 * Helper class to represent a {@link SortableUnit} that just contains the unit number - no other information.
	 * Of course, since this doesn't have address or loadsheet sequence or dates, it will never be able to tell
	 * if it has the same address/date/loadsheet as another Sortable Unit. As a result, any unit number grouping
	 * done with this class will <b>always</b> assume the objects have the same address, the same dates, and the same loadsheet,
	 * and will group by unit number alone.
	 */
	private static class UnitNumberSortableUnit implements SortableUnit
	{
		private final String unitNumber;

		public UnitNumberSortableUnit(String unitNumber)
		{
			this.unitNumber = unitNumber;
		}

		/** {@inheritDoc} */
		@Override
		public String getUnitNumber()
		{
			return unitNumber;
		}
	}
}