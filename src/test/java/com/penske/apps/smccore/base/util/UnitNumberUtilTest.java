/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.exception.HumanReadableException;
import com.penske.apps.smccore.base.util.UnitNumberUtil.UnitNumberChunks;

/**
 * Class under test: {@link UnitNumberUtil}
 */
public class UnitNumberUtilTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void shouldGetNextUnitNumber()
	{
		assertEquals("Numeric unit number", "123456", UnitNumberUtil.nextUnitNumber("123455"));
		assertEquals("w/ prefix", "ABC123456", UnitNumberUtil.nextUnitNumber("ABC123455"));
		assertEquals("w/ suffix", "123456DEF", UnitNumberUtil.nextUnitNumber("123455DEF"));
		assertEquals("Both prefix and suffix", "ABC123456DEF", UnitNumberUtil.nextUnitNumber("ABC123455DEF")); // parasoft-suppress SECURITY.WSC.HCCK "This is not being used as a cryptographic key, but rather as a test case."
		assertEquals("Complex", "ABC123DEF456GHI", UnitNumberUtil.nextUnitNumber("ABC123DEF455GHI"));
		assertEquals("Don't rollover ", null, UnitNumberUtil.nextUnitNumber("ABC999DEF"));
		assertEquals("Don't rollover (plain numbers)", null, UnitNumberUtil.nextUnitNumber("99"));
		assertEquals("Null unit number", null, UnitNumberUtil.nextUnitNumber(null));
		assertEquals("Empty unit number", null, UnitNumberUtil.nextUnitNumber(""));
	}
	
	@Test
	public void shouldEnumerateUnits()
	{
		assertEquals("Same start and end", Arrays.asList("ABC12345DEF"), UnitNumberUtil.enumerateUnitRange("ABC12345DEF", "ABC12345DEF"));
		assertEquals("Only start provided", Arrays.asList("ABC12345DEF"), UnitNumberUtil.enumerateUnitRange("ABC12345DEF", null));
		assertEquals("Only end provided", Arrays.asList("ABC12345DEF"), UnitNumberUtil.enumerateUnitRange(null, "ABC12345DEF"));
		assertEquals("End before start", null, UnitNumberUtil.enumerateUnitRange("50", "25"));
		assertEquals("No numbers in unit number", null, UnitNumberUtil.enumerateUnitRange("ABC", "ABD"));
		
		assertEquals("Different lengths", null, UnitNumberUtil.enumerateUnitRange("123", "1234"));
		assertEquals("Different prefixes", null, UnitNumberUtil.enumerateUnitRange("B123", "A123"));
		assertEquals("Different suffixes", null, UnitNumberUtil.enumerateUnitRange("123Z", "123X"));
		assertEquals("Different prefix and suffix", null, UnitNumberUtil.enumerateUnitRange("A123Z", "B123X"));
		assertEquals("No Rollover", null, UnitNumberUtil.enumerateUnitRange("89", "02"));
		assertEquals("No Rollover 2", null, UnitNumberUtil.enumerateUnitRange("89", "102"));
		
		assertEquals("5 Simple Units", Arrays.asList("12", "13", "14", "15", "16"), UnitNumberUtil.enumerateUnitRange("12", "16"));
		assertEquals("5 Units", Arrays.asList("A89B", "A90B", "A91B", "A92B", "A93B"), UnitNumberUtil.enumerateUnitRange("A89B", "A93B"));
		assertEquals("3 Units Complex", Arrays.asList("A89B45C", "A89B46C", "A89B47C"), UnitNumberUtil.enumerateUnitRange("A89B45C", "A89B47C"));
	}
	
	public void shouldFailEnumerationForNulls()
	{
		thrown.expectMessage("Invalid unit range. Both start and end unit numbers are empty.");
		UnitNumberUtil.enumerateUnitRange(null, null);
	}
	
	@Test
	public void shouldFailEnumerationForTooManyUnits()
	{
		thrown.expect(HumanReadableException.class);
		thrown.expectMessage("units allowed in a single unit range");
		UnitNumberUtil.enumerateUnitRange("00000", "49999");
	}

	@Test
	public void testUnitNumberChunksComparable()
	{
		//Comparable - same length and only numeric - this is the typical case
		assertThat(new UnitNumberChunks("576242").isComparable(new UnitNumberChunks("876244")), is(true));
		
		//Comparable - same length and only differ only in last numeric chunks
		assertThat(new UnitNumberChunks("CW1000A01").isComparable(new UnitNumberChunks("CW1000A56")), is(true));
		
		//Not Comparable - different lengths
		assertThat(new UnitNumberChunks("CW999A").isComparable(new UnitNumberChunks("CW1001A")), is(false));
		
		//Not Comparable - same length, but numeric difference is not in the last numeric segment
		assertThat(new UnitNumberChunks("CW1001A2").isComparable(new UnitNumberChunks("CW1002A2")), is(false));
		
		//Unit numbers having no numeric chunks aren't comparable with any other unit number except themselves
		assertThat(new UnitNumberChunks("ABC").isComparable(new UnitNumberChunks("ABC")), is(true));
		assertThat(new UnitNumberChunks("ABC").isComparable(new UnitNumberChunks("ABD")), is(false));
		assertThat(new UnitNumberChunks("ABC").isComparable(new UnitNumberChunks("AB1")), is(false));
	}
	
	@Test
	public void testUnitNumberChunksContiguous()
	{
		//Contiguous - same length and only numeric differing by one
		assertThat(new UnitNumberChunks("375872").isContiguous(new UnitNumberChunks("375871")), is(true));
		
		//Contiguous - comparable and differ by one numeric digit in last segment
		assertThat(new UnitNumberChunks("CW1000A01").isContiguous(new UnitNumberChunks("CW1000A02")), is(true));
		
		//Not contiguous - differ by more than one integer
		assertThat(new UnitNumberChunks("CW1000A01").isContiguous(new UnitNumberChunks("CW1000A08")), is(false));
		
		//Not contiguous - not comparable
		assertThat(new UnitNumberChunks("A01").isContiguous(new UnitNumberChunks("B02")), is(false));
	}

	@Test
	public void testFormatUnitRangesFromStrings()
	{
		//One unit range, single unit
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(CoreTestUtil.setOf(Arrays.asList("1")), false), is(Arrays.asList("1")));
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(CoreTestUtil.setOf(Arrays.asList("1")), true), is(Arrays.asList("1-1")));
		
		//Multiple unit ranges, one with single, one with multiple, treat single unit as range
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(CoreTestUtil.setOf(
			Arrays.asList("1"),
			Arrays.asList("10", "11", "12")
		), false), is(Arrays.asList("1", "10-12")));
		
		//Multiple unit ranges, one with single, one with multiple, don't treat single unit as range
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(CoreTestUtil.setOf(
				Arrays.asList("1"),
				Arrays.asList("10", "11", "12")
		), true), is(Arrays.asList("1-1", "10-12")));
		
		//Boundary conditions
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(null, true), is(Collections.emptyList()));
		assertThat(UnitNumberUtil.formatUnitRangesFromStrings(Collections.emptySet(), true), is(Collections.emptyList()));
	}
}
