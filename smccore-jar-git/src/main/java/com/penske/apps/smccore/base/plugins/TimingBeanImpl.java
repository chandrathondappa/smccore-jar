/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.plugins;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Implementation of {@link TimingBean}
 */
//FIXME: test
public class TimingBeanImpl implements TimingBean, Serializable
{
	private static final long serialVersionUID = -7907648973985144639L;
	
	private transient ConcurrentHashMap<TimingType, AtomicLongArray> timings = new ConcurrentHashMap<TimingType, AtomicLongArray>();
	private transient ConcurrentHashMap<String, AtomicLongArray> queryTimingsByName = new ConcurrentHashMap<String, AtomicLongArray>();

	/** {@inheritDoc} */
	@Override
	public void reset()
	{
		this.timings.clear();
		this.queryTimingsByName.clear();
	}
	
	/** {@inheritDoc} */
	@Override
	public void logTiming(TimingType timingType, long elapsedTime)
	{
		logTiming(timingType, elapsedTime, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public void logTiming(TimingType timingType, long elapsedTime, String queryName)
	{
		if(timingType == null)
			return;
		
		timings.computeIfAbsent(timingType, x -> new AtomicLongArray(2));
		
		AtomicLongArray timing = timings.get(timingType);
		timing.addAndGet(0, elapsedTime);
		timing.incrementAndGet(1);
		
		if(StringUtils.isNotBlank(queryName) && timingType == CoreTimingType.SMC_CORE_QUERY)
		{
			queryTimingsByName.computeIfAbsent(queryName, x -> new AtomicLongArray(2));
			
			AtomicLongArray queryTiming = queryTimingsByName.get(queryName);
			queryTiming.addAndGet(0, elapsedTime);
			queryTiming.incrementAndGet(1);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public Long getTotalElapsedTime(TimingType timingType)
	{
		AtomicLongArray timing = timings.get(timingType);
		if(timing == null)
			return null;
		
		return timing.get(0);
	}
	
	/** {@inheritDoc} */
	@Override
	public Long getTotalInvocations(TimingType timingType)
	{
		AtomicLongArray timing = timings.get(timingType);
		if(timing == null)
			return null;
		
		return timing.get(0);
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<TimingType, Pair<Long, Long>> getAllTimings()
	{
		Map<TimingType, Pair<Long, Long>> result = new HashMap<TimingType, Pair<Long,Long>>();
		for(Entry<TimingType, AtomicLongArray> entry : timings.entrySet())
		{
			TimingType timingType = entry.getKey();
			AtomicLongArray timing = entry.getValue();
			
			Long elapsedTime = timing.get(0);
			Long invocations = timing.get(1);
			
			//This shouldn't happen, since we're checking for null when logging the timing type in the first place, but just in case ...
			if(timingType == null)
				continue;
			
			result.put(timingType, Pair.of(elapsedTime, invocations));
		}
		return Collections.unmodifiableMap(result);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Pair<Long, Long>> getAllQueryTimings()
	{
		Map<String, Pair<Long, Long>> result = new HashMap<String, Pair<Long,Long>>();
		for(Entry<String, AtomicLongArray> entry : queryTimingsByName.entrySet())
		{
			String queryName = entry.getKey();
			AtomicLongArray timing = entry.getValue();
			
			Long elapsedTime = timing.get(0);
			Long invocations = timing.get(1);
			result.put(queryName, Pair.of(elapsedTime, invocations));
		}
		return Collections.unmodifiableMap(result);
	}
}
