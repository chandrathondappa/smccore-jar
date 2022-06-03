/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.search.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.PoStatus;
import com.penske.apps.smccore.base.util.DateUtil;
import com.penske.apps.smccore.base.util.Util;
import com.penske.apps.smccore.search.domain.enums.AlertType;
import com.penske.apps.smccore.search.domain.enums.ConfirmationComplianceType;

/**
 * Represents all the search parameters that can be used to filter a search on the Order Confirmation screen.
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
	
	/** The start date for searches on the issue date */
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate issueDateFrom;
	/** The end date for searches on the issue date */
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate issueDateTo;
	/** The start date for searches on the confirmed date */
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate confirmedDateFrom;
	/** The end date for searches on the confirmed date */
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
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
	
	/** The corp the user wanted to search in */
	private String corp;
	
	/** The customer name the user wanted to search in */
	private String customerName;
	
	/** The customer number the user wanted to search in */
	private String customerNumber;
	
	/** The makes the user wanted to search in */
	private List<String> makes;
	
	/** The model years the user wanted to search in */
	private List<String> modelYears;
	
	/** The models the user wanted to search in */
	private List<String> models;
	
	/** The vehicle types the user wanted to search in */
	private List<String> vehicleTypes;
	
	/** The vehicle uses the user wanted to search in */
	private List<String> vehicleUses;
	
	/** The compliance alerts the user wanted to search in */
	private List<ConfirmationComplianceType> compliances;
	
	/** The PO categories the user wanted to search in */
	private List<String> poCategories;
	
	/** The PO sub-categories the user wanted to search in */
	private List<String> poSubCategories;
	
	/** The vendor names the user wanted to search in */
	private List<Integer> vendorIds;
	
	/** The Vehicle Supply specialist SSOs the user wanted to search in */
	private List<String> vsSpecialists;
	
	List<ConfirmationSearch> complianceSearches;
	
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
	
	public ConfirmationSearch() {
	}

	public ConfirmationSearch(String poNumberString, String unitNumberString, String vendorOrderNumber,
			String coOperatorString, Integer coNumber, String cancelSequenceOperatorString, Integer cancelSequence,
			String daysUnconfirmedOperatorString, Integer daysUnconfirmed, LocalDate issueDateFrom,
			LocalDate issueDateTo, LocalDate confirmedDateFrom, LocalDate confirmedDateTo, Boolean sentViaEdi,
			List<PoStatus> statuses, Boolean penskeUser, List<Integer> vendorIdsFromFilter, String corp,
			String customerName, String customerNumber, List<String> makes, List<String> modelYears, List<String> models,
			List<String> vehicleTypes, List<String> vehicleUses, List<ConfirmationComplianceType> compliances, List<String> poCategories, List<String> poSubCategories,
			List<Integer> vendorIds, List<String> vsSpecialists) {
		this.poNumberString = poNumberString;
		this.unitNumberString = unitNumberString;
		this.vendorOrderNumber = vendorOrderNumber;
		this.coOperatorString = coOperatorString;
		this.coNumber = coNumber;
		this.cancelSequenceOperatorString = cancelSequenceOperatorString;
		this.cancelSequence = cancelSequence;
		this.daysUnconfirmedOperatorString = daysUnconfirmedOperatorString;
		this.daysUnconfirmed = daysUnconfirmed;
		this.issueDateFrom = issueDateFrom;
		this.issueDateTo = issueDateTo;
		this.confirmedDateFrom = confirmedDateFrom;
		this.confirmedDateTo = confirmedDateTo;
		this.sentViaEdi = sentViaEdi;
		this.statuses = statuses;
		this.penskeUser = penskeUser;
		this.vendorIdsFromFilter = vendorIdsFromFilter;
		this.corp = corp;
		this.customerName = customerName;
		this.customerNumber = customerNumber;
		this.makes = makes;
		this.modelYears = modelYears;
		this.models = models;
		this.vehicleTypes = vehicleTypes;
		this.vehicleUses = vehicleUses;
		this.compliances = compliances;
		this.poCategories = poCategories;
		this.poSubCategories = poSubCategories;
		this.vendorIds = vendorIds;
		this.vsSpecialists = vsSpecialists;
		this.complianceSearches = null;
	}



	//***** SPECIALIZED GETTERS FOR SEARCH QUERY *****//
	//These methods should not be serialized to JSON when sending the search to the client, but are only here for using the object in MyBatis
	/**
	 * Gets a tokenized list of the individual PO numbers the user wants to search for
	 * @return The PO numbers, listed out
	 */
	@JsonIgnore
	public Set<Integer> getPoNumbers()
	{
		return Util.getTokenizedPoNumbers(poNumberString, null);
	}
	
	/**
	 * Gets a tokenized list of all the individual unit numbers the user wants to search for
	 * @return The unit numbers, listed out
	 */
	@JsonIgnore
	public Set<String> getUnitNumbers()
	{
		return Util.getTokenizedUnitNumbers(unitNumberString, null, true);
	}
	
	/**
	 * Gets the actual CO operator string that should be used in the query.
	 * 	This is separate from the operator the user passed from the client, so that we aren't open to SQL injection attacks.
	 * @return The operator to use to compare CO numbers when searching.
	 */
	@JsonIgnore
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
	@JsonIgnore
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
	@JsonIgnore
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
	
	@JsonIgnore
	public String getVendorOrderNumberForQuery()
	{
		return StringUtils.isBlank(this.vendorOrderNumber) ? null : "%" + this.vendorOrderNumber + "%";
	}

	@JsonIgnore
	public Boolean getPenskeUser()
	{
		if(penskeUser == null)
			throw new IllegalStateException("User has not been set on confirmation search. Can not run search query without filtering by user permissions.");
		
		return penskeUser;
	}
	
	/**
	 * Gets the list of ConfirmationSearchs generated by the user's selections for compliances. This should only be used in the query
	 */
	@JsonIgnore
	public List<ConfirmationSearch> getComplianceSearches()
	{
		return complianceSearches;
	}
	
	/**
	 * Takes the compliances the user selected and makes ConfirmationSearch objects from them to be used in the query
	 */
	public void setComplianceSearches(Map<AlertType, Integer> measureValues, User user)
	{
		if(compliances != null && !compliances.isEmpty()) {
			this.complianceSearches = compliances.stream()
				.map(c->c.createComplianceSearch(measureValues).asUser(user, vendorIdsFromFilter))
				.collect(Collectors.toList());
		}
	}
	
	//***** MODIFIED ACCESSORS *****//
	@JsonIgnore
	public String getFormattedConfirmedDateFrom() {
		return DateUtil.formatDateUS(confirmedDateFrom);
	}
	
	@JsonIgnore
	public String getFormattedConfirmedDateTo() {
		return DateUtil.formatDateUS(confirmedDateTo);
	}
	
	@JsonIgnore
	public String getFormattedIssueDateFrom() {
		return DateUtil.formatDateUS(issueDateFrom);
	}
	
	@JsonIgnore
	public String getFormattedIssueDateTo() {
		return DateUtil.formatDateUS(issueDateTo);
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

	public String getCorp() {
		return corp;
	}

	public void setCorp(String corp) {
		this.corp = corp;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public List<String> getMakes() {
		return makes;
	}

	public void setMakes(List<String> makes) {
		this.makes = makes;
	}

	public List<String> getModelYears() {
		return modelYears;
	}

	public void setModelYears(List<String> modelYears) {
		this.modelYears = modelYears;
	}

	public List<String> getModels() {
		return models;
	}

	public void setModels(List<String> models) {
		this.models = models;
	}

	public List<String> getVehicleTypes() {
		return vehicleTypes;
	}

	public void setVehicleTypes(List<String> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
	}

	public List<String> getVehicleUses() {
		return vehicleUses;
	}

	public void setVehicleUses(List<String> vehicleUses) {
		this.vehicleUses = vehicleUses;
	}

	public List<ConfirmationComplianceType> getCompliances() {
		return compliances;
	}

	public void setCompliances(List<ConfirmationComplianceType> compliances) {
		this.compliances = compliances;
	}

	public List<String> getPoCategories() {
		return poCategories;
	}

	public void setPoCategories(List<String> poCategories) {
		this.poCategories = poCategories;
	}

	public List<String> getPoSubCategories() {
		return poSubCategories;
	}

	public void setPoSubCategories(List<String> poSubCategories) {
		this.poSubCategories = poSubCategories;
	}

	public List<Integer> getVendorIds() {
		return vendorIds;
	}

	public void setVendorIds(List<Integer> vendorIds) {
		this.vendorIds = vendorIds;
	}

	public List<String> getVsSpecialists() {
		return vsSpecialists;
	}

	public void setVsSpecialists(List<String> vsSpecialists) {
		this.vsSpecialists = vsSpecialists;
	}

}
