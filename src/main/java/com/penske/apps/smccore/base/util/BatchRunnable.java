/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Indicates a task that can accept a collection of items, which can be handed to the task in multiple chunks to run them in batches.
 * This is usually used to execute a database save in chunks so we don't overflow the maximum DB2 statement size or parameter count.
 */
public abstract class BatchRunnable<T> implements Runnable
{
	private final int batchLimit;
	private final List<T> allItems;
	
	public BatchRunnable(Collection<T> allItems, int batchLimit)
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
	public void run()
	{
		for(int i = 0; i< allItems.size(); i+= batchLimit)
		{
			int maxIndex = Math.min(allItems.size(), i + batchLimit);
			this.runBatch(allItems.subList(i, maxIndex));
		}
	}
	
	/**
	 * Executes the actual action to be run in batch.
	 * @param items The subset of items to operate on in this execution.
	 */
	protected abstract void runBatch(List<T> items);
}
