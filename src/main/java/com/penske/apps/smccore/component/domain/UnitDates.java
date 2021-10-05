/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import java.util.Date;

import com.penske.apps.smccore.base.domain.enums.TransportationTypeEnum;
import com.penske.apps.smccore.base.util.DateUtil;

/**
 * Holder for the four primary dates SMC cares about for a unit:
 * <ul>
 * 	<li>Estimated Production Date</li>
 * 	<li>Estimated Delivery Date</li>
 * 	<li>Actual Production Date</li>
 * 	<li>Actual Delivery Date</li>
 * </ul>
 */
//FIXME: document
public class UnitDates
{
	/** Database ID for the PO / Unit combination */
	private final int masterId;
	/** The date Penske requested the vendor produce the unit */
	private final Date requestedProductionDate;
	/** The date Penske requested that the unit arrives at its destination */
	private final Date requestedDeliveryDate;
	/** The date the vendor estimates the unit will be produced */
	private final Date estimatedProductionDate;
	/** The date the vendor estimates the unit will be delivered */
	private final Date estimatedDeliveryDate;
	/** The date the unit was actually produced */
	private final Date actualProductionDate;
	/** The date the unit was actually delivered */
	private final Date actualDeliveryDate;
	
	/**
	 * Number of days between the requested production date and the estimated production date.
	 * Negative value indicates the estimated date is before the requested date.
	 * Lazily initialized, so the calendar math doesn't have to happen if it's not used, but it can be re-used quickly once computed.
	 * 	Do not access this field directly. Instead, use {@link #getRequestedToEstimatedProduction()}.
	 */
	private Integer requestedToEstimatedProduction;
	/**
	 * Number of days between the requested delivery date and the estimated delivery date.
	 * Negative value indicates the estimated date is before the requested date.
	 * Lazily initialized, so the calendar math doesn't have to happen if it's not used, but it can be re-used quickly once computed.
	 * 	Do not access this field directly. Instead, use {@link #getRequestedToEstimatedDelivery()}.
	 */
	private Integer requestedToEstimatedDelivery;
	
	public UnitDates(int masterId, Date requestedProductionDate, Date requestedDeliveryDate, Date estimatedProductionDate, Date estimatedDeliveryDate, Date actualProductionDate, Date actualDeliveryDate)
	{
		//Normalize the dates to start with - requested and estimated dates shouldn't be null (they shouldn't be 0001-01-01, either, but there are a few dates with that value, so we don't filter it out)
		if(requestedProductionDate == null)
			throw new IllegalArgumentException("Requested Production Date may not be empty: master ID " + masterId);
		if(requestedDeliveryDate == null)
			throw new IllegalArgumentException("Requested Delivery Date may not be empty: master ID " + masterId);
		if(estimatedProductionDate == null)
			throw new IllegalArgumentException("Estimated Production Date may not be empty: master ID " + masterId);
		if(estimatedDeliveryDate == null)
			throw new IllegalArgumentException("Estimated Delivery Date may not be empty: master ID " + masterId);
		//Treat the DB2 default date as though it's null for actual date fields
		if(actualProductionDate != null && DateUtil.isDateIsTheAs400DefaultTime(actualProductionDate))
			actualProductionDate = null;
		if(actualDeliveryDate != null && DateUtil.isDateIsTheAs400DefaultTime(actualDeliveryDate))
			actualDeliveryDate = null;
		
		this.masterId = masterId;
		this.requestedProductionDate = requestedProductionDate;
		this.requestedDeliveryDate = requestedDeliveryDate;
		this.estimatedProductionDate = estimatedProductionDate;
		this.estimatedDeliveryDate = estimatedDeliveryDate;
		this.actualProductionDate = actualProductionDate;
		this.actualDeliveryDate = actualDeliveryDate;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{UnitDates - Est Prod: " + DateUtil.formatDateISO(estimatedProductionDate) + ", Est Del: " + DateUtil.formatDateISO(estimatedDeliveryDate) + 
			", Act Prod: " + DateUtil.formatDateISO(actualProductionDate) + ", Act Del: " + DateUtil.formatDateISO(actualDeliveryDate) + "}";
	}

	//***** HELPER FUNCTIONS *****//
	private int getDaysBetweenWithSign(Date startDate, Date endDate)
	{
		//The getDaysBetween() utility method returns an absolute value, so we need to look which one comes first below
		int difference = DateUtil.getDaysBetween(startDate, endDate, false);
		
		//If the start end date is before the start date, reverse the sign.
		if(endDate.compareTo(startDate) < 0)
			return -difference;
		else
			return difference;
	}
	
	private int getRequestedToEstimatedProduction()
	{
		if(requestedToEstimatedProduction == null)
			requestedToEstimatedProduction = getDaysBetweenWithSign(requestedProductionDate, estimatedProductionDate);
		
		return requestedToEstimatedProduction;
	}
	
	private int getRequestedToEstimatedDelivery()
	{
		if(requestedToEstimatedDelivery == null)
			requestedToEstimatedDelivery = getDaysBetweenWithSign(requestedDeliveryDate, estimatedDeliveryDate);
		
		return requestedToEstimatedDelivery;
	}
	
	//***** MODIFIED ACCESSORS *****//
	public int getComplianceDaysProduction(Date today)
	{
		Date endDate;
		if(actualProductionDate != null)
			endDate = actualProductionDate;
		else
			endDate = DateUtil.maxDate(today, estimatedProductionDate);
		
		int difference = getDaysBetweenWithSign(requestedProductionDate, endDate);
		
		return difference;
	}
	
	public int getComplianceDaysDelivery(Date today)
	{
		Date endDate;
		if(actualDeliveryDate != null)
			endDate = actualDeliveryDate;
		else
			endDate = DateUtil.maxDate(today, estimatedDeliveryDate);
		
		int difference = getDaysBetweenWithSign(requestedDeliveryDate, endDate);
		return difference;
	}
	
	public boolean isOutOfComplianceProduction(int productionComplianceThreshold)
	{
		//If we have either an actual production date or an actual delivery date,
		//	then production has been completed, so it can't be out of production compliance
		if(actualProductionDate != null || actualDeliveryDate != null)
			return false;
		
		int difference = getRequestedToEstimatedProduction();
		
		return difference > productionComplianceThreshold;
	}
	
	public boolean isOutOfComplianceDelivery(int deliveryComplianceThreshold, TransportationTypeEnum transportationType)
	{
		//If it's already been delivered, then it can't be out of delivery compliance.
		if(actualDeliveryDate != null)
			return false;
		
		//If production hasn't completed yet, then it can't be out of delivery compliance.
		//	It might be out of production compliance, but not delivery compliance.
		if(actualProductionDate == null)
			return false;
		
		//If we get to here, it means the unit has been produced, but not delivered yet
		
		//If it's LOCAL or TRANSPORTER, the OEM is not responsible for delivery, so they should not be held accountable for delivery compliance
		if(transportationType == TransportationTypeEnum.LOCAL || transportationType == TransportationTypeEnum.TRANSPORTER)
			return false;
		
		int difference = getRequestedToEstimatedDelivery();
		
		return difference > deliveryComplianceThreshold;
	}
	
	public boolean isOutOfCompliance(int productionComplianceThreshold, int deliveryComplianceThreshold, TransportationTypeEnum transportationType)
	{
		boolean productionOutOfCompliance = isOutOfComplianceProduction(productionComplianceThreshold);
		boolean deliveryOutOfCompliance = isOutOfComplianceDelivery(deliveryComplianceThreshold, transportationType);
		
		return productionOutOfCompliance || deliveryOutOfCompliance;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public int getMasterId()
	{
		return masterId;
	}

	public Date getRequestedProductionDate()
	{
		return requestedProductionDate;
	}

	public Date getRequestedDeliveryDate()
	{
		return requestedDeliveryDate;
	}

	public Date getEstimatedProductionDate()
	{
		return estimatedProductionDate;
	}

	public Date getEstimatedDeliveryDate()
	{
		return estimatedDeliveryDate;
	}

	public Date getActualProductionDate()
	{
		return actualProductionDate;
	}

	public Date getActualDeliveryDate()
	{
		return actualDeliveryDate;
	}
}