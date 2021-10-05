package com.penske.apps.smccore.base.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.penske.apps.smccore.CoreTestUtil;

/**
 * Class under test: {@link DateUtil}.
 * Exercise all the various loadsheet date utility methods.
 */
public class DateUtilTest {

	@Test
	public void shouldParseDate()
	{
		assertThat(DateUtil.parseDate("2017-01-01"), is(CoreTestUtil.dateAt(2017, 1, 1)));			//Valid date (ISO format)
		assertThat(DateUtil.parseDate("01/01/2017"), is(CoreTestUtil.dateAt(2017, 1, 1)));			//Valid date (US format w/ 4-digit year)
		assertThat(DateUtil.parseDate("01/01/17"),   is(CoreTestUtil.dateAt(2017, 1, 1)));			//Valid date (US format w/ 2-digit year)
		assertThat(DateUtil.parseDate("01/45/2017", false),  is(CoreTestUtil.dateAt(2017, 2, 14)));	//Using non-strict parsing, this date rolls over to February 14, 2017
		assertThat(DateUtil.parseDate("01/45/2017", true),   is(nullValue()));			//Strict parsing - January 45th doesn't exist
		assertThat(DateUtil.parseDate("02/29/2020", true),   is(CoreTestUtil.dateAt(2020, 2, 29)));	//Valid leap-year value
		assertThat(DateUtil.parseDate("02/29/2018", true),   is(nullValue()));			//No leap-year in 2018
		assertThat(DateUtil.parseDate("13/01/2017", true),   is(nullValue()));			//Strict parsing - there is no 13th month
		assertThat(DateUtil.parseDate("01/01/1800", true),   is(nullValue()));			//Special SMC rule - strict dates can't be before January 1, 1900.
	}
	
	@Test
	public void shouldParseLocalDate()
	{
		assertThat(DateUtil.parseLocalDate("2017-01-01"), is(LocalDate.of(2017, 1, 1)));			//Valid date (ISO format)
		assertThat(DateUtil.parseLocalDate("01/01/2017"), is(LocalDate.of(2017, 1, 1)));			//Valid date (US format w/ 4-digit year)
		assertThat(DateUtil.parseLocalDate("01/01/17"),   is(LocalDate.of(2017, 1, 1)));			//Valid date (US format w/ 2-digit year)
		assertThat(DateUtil.parseLocalDate("01/45/2017", false),  is(LocalDate.of(2017, 2, 14)));	//Using non-strict parsing, this date rolls over to February 14, 2017
		assertThat(DateUtil.parseLocalDate("01/45/2017", true),   is(nullValue()));			//Strict parsing - January 45th doesn't exist
		assertThat(DateUtil.parseLocalDate("02/29/2020", true),   is(LocalDate.of(2020, 2, 29)));	//Valid leap-year value
		assertThat(DateUtil.parseLocalDate("02/29/2018", true),   is(nullValue()));			//No leap-year in 2018
		assertThat(DateUtil.parseLocalDate("13/01/2017", true),   is(nullValue()));			//Strict parsing - there is no 13th month
		assertThat(DateUtil.parseLocalDate("01/01/1800", true),   is(nullValue()));			//Special SMC rule - strict dates can't be before January 1, 1900.
	}

	@Test
	public void shouldGetDaysBetween()
	{
		assertThat(DateUtil.getDaysBetween(null				  			   , CoreTestUtil.dateAt(2018, 2, 16), true), is(0));		//Bogus input (null)
		
		//Exclude weekends
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  1), CoreTestUtil.dateAt(2018, 2, 16), true), is(34));		//Start on a working day
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  6), CoreTestUtil.dateAt(2018, 2, 16), true), is(30));		//Start on a Saturday
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  7), CoreTestUtil.dateAt(2018, 2, 16), true), is(30));		//Start on a Sunday
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 21), CoreTestUtil.dateAt(2018, 2, 16), true), is(3));		//Start date after end date
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 16), CoreTestUtil.dateAt(2018, 2, 16), true), is(0));		//Start and end are the same
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 15), CoreTestUtil.dateAt(2018, 2, 16), true), is(1));		//Start and end are adjacent
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 16, 12, 0, 0, 0), CoreTestUtil.dateAt(2018, 2, 16, 8, 0, 0, 0), true), is(0));		//Start and end are the same day, but different times of day
		
		//Include weekends
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  1), CoreTestUtil.dateAt(2018, 2, 16), false), is(46));		//Start on a working day
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  6), CoreTestUtil.dateAt(2018, 2, 16), false), is(41));		//Start on a Saturday
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 1,  7), CoreTestUtil.dateAt(2018, 2, 16), false), is(40));		//Start on a Sunday
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 21), CoreTestUtil.dateAt(2018, 2, 16), false), is(5));		//Start date after end date
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 16), CoreTestUtil.dateAt(2018, 2, 16), false), is(0));		//Start and end are the same
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 15), CoreTestUtil.dateAt(2018, 2, 16), false), is(1));		//Start and end are adjacent
		assertThat(DateUtil.getDaysBetween(CoreTestUtil.dateAt(2018, 2, 16, 12, 0, 0, 0), CoreTestUtil.dateAt(2018, 2, 16, 8, 0, 0, 0), false), is(0));		//Start and end are the same day, but different times of day
	}
	
	@Test
	public void shouldFormatDateUS()
	{
		assertThat(DateUtil.formatDateUS(CoreTestUtil.dateAt(2018, 1, 1)), is("01/01/2018"));					//Ordinary date
		assertThat(DateUtil.formatDateUS(CoreTestUtil.dateAt(2020, 2, 29)), is("02/29/2020"));					//Leap year
		assertThat(DateUtil.formatDateUS(CoreTestUtil.dateAt(2018, 1, 1, 23, 59, 59, 999)), is("01/01/2018"));	//1 millisecond before midnight
		assertThat(DateUtil.formatDateUS((Date) null), is(nullValue()));										//Null
		
		assertThat(DateUtil.formatDateUS(LocalDate.of(2018, 1, 1)), is("01/01/2018"));							//Ordinary date
		assertThat(DateUtil.formatDateUS(LocalDate.of(2020, 2, 29)), is("02/29/2020"));							//Leap year
		assertThat(DateUtil.formatDateUS(LocalDateTime.of(2018, 1, 1, 23, 59, 59, 999)), is("01/01/2018"));	//1 millisecond before midnight
		assertThat(DateUtil.formatDateUS((LocalDateTime) null), is(nullValue()));								//Null
	}
	
	@Test
	public void shouldFormatDateISO()
	{
		assertThat(DateUtil.formatDateISO(CoreTestUtil.dateAt(2018, 1, 1)), is("2018-01-01"));					//Ordinary date
		assertThat(DateUtil.formatDateISO(CoreTestUtil.dateAt(2020, 2, 29)), is("2020-02-29"));					//Leap year
		assertThat(DateUtil.formatDateISO(CoreTestUtil.dateAt(2018, 1, 1, 23, 59, 59, 999)), is("2018-01-01"));	//1 second before midnight
		assertThat(DateUtil.formatDateISO((Date) null), is(nullValue()));										//Null
		
		assertThat(DateUtil.formatDateISO(LocalDate.of(2018, 1, 1)), is("2018-01-01"));							//Ordinary date
		assertThat(DateUtil.formatDateISO(LocalDate.of(2020, 2, 29)), is("2020-02-29"));						//Leap year
		assertThat(DateUtil.formatDateISO(LocalDateTime.of(2018, 1, 1, 23, 59, 59, 999)), is("2018-01-01"));	//1 millisecond before midnight
		assertThat(DateUtil.formatDateISO((LocalDateTime) null), is(nullValue()));								//Null
	}

	@Test
	public void shouldFormatDateTimeUS()
	{
		assertThat(DateUtil.formatDateTimeUS(CoreTestUtil.dateAt(2018, 1, 1)), is("01/01/2018 12:00:00 AM"));					//Ordinary date (12-hour clock)
		assertThat(DateUtil.formatDateTimeUS(CoreTestUtil.dateAt(2018, 1, 1, 23, 59, 59, 999)), is("01/01/2018 11:59:59 PM"));	//After noon
		assertThat(DateUtil.formatDateTimeUS(CoreTestUtil.dateAt(2018, 1, 1, 8, 0, 5, 0)), is("01/01/2018 08:00:05 AM"));		//Before noon
		assertThat(DateUtil.formatDateTimeUS((Date) null), is(nullValue()));													//Null
		
		assertThat(DateUtil.formatDateTimeUS(LocalDateTime.of(2018, 1, 1, 0, 0, 0)), is("01/01/2018 12:00:00 AM"));			//Ordinary date (12-hour clock)
		assertThat(DateUtil.formatDateTimeUS(LocalDateTime.of(2018, 1, 1, 23, 59, 59, 999)), is("01/01/2018 11:59:59 PM"));	//After noon
		assertThat(DateUtil.formatDateTimeUS(LocalDateTime.of(2018, 1, 1, 8, 0, 5, 0)), is("01/01/2018 08:00:05 AM"));		//Before noon
		assertThat(DateUtil.formatDateTimeUS((LocalDateTime) null), is(nullValue()));										//Null
	}
	
	@Test
	public void shouldFormatDateTimeISO()
	{
		assertThat(DateUtil.formatDateTimeISO(CoreTestUtil.dateAt(2018, 1, 1)), is("2018-01-01 00:00:00.000"));						//Ordinary date (24-hour clock)
		assertThat(DateUtil.formatDateTimeISO(CoreTestUtil.dateAt(2018, 1, 1, 23, 59, 59, 999)), is("2018-01-01 23:59:59.999"));	//After noon
		assertThat(DateUtil.formatDateTimeISO(CoreTestUtil.dateAt(2018, 1, 1, 8, 0, 5, 0)), is("2018-01-01 08:00:05.000"));			//Before noon
		assertThat(DateUtil.formatDateTimeISO((Date) null), is(nullValue()));														//Null
		
		assertThat(DateUtil.formatDateTimeISO(LocalDateTime.of(2018, 1, 1, 0, 0, 0)), is("2018-01-01 00:00:00.000"));			//Ordinary date (24-hour clock)
		assertThat(DateUtil.formatDateTimeISO(LocalDateTime.of(2018, 1, 1, 23, 59, 59, 999)), is("2018-01-01 23:59:59.000"));	//After noon
		assertThat(DateUtil.formatDateTimeISO(LocalDateTime.of(2018, 1, 1, 8, 0, 5, 0)), is("2018-01-01 08:00:05.000"));		//Before noon
		assertThat(DateUtil.formatDateTimeISO((LocalDateTime) null), is(nullValue()));											//Null
	}

	@Test
	public void shouldFormatDateForFolderName()
	{
		assertThat(DateUtil.formatDateForFolderName(CoreTestUtil.dateAt(2018, 1, 1)), is("20180101"));					//Ordinary date (12-hour clock)
		assertThat(DateUtil.formatDateForFolderName(CoreTestUtil.dateAt(2018, 1, 1, 23, 59, 59, 999)), is("20180101"));	//Date and time
		assertThat(DateUtil.formatDateTimeUS((Date) null), is(nullValue()));											//Null
	}
	
	@Test
	public void shouldGetModelYearDates()
	{
		assertThat(DateUtil.getModelYearDates(CoreTestUtil.dateAt(2018, 1, 1)), is(Arrays.asList(
			"2021", "2020", "2019", "2018", "2017", "2016", "2015", "2014", "2013", "2012", "2011", "2010", "2009", "2008"
		)));
	}
	
    @Test
    public void shouldGetDateAs400Date()
    {
    	assertThat(DateUtil.getDateAs400Date(CoreTestUtil.dateAt(2015, 10, 15)), is(1151015));	//After 2000
    	assertThat(DateUtil.getDateAs400Date(CoreTestUtil.dateAt(1985, 5, 27)), is(850527));	//Before 2000, but after 1900
    	
    	assertThat(DateUtil.getDateAs400Date(LocalDate.of(2015, 10, 15)), is(1151015));	//After 2000
    	assertThat(DateUtil.getDateAs400Date(LocalDate.of(1985, 5, 27)), is(850527));	//Before 2000, but after 1900
    }

    @Test
    public void shouldGetDateAs400USDate()
    {
    	assertThat(DateUtil.getDateAs400USDate(LocalDate.of(2015, 10, 15)), is(101515));	//After 2000
    	assertThat(DateUtil.getDateAs400USDate(LocalDate.of(1985, 5, 27)), is(52785));		//Before 2000, but after 1900
    }
    
    @Test
    public void shouldGet400DateAsDate() {
        
        
        Date date = DateUtil.get400DateAsDate(1151015); // 15 Oct 2015
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int monthDay = calendar.get(Calendar.DATE);
        
        assertThat(year, is(2015));
        assertThat(month, is(10));
        assertThat(monthDay, is(15));
        
    }
    
    @Test
    public void shouldGetStartofDay() {
        
        String dateAsString = "1990-10-02";
        
        Date date = DateUtil.parseDate(dateAsString);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        calendar.add(Calendar.HOUR, 6);
        
        int hour = calendar.get(Calendar.HOUR);
        
        assertThat(hour, is(6));
        
        
        Date startOfDay = DateUtil.startOfDay(date);
        
        calendar.setTime(startOfDay);
        
        hour = calendar.get(Calendar.HOUR);
        
        assertThat(hour, is(0));
    }
    
    @Test
    public void shouldGetMinDate() {
        
        String minDateAsString = "2015-10-02";
        String maxDateAsString = "2015-10-03";
        
        Date minDate = DateUtil.parseDate(minDateAsString);
        Date maxDate = DateUtil.parseDate(maxDateAsString);
        
        
        Date supposedMinDate = DateUtil.minDate(minDate, maxDate);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(supposedMinDate);
        
        int dayOfMonth = calendar.get(Calendar.DATE);
        assertThat(dayOfMonth, is(2));
    }
    @Test
    public void shouldGetMaxDate() {
        
        String minDateAsString = "2015-10-02";
        String maxDateAsString = "2015-10-03";
        
        Date minDate = DateUtil.parseDate(minDateAsString);
        Date maxDate = DateUtil.parseDate(maxDateAsString);
        
        
        Date supposedMaxDate = DateUtil.maxDate(minDate, maxDate);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(supposedMaxDate);
        
        int dayOfMonth = calendar.get(Calendar.DATE);
        assertThat(dayOfMonth, is(3));
        
    }
        
    @Test
    public void shouldbeDefaultAs400Date() {
        
        Calendar c = Calendar.getInstance();
        c.set(1, 0, 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        Date d = c.getTime();
        
        boolean dateIsTheAs400DefaultTime = DateUtil.isDateIsTheAs400DefaultTime(d);
        
        assertThat(dateIsTheAs400DefaultTime, is(true));
    }

    @Test
	public void shouldGetDateAsAS400Time()
	{
    	LocalDate today = LocalDate.now();
		assertThat(DateUtil.getDateAsAS400Time(null), is(0));
		assertThat(DateUtil.getDateAsAS400Time(LocalDateTime.of(today, LocalTime.of(8, 20, 33))), is(82033));
		assertThat(DateUtil.getDateAsAS400Time(LocalDateTime.of(today, LocalTime.of(14, 57, 10))), is(145710));
		assertThat(DateUtil.getDateAsAS400Time(LocalDateTime.of(today, LocalTime.of(1, 0, 0))), is(10000));
		assertThat(DateUtil.getDateAsAS400Time(LocalDateTime.of(today, LocalTime.of(0, 0, 0))), is(0));
	}
}