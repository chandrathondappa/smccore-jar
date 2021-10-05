/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Indicates a task that can accept a collection of items, which can be handed to the task in multiple chunks to run them in batches.
 * This is usually used to execute a database save in chunks so we don't overflow the maximum DB2 statement size or parameter count.
 * 
 * This is different from {@link BatchRunnable} in that this returns a result
 */
public abstract class BatchCallable<T, R> implements Callable<List<R>>
{
	private final int batchLimit;
	private final List<T> allItems;
	
	public BatchCallable(Collection<T> allItems, int batchLimit)
	{
		this.batchLimit = batchLimit;
		if(allItems == null)
			this.allItems = Collections.emptyList();
		else if(allItems instanceof ArrayList)
			this.allItems = (ArrayList<T>) allItems;
		else
			this.allItems = new ArrayList<T>(allItems);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<R> call()
	{
		List<R> results = new ArrayList<R>();
		for(int i = 0; i< allItems.size(); i+= batchLimit)
		{
			int maxIndex = Math.min(allItems.size(), i + batchLimit);
			Collection<R> batchResults = this.runBatch(allItems.subList(i, maxIndex));
			if(batchResults != null)
				results.addAll(batchResults);
		}
		return results;
	}
	
	/**
	 * Executes the actual action to be run in batch.
	 * @param items The subset of items to operate on in this execution.
	 * @return A collection of result objects. If your action only returns one result, then you can make use of {@link Arrays#asList(Object...)} to wrap that single object in a collection.
	 */
	protected abstract Collection<R> runBatch(List<T> items);
}
