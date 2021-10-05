/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;
import com.penske.apps.smccore.component.domain.UnitDates;
import com.penske.apps.smccore.component.domain.enums.CommentRequiredComplianceType;

/**
 * Class under test: {@link CalculatedDataService}
 */
public class CalculatedDataServiceTest
{ 
	private final Date now = new Date();
	private final Date oneMonth = DateUtils.addMonths(now, 1);
	private final Date twoMonths = DateUtils.addMonths(now, 2);
	private final Date threeMonths = DateUtils.addMonths(now, 3);
	private final Date fourMonths = DateUtils.addMonths(now, 4);
	
	private final CalculatedDataService calculatedDataService = new DefaultCalculatedDataService();
	
	@Test
	public void shouldCheckCommentRequired()
	{
		int threshold = 7;
		TransportationTypeEnum oem = TransportationTypeEnum.OEM;
		TransportationTypeEnum local = TransportationTypeEnum.LOCAL;
		
		//												reqProd	  reqDel	 estProd	estDel		 actProd	  actDel
		UnitDates inCompliance =	new UnitDates(1234, oneMonth, twoMonths, oneMonth,  twoMonths,   null,		  null);
		UnitDates productionLate =	new UnitDates(1234, oneMonth, twoMonths, twoMonths, threeMonths, null,		  null);
		UnitDates deliveryLate =	new UnitDates(1234, oneMonth, twoMonths, twoMonths, threeMonths, threeMonths, null);
		UnitDates delivered =		new UnitDates(1234, oneMonth, twoMonths, twoMonths, threeMonths, threeMonths, fourMonths);
		
		//In compliance - no comment required, even though dates changed and no comments were made
		assertThat(calculatedDataService.isCommentRequired(inCompliance, oem, false, threshold, threshold), is(nullValue()));
		
		//Production late, and no comment ever - comment required
		assertThat(calculatedDataService.isCommentRequired(productionLate, oem, false, threshold, threshold), is(CommentRequiredComplianceType.PRODUCTION));
		
		//Production late, but comment provided - comment not required
		assertThat(calculatedDataService.isCommentRequired(productionLate, oem, true, threshold, threshold), is(nullValue()));
		
		//Produced, but delivery late - OEM transporter, so vendor is still on the hook for delivery compliance - comment not provided - so, comment is required
		assertThat(calculatedDataService.isCommentRequired(deliveryLate, oem, false, threshold, threshold), is(CommentRequiredComplianceType.DELIVERY));
		
		//Produced, but delivery late - LOCAL transporter, so vendor is not responsible for delivery - comment not required
		assertThat(calculatedDataService.isCommentRequired(deliveryLate, local, false, threshold, threshold), is(nullValue()));
		
		//Delivered - no comment required, even though the actual dates were way later than the requested dates - it's already delivered and out of the vendor's control
		assertThat(calculatedDataService.isCommentRequired(delivered, oem, false, threshold, threshold), is(nullValue()));
	}
}
