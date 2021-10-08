/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;
import com.penske.apps.smccore.component.domain.UnitDates;
import com.penske.apps.smccore.component.domain.enums.CommentRequiredComplianceType;

/**
 * Implementation for {@link CalculatedDataService}
 */
@Service
public class DefaultCalculatedDataService implements CalculatedDataService
{
	/** {@inheritDoc} */
	@Override
	public CommentRequiredComplianceType isCommentRequired(UnitDates unitDates, TransportationTypeEnum transportationType, boolean hasComment,
			int productionComplianceThreshold, int deliveryComplianceThreshold)
	{
		if(unitDates == null)
			throw new IllegalArgumentException("Can not calculate comment required without unit dates.");
		
		//No comment is required if a comment of any kind has already been provided at any time in the past
		if(hasComment)
			return null;

		//Check whether the unit is out of compliance or not - if it is, and no comment has been provided, then a comment is required
		if(unitDates.isOutOfComplianceProduction(productionComplianceThreshold))
			return CommentRequiredComplianceType.PRODUCTION;
		else if(unitDates.isOutOfComplianceDelivery(deliveryComplianceThreshold, transportationType))
			return CommentRequiredComplianceType.DELIVERY;
		else
			return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isNextCommentDelayComment(UnitDates unitDates, TransportationTypeEnum transportationType, Date previousCommentDate,
			int productionComplianceThreshold, int deliveryComplianceThreshold)
	{
		if(unitDates == null)
			throw new IllegalArgumentException("Can not calculate comment required without unit dates.");
		
		//If it is out of compliance and the dates also changed since last comment, it is a delay comment
		if(unitDates.isOutOfCompliance(productionComplianceThreshold, deliveryComplianceThreshold, transportationType))
			return true;
		else
			return false;
	}
}
