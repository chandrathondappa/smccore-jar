/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.search.domain.enums;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;
import com.penske.apps.smccore.base.domain.enums.PoStatus;
import com.penske.apps.smccore.search.domain.ConfirmationSearch;
import com.penske.apps.smccore.search.domain.SearchTemplate;

/**
 * The list of pre-configured searches that are selectable on the confirmation search page
 */
//FIXME: test
public enum ConfirmationSearchTemplateType implements MappedEnum
{
	/** All orders not yet confirmed */
	ALL_UNCONFIRMED("OC_ALL_UNCONF") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.ISSUED))
				.setSentViaEdi(false);
			return search;
		}
	},
	/** All orders that have been unconfirmed 3 days or more */
	UNCONFIRMED_OVER_3_DAYS("OC_UNCONF_3_DAYS") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.ISSUED))
				.setDaysUnconfirmedOperatorString("greaterEqual")
				.setDaysUnconfirmed(3)
				.setSentViaEdi(false);
			return search;
		}
	},
	/** All purchase orders that are not yet confirmed */
	UNCONFIRMED_PURCHASE_ORDERS("OC_UNCONF_PO") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.ISSUED))
				.setCoOperatorString("equal")
				.setCoNumber(0)
				.setCancelSequenceOperatorString("equal")
				.setCancelSequence(0)
				.setSentViaEdi(false);
			return search;
		}
	},
	/** All change orders that are not yet confirmed */
	UNCONFIRMED_CHANGE_ORDERS("OC_UNCONF_CO") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.ISSUED))
				.setCoOperatorString("greater")
				.setCoNumber(0)
				.setCancelSequenceOperatorString("equal")
				.setCancelSequence(0)
				.setSentViaEdi(false);
			return search;
		}
	},
	/** All cancellations that are not yet confirmed */
	UNCONFIRMED_CANCELLATIONS("OC_UNCONF_CS") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.ISSUED))
				.setCancelSequenceOperatorString("greater")
				.setCancelSequence(0)
				.setSentViaEdi(false);
			return search;
		}
	},
	/** All orders that have been confirmed in the last 90 days */
	CONFIRMED_PAST_90_DAYS("OC_CONF") {
		@Override public ConfirmationSearch createTemplateSearch()
		{
			ConfirmationSearch search = new ConfirmationSearch()
				.setStatuses(Arrays.asList(PoStatus.CONFIRM))
				.setConfirmedDateFrom(LocalDate.now().minusDays(90))
				.setConfirmedDateTo(LocalDate.now())
				.setSentViaEdi(false);
			return search;
		}
	}
	;
	
	private final String templateKey;
	
	private static final Map<String, ConfirmationSearchTemplateType> ALL_VALUES = Stream.of(values())
		.collect(toMap(type -> type.getTemplateKey(), x -> x));
	
	/**
	 * Looks up a search template type based on its identifier
	 * @param templateKey The template key to search for
	 * @return The search template type with the given identifier, or null if no match is found.
	 */
	public static ConfirmationSearchTemplateType fromTemplateKey(String templateKey)
	{
		return ALL_VALUES.get(templateKey);
	}
	
	/**
	 * Given a search template, looks up its template type
	 * @param searchTemplate The search template to look for
	 * @return The template type whose identifier matches that of the given search template, or null if there is no matching template type
	 */
	public static ConfirmationSearchTemplateType fromSearchTemplate(SearchTemplate searchTemplate)
	{
		String templateKey = searchTemplate.getTemplateKey();
		return fromTemplateKey(templateKey);
	}
	
	private ConfirmationSearchTemplateType(String templateKey)
	{
		this.templateKey = templateKey;
	}
	
	/**
	 * Creates a pre-configured confirmation search object based upon this template type.
	 * Further configuration of the result may be necessary after this method is called (ex: further filtering based on the user or available buddies or vendors)
	 * @return A search parameter object with parameters necessary to find orders matching this template type.
	 */
	public abstract ConfirmationSearch createTemplateSearch();
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return templateKey;
	}

	public String getTemplateKey()
	{
		return templateKey;
	}
}