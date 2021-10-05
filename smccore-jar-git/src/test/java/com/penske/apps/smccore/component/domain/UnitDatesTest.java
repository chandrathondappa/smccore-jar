/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;

import org.junit.Test;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;

/**
 * Class under test: {@link UnitDates}
 */
public class UnitDatesTest
{
	private final Date jan1 = CoreTestUtil.dateAt(2018, 01, 01);
	private final Date jan5 = CoreTestUtil.dateAt(2018, 01, 05);
	private final Date jan7 = CoreTestUtil.dateAt(2018, 01, 07);
	private final Date jan15 = CoreTestUtil.dateAt(2018, 01, 15);
	private final Date jan20 = CoreTestUtil.dateAt(2018, 01, 20);
	private final Date jan31 = CoreTestUtil.dateAt(2018, 01, 31);
	
	private final Date feb1 = CoreTestUtil.dateAt(2018, 02, 01);
	private final Date feb4 = CoreTestUtil.dateAt(2018, 02, 04);
	private final Date feb8 = CoreTestUtil.dateAt(2018, 02,  8);
	private final Date feb10 = CoreTestUtil.dateAt(2018, 02, 10);
	private final Date feb15 = CoreTestUtil.dateAt(2018, 02, 15);
	
	private final Date lowVal = CoreTestUtil.dateAt(0001, 01, 01);
	
	@Test
	public void shouldCreate()
	{
		UnitDates unitDates = new UnitDates(1234, jan1, feb1, jan20, feb10, null, lowVal);
		
		assertThat(unitDates.getRequestedProductionDate(),	is(jan1));
		assertThat(unitDates.getRequestedDeliveryDate(),	is(feb1));
		assertThat(unitDates.getEstimatedProductionDate(),	is(jan20));
		assertThat(unitDates.getEstimatedDeliveryDate(),	is(feb10));
		assertThat(unitDates.getActualProductionDate(), is(nullValue()));
		assertThat(unitDates.getActualDeliveryDate(), is(nullValue()));
	}
	
	@Test
	public void shouldGetComplianceDaysProduction()
	{
		//								reqProd	reqDel	estProd	estDel	actProd	actDel								today
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	feb15).getComplianceDaysProduction(jan15), is(30));		//Actual prod date provided after requested date
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysProduction(feb1), is(31));		//Today > est prod date
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysProduction(jan15), is(19));		//Today < est prod date
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysProduction(jan20), is(19));		//Today = est prod date
				
		//Real-world scenario - act prod date before requested date
		assertThat(new UnitDates(1234, 
				CoreTestUtil.dateAt(2018, 11, 26),	//reqProd
				CoreTestUtil.dateAt(2018, 12, 17),	//reqDel
				CoreTestUtil.dateAt(2018, 11, 17),	//estProd
				CoreTestUtil.dateAt(2018, 11, 25),	//estDel
				CoreTestUtil.dateAt(2018, 11, 07),	//actProd
				CoreTestUtil.dateAt(2018, 11, 12)	//actDel
			).getComplianceDaysProduction(CoreTestUtil.dateAt(2018, 11, 15)),
		is(-19));
	}
	
	@Test
	public void shouldGetComplianceDaysDelivery()
	{
		//								reqProd	reqDel	estProd	estDel	actProd	actDel							today
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	feb10).getComplianceDaysDelivery(jan15), is(9));		//Actual del date provided after today
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysDelivery(feb15), is(14));		//Today > est del date
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysDelivery(jan15), is(9));		//Today < est del date
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .getComplianceDaysDelivery(feb10), is(9));		//Today = est del date

		//Real-world scenario - act del date before requested date
		assertThat(new UnitDates(1234, 
				CoreTestUtil.dateAt(2018, 11, 26),	//reqProd
				CoreTestUtil.dateAt(2018, 12, 17),	//reqDel
				CoreTestUtil.dateAt(2018, 11, 17),	//estProd
				CoreTestUtil.dateAt(2018, 11, 25),	//estDel
				CoreTestUtil.dateAt(2018, 11, 07),	//actProd
				CoreTestUtil.dateAt(2018, 11, 12)	//actDel
			).getComplianceDaysDelivery(CoreTestUtil.dateAt(2018, 11, 15)),
		is(-35));
	}
	
	@Test
	public void shouldCheckOutOfComplianceProduction()
	{
		int threshold = 7;
		
		//								reqProd	reqDel	estProd	estDel	actProd	actDel
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	null) .isOutOfComplianceProduction(threshold), is(false));	//Already produced - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	feb10).isOutOfComplianceProduction(threshold), is(false));	//Already delivered - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan15,	feb10,	null,	null) .isOutOfComplianceProduction(threshold), is(true));	//Estimate more than 7 days late - out of compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan5,	feb10,	null,	null) .isOutOfComplianceProduction(threshold), is(false));	//Estimate less than 7 days late - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan7,	feb10,	null,	null) .isOutOfComplianceProduction(threshold), is(false));	//Estimate exactly 7 days late - in compliance
	}
	
	@Test
	public void shouldCheckOutOfComplianceDelivery()
	{
		int threshold = 7;
		TransportationTypeEnum oem = TransportationTypeEnum.OEM;
		TransportationTypeEnum local = TransportationTypeEnum.LOCAL;
		
		//								reqProd	reqDel	estProd	estDel	actProd	actDel
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .isOutOfComplianceDelivery(threshold, oem), is(false));		//Not yet produced - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	null) .isOutOfComplianceDelivery(threshold, local), is(false));		//Delivery late, but LOCAL transport type - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	feb10).isOutOfComplianceDelivery(threshold, oem), is(false));		//Already delivered - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan5,	feb10,	jan31,	null) .isOutOfComplianceDelivery(threshold, oem), is(true));		//Estimate more than 7 days late - out of compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan5,	feb4,	jan31,	null) .isOutOfComplianceDelivery(threshold, oem), is(false));		//Estimate less than 7 days late - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan5,	feb8,	jan31,	null) .isOutOfComplianceDelivery(threshold, oem), is(false));		//Estimate exactly 7 days late - in compliance
	}
	
	@Test
	public void shouldCheckOutOfCompliance()
	{
		int threshold = 7;
		TransportationTypeEnum oem = TransportationTypeEnum.OEM;
		TransportationTypeEnum local = TransportationTypeEnum.LOCAL;
		
		//								reqProd	reqDel	estProd	estDel	actProd	actDel
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan5,	feb10,	null,	null) .isOutOfCompliance(threshold, threshold, oem), is(false));	//Production estimate < 7 days late - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	null,	null) .isOutOfCompliance(threshold, threshold, oem), is(true));		//Production estimate > 7 days late - out of compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb4,	jan31,	null) .isOutOfCompliance(threshold, threshold, oem), is(false));	//Delivery estimate < 7 days late - in compliance		
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	null) .isOutOfCompliance(threshold, threshold, oem), is(true));		//Delivery estimate > 7 days late - out of compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	null) .isOutOfCompliance(threshold, threshold, local), is(false));	//Delivery estimate > 7 days late, but LOCAL transport type - in compliance
		assertThat(new UnitDates(1234, 	jan1,	feb1,	jan20,	feb10,	jan31,	feb10).isOutOfCompliance(threshold, threshold, oem), is(false));	//Already delivered - in compliance
	}
}
