/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.penske.apps.smccore.CoreTestUtil;

/**
 * Class under test: {@link Util}
 */
public class UtilTest
{
	private final DummyService service = mock(DummyService.class);
	
	@Rule
	public final ExpectedException thrown = ExpectedException.none();
	
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

	@Test
	public void shouldGetTokenizedUnitNumbers()
	{
		//Null
		assertThat(Util.getTokenizedUnitNumbers(null, null, false),					is(empty()));
		
		//Empty
		assertThat(Util.getTokenizedUnitNumbers("", null, false),					is(empty()));
		
		//Single unit
		assertThat(Util.getTokenizedUnitNumbers("5", null, false), 					containsInAnyOrder("5"));
		
		//Multiple single units
		assertThat(Util.getTokenizedUnitNumbers("1,3,5", null, false),				containsInAnyOrder("1", "3", "5"));
		
		//Single unit range
		assertThat(Util.getTokenizedUnitNumbers("50-55", null, false),				containsInAnyOrder("50", "51", "52", "53", "54", "55"));
		
		//Range with start = end
		assertThat(Util.getTokenizedUnitNumbers("50-50", null, false),				containsInAnyOrder("50"));
		
		//Multiple unit ranges
		assertThat(Util.getTokenizedUnitNumbers("1-2,8,2A-5A", null, false),		containsInAnyOrder("1", "2", "2A", "3A", "4A", "5A", "8"));
		
		//Large unit range (without max size specified)
		assertThat(Util.getTokenizedUnitNumbers("0001-4000", null, false),		 	hasSize(4000));
		
		//Large unit range (with acceptable max size specified)
		assertThat(Util.getTokenizedUnitNumbers("0001-4000", 5000, false),			hasSize(4000));
		
		//Invalid unit range (unit numbers can only be numbers and capital letters)
		assertThat(Util.getTokenizedUnitNumbers("1-2,4-7*", null, false),			containsInAnyOrder("1", "2"));
		
		//Invalid unit range (end unit before start unit)
		assertThat(Util.getTokenizedUnitNumbers("1-2,7-4", null, false),			containsInAnyOrder("1", "2"));
		
		//Invalid unit range (unit numbers in a range must be the same length)
		assertThat(Util.getTokenizedUnitNumbers("1-2,5-50", null, false),			containsInAnyOrder("1", "2"));
		
		//Empty ranges, and miscellaneous separators
		assertThat(Util.getTokenizedUnitNumbers(",1-2,,-,8-9,", null, false),		containsInAnyOrder("1", "2", "8", "9"));
		
		//Spaces at odd places
		assertThat(Util.getTokenizedUnitNumbers(" 1 -2,5- 6, 7 ,8 ", null, false),	containsInAnyOrder("1", "2", "5", "6", "7", "8"));
		
		//Padded unit numbers
		assertThat(Util.getTokenizedUnitNumbers("1-3,5", null, true),				containsInAnyOrder("         1", "         2", "         3", "         5"));
		
		//Handle lower-case letters
		assertThat(Util.getTokenizedUnitNumbers("1a-2A", null, false),				containsInAnyOrder("1A", "2A"));
	}
	
	@Test
	public void shouldNotGetTokenizedUnitNumbersTooMany()
	{
		thrown.expectMessage("Unit range resulted in more than");
		Util.getTokenizedUnitNumbers("01-10", 5, false);
	}
	
	@Test
	public void shouldGetTokenizedPoNumbers()
	{
		//Null
		assertThat(Util.getTokenizedPoNumbers(null, null),					is(empty()));
		
		//Empty
		assertThat(Util.getTokenizedPoNumbers("", null),					is(empty()));
		
		//Single unit
		assertThat(Util.getTokenizedPoNumbers("5", null), 					containsInAnyOrder(5));
		
		//Multiple single units
		assertThat(Util.getTokenizedPoNumbers("1,3,5", null),				containsInAnyOrder(1, 3, 5));
		
		//Single unit range
		assertThat(Util.getTokenizedPoNumbers("50-55", null),				containsInAnyOrder(50, 51, 52, 53, 54, 55));
		
		//Range with start = end
		assertThat(Util.getTokenizedPoNumbers("50-50", null),				containsInAnyOrder(50));
		
		//Multiple unit ranges
		assertThat(Util.getTokenizedPoNumbers("1-2,8,5-7", null),			containsInAnyOrder(1, 2, 5, 6, 7, 8));
		
		//Large PO (without max size specified)
		assertThat(Util.getTokenizedPoNumbers("1-4000", null),		 		hasSize(4000));
		
		//PO number larger than max integer
		assertThat(Util.getTokenizedPoNumbers("1,49999-5000000000", null),	containsInAnyOrder(1));
		
		//PO number invalid (not digits)
		assertThat(Util.getTokenizedPoNumbers("1,5A-6", null),				containsInAnyOrder(1));
		
		//Invalid range (end before start)
		assertThat(Util.getTokenizedPoNumbers("1,7-3", null),				containsInAnyOrder(1));
		
		//Empty ranges, and miscellaneous separators
		assertThat(Util.getTokenizedPoNumbers(",1-2,,-,8-9,", null),		containsInAnyOrder(1, 2, 8, 9));
		
		//Spaces at odd places
		assertThat(Util.getTokenizedPoNumbers(" 1 -2,5- 6 , 7 ,8 ", null),	containsInAnyOrder(1, 2, 5, 6, 7, 8));
	}
	
	@Test
	public void shouldNotGetTokenizedPoNumbersTooMany()
	{
		thrown.expectMessage("PO range resulted in more than");
		Util.getTokenizedPoNumbers("1-10", 5);
	}
	
	//***** HELPER CLASSES *****//
	public interface DummyService
	{
		public List<String> getResults(Collection<Integer> items);
		
		public void processItems(Collection<Integer> items);
	}
}
