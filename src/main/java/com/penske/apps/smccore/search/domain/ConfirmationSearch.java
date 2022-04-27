/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.search.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.PoStatus;
import com.penske.apps.smccore.base.util.Util;

/**
 * Represents all the search parameters that can be used to filter a search on the Order Confirmation screen.
 */
/**
 * 
 */
public class ConfirmationSearch
{
	/** Comma-separated string of PO numbers. Used to store the PO numbers the user wanted to search. */
	private String poNumberString;
	/** Comma-separated string of unit ranges that the user wanted to search. */
	private String unitNumberString;
	/** The vendor order number the user wanted to find */
	private String vendorOrderNumber;
	
	/** A string naming the operator to use when comparing CO number. */
	private String coOperatorString;
	/** The CO number to use when comparing other orders. */
	private Integer coNumber;
	/** A string naming the operator to use when comparing cancel sequence. */
	private String cancelSequenceOperatorString;
	/** The cancel sequence to use when comparing other orders. */
	private Integer cancelSequence;
	/** A string naming the operator to use when comparing unconfirmed days */
	private String daysUnconfirmedOperatorString;
	/** The number of business days (excluding weekends) that the order existed before it was confirmed. */
	private Integer daysUnconfirmed;
	
	private LocalDate issueDateFrom;
	private LocalDate issueDateTo;
	private LocalDate confirmedDateFrom;
	private LocalDate confirmedDateTo;
	
	/**
	 * True to only return orders sent via EDI.
	 * False to only return orders not sent via EDI.
	 * Null to return orders regardless of whether they were sent EDI or not.
	 */
	private Boolean sentViaEdi;
	
	/** If this list contains statuses, only return orders with one of the given statuses */
	private List<PoStatus> statuses;
	
	private Boolean penskeUser;
	
	/**
	 * For a Penske user, this is the set of vendor IDs they have selected in their vendor filter.
	 * For a Vendor user, this should be null.
	 * If this list contains items, only return orders issued to vendors in this list.
	 */
	private List<Integer> vendorIdsFromFilter;
	
	/**
	 * Sets filters that are based on the user's type (Penske vs. Vendor) and associated vendors.
	 * It is important that this method be called last, after all other parameters are set, and right before the search is run,
	 * 	as it may modify other existing fields.
	 * @param user The user running the search
	 * @param vendorIdsFromVendorFilters If {@code user} is a Penske user, this is the list of vendor IDs they have selected from their vendor filter.
	 * 	For a vendor user, this will be ignored.
	 */
	public ConfirmationSearch asUser(User user, List<Integer> vendorIdsFromVendorFilters)
	{
		this.penskeUser = user.isPenskeUser();
		
		//If a Penske user has a vendor filter set, then limit the results to only things from that vendor filter
		if(user.isPenskeUser() && vendorIdsFromVendorFilters != null && !vendorIdsFromVendorFilters.isEmpty())
			this.vendorIdsFromFilter = vendorIdsFromVendorFilters;
		else
			this.vendorIdsFromFilter = null;
		
		return this;
	}
	
	//***** SPECIALIZED GETTERS FOR SEARCH QUERY *****//
	//These methods should not be serialized to JSON when sending the search to the client, but are only here for using the object in MyBatis
	/**
	 * Gets a tokenized list of the individual PO numbers the user wants to search for
	 * @return The PO numbers, listed out
	 */
	public Set<Integer> getPoNumbers()
	{
		return Util.getTokenizedPoNumbers(poNumberString, null);
	}
	
	/**
	 * Gets a tokenized list of all the individual unit numbers the user wants to search for
	 * @return The unit numbers, listed out
	 */
	public Set<String> getUnitNumbers()
	{
		return Util.getTokenizedUnitNumbers(unitNumberString, null, true);
	}
	
	/**
	 * Gets the actual CO operator string that should be used in the query.
	 * 	This is separate from the operator the user passed from the client, so that we aren't open to SQL injection attacks.
	 * @return The operator to use to compare CO numbers when searching.
	 */
	public String getCoOperator()
	{
		if(StringUtils.isBlank(coOperatorString))
			return null;
		
		switch(coOperatorString)
		{
		case "equal": return "=";
		case "less": return "<";
		case "greater": return ">";
		default: throw new IllegalArgumentException("Unrecognized CO operator: " + coOperatorString);
		}
	}

	/**
	 * Gets the actual Cancel Sequence operator string that should be used in the query.
	 * 	This is separate from the operator the user passed from the client, so that we aren't open to SQL injection attacks.
	 * @return The operator to use to compare cancel sequences when searching.
	 */	
	public String getCancelSequenceOperator()
	{
		if(StringUtils.isBlank(cancelSequenceOperatorString))
			return null;
		
		switch(cancelSequenceOperatorString)
		{
		case "equal": return "=";
		case "less": return "<";
		case "greater": return ">";
		default: throw new IllegalArgumentException("Unrecognized Cancel Sequence operator: " + cancelSequenceOperatorString);
		}
	}
	
	/**
	 * Gets the actual days unconfirmed operator string that should be used in the query.
	 * 	This is separate from the operator the user passed from the client, so that we aren't open to SQL injection attacks.
	 * @return The operator to use to compare the number of days unconfirmed when searching.
	 */
	public String getDaysUnconfirmedOperator()
	{
		if(StringUtils.isBlank(daysUnconfirmedOperatorString))
			return null;
		
		switch(daysUnconfirmedOperatorString)
		{
		case "equal": return "=";
		case "less": return "<";
		case "greater": return ">";
		case "lessEqual": return "<=";
		case "greaterEqual": return ">=";
		default: throw new IllegalArgumentException("Unrecognized Days Unconfirmed operator: " + daysUnconfirmedOperatorString);
		}
	}
	
	public String getVendorOrderNumberForQuery()
	{
		return StringUtils.isBlank(this.vendorOrderNumber) ? null : "%" + this.vendorOrderNumber + "%";
	}

	public Boolean getPenskeUser()
	{
		if(penskeUser == null)
			throw new IllegalStateException("User has not been set on confirmation search. Can not run search query without filtering by user permissions.");
		
		return penskeUser;
	}
	
	//***** DEFAULT ACCESSORS *****//
	public String getPoNumberString()
	{
		return poNumberString;
	}

	public ConfirmationSearch setPoNumberString(String poNumberString)
	{
		this.poNumberString = poNumberString;
		return this;
	}

	public String getUnitNumberString()
	{
		return unitNumberString;
	}

	public ConfirmationSearch setUnitNumberString(String unitNumberString)
	{
		this.unitNumberString = unitNumberString;
		return this;
	}

	public String getVendorOrderNumber()
	{
		return vendorOrderNumber;
	}

	public ConfirmationSearch setVendorOrderNumber(String vendorOrderNumber)
	{
		this.vendorOrderNumber = vendorOrderNumber;
		return this;
	}

	public String getCoOperatorString()
	{
		return coOperatorString;
	}

	public ConfirmationSearch setCoOperatorString(String coOperatorString)
	{
		this.coOperatorString = coOperatorString;
		return this;
	}

	public List<Integer> getVendorIdsFromFilter()
	{
		return vendorIdsFromFilter;
	}

	public ConfirmationSearch setVendorIdsFromFilter(List<Integer> vendorIdsFromFilter)
	{
		this.vendorIdsFromFilter = vendorIdsFromFilter;
		return this;
	}

	public Integer getCoNumber()
	{
		return coNumber;
	}

	public ConfirmationSearch setCoNumber(Integer coNumber)
	{
		this.coNumber = coNumber;
		return this;
	}

	public List<PoStatus> getStatuses()
	{
		return statuses;
	}

	public ConfirmationSearch setStatuses(List<PoStatus> statuses)
	{
		this.statuses = statuses;
		return this;
	}

	public Boolean getSentViaEdi()
	{
		return sentViaEdi;
	}

	public ConfirmationSearch setSentViaEdi(Boolean sentViaEdi)
	{
		this.sentViaEdi = sentViaEdi;
		return this;
	}

	public String getCancelSequenceOperatorString()
	{
		return cancelSequenceOperatorString;
	}

	public ConfirmationSearch setCancelSequenceOperatorString(String cancelSequenceOperatorString)
	{
		this.cancelSequenceOperatorString = cancelSequenceOperatorString;
		return this;
	}

	public Integer getCancelSequence()
	{
		return cancelSequence;
	}

	public ConfirmationSearch setCancelSequence(Integer cancelSequence)
	{
		this.cancelSequence = cancelSequence;
		return this;
	}

	public String getDaysUnconfirmedOperatorString()
	{
		return daysUnconfirmedOperatorString;
	}

	public ConfirmationSearch setDaysUnconfirmedOperatorString(String daysUnconfirmedOperatorString)
	{
		this.daysUnconfirmedOperatorString = daysUnconfirmedOperatorString;
		return this;
	}

	public Integer getDaysUnconfirmed()
	{
		return daysUnconfirmed;
	}

	public ConfirmationSearch setDaysUnconfirmed(Integer daysUnconfirmed)
	{
		this.daysUnconfirmed = daysUnconfirmed;
		return this;
	}

	public LocalDate getIssueDateFrom()
	{
		return issueDateFrom;
	}

	public ConfirmationSearch setIssueDateFrom(LocalDate issueDateFrom)
	{
		this.issueDateFrom = issueDateFrom;
		return this;
	}

	public LocalDate getIssueDateTo()
	{
		return issueDateTo;
	}

	public ConfirmationSearch setIssueDateTo(LocalDate issueDateTo)
	{
		this.issueDateTo = issueDateTo;
		return this;
	}

	public LocalDate getConfirmedDateFrom()
	{
		return confirmedDateFrom;
	}

	public ConfirmationSearch setConfirmedDateFrom(LocalDate confirmedDateFrom)
	{
		this.confirmedDateFrom = confirmedDateFrom;
		return this;
	}

	public LocalDate getConfirmedDateTo()
	{
		return confirmedDateTo;
	}

	public ConfirmationSearch setConfirmedDateTo(LocalDate confirmedDateTo)
	{
		this.confirmedDateTo = confirmedDateTo;
		return this;
	}

}
