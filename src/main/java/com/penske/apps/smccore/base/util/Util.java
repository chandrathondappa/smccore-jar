/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.penske.apps.smccore.base.exception.HumanReadableException;

/**
 * Class containing utility functions useful in all of SMC.
 */
//FIXME: test
public final class Util
{
	/** Can't instantiate a utility class */
	private Util() {}

	/**
	 * Attempts to run a function that might throw an unchecked exception, running the associated exception handler if one is thrown.
	 * 	This can be used, for example, to swallow an exception or translate it into some other format. Or, it can be used to wrap an exception and re-throw it. 
	 * @param function The function to run
	 * @param exceptionHandler If the function throws an unchecked exception, this function will be called and its result returned instead.
	 * @return If no exception was thrown, the results of running {@code function}. If an exception was thrown, the results of the exception handler.
	 */
	public static <T, R> Function<T, R> tryTo(Function<T, R> function, Function<RuntimeException, R> exceptionHandler)
	{
		return tryTo(function, exceptionHandler, RuntimeException.class);
	}
	
	/**
	 * Attempts to run a function that might throw a specific class of unchecked exception, running the associated handler if one is thrown, and re-throwing any non-matching unchecked exceptions
	 * @param function The function to attempt to run
	 * @param exceptionHandler An exception handler function that can accept the specific class of exception given by {@link exceptionClass}
	 * @param exceptionClass If there is an exception thrown and it is the same or a subclass of this parameter, the handler will be run on it.
	 * 	If an exception is thrown, but it doesn't match this parameter, the exception will be rethrown. 
	 * @return If no exception was thrown, the results of running {@code function}. If an exception matching {@code exceptionClass} was thrown, the results of the exception handler.
	 */
	public static <T, R, E extends RuntimeException> Function<T, R> tryTo(Function<T, R> function, Function<E, R> exceptionHandler, Class<E> exceptionClass)
	{
		return t -> {
			try {
				return function.apply(t);
			} catch(RuntimeException e) {
				if(exceptionClass.isAssignableFrom(e.getClass()))
				{
					E ex = exceptionClass.cast(e);
					return exceptionHandler.apply(ex);
				}
				else
					throw e;
			}
		};
	}

	/**
	 * Divides the given stream of elements up into chunks of at most {@code batchLimit} size, and performs the given action on each chunk sequentially.
	 * 	If the stream is ordered, the elements are put into the chunks in order.
	 * @param <T> The type of object being operated on
	 * @param allElements All the elements that the given action should be performed on
	 * @param batchLimit The maximum size of the chunks that will be handed to {@code action} for processing
	 * @param action The action to perform on the elements. Accepts a chunk pulled from {@code allElements} and processes that chunk.
	 */
	public static <T> void batchStream(Stream<T> allElements, int batchLimit, Consumer<List<T>> action)
	{
		if(allElements == null)
			return;
		if(batchLimit <= 0)
			throw new IllegalArgumentException("Batch limit must be a positive number");
		
		Spliterator<T> spliterator = allElements.spliterator();
		while(true)
		{
			List<T> chunk = new ArrayList<T>(batchLimit);
			for(int i = 0; i < batchLimit && spliterator.tryAdvance(chunk::add) ; i++) {}
			if(chunk.isEmpty())
				break;
			
			action.accept(chunk);
		}
	}
	
	/**
	 * Divides the given stream of elements up into chunks of at most {@code batchLimit} size, and performs the given action on each chunk sequentially,
	 * 	<b>aggregating and returning the results</b>.
	 * @param <T> The type of object being operated on
	 * @param <R> The type of object that results from the operation
	 * @param allElements All the elements that the given action should be performed on
	 * @param batchLimit The maximum size of the chunks that will be handed to {@code action} for processing
	 * @param action The action to perform on the elements, which returns a result of type {@code List<R>}. Accepts a chunk pulled from {@code allElements} and processes that chunk.
	 * @return A collection of result objects aggregated from calling {@code action} on each chunk of the given stream.
	 */
	public static <T, R> List<R> batchStreamWithResults(Stream<T> allElements, int batchLimit, Function<List<T>, Collection<R>> action)
	{
		if(allElements == null)
			allElements = Stream.empty();
		if(batchLimit <= 0)
			throw new IllegalArgumentException("Batch limit must be a positive number");
		
		List<R> result = new ArrayList<>();
		Spliterator<T> spliterator = allElements.spliterator();
		while(true)
		{
			List<T> chunk = new ArrayList<T>(batchLimit);
			for(int i = 0; i < batchLimit && spliterator.tryAdvance(chunk::add); i++) {}
			if(chunk.isEmpty())
				break;
			
			Collection<R> chunkResult = action.apply(chunk);
			
			if(chunkResult != null)
				result.addAll(chunkResult);
		}
		
		return result;
	}
	
	/** 
	 * This method compares two BigDecimal objects for equal values (using compareTo()). It takes care of nulls.
	 * @param bigDecimalOne The first number to compare.
	 * @param bigDecimal2 The second number to compare.
	 * @return boolean True if both arguments are null, or if {@link BigDecimal#compareTo(BigDecimal)} returns 0. False otherwise.
	 */
	public static boolean isEqualBigDecimals(BigDecimal bigDecimalOne, BigDecimal bigDecimal2) {
	    
	    boolean bigDecimalOneIsNull = bigDecimalOne == null;
	    boolean bigDecimalTwoIsNull = bigDecimal2 == null;
	    
	    if(bigDecimalOneIsNull) {
	        if(bigDecimalTwoIsNull) return true;
	        return false;
	    }
	    
	    if(bigDecimalTwoIsNull) return false;
	    
	    return bigDecimalOne.compareTo(bigDecimal2)==0;
	};

	/**
	 * Left-pads the given unit number to 10 characters so it will match tables that have it formatted that way (like the vehicle file).
	 * @param unitNumber The unit number to pad
	 * @return The given unit number, left-padded to 10 characters
	 */
	public static String getPaddedUnitNumber(String unitNumber)
	{
		return StringUtils.leftPad(unitNumber, 10);
	}

	/**
	 * Accepts a string containing a comma-separated list of unit numbers or unit ranges, and expands that string out into a set of individual unit numbers.
	 * 	Any spaces in the input string are first stripped out before processing.
	 * <p>
	 * 	A unit range is two unit numbers separated by a "-" character.
	 * 	In order to be a valid range, the start and end unit numbers have to be "comparable", as that term is defined in {@link UnitNumberUtil#enumerateUnitRange(String, String)}.
	 * 	Unit ranges that are not valid are ignored.
	 * </p>
	 * @param unitRangeString The string containing lists of unit ranges, to be broken up into a set of unit numbers.
	 * @param maxResultSize Optional. If this is not null, and if {@code unitRangeString} expands to more than {@code maxResultSize} unit numbers, then this method will throw an exception.
	 * 	If this is null, then lists representing arbitrarily large numbers of units are allowed.
	 * @param padUnitNumbers True if the unit numbers should be left-padded with spaces, as per {@link #getPaddedUnitNumber(String)}, before they are added to the result set.
	 * 	This may be useful if the result is going to be used in a query matching against a field where unit numbers are represented as left-padded with spaces.
	 * @return The set of unit numbers represented by the range. NOTE: these unit numbers are not in any particular order.
	 */
	public static Set<String> getTokenizedUnitNumbers(String unitRangeString, Integer maxResultSize, boolean padUnitNumbers)
	{
		if(unitRangeString == null)
			unitRangeString = "";
		
		Set<String> result = new HashSet<>();
		
		//Split into ranges by commas first
		String[] ranges = StringUtils.split(StringUtils.trim(StringUtils.upperCase(unitRangeString)), ",");
		for(String range : ranges)
		{
			//Then split ranges into start and end unit numbers by hyphens
			String[] parts = StringUtils.split(StringUtils.trim(range), "-");
			if(parts == null || parts.length == 0)
				continue;
			
			if(parts.length == 1)
				result.add(parts[0].trim());
			else
			{
				String start = parts[0].trim();
				String end = parts[parts.length - 1].trim();
				List<String> unitNumbers = UnitNumberUtil.enumerateUnitRange(start, end);
				if(unitNumbers != null)
					result.addAll(unitNumbers);
			}
		}
		
		//If this exceeds the max allowable size, then bail out
		if(maxResultSize != null && result.size() > maxResultSize)
			throw new HumanReadableException("Unit range resulted in more than " + maxResultSize + " units. Try narrowing your unit range.", false);
		
		//Make sure all the unit numbers are appropriately padded
		if(padUnitNumbers)
			result = result.stream().map(Util::getPaddedUnitNumber).collect(toSet());
		
		return result;
	}

	/**
	 * Accepts a string containing a comma-separated list of PO numbers or PO number ranges, and expands that string out into a set of individual PO numbers.
	 * 	Any spaces in the input string are first stripped out before processing.
	 * <p>
	 * 	A PO number range is two PO numbers separated by a "-" character.
	 * 	PO number ranges where the start PO number is larger than the end PO number will be ignored.
	 * 	PO number ranges where one of the endpoints is not a non-negative number will be ignored, as will single PO numbers that are not non-negative integers
	 * </p>
	 * @param poRangeString The string containing lists of PO number ranges, to be broken up into a set of PO numbers.
	 * @param maxResultSize Optional. If this is not null, and if {@code poRangeString} expands to more than {@code maxResultSize} PO numbers, then this method will throw an exception.
	 * 	If this is null, then lists representing arbitrarily large numbers of POs are allowed.
	 * @return The set of PO numbers represented by the range. NOTE: these PO numbers are not in any particular order.
	 */
	public static Set<Integer> getTokenizedPoNumbers(String poRangeString, Integer maxResultSize)
	{
		if(poRangeString == null)
			poRangeString = "";
		
		Set<Integer> result = new HashSet<>();
		
		//Split into ranges by commas first
		String[] ranges = StringUtils.split(StringUtils.trim(poRangeString), ",");
		for(String range : ranges)
		{
			//Then split ranges into start and end unit numbers by hyphens
			String[] parts = StringUtils.split(StringUtils.trim(range), "-");
			if(parts == null || parts.length == 0)
				continue;
			
			//If any of the parts of the range are outside the size of an integer, then discard the range.
			// It can't possibly match any PO numbers in our system, since PO numbers are integers
			if(Stream.of(parts)
				.map(StringUtils::trim)
				.anyMatch(p -> p == null || !NumberUtils.isDigits(p) || Long.valueOf(p) > Integer.MAX_VALUE || Long.valueOf(p) < 0))
			{
				continue;
			}
			
			if(parts.length == 1)
				result.add(Integer.valueOf(parts[0].trim()));
			else
			{
				String start = parts[0].trim();
				String end = parts[parts.length - 1].trim();
				List<Integer> poNumbers = new ArrayList<>();
				if((start != null && !start.isEmpty()) && (end != null && !end.isEmpty())) {
					int startInt = Integer.parseInt(start);
					int endInt = Integer.parseInt(end);
					
					IntStream.rangeClosed(startInt, endInt).forEach(no -> {
						poNumbers.add(no);
				    });
				}
				if(poNumbers != null && !poNumbers.isEmpty())
					result.addAll(poNumbers);
			}
		}
		
		//If this exceeds the max allowable size, then bail out
		if(maxResultSize != null && result.size() > maxResultSize)
			throw new HumanReadableException("PO range resulted in more than " + maxResultSize + " POs. Try narrowing your PO range.", false);
		
		return result;
	}
	
	/**
	 * Gets a single element from the given collection. Which specific element is returned is not defined.
	 * @param elements The collection to pick an element out of
	 * @return A single element from the collection. If the collection is null or there are no elements, null is returned.
	 */
	public static <T> T getSampleElement(Collection<T> elements)
	{
		if(elements == null)
			return null;
		
		for(T element : elements)
			return element;
		
		return null;
	}
}