/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.penske.apps.smccore.CoreTestUtil;

/**
 * Class under test: {@link BatchCallable}
 */
public class BatchCallableTest
{
	private final DummyService service = mock(DummyService.class);
	
	@Captor
	private ArgumentCaptor<List<Integer>> intListCaptor;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		doAnswer(new Answer<List<String>>(){
			@Override public List<String> answer(InvocationOnMock invocation) throws Throwable {
				List<Integer> items = invocation.getArgument(0);
				
				List<String> results = new ArrayList<String>();
				for(Integer item : items)
					results.add("Item: " + item);
				return results;
			}
		}).when(service).getResults(ArgumentMatchers.<Integer>anyCollection());
	}
	
	@Test
	public void shouldCallInBatches()
	{
		List<Integer> allItems = new ArrayList<Integer>();
		for(int i = 999 ; i >= 0 ; i--)
			allItems.add(i);
		
		List<String> results = new BatchCallable<Integer, String>(allItems, 50){
			@Override protected Collection<String> runBatch(List<Integer> items) {
				return service.getResults(items);
			}
		}.call();
		
		//Check that the service gets called the correct number of times
		verify(service, times(20)).getResults(intListCaptor.capture());
		
		//Check that all arguments were passed in in the correct order (i.e. the arguments were split up correctly)
		List<Integer> args = CoreTestUtil.flattenList(intListCaptor.getAllValues());
		assertThat(args, is(allItems));
		
		//Check that the result we get back is the same as invoking the service directly on the large list
		assertThat(results, is(service.getResults(allItems)));
	}
	
	//***** HELPER CLASSES *****//
	public interface DummyService
	{
		public List<String> getResults(Collection<Integer> items);
	}
}
