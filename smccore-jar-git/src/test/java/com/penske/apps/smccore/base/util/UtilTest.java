/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import com.penske.apps.smccore.CoreTestUtil;

/**
 * Class under test: {@link Util}
 */
public class UtilTest
{
	private final DummyService service = mock(DummyService.class);
	
	@Captor
	private ArgumentCaptor<List<Integer>> intListCaptor;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
//		doAnswer(new Answer<List<String>>(){
//			@Override public List<String> answer(InvocationOnMock invocation) throws Throwable {
//				List<Integer> items = invocation.getArgument(0);
//				
//				List<String> results = new ArrayList<String>();
//				for(Integer item : items)
//					results.add("Item: " + item);
//				return results;
//			}
//		}).when(service).getResults(ArgumentMatchers.<Integer>anyCollection());
	}
	
	@Test
	public void shouldBatchStream()
	{
		List<Integer> items = Stream.iterate(999, i -> i-1).limit(1000).collect(toList());
		
		Util.batchStream(items.stream(), 50, chunk -> service.processItems(chunk));
		
		//Check that the service gets called the correct number of times
		verify(service, times(20)).processItems(intListCaptor.capture());
		
		//Check that all arguments were passed in in the correct order (i.e. the arguments were split up correctly)
		List<Integer> args = CoreTestUtil.flattenList(intListCaptor.getAllValues());
		assertThat(args, is(items));
	}
	
	@Test
	public void shouldBatchStreamWithResults()
	{
		List<Integer> items = Stream.iterate(999, i -> i-1).limit(1000).collect(toList());
		
		List<String> results = Util.batchStreamWithResults(items.stream(), 50, chunk -> service.getResults(chunk));
		
		//Check that the service gets called the correct number of times
		verify(service, times(20)).getResults(intListCaptor.capture());
		
		//Check that all arguments were passed in in the correct order (i.e. the arguments were split up correctly)
		List<Integer> args = CoreTestUtil.flattenList(intListCaptor.getAllValues());
		assertThat(args, is(items));
		
		//Check that the result we get back is the same as invoking the service directly on the large list
		assertThat(results, is(service.getResults(items)));
	}
	
	//***** HELPER CLASSES *****//
	public interface DummyService
	{
		public List<String> getResults(Collection<Integer> items);
		
		public void processItems(Collection<Integer> items);
	}
}
