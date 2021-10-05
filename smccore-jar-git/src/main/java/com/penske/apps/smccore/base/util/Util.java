/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

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