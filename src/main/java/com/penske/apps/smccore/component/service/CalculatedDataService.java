/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.service;

import java.util.Date;

import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;
import com.penske.apps.smccore.component.domain.UnitDates;
import com.penske.apps.smccore.component.domain.enums.CommentRequiredComplianceType;

/**
 * Service that provides methods that compute various calculated values about a unit master
 */
public interface CalculatedDataService
{
	/**
	 * Determines if the given unit requires a comment or not.
	 * Generally, a comment is required if it is out of compliance, the manufacturer still has charge of it,
	 * 	and a comment has not already been provided since it moved out of compliance.
	 * @param unitDates The sets of production and delivery dates for the unit master
	 * @param transportationType The unit master's transportation type
	 * @param hasComment True if the unit master has ever had a comment. False if it has never had any comments.
	 * @param complianceThresholdProduction The max number of days out of compliance the production date can be without Vehicle Supply requiring a comment for why it's late
	 * @param complianceThresholdDelivery The max number of days out of compliance the delivery date can be without Vehicle Supply requiring a comment for why it's late
	 * @return True if a comment is required on the unit. False if it is not.
	 */
	public CommentRequiredComplianceType isCommentRequired(UnitDates unitDates, TransportationTypeEnum transportationType, boolean hasComment, int complianceThresholdProduction, int complianceThresholdDelivery);
	
	/**
	 * Determines if the next comment on a unit will be a delay comment
	 * @param unitDates The main dates from the unit master. If this is run at the same time as the dates are being updated, this should contain the "new" values for the dates.
	 * @param transportationType The transporation type of the unit master
	 * @param previousCommentDate The last date a comment was given. If this is run at the same time a comment is being added, this is the "old" value for the last comment date
	 * @param productionComplianceThreshold The number of days past the requested production date the unit master can be without being considered out of compliance
	 * @param deliveryComplianceThreshhold The number of days past the requested delivery date the unit master can be without being considered out of compliance
	 * @return True if the next comment on this unit will be a delay comment. False if it will be a normal comment.
	 */
	public boolean isNextCommentDelayComment(UnitDates unitDates, TransportationTypeEnum transportationType, Date previousCommentDate, int productionComplianceThreshold, int deliveryComplianceThreshhold);
}
