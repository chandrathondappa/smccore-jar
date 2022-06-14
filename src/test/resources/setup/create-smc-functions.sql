--NOTE: Since this SQL file contains procedure / function defnitions, we need a different line-ending character to tell Spring when the function is complete instead of just a single line being complete.
-- Thus, we end each function and procedure with the two-character combination "/;". The place where this script is used in the Spring configuration must also be updated to use "/;" as a separator.
-- See: https://stackoverflow.com/questions/11501667/trigger-syntax-on-hsqldb-expected

CREATE FUNCTION SMC.SMC_CALC_WORKING_DAYS (DATE_START_IN DATE , DATE_END_IN DATE) 
	RETURNS INTEGER
	BEGIN ATOMIC
		
		DECLARE DAYS_COUNT INTEGER ; 
		DECLARE YEARS_COUNT INTEGER ; 
		DECLARE WEEKS_COUNT INTEGER ; 
		DECLARE WEEKENDS_COUNT INTEGER ; 
		DECLARE STARTS_ON_SUNDAY INTEGER ; 
		DECLARE ENDS_ON_SATURDAY INTEGER ; 
	 
		SET DAYS_COUNT = DAYS ( DATE_START_IN ) - DAYS ( DATE_END_IN ) ; 
		SET YEARS_COUNT = YEAR ( DATE_START_IN ) - YEAR ( DATE_END_IN ) ; 
		SET WEEKS_COUNT = YEARS_COUNT * 52 + WEEK ( DATE_START_IN ) - WEEK ( DATE_END_IN ) ; 
		SET WEEKENDS_COUNT = WEEKS_COUNT * 2 ; 
		SET STARTS_ON_SUNDAY = CASE WHEN ( DAYOFWEEK ( DATE_START_IN ) = 7 ) THEN 1 ELSE 0 END ; 
		SET ENDS_ON_SATURDAY = CASE WHEN ( DAYOFWEEK ( DATE_END_IN ) = 7 ) THEN 1 ELSE 0 END ; 
		 
		RETURN DAYS_COUNT - WEEKENDS_COUNT - STARTS_ON_SUNDAY - ENDS_ON_SATURDAY ;
	END/;