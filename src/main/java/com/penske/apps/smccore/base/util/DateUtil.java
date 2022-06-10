/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Class containing date utility functions for the loadsheet module.
 */
public final class DateUtil
{
	/** The default format of dates in the USA, with a 4-digit year. */
	public static final String DATE_FORMAT_US =				"MM/dd/yyyy";
	public static final String DATE_FORMAT_US_SHORT_YEAR =	"MM/dd/yy";
	private static final String DATE_FORMAT_ISO =			"yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT_ISO =		"yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_TIME_FORMAT_ISO_LOCAL_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_TIME_FORMAT_US =		"MM/dd/yyyy hh:mm:ss a";
	private static final String FOLDER_NAME_FORMAT =		"yyyyMMdd";

	private static final String[] DATE_PARSE_FORMATS = new String[]{DATE_FORMAT_ISO, DATE_FORMAT_US_SHORT_YEAR, DATE_FORMAT_US};
	
	private static final DateTimeFormatter FORMATTER_US_DATE = DateTimeFormatter.ofPattern(DATE_FORMAT_US);
	private static final DateTimeFormatter FORMATTER_US_DATE_TIME = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_US);
	private static final DateTimeFormatter FORMATTER_ISO_DATE = DateTimeFormatter.ofPattern(DATE_FORMAT_ISO);
	private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_ISO);
	private static final DateTimeFormatter PARSER_DATE = new DateTimeFormatterBuilder()
		.appendOptional(FORMATTER_US_DATE)
		.appendOptional(DateTimeFormatter.ofPattern(DATE_FORMAT_US_SHORT_YEAR))
		.appendOptional(FORMATTER_ISO_DATE)
		.parseDefaulting(ChronoField.ERA, 1)
		.toFormatter();

	private static final Date START_OF_1900;
	private static final Date DB2_DEFAULT_DATE;
	private static final LocalDate DB2_DEFAULT_LOCAL_DATE = LocalDate.of(0001, 1, 1);
	private static final LocalDate START_OF_1900_LOCAL_DATE;
	
	static {
		try {
			START_OF_1900 = DateUtils.parseDate("1900-01-01", DateFormatUtils.ISO_DATE_FORMAT.getPattern());
			START_OF_1900_LOCAL_DATE = LocalDate.of(1900, 1, 1);
			DB2_DEFAULT_DATE = DateUtils.parseDate("0001-01-01 00:00:00.000", "yyyy-MM-dd HH:mm:ss.SSS");
		} catch(ParseException ex) {
			throw new RuntimeException("Error parsing dates for date comparison utility on startup", ex);
		}
	}
    
    //Can't instatiate this - it's a utility class.
    private DateUtil() {}

	//***** Convert Strings to Dates ******//
    /**
	 * Attempt to parse a date from a string, using the ISO date format and US date formats (both 2-digit and 4-digit years).
	 * The parser is lenient with the date strings it accepts.
	 * @param dateStr The string to turn into a date.
	 * @return The date parsed, or null if it does not match any of the patterns.
	 */
	public static Date parseDate(String dateStr)
	{
		return parseDate(dateStr, false);
	}

	/**
	 * Attempt to parse a date from a string, using the ISO date format and US date formats (both 2-digit and 4-digit years).
	 * @param dateStr The string to turn into a date.
	 * @param strict If this is false, the parser is lenient with the date strings it accepts.
	 * 	If this is true, the parser will not allow for dates like "02/942/1996". It will also not allow for dates prior to January 1, 1900.
	 * @return The date parsed, or null if it does not match any of the patterns.
	 */
	public static Date parseDate(String dateStr, boolean strict)
	{
		if(StringUtils.isBlank(dateStr))
			return null;

		Date result;
		try {
			if(strict)
				result = DateUtils.parseDateStrictly(dateStr, DATE_PARSE_FORMATS);
			else
				result = DateUtils.parseDate(dateStr, DATE_PARSE_FORMATS);
		} catch(ParseException ex) {
			ex.toString();
			return null;
		}

		if(strict)
		{
			if(START_OF_1900.compareTo(result) > 0)
				return null;
		}

		return result;
	}

    /**
     * Attempt to parse a date and time in yyyy-MM-dd HH:MM:SS.ssss format
     * @param dateTimeStr The string to parse as a date
     * @return The date, if it was able to be parsed; null if it was not able to be parsed.
     */
    public static Date parseDateTimeISO(String dateTimeStr)
    {
    	if(StringUtils.isBlank(dateTimeStr))
            return null;

        try {
            return DateUtils.parseDate(dateTimeStr, new String[]{DATE_TIME_FORMAT_ISO});
        } catch(ParseException ex) {
            ex.toString();
            return null;
        }
    }
    
    /**
	 * Attempt to parse a date from a string, using the ISO date format and US date formats (both 2-digit and 4-digit years).
	 * The parser is lenient with the date strings it accepts.
	 * @param dateStr The string to turn into a date.
	 * @return The date parsed, or null if it does not match any of the patterns.
	 */
    public static LocalDate parseLocalDate(String dateStr)
    {
    	return parseLocalDate(dateStr, false);
    }
    
	/**
	 * Attempt to parse a date from a string, using the ISO date format and US date formats (both 2-digit and 4-digit years).
	 * @param dateStr The string to turn into a date.
	 * @param strict If this is false, the parser is lenient with the date strings it accepts.
	 * 	If this is true, the parser will not allow for dates like "02/942/1996". It will also not allow for dates prior to January 1, 1900.
	 * @return The date parsed, or null if it does not match any of the patterns.
	 */
    public static LocalDate parseLocalDate(String dateStr, boolean strict)
    {
    	if(StringUtils.isBlank(dateStr))
			return null;
    	
    	LocalDate result;
    	try {
    		if(strict)
    			result = LocalDate.parse(dateStr, PARSER_DATE.withResolverStyle(ResolverStyle.STRICT));
    		else
    			result = LocalDate.parse(dateStr, PARSER_DATE.withResolverStyle(ResolverStyle.LENIENT));
    	} catch(DateTimeParseException ex) {
    		ex.toString();
    		return null;
    	}
    	
    	if(strict)
    	{
    		if(START_OF_1900_LOCAL_DATE.isAfter(result))
    			return null;
    	}
    	
    	return result;
    }
    
	//***** Convert Dates to Strings *****//
	/**
	 * Formats a date in the typical format used in the United States, with a 4-digit year
	 * 	(ex: March 5, 2015 would be formatted as "3/5/2015").
	 * @param date The date to format
	 * @return A string representation of the date
	 */
	public static String formatDateUS(Date date)
	{
		if(date == null)
			return null;
		
		return DateFormatUtils.format(date, DATE_FORMAT_US);
	}
	
	/**
	 * Formats a date in the typical format used internationally.
	 * 	(ex: March 5, 2015 would be formatted as "2015-03-05")
	 * @param date The date to format
	 * @return A string representation of the date
	 */
	public static String formatDateISO(Date date)
	{
		if(date == null)
			return null;
		
		return DateFormatUtils.format(date, DateFormatUtils.ISO_DATE_FORMAT.getPattern());
	}
	
	/**
	 * Formats a timestamp in the typical format used in the United States for a date / time combination (without AM/PM indicator)
	 * 	(ex: 1:05 PM on March 5, 2015 would be formatted "03/05/2015 01:05:00")
	 * @param date The date to format
	 * @return A string representation of the timestamp
	 */
	public static String formatDateTimeUS(Date date)
	{
		if(date == null)
			return null;
		
		return DateFormatUtils.format(date, DATE_TIME_FORMAT_US);
	}
	
	/**
	 * Formats a timestamp in the typical format used internationally, without a time zone offset.
	 * 	(ex: 1:05 PM on March 5, 2015 would be formatted "2015-05-03 13:05:00")
	 * @param date The timestamp to format.
	 * @return The formatted date / time.
	 */
	public static String formatDateTimeISO(Date date)
	{
		if(date == null)
			return null;
		
		return DateFormatUtils.format(date, DATE_TIME_FORMAT_ISO);
	}
	
	/**
	 * Formats a date in the typical format used in the United States, with a 4-digit year
	 * 	(ex: March 5, 2015 would be formatted as "3/5/2015").
	 * @param date The date to format
	 * @return A string representation of the date
	 */
	public static String formatDateUS(Temporal date)
	{
		if(date == null)
			return null;
		
		return FORMATTER_US_DATE.format(date);
	}
	
	/**
	 * Formats a datte in the typical format used internationally.
	 * 	(ex: March 5, 2015 would be formatted as "2015-03-05")
	 * @param date The date to format
	 * @return A string representation of the date
	 */
	public static String formatDateISO(TemporalAccessor date)
	{
		if(date == null)
			return null;
		
		return FORMATTER_ISO_DATE.format(date);
	}
	
	/**
	 * Formats a timestamp in the typical format used in the United States for a date / time combination (without AM/PM indicator)
	 * 	(ex: 1:05 PM on March 5, 2015 would be formatted "03/05/2015 01:05:00")
	 * @param date The date to format
	 * @return A string representation of the timestamp
	 */
	public static String formatDateTimeUS(TemporalAccessor date)
	{
		if(date == null)
			return null;
		
		return FORMATTER_US_DATE_TIME.format(date);
	}
	
	/**
	 * Formats a timestamp in the typical format used internationally, without a time zone offset.
	 *  (ex: 1:05 PM on March 5, 2015 would be formatted "2015-05-03 13:05:00")
	 * @param date The timestamp to format.
	 * @return The formatted date / time.
	 */
	public static String formatDateTimeISO(TemporalAccessor date)
	{
		if(date == null)
			return null;
		
		return FORMATTER_ISO_DATE_TIME.format(date);
	}

	/**
	 * Method to get the current date formatted for a folder name (i.e. no spaces and no special characters)
	 * @param date The date to format
	 * @return date in format yyyyMMdd
	 */
	public static String formatDateForFolderName(Date date)
	{
		if(date == null)
			return null;
		
		return DateFormatUtils.format(date, FOLDER_NAME_FORMAT);
	}	
    //***** Calculate Dates From Other Dates *****//
    /**
     * Answers a date that is at midnight (00:00:00) of the day given by {@code date}.
     * @param date The date for which to get the start of the day.
     * @return A date that is at midnight (00:00:00) of the day given by {@code date}.
     */
    public static Date startOfDay(Date date)
    {
        return DateUtils.truncate(date, Calendar.DATE);
    }

    /**
     * Answers the earlier of two dates.
     * @param first The first date to compare
     * @param second The second date to compare
     * @return The date representing the earlier of the two given dates. If only one argument is null, the non-null argument is returned.
     * 	If both arguments are null, null is returned.
     */
    public static Date minDate(Date first, Date second)
    {
        if(first == null)
            return second;
        if(second == null)
            return first;

        return first.compareTo(second) <= 0 ? first : second;
    }

    /**
     * Answers the later of two dates.
     * @param first The first date to compare
     * @param second The second date to compare
     * @return The date representing the later of the two given dates. If only one argument is null, the non-null argument is returned.
     * 	If both arguments are null, null is returned.
     */
    public static Date maxDate(Date first, Date second)
    {
        if(first == null)
            return second;
        if(second == null)
            return first;

        return first.compareTo(second) >= 0 ? first : second;
    }

    //***** Other Miscellaneous Date Helper Functions *****//
    /**
     * Gets the current year as an integer, in the local calendar
     * @return The current year
     */
    public static int getCurrentYear(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year;
    }
    
	/**
	 * Gets the number of calendar days between two dates.
	 * 	The hours/minutes/seconds/milliseconds of the start and end dates are ignored.
	 * 	The start date is not counted in the result. That is, if the start date is 1/1/2018 and the end date is 1/2/2018, then the result will be 1.
	 * @param start The date to start counting from
	 * @param end The date at which to finish counting
	 * @param excludeWeekends True if Saturdays and Sundays should not be counted in the result. False if the result should count Saturdays and Sundays. 
	 * @return The number of calendar days between the two given dates.
	 */
	public static int getDaysBetween(Date start, Date end, boolean excludeWeekends) {
		if(start == null || end == null)
			return 0;

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);

		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);

		int result = 0;
		

		//Return 0 if start and end are the same
		if(DateUtils.isSameDay(startCal, endCal))
			return 0;
		
		//If start date is after end date, switch them - we're trying to get the distance between them
		if(startCal.compareTo(endCal) > 0)
		{
			Calendar tmpCal = startCal;
			startCal = endCal;
			endCal = tmpCal;
		}

		do {
			//excluding start date
			startCal.add(Calendar.DAY_OF_MONTH, 1);
			boolean isWeekend = startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
			if (!(isWeekend && excludeWeekends))
				++result;
			
		} while (startCal.getTimeInMillis() < endCal.getTimeInMillis()); //excluding end date

		return result;
	}

	/**
	 * Method to get list of years +3/-10 from the year of the given date, in reverse order
	 * @param baseDate The date around which to center the list.
	 * @return Strings representing years from 3 years after the given date to 10 years before the given date, ordered greatest to least
	 */
	public static List<String> getModelYearDates(Date baseDate)
	{
		return getModelYearDates(baseDate, 10, 3, true);
	}
	
	/**
	 * Gets a list of years in an interval around {@literal baseDate}.
	 * @param baseDate The date around which to base the list. This year will definitely be included in the list.
	 * @param yearsBeforeBaseDate The number of years before the base date that should be included in the list, not counting the base date itself
	 * @param yearsAfterBaseDate The number of years after the base date that should be included in the list, not counting the base date itself
	 * @param descending True if the list should go from greatest to least. False to go from least to greatest.
	 * @return
	 */
	public static List<String> getModelYearDates(Date baseDate, int yearsBeforeBaseDate, int yearsAfterBaseDate, boolean descending)
	{
		List<String> modelYearList = new ArrayList<String>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(baseDate);
		
		int curYear  = cal.get(Calendar.YEAR);
		int minYear = curYear - yearsBeforeBaseDate;
		int maxYear = curYear + yearsAfterBaseDate;

		if(descending)
		{
			for (int year = maxYear; year >= minYear; year--)
				modelYearList.add(Integer.toString(year));
		}
		else
		{
			for (int year = minYear; year <= maxYear; year++)
				modelYearList.add(Integer.toString(year));
		}
		
		return modelYearList;
	}
	
    /**
     * Converts a date object into an non pretty as400 int date
     * 
     * @param date
     * @return A integer representing a not pretty as400 date
     */
	//FIXME: document
    public static int getDateAs400Date(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; //We don't want as zero based
        int monthDay = calendar.get(Calendar.DATE);
        int yearsAfter1900 = year - 1900;

        String fourHundredDateAsString = StringUtils.leftPad(String.valueOf(yearsAfter1900), 2, '0') +
        								 StringUtils.leftPad(String.valueOf(month), 2, '0') +
        								 StringUtils.leftPad(String.valueOf(monthDay), 2, '0');

        int fourHundredDate = Integer.parseInt(fourHundredDateAsString);

        return fourHundredDate;
    }
    
    /**
     * Converts a date object into an non pretty as400 int date
     * 
     * @param date The date to convert into an AS 400 date
     * @return A integer representing a not pretty as400 date
     */
    public static int getDateAs400Date(TemporalAccessor date)
    {
    	if(date == null)
    		return 0;
    	
        int year = date.get(ChronoField.YEAR);
        int month = date.get(ChronoField.MONTH_OF_YEAR);
        int day = date.get(ChronoField.DAY_OF_MONTH);
        int yearsAfter1900 = year - 1900;
        
        int fourHundredDate =
        	yearsAfter1900 * 10000 +
        	month * 100 + 
        	day;
        
        return fourHundredDate;
    }
    
    /**
     * Converts a date object into an integer for the AS400 in the format MMDDYY (the US date order)
     * @param date The date to format
     * @return The date formatted as a number. So, for example, 2019-12-30 would be formatted as 123019.
     * 	2019-09-08 would be formatted as 90819.
     */
    public static int getDateAs400USDate(TemporalAccessor date)
    {
    	if(date == null)
    		return 0;
    	
        int year = date.get(ChronoField.YEAR);
        int month = date.get(ChronoField.MONTH_OF_YEAR);
        int day = date.get(ChronoField.DAY_OF_MONTH);
        int twoDigitYear = year % 100;
        
        int fourHundredDate =
        	month * 10000 +
        	day * 100 + 
        	twoDigitYear;
        
        return fourHundredDate;
    }

    /**
     * Converts a non pretty as400 int date into a proper date object
     * 
     * @param fourHundredDate
     * @return A pretty date
     */
    //FIXME: document
    public static Date get400DateAsDate(int fourHundredDate)
    {

        String forHundredDateAsString = Integer.toString(fourHundredDate);

        boolean is90sKid = forHundredDateAsString.length() == 6;

        String lastTwoDigitsForYear = is90sKid? forHundredDateAsString.substring(0, 1) : forHundredDateAsString.substring(1, 3);
        String monthAsString = is90sKid? forHundredDateAsString.substring(2, 4) : forHundredDateAsString.substring(3, 5);
        String monthDayAsString = is90sKid? forHundredDateAsString.substring(4, 6) : forHundredDateAsString.substring(5, 7);

        String fullYearAsString = is90sKid? "19" + lastTwoDigitsForYear : "20" + lastTwoDigitsForYear;

        Calendar calendar = Calendar.getInstance();

        int year = Integer.parseInt(fullYearAsString);
        int month = Integer.parseInt(monthAsString) - 1; //We want as zero based
        int monthDate = Integer.parseInt(monthDayAsString);

        calendar.set(year, month, monthDate, 0, 0, 0);

        Date date = calendar.getTime();

        return date;

    }

    //FIXME: document
    public static int get400Date30MonthsAgo()
    {
    	Date now = new Date();
    	Date date30MonthsAgo = DateUtils.addMonths(now, -30);
    	int dateAs400Date = getDateAs400Date(date30MonthsAgo);
    	
        return dateAs400Date;
    }

    //FIXME: document
    public static boolean isDateIsTheAs400DefaultTime(Date date)
    {
        if(date == null)
            return false;
        
        return date.getTime() == DB2_DEFAULT_DATE.getTime();
    }
    
    public static boolean isDateTheAs400DefaultDate(LocalDate localDate)
    {
    	if(localDate == null)
    		return false;
    	return localDate.equals(DB2_DEFAULT_LOCAL_DATE);
    }

    //FIXME: document
    public static int getDateAsAS400Time(TemporalAccessor timestamp)
    {
    	if(timestamp == null)
    		return 0;

    	int fourHundredTime =
    		timestamp.get(ChronoField.HOUR_OF_DAY) * 10000 + 
    		timestamp.get(ChronoField.MINUTE_OF_HOUR) * 100 +
    		timestamp.get(ChronoField.SECOND_OF_MINUTE);
    	
    	return fourHundredTime;
    }
}
