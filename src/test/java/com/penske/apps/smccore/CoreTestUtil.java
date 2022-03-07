/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.penske.apps.smccore.base.beans.LookupManager;
import com.penske.apps.smccore.base.domain.ConfirmationAlertData;
import com.penske.apps.smccore.base.domain.DocTypeMaster;
import com.penske.apps.smccore.base.domain.EmailTemplate;
import com.penske.apps.smccore.base.domain.FulfillmentAlertData;
import com.penske.apps.smccore.base.domain.LookupCacheInfo;
import com.penske.apps.smccore.base.domain.LookupContainer;
import com.penske.apps.smccore.base.domain.LookupItem;
import com.penske.apps.smccore.base.domain.ProductionAlertData;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.UserLogin;
import com.penske.apps.smccore.base.domain.enums.AlertType;
import com.penske.apps.smccore.base.domain.enums.EmailTemplateType;
import com.penske.apps.smccore.base.domain.enums.LookupKey;
import com.penske.apps.smccore.base.domain.enums.PayableStatus;
import com.penske.apps.smccore.base.domain.enums.SecurityFunction;
import com.penske.apps.smccore.base.domain.enums.UserDepartment;
import com.penske.apps.smccore.base.domain.enums.UserType;
import com.penske.apps.smccore.component.domain.RuleOutcome;
import com.penske.apps.smccore.component.domain.enums.ConflictStatus;
import com.penske.apps.smccore.component.domain.enums.Visibility;
import com.penske.apps.smccore.component.domain.unittemplate.UnitComponent;
import com.penske.apps.smccore.component.domain.unittemplate.UnitMasterInfo;

/**
 * A class containing utility methods for testing, including methods for creating basic
 * 	versions of some of the domain objects in the Loadsheet application.
 * NOTE: this class should only be used for jUnit tests, not for actual business logic.
 */
public final class CoreTestUtil
{
	/** Private constructor - can't instantiate this class. Utility methods only. */
	private CoreTestUtil() {}

	private static int idCounter = 1;
	
	//***** PRE-MADE DATA *****//
	static {
		int seq = 0;
		List<LookupItem> items = Arrays.asList(
			createLookupItem(LookupKey.RECOGNIZED_FILE_TYPES, "pdf", seq++),
			createLookupItem(LookupKey.RECOGNIZED_FILE_TYPES, "msg", seq++),
			createLookupItem(LookupKey.SUPPORT_PHONE_NUM, "555-555-5555", seq++),
			createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "A", seq++),
			createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "B", seq++),
			createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "C", seq++),
			createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "D", seq++),
			createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "E", seq++),
			createLookupItem(LookupKey.EBS_PRIORITY, "0", seq++),
			createLookupItem(LookupKey.EBS_HIGH_PRIORITY, "9", seq++)
		);
		
		List<DocTypeMaster> salesnetDocTypes = Arrays.asList(
			createDocTypeMaster(1, "Ratesheet"),
			createDocTypeMaster(2, "Sales Docs"),
			createDocTypeMaster(3, "Schedule \"S\""),
			createDocTypeMaster(4, "Schedule A Amdnedment"),
			createDocTypeMaster(14, "EOOD Override"),
			createDocTypeMaster(9, "Electronic OEM Ordering Document")
		);
		
		lookupContainer = new LookupContainer(items, salesnetDocTypes);
	}
	
	private static final LookupContainer lookupContainer;
	
	public static Map<String, DocTypeMaster> getSalesnetDocTypeMasters()
	{
		return lookupContainer.getDocTypeMastersByDocTypeName();
	}
	
	public static LookupContainer getLookupContainer()
	{
		return lookupContainer;
	}
	
	public static LookupManager createLookupManager()
	{
		return new LookupManager() {
			/** {@inheritDoc} */
			@Override
			public LookupContainer getLookupContainer()
			{
				return lookupContainer;
			}
		};
	}

	//***** DOMAIN OBJECT HELPERS *****//
	public static void addSecurityFunctions(User user, SecurityFunction ... functions)
	{
		Set<SecurityFunction> secFuncs = Stream.of(Arrays.asList(functions), user.getSecurityFunctions())
			.flatMap(Collection::stream)
			.collect(toSet());
		
		set(user, "securityFunctions", secFuncs);
	}
	
	public static void addAssociatedVendors(User user, Integer... vendorIds)
	{
		Set<Integer> vendIds = Stream.of(Arrays.asList(vendorIds), user.getAssociatedVendorIds())
			.flatMap(Collection::stream)
			.collect(toSet());
		
		set(user, "associatedVendorIds", vendIds);
	}
	
	//***** DOMAIN OBJECT CREATION METHODS *****//
	public static LookupCacheInfo createLookupCacheInfo(Date lastModifiedDate, int lookupCount)
	{
		LookupCacheInfo result = newInstance(LookupCacheInfo.class);
		set(result, "lookupModified", lastModifiedDate);
		set(result, "doctypeMasterModified", lastModifiedDate);
		set(result, "lookupCount", lookupCount);
		set(result, "doctypeCount", lookupCount);
		return result;
	}
	
	public static LookupItem createLookupItem(LookupKey key, String value, int sequence)
	{
		LookupItem result = newInstance(LookupItem.class);
		set(result, "name", key.getDbName());
		set(result, "value", value);
		set(result, "sequence", sequence);
		return result;
	}
	
	public static EmailTemplate createEmailTemplate(EmailTemplateType emailType, String subjectTemplate, String bodyTemplate)
	{
		EmailTemplate result = newInstance(EmailTemplate.class);
		set(result, "emailType", emailType);
		set(result, "subjectTemplate", subjectTemplate);
		set(result, "bodyTemplate", bodyTemplate);
		return result;
	}
	
	public static ConfirmationAlertData createConfirmationAlertData(Map<AlertType, Integer> alerts)
	{
		if(alerts == null)
			alerts = Collections.emptyMap();
		
		ConfirmationAlertData result = newInstance(ConfirmationAlertData.class);
		for(Entry<AlertType, Integer> alert : alerts.entrySet())
		{
			String fieldName;
			switch(alert.getKey())
			{
			case OC_PURCHASE_ORDER: 		fieldName = "purchaseOrderCount"; break;
			case OC_CHANGE_ORDER:			fieldName = "changeOrderCount"; break;
			case OC_CANC_ORDER:				fieldName = "cancellationCount"; break;
			default: fieldName = null;
			}
			
			if(StringUtils.isNotBlank(fieldName))
				set(result, fieldName, alert.getValue());
		}
		
		return result;
	}
	
	public static ProductionAlertData createProductionAlertData(Map<AlertType, Integer> alerts)
	{
		if(alerts == null)
			alerts = Collections.emptyMap();
		
		ProductionAlertData result = newInstance(ProductionAlertData.class);
		for(Entry<AlertType, Integer> alert : alerts.entrySet())
		{
			String fieldName;
			switch(alert.getKey())
			{
			case PROD_EST_PROD_PAST_DUE:	fieldName = "estProductionDatePastDueCount"; break;
			case PROD_EST_DELV_PAST_DUE:	fieldName = "estDeliveryDatePastDueCount"; break;
			case PROD_PROD_HOLDS:			fieldName = "prodHoldsCount"; break;
			case ALL_MISSING_INFO:			fieldName = "missingInfoCount"; break;
			case PROD_DATA_CONFLICT:		fieldName = "dataConflictCount"; break;
			case PROD_DELAY_COMM_REQ:		fieldName = "delayCommReqCount"; break;
			case PROD_PROD_DT_EARLY:		fieldName = "prodDateEarlyCount"; break;
			case PROD_PROD_DT_LATE:			fieldName = "prodDateLateCount"; break;
			case PROD_DELV_DT_EARLY:		fieldName = "deliveryDateEarlyCount"; break;
			case PROD_DELV_DT_LATE:			fieldName = "deliveryDateLateCount"; break;
			default: fieldName = null;
			}
			
			if(StringUtils.isNotBlank(fieldName))
				set(result, fieldName, alert.getValue());
		}
		
		return result;
	}
	
	public static DocTypeMaster createDocTypeMaster(int docTypeId, String docType)
	{
		DocTypeMaster result = newInstance(DocTypeMaster.class);
		set(result, "docTypeId", docTypeId);
		set(result, "docGroupName", LookupManager.SALESNET_DOCUMENT_GROUP);
		set(result, "docType", docType);
		set(result, "editable", false);
		return result;
	}
	
	public static User createUser(Integer userId, String sso, String firstName, String lastName, String email, UserType userType)
	{
		User user = newInstance(User.class);
		set(user, "userId", userId);
		set(user, "sso", sso);
		set(user, "firstName", firstName);
		set(user, "lastName", lastName);
		set(user, "emailAddress", email);
		set(user, "userType", userType);
		set(user, "orgName", "Test Org");
		set(user, "userDepartment", UserDepartment.SUPPLY_SPECIALIST);
		set(user, "securityFunctions", Collections.emptySet());
		set(user, "associatedVendorIds", Collections.emptySet());

		return user;
	}

	public static UserLogin createUserLogin(int loginId, User user)
	{
		UserLogin result = newInstance(UserLogin.class);
		set(result, "loginId", loginId);
		set(result, "sso", user.getSso());
		set(result, "serverLocation", "apps.pensketruckleasing.net");
		set(result, "loginTime", LocalDateTime.now());
		return result;
	}
	
	public static FulfillmentAlertData createFulfillmentAlertData(int working, int pending, int readyToOrder)
	{
		FulfillmentAlertData result = newInstance(FulfillmentAlertData.class);
		set(result, "workingCount", working);
		set(result, "pendingCount", pending);
		set(result, "readyToOrderCount", readyToOrder);
		set(result, "contractReviewCount", 0);
		set(result, "vendorAnalystAssignmentRequiredCount", 0);
		set(result, "newVendorSetupRequiredCount", 0);
		set(result, "vendorUserSetupRequiredCount", 0);
		return result;
	}
	
	public static UnitMasterInfo createUnitMasterInfo(int masterId, String unitNumber, int vendorId, int poCategoryAssociationId, int templateId, String unitSignature)
	{
		return createUnitMasterInfoDelivered(masterId, unitNumber, vendorId, poCategoryAssociationId, templateId, unitSignature, PayableStatus.ISSUED, null, 1);
	}
	
	public static UnitMasterInfo createUnitMasterInfoTemplateOutOfDate(int masterId, String unitNumber, int vendorId, int poCategoryAssociationId, int templateId, String unitSignature)
	{
		UnitMasterInfo master = createUnitMasterInfoDelivered(masterId, unitNumber, vendorId, poCategoryAssociationId, templateId, unitSignature, PayableStatus.ISSUED, null, 1);
		set(master, "previousTemplateHash", "out of date");
		return master;
	}
	
	public static UnitMasterInfo createUnitMasterInfoDelivered(int masterId, String unitNumber, int vendorId, int poCategoryAssociationId, int templateId, String unitSignature, PayableStatus payableStatus, Date actualDeliveryDate, int missingInfoCount)
	{
		UnitMasterInfo result = newInstance(UnitMasterInfo.class);
		set(result, "masterId", masterId);
		set(result, "unitNumber", unitNumber);
		set(result, "vendorId", vendorId);
		set(result, "poCategoryAssociationId", poCategoryAssociationId);
		set(result, "previousUnitSignature", unitSignature);
		set(result, "calculatedUnitSignature", unitSignature);
		set(result, "templateId", templateId);
		set(result, "previousTemplateHash", "test hash");
		set(result, "masterTemplateHash", "test hash");
		set(result, "payableStatus", payableStatus);
		set(result, "actualDeliveryDate", actualDeliveryDate);
		set(result, "missingInfoCount", missingInfoCount);
		return result;
	}
	
	public static RuleOutcome createRuleOutcome(int ruleId, int componentId, int templateId, int priority, Visibility visibility)
	{
		RuleOutcome result = newInstance(RuleOutcome.class);
		set(result, "ruleId", ruleId);
		set(result, "componentId", componentId);
		set(result, "templateId", templateId);
		set(result, "priority", priority);
		set(result, "visibility", visibility);
		return result;
	}

	public static UnitComponent createUnitComponent(int masterId, int componentId, Visibility baseVisibility, Visibility ruleVisibility, Visibility finalVisibility, ConflictStatus conflictStatus, boolean valueProvided)
	{
		UnitComponent result = newInstance(UnitComponent.class);
		set(result, "masterId", masterId);
		set(result, "componentId", componentId);
		set(result, "baseVisibility", baseVisibility);
		set(result, "ruleVisibility", ruleVisibility);
		set(result, "finalVisibility", finalVisibility);
		set(result, "conflictStatus", conflictStatus);
		set(result, "valueProvided", valueProvided);
		
		return result;
	}
	
	//***** HELPER METHODS *****//
	public static Date dateAt(int year, int month, int day)
	{
		return dateAt(year, month, day, 0, 0, 0, 0);
	}
	
	public static Date dateAt(int year, int month, int day, int hour, int minute, int second, int millis)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millis);
		return cal.getTime();
	}
	
	/**
	 * Forces creation of a new instance of an object, regardless of the accessibility of that object's null constructor.
	 * This method will throw an exception if there is no null constructor.
	 * @param claz The class to create a new object from.
	 * @return An instance of the object created using the default constructor, even if that constructor is private.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static <E> E newInstance(Class<E> claz)
	{
		try {
			Constructor<E> ctor = claz.getDeclaredConstructor();
			boolean accessible = ctor.isAccessible();
			ctor.setAccessible(true);
			E result = ctor.newInstance();
			ctor.setAccessible(accessible);
			return result;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Forces a field in an object to a particular value, regardless of that field's access modifiers.
	 * NOTE: this is to be used only in creating test objects for jUnit tests, not in business logic.
	 * @param target The object on which to set the field value.
	 * @param name The name of the field to set.
	 * @param value The value to put into the field.
	 * @throws IllegalAccessException
	 */
	public static void set(Object target, String name, Object value)
	{
		try {
			FieldUtils.writeField(FieldUtils.getField(target.getClass(), name, true), target, value);
		} catch(IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void callMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] args)
	{
		try {
			Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
			boolean accessible = method.isAccessible();
			method.setAccessible(true);
			method.invoke(target, args);
			method.setAccessible(accessible);
		} catch(IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException e){
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Flattens a list of lists into a single list. The final list has all the elements of all the child lists.
	 * @param originalList The list of lists to flatten.
	 * @return A list containing all elements in any of the lists inside the given list.
	 */
	public static <T> List<T> flattenList(Collection<? extends Collection<T>> originalList)
	{
		if(originalList == null)
			return Collections.emptyList();

		List<T> result = new ArrayList<T>();
		for(Collection<T> subList : originalList)
			result.addAll(subList);

		return result;
	}

	/**
	 * Creates a Mockito {@link Answer} instance that sets the given value on the given field of the first argument passed to the mocked method, and returns nothing.
	 * The purpose for this method is there are a number of AS400 stored procedures used by the application that use OUT parameters to return their results.
	 * 	These procedures have to be invoked by passing an object in, and then examining the results of fields on the object.
	 * 	This is tedious and wordy to mock, so this method is a shortcut for that (relatively complicated) mocking behavior.
	 * @param fieldName The name of the field on the parameter object that should be set.
	 * @param value The value to set into the given field on the parameter object.
	 */
	public static Answer<Object> answerOutParam(final String fieldName, final Object value)
	{
		return new Answer<Object>(){
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				Object container = invocation.getArgument(0);
				set(container, fieldName, value);
				return null;
			}
		};
	}

	//FIXME: document
	public static Answer<Void> answerSetId(final int parameterIndex, final String idFieldName)
	{
		return new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) {
				Object arg = invocation.getArgument(parameterIndex);
				
				Collection<?> items;
				if(arg instanceof Collection || arg instanceof Map)
					items = (arg instanceof Collection) ? (Collection<?>) arg : ((Map<?, ?>) arg).values();
				else
					items = Arrays.asList(arg);
				
				for(Object item : items)
					set(item, idFieldName, idCounter++);
				
				return null;
			}
		};
	}
	
	/**
	 * Returns the given elements, put into a set, similar to {@link Arrays#asList(Object...)}
	 * @param elements The elements to add to the set.
	 * @return The elements, put into a set.
	 */
	@SafeVarargs	//This doesn't do anything that would pollute the heap, like casting to Object[]
	public static <E> Set<E> setOf(E... elements)
	{
		return new HashSet<E>(Arrays.asList(elements));
	}

	/**
	 * Returns the given entries, stored in a map.
	 * @param entries The keys and values that should go in the map.
	 * @return A map containing all the entries. If two entries in the list have the same key, the latter one wins.
	 */
	@SafeVarargs	//This doesn't do anything that would pollute the heap, like casting to Object[]
	public static <K, V> Map<K, V> mapOf(Entry<? extends K, ? extends V>... entries)
	{
		Map<K, V> result = new LinkedHashMap<K, V>();
		for(Entry<? extends K, ? extends V> entry : entries)
			result.put(entry.getKey(), entry.getValue());
		return result;
	}
	
	//***** CUSTOM MATCHERS *****//
	public static <T extends Comparable<T>> CompareToEqualMatcher<T> comparesEqualTo(T expected)
	{
		return new CompareToEqualMatcher<T>(expected);
	}

	public static class CompareToEqualMatcher<T extends Comparable<T>> extends TypeSafeMatcher<T>
	{
		private final T expected;

		private CompareToEqualMatcher(T expected)
		{
			this.expected = expected;
		}

		/** {@inheritDoc} */
		@Override
		public void describeTo(Description description)
		{
			description.appendText("a value equal to ").appendValue(expected);
		}

		/** {@inheritDoc} */
		@Override
		protected boolean matchesSafely(T actual)
		{
			return actual.compareTo(expected) == 0;
		}

		/** {@inheritDoc} */
		@Override
		protected void describeMismatchSafely(T actual, Description mismatchDescription)
		{
			mismatchDescription.appendValue(actual).appendText(" was not equal to ").appendValue(expected);
		}
	}
}