/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;


/**
 * Bean to hold timing information for various pieces of the application
 * (where "timing" means how long that piece took to execute on the server).
 */
public interface TimingBean
{
	public void logTiming(TimingType timingType, long elapsedTime);

	/** Clears all timing data from the bean, and resets it to a fresh state */
	public void reset();
	
	/**
	 * Records the timing for a single event that occurred in the current thread.
	 * If an event for the given TimingType has already been recorded, this event's time will be added to it.
	 * @param timingType The type of event being recorded. Timings are aggregated by TimingType.
	 * @param elapsedTime The amount of time the event took.
	 * @param queryName Optional. A unique name for the query that was run, if there was one run. Only required if this is logging a query timing type.
	 */
	public void logTiming(TimingType timingType, long elapsedTime, String queryName);
	
	/**
	 * Gets the total length of all events of a given {@link TimingType} that have been logged 
	 * @param timingType The type of event for which to fetch timing information.
	 * @return The total duration of all events of the given TimingType.
	 */
	public Long getTotalElapsedTime(TimingType timingType);
	
	/**
	 * Gets the total number of times a given timing type has been logged in this bean.
	 * @param timingType The type of event for which to fetch timing information. 
	 * @return The total number of times this timing type has been logged.
	 */
	public Long getTotalInvocations(TimingType timingType);
	
	/**
	 * Gets an unmodifiable map of all timing information for this bean.
	 * @return The total duration of events (left-hand element of the pair) and how many times they were invoked (right-hand element of the pair) for each TimingType.
	 */
	public Map<TimingType, Pair<Long, Long>> getAllTimings();

	/**
	 * Gets an unmodifiable map of all query timing information for this bean, broken down by query name.
	 * @return The total duration of queries (left-hand element of the pair) and how many times they were invoked (right-hand element of the pair) for each query name.
	 */
	public Map<String, Pair<Long, Long>> getAllQueryTimings();
}
