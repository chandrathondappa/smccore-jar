/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.penske.apps.smccore.base.util.UnitRangeBuilder.UnitRangeThreeLayerBuilder;
import com.penske.apps.smccore.base.util.UnitRangeBuilder.UnitRangeTwoLayerBuilder;

/**
 * Class under test: {@link UnitRangeBuilder}
 */
public class UnitRangeBuilderTest
{
	private static final int DCN = 12345;
	private static final String SCHEDULE_A = "17-01";
	private static final String ADDRESS_1 = "Allentown, PA";
	private static final String ADDRESS_2 = "Irving, TX";
	private static final String ADDRESS_3 = "Denver, CO";
	private static final String DEL_JAN_1 = "2018-01-01";
	private static final String DEL_JAN_7 = "2018-01-07";
	private static final String DEL_FEB_1 = "2018-02-01";
	private static final String DEL_FEB_7 = "2018-01-07";
	private static final String DEL_MAR_1= "2018-03-01";
	private static final String LS_BODY = "Body LS";
	private static final String VENDOR_SUPREME_PA = "Supreme, PA";
	private static final String VENDOR_SUPREME_TX = "Supreme, TX";
	
	/**
	 * This is the most basic grouping test. It just makes sure that units are grouped by their unit numbers,
	 * where unit numbers can be any combination of letters and numbers.
	 */
	@Test
	public void shouldGroupUnitRangesByUnitNumberOnly()
	{
		String lsDesc = "Body LS";
		String vendor = "Morgan";
		int freight = 1200;
		
		UnitRangeBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>().byUnitNumber();
		
		assertThat("One range - all numeric", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "1", "2", "3", "4", "5")
			)),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3", "4", "5"}
			)));
		
		assertThat("One range - all numeric - out of order", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1,  "4", "3", "5", "1", "2")
			)),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3", "4", "5"}
			)));
		
		assertThat("One range - all numeric - duplicates", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "4", "3", "5", "1", "3", "2")
			)),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3", "4", "5"}
			)));
		
		assertThat("One range - with letters", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "A1B", "A5B", "A3B", "A4B", "A2B")
			)),
			is(mockUnitNumbers(
				new String[]{"A1B", "A2B", "A3B", "A4B", "A5B"}
			)));
		
		assertThat("One range - complex", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "A1B1C", "A1B5C", "A1B3C", "A1B4C", "A1B2C")
			)),
			is(mockUnitNumbers(
				new String[]{"A1B1C", "A1B2C", "A1B3C", "A1B4C", "A1B5C"}
			)));
		
		assertThat("Multiple ranges - not contiguous", getUnitNumbers(
			new UnitRangeBuilder<TestSortableUnit>()
				.byUnitNumber()
			.build(createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "3", "1", "11", "10", "2"))),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3"},
				new String[]{"10", "11"}
			)));
		
		assertThat("Multiple ranges - complex", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, lsDesc, vendor, freight, ADDRESS_1, DEL_JAN_1, "12345", "A1B1C", "12346", "A1B2C", "XXX", "A1B3C", "A1B4C", "A1B5C", "A33B2C", "A1B33C", "A1B34C", "543")
			)),
			is(mockUnitNumbers(
				s("12345", "12346"),
				s("A1B1C", "A1B2C", "A1B3C", "A1B4C", "A1B5C"),
				s("XXX"),
				s("A33B2C"),
				s("A1B33C", "A1B34C"),
				s("543")
			)));
	}
	
	@Test
	public void shouldGroupUnitRangesAcrossUnitNumberLengths()
	{
		UnitRangeBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>().byUnitNumber();
		
		assertThat("Multiple Ranges - different lengths", getUnitNumbers(
			builder.build(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "CW998", "CW999", "CW1000", "CW1001", "CW1002")
			)),
			is(mockUnitNumbers(
				new String[]{"CW998", "CW999"},
				new String[]{"CW1000", "CW1001", "CW1002"}
			))
		);
	}

	/**
	 * Tests grouping unit numbers by only a single grouping level (in this case, by their delivery address)
	 */
	@Test
	public void shouldGroupByAddress()
	{
		UnitRangeBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>()
			.byUnitNumber()
			.by(TestSortableUnit::getAddress);
		
		assertThat("Multiple Ranges, Single Address, Single Date", getUnitNumbers(builder.build(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "3", "1", "11", "10", "2")
			)),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3"},
				new String[]{"10", "11"}
			)));
		
		assertThat("Multiple Ranges, Single Address, Multiple Dates", getUnitNumbers(builder.build(flattenList(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "3", "1", "11"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_FEB_1, "10", "2")
			))),
			is(mockUnitNumbers(
				new String[]{"1", "2", "3"},
				new String[]{"10", "11"}
			)));

		assertThat("Multiple Ranges, Multiple Address, Multiple Dates", getUnitNumbers(builder.build(flattenList(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "3", "1", "11"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_2, DEL_FEB_1, "10", "2")
			))),
			is(mockUnitNumbers(
				s("1"),
				s("2"),
				s("3"),
				s("10"),
				s("11")
			)));
		
		assertThat("Multiple ranges, vendors, addresses, and dates", getUnitNumbers(builder.build(flattenList(
				//The first two sets of units have the same delivery address - they should get merged together into ranges
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "1", "3", "5", "7", "10"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_1, DEL_JAN_1, "2", "6", "8"),
				//The second set of units has a different delivery address - it should be separate
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_2, DEL_FEB_1, "4", "11", "12", "18", "12A")
			))), is(mockUnitNumbers(
				s("1", "2", "3"),
				s("5", "6", "7", "8"),
				s("4"),
				s("11", "12"),
				s("10"),
				s("18"),
				s("12A")
			)));
	}
		
	/**
	 * Tests grouping units by two grouping levels (PO vendor on one level, and freight / delivery address on another level).
	 * This is similar to what would happen on the Destination Management screen of Loadsheet (as of 2018-08-21)
	 */
	@Test
	public void shouldGroupByVendorFreightAndAddress()
	{
		//Group by Freight and Address, then by PO Vendor (like destination management screen)
		UnitRangeTwoLayerBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>()
			.byUnitNumber()
			.by(u -> String.valueOf(u.getFreight()))
			.by(TestSortableUnit::getAddress)
		.andThen()
			.by(TestSortableUnit::getVendor);
		
		assertThat("Multiple freights and districts, multiple vendors", getUnitNumbers2(builder.build(flattenList(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 800, ADDRESS_1, DEL_JAN_1, "1"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1500, ADDRESS_2, DEL_JAN_1, "2", "4"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_PA, 1800, ADDRESS_3, DEL_JAN_1, "3"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_1, DEL_JAN_1, "5")
			))),
			is(mockUnitNumbers2(
				s2(		s("1"),
						s("2"),
						s("3"),
						s("4")),
				s2(		s("5"))
			)));
	}

	/**
	 * Tests grouping units by three grouping levels (PO vendor / loadsheets on one level, dates on a second level, and then by unit number on a third level)
	 * This is similar to what would happen on the Schedule & Order screen of Loadsheet (as of 2018-08-21)
	 */
	@Test
	public void shouldGroupByVendorLoadsheetDatesAndUnitNumbers()
	{
		//Group contiguous unit number, then by dates, then by PO vendor and Loadsheet (like Schedule & Order)
		UnitRangeThreeLayerBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>()
				.byUnitNumber()
			.andThen()
				.by(TestSortableUnit::getDeliveryDate)
			.andThen()
				.by(TestSortableUnit::getVendor)
				.by(TestSortableUnit::getLoadsheetDesc);
		
		assertThat("Multiple Ranges, Single Address, Single Date", getUnitNumbers3(builder.build(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_1, DEL_JAN_1, "3", "1", "11", "10", "2")
				)),
				is(mockUnitNumbers3(
					s3(s2(
							s("1", "2", "3"),
							s("10", "11")
						)
					)
				)));
			
		assertThat("Multiple Ranges, Multiple Addresses, Single Date", getUnitNumbers3(builder.build(flattenList(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_1, DEL_JAN_1, "3", "1", "11"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_2, DEL_JAN_1, "10", "2")
			))),
			is(mockUnitNumbers3(
				s3(s2(
						s("1", "2", "3"),
						s("10", "11")
					)
				)
			)));
		
		assertThat("Multiple Ranges, Multiple Address, Multiple Dates", getUnitNumbers3(builder.build(flattenList(
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_1, DEL_JAN_1, "3", "2", "11"),
				createUnitRange(DCN, SCHEDULE_A, LS_BODY, VENDOR_SUPREME_TX, 800, ADDRESS_2, DEL_FEB_1, "10", "1")
			))),
			is(mockUnitNumbers3(
				s3(s2(
						s("2", "3"),
						s("11")
				), s2(
						s("1"),
						s("10")
					)
				)
			)));
		
		assertThat("Multiple vendors, ranges, loadsheets, and dates", getUnitNumbers3(builder.build(flattenList(
				// FTL/SUP(PA)/THK(MA)
				createUnitRange(DCN, SCHEDULE_A, "SUPREME BODY/REEFER", VENDOR_SUPREME_PA, 800, ADDRESS_1, DEL_JAN_1+DEL_FEB_1+DEL_MAR_1, "1", "2", "3", "4", "20", "21", "22"),
				// HIN/SUP(PA)/THK(MA) - different delivery address, but doesn't matter, since we're not splitting on delivery address
				createUnitRange(DCN, SCHEDULE_A, "SUPREME BODY/REEFER", VENDOR_SUPREME_PA, 800, ADDRESS_2, DEL_JAN_1+DEL_FEB_1+DEL_MAR_1, "210", "211", "212"),
				// FTL/SUP(PA)/THK(MA) - different delivery dates matters
				createUnitRange(DCN, SCHEDULE_A, "SUPREME BODY/REEFER", VENDOR_SUPREME_PA, 800, ADDRESS_2, DEL_JAN_7+DEL_FEB_1+DEL_MAR_1, "23", "15", "16", "17", "40", "41", "42"),
				// FTL/SUP(TX)/THK(MA) - different vendor matters
				createUnitRange(DCN, SCHEDULE_A, "SUPREME BODY/REEFER", VENDOR_SUPREME_TX, 1200, ADDRESS_2, DEL_JAN_1+DEL_FEB_1+DEL_MAR_1, "101", "102"),
				// FTL/SUP(TX)/THK(MA) - different body delivery dates matters, too
				createUnitRange(DCN, SCHEDULE_A, "SUPREME BODY/REEFER", VENDOR_SUPREME_TX, 1200, ADDRESS_2, DEL_JAN_1+DEL_FEB_7+DEL_MAR_1, "103", "104"),
				// FTL/MOR(PA)/THK(MA) - differenet combination of loadsheets being ordered
				createUnitRange(DCN, SCHEDULE_A, "MORGAN BODY/REEFER", "MORGAN, PA", 650, ADDRESS_1, DEL_JAN_1+DEL_FEB_1+DEL_MAR_1, "301", "302", "303")
			))),
			is(mockUnitNumbers3(
				s3(	s2(		s("1", "2", "3", "4"),
							s("20", "21", "22"),
							s("210", "211", "212")),
					s2(		s("15", "16", "17"),
							s("23"),
							s("40", "41", "42"))),
				s3(	s2(		s("101", "102")),
					s2(		s("103", "104"))),
				s3(	s2(		s("301", "302", "303")))
			)));
	}
	
	/**
	 * Tests grouping units by schedule A and DCN.
	 * This is similar to what would happen on Manage Units or Deal Review of SMCOF (as of 2018-08-21)
	 */
	@Test
	public void shouldGroupByScheduleA()
	{
		//Group by SchA / DCN (like deal review screen)
		UnitRangeBuilder<TestSortableUnit> builder = new UnitRangeBuilder<TestSortableUnit>()
			.byUnitNumber()
			.by(u -> String.valueOf(u.getDcn()))
			.by(TestSortableUnit::getScheduleA);
		
		assertThat("Multiple schedule A's, multiple DCNs", getUnitNumbers(builder.build(flattenList(
				createUnitRange(12345, "18-02", LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "3", "1", "2"),
				createUnitRange(12345, "18-03", LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "4", "5", "6"),
				createUnitRange(98765, "18-03", LS_BODY, VENDOR_SUPREME_PA, 1200, ADDRESS_1, DEL_JAN_1, "7", "8", "9")
			))),
			is(mockUnitNumbers(
				s("1", "2", "3"),
				s("4", "5", "6"),
				s("7", "8", "9")
			)));
	}
	
	//***** HELPER METHODS / CLASSES *****//
	/**
	 * Extracts just unit numbers from a group of units, for comparison.
	 * @param units The units to extract numbers from.
	 * @return The unit numbers
	 */
	private <T extends SortableUnit> List<String> getUnitNumbers(List<T> units)
	{
		List<String> results = new ArrayList<String>();
		for(SortableUnit unit : units)
			results.add(unit.getUnitNumber());
		return results;
	}

	/**
	 * Extracts just unit numbers from a group of units, for comparison.
	 * @param unitSet The units to extract numbers from.
	 * @return The unit numbers
	 */
	private <T extends SortableUnit> Set<List<String>> getUnitNumbers(Set<List<T>> unitSet)
	{
		Set<List<String>> results = new HashSet<List<String>>();
		for(List<T> units : unitSet)
		{
			List<String> unitNums = new ArrayList<String>();
			unitNums.addAll(getUnitNumbers(units));
			results.add(unitNums);
		}
		return results;
	}

	/**
	 * Extracts just unit numbers from a group of units, for comparison.
	 * @param unitSet The units to extract numbers from.
	 * @return The unit numbers
	 */
	private <T extends SortableUnit> Set<Set<List<String>>> getUnitNumbers2(Set<Set<List<T>>> unitSet)
	{
		Set<Set<List<String>>> results = new HashSet<Set<List<String>>>();
		for(Set<List<T>> units : unitSet)
		{
			Set<List<String>> unitNums = new HashSet<List<String>>();
			unitNums.addAll(getUnitNumbers(units));
			results.add(unitNums);
		}
		return results;
	}
	
	/**
	 * Extracts just unit numbers from a group of units, for comparison.
	 * @param unitSet The units to extract numbers from.
	 * @return The unit numbers
	 */
	private <T extends SortableUnit> Set<Set<Set<List<String>>>> getUnitNumbers3(Set<Set<Set<List<T>>>> unitSet)
	{
		Set<Set<Set<List<String>>>> results = new HashSet<Set<Set<List<String>>>>();
		for(Set<Set<List<T>>> units : unitSet)
		{
			Set<Set<List<String>>> unitNums = new HashSet<Set<List<String>>>();
			unitNums.addAll(getUnitNumbers2(units));
			results.add(unitNums);
		}
		return results;
	}
	
	/**
	 * Creates a mock set of units, with the given parameters and unit numbers.
	 * @param dcn The DCN number the units should have
	 * @param scheduleA The Schedule A number the units should have
	 * @param loadsheetDesc A string indicating the combination of loadsheets the units have
	 * @param vendor A String describing the vendors the units have
	 * @param freight The freight cost of the units
	 * @param address The delivery address of the units
	 * @param deliveryDate A string containing all the delivery dates of the units
	 * @param transType A string containing the transportation types
	 * @param unitNums The unit numbers to create. One unit will be returned per unit number in this array/
	 * @return The mock units.
	 */
	private List<TestSortableUnit> createUnitRange(int dcn, String scheduleA, String loadsheetDesc, String vendor, int freight, String address, String deliveryDate, String... unitNums)
	{
		List<TestSortableUnit> result = new ArrayList<TestSortableUnit>();
		for(String num : unitNums)
		{
			TestSortableUnit unit = new TestSortableUnit(num, dcn, scheduleA, loadsheetDesc, vendor, freight, address, deliveryDate);
			result.add(unit);
		}
		return result;
	}

	/**
	 * Creates a collection of unit numbers, given an array of them, for verification during unit tests.
	 * @param unitNums The unit numbers to make into a collection
	 * @return The collection of unit numbers
	 */
	private Set<List<String>> mockUnitNumbers(String[]... unitNums)
	{
		if(unitNums == null)
			return Collections.emptySet();
		
		Set<List<String>> result = new LinkedHashSet<List<String>>();
		for(String[] array : unitNums)
			result.add(Arrays.asList(array));
		
		return result;
	}
	
	/**
	 * Creates a collection of unit numbers, given an array of them, for verification during unit tests.
	 * @param unitNums The unit numbers to make into a collection
	 * @return The collection of unit numbers
	 */
	private Set<Set<List<String>>> mockUnitNumbers2(String[][]... unitNums)
	{
		if(unitNums == null)
			return Collections.emptySet();
		
		Set<Set<List<String>>> result = new LinkedHashSet<Set<List<String>>>();
		for(String[][] array : unitNums)
			result.add(mockUnitNumbers(array));
		
		return result;
	}
	
	/**
	 * Creates a collection of unit numbers, given an array of them, for verification during unit tests.
	 * @param unitNums The unit numbers to make into a collection
	 * @return The collection of unit numbers
	 */
	private Set<Set<Set<List<String>>>> mockUnitNumbers3(String[][][]... unitNums)
	{
		if(unitNums == null)
			return Collections.emptySet();
		
		Set<Set<Set<List<String>>>> result = new LinkedHashSet<Set<Set<List<String>>>>();
		for(String[][][] array : unitNums)
			result.add(mockUnitNumbers2(array));
		
		return result;
	}

	/**
	 * Creates an array of unit numbers, for verification of test methods
	 * @param unitNums The unit numbers
	 * @return The array of unit numbers
	 */
	private String[] s(String... unitNums)
	{
		return unitNums;
	}
	/**
	 * Creates an array of unit numbers, for verification of test methods
	 * @param unitNums The unit numbers
	 * @return The array of unit numbers
	 */
	private String[][] s2(String[]... unitNums)
	{
		return unitNums;
	}
	/**
	 * Creates an array of unit numbers, for verification of test methods
	 * @param unitNums The unit numbers
	 * @return The array of unit numbers
	 */
	private String[][][] s3(String[][]... unitNums)
	{
		return unitNums;
	}

	/**
	 * Turns a List of Lists into a single List by combining the elements from all the inner lists.
	 * @param collections The lists to combine into one.
	 * @return A list containing all the elements of each of the lists in the argument.
	 */
	@SafeVarargs	//This doesn't do anything that would pollute the heap, like casting to an Object[]
	private final <T> List<T> flattenList(Collection<T>... collections)
	{
		List<T> result = new ArrayList<T>();
		for(Collection<T> coll : collections)
			result.addAll(coll);
		return result;
	}
	
	/**
	 * Helper object implementing {@link SortableUnit}, which can be used for unit testing
	 */
	private static class TestSortableUnit implements SortableUnit
	{
		private final String unitNumber;
		private final int dcn;
		private final String scheduleA;
		private final String loadsheetDesc;
		private final String vendor;
		private final int freight;
		private final String address;
		private final String deliveryDate;
		
		private TestSortableUnit(String unitNumber, int dcn, String scheduleA, String loadsheetDesc, String vendor, int freight, String address, String deliveryDate)
		{
			this.unitNumber = unitNumber;
			this.dcn = dcn;
			this.scheduleA = scheduleA;
			this.loadsheetDesc = loadsheetDesc;
			this.vendor = vendor;
			this.freight = freight;
			this.address = address;
			this.deliveryDate = deliveryDate;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{Unit " + unitNumber + "}";
		}
		
		/** {@inheritDoc} */
		@Override
		public String getUnitNumber()
		{
			return unitNumber;
		}
		
		public int getDcn()
		{
			return dcn;
		}

		public String getScheduleA()
		{
			return scheduleA;
		}

		public String getLoadsheetDesc()
		{
			return loadsheetDesc;
		}

		public String getVendor()
		{
			return vendor;
		}

		public int getFreight()
		{
			return freight;
		}

		public String getAddress()
		{
			return address;
		}

		public String getDeliveryDate()
		{
			return deliveryDate;
		}
	}
}