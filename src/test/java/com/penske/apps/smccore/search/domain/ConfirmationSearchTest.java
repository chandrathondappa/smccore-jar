/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.search.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.penske.apps.smccore.TestData;

/**
 * Class under test: {@link ConfirmationSearch}
 */
public class ConfirmationSearchTest
{
	private final ConfirmationSearch search = new ConfirmationSearch();
	
	private final TestData data = new TestData();
	
	@Rule
	public final ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void shouldRunAsPenskeUser()
	{
		search.asUser(data.userPenske, Arrays.asList(1, 2, 3));
		assertThat(search.getVendorIdsFromFilter(), contains(1, 2, 3));
		
		search.asUser(data.userPenske, null);
		assertThat(search.getVendorIdsFromFilter(), is(nullValue()));
		
		search.asUser(data.userPenske, Collections.emptyList());
		assertThat(search.getVendorIdsFromFilter(), is(nullValue()));
	}
	
	@Test
	public void shouldRunAsVendorUser()
	{
		search.asUser(data.userVendor, Arrays.asList(1, 2, 3));
		assertThat(search.getVendorIdsFromFilter(), is(nullValue()));
	}
	
	@Test
	public void shouldTokenizePoNumbers()
	{
		search.setPoNumberString("1, 2, 5");
		assertThat(search.getPoNumbers(), containsInAnyOrder(1, 2, 5));
	}
	
	@Test
	public void shouldTokenizeUnitNumbers()
	{
		search.setUnitNumberString("1-3,10");
		assertThat(search.getUnitNumbers(), containsInAnyOrder("         1", "         2", "         3", "        10"));
	}
	
	@Test
	public void shouldSearchByCO()
	{
		search.setCoOperatorString("greater");
		assertThat(search.getCoOperator(), is(">"));
		
		search.setCoOperatorString("less");
		assertThat(search.getCoOperator(), is("<"));
		
		search.setCoOperatorString("equal");
		assertThat(search.getCoOperator(), is("="));
	}
	
	@Test
	public void shouldNotSearchByCOBadOperator()
	{
		search.setCoOperatorString("greaterEqual");
		
		thrown.expectMessage("Unrecognized CO operator");
		search.getCoOperator();
	}
}
