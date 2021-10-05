/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.domain.enums.LookupKey;

/**
 * Class under test: {@link LookupContainer}
 */
public class LookupContainerTest
{
	private final List<LookupItem> items = Arrays.asList(
		CoreTestUtil.createLookupItem(LookupKey.RECOGNIZED_FILE_TYPES, "bmp", 5),
		CoreTestUtil.createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "A", 1),
		CoreTestUtil.createLookupItem(LookupKey.RECOGNIZED_FILE_TYPES, "docx", 10),
		CoreTestUtil.createLookupItem(LookupKey.SERVICE_VEHICLE_GROUP_CODE, "B", 2)
	);
	
	private final Map<String, DocTypeMaster> docTypeMastersMap = CoreTestUtil.getSalesnetDocTypeMasters();
	private final List<DocTypeMaster> salesnetDocTypes = new ArrayList<DocTypeMaster>(docTypeMastersMap.values());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	public void shouldCreateWithNoData()
	{
		LookupContainer lookups = new LookupContainer(null, null);
		assertThat(lookups.getLookupItems(LookupKey.RECOGNIZED_FILE_TYPES), is(Collections.<LookupItem>emptyList()));
		assertThat(lookups.getLookupItems(LookupKey.SERVICE_VEHICLE_GROUP_CODE), is(Collections.<LookupItem>emptyList()));
		assertThat(lookups.getLookupValues(LookupKey.RECOGNIZED_FILE_TYPES), is(Collections.<String>emptySet()));
		assertThat(lookups.getLookupValues(LookupKey.SERVICE_VEHICLE_GROUP_CODE), is(Collections.<String>emptySet()));
		assertThat(lookups.getDocTypeMastersByDocTypeName(), is(Collections.<String, DocTypeMaster>emptyMap()));
	}
	
	@Test
	public void shouldCreateLookupsInOrder()
	{
		LookupContainer lookups = new LookupContainer(items, salesnetDocTypes);
		assertThat(lookups.getLookupItems(LookupKey.RECOGNIZED_FILE_TYPES), is(Arrays.asList(items.get(0), items.get(2))));
		assertThat(lookups.getLookupValues(LookupKey.RECOGNIZED_FILE_TYPES), is((Set<String>)new HashSet<String>(Arrays.asList("bmp", "docx"))));
		assertThat(lookups.getLookupItems(LookupKey.SERVICE_VEHICLE_GROUP_CODE), is(Arrays.asList(items.get(1), items.get(3))));
		
		assertThat(lookups.getDocTypeMastersByDocTypeName(), is(docTypeMastersMap));
	}

	@Test
	public void shouldNotCreateLookupsOutOfOrder()
	{
		List<LookupItem> outOfOrderItems = Arrays.asList(items.get(3), items.get(2), items.get(0), items.get(1));
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Lookups must be in ascending sequence order for each lookup name");
		
		new LookupContainer(outOfOrderItems, salesnetDocTypes);
	}

	@Test
	public void shouldGetSingleLookupValue()
	{
		LookupContainer lookups = CoreTestUtil.getLookupContainer();
		String phone = lookups.getSingleLookupValue(LookupKey.SUPPORT_PHONE_NUM);
		assertThat(phone, is("555-555-5555"));
	}
	
	@Test
	public void shouldGetSingleLookupValueInt()
	{
		LookupContainer lookups = new LookupContainer(Arrays.asList(
			CoreTestUtil.createLookupItem(LookupKey.XLS_MAIL_GENERATOR_SLEEP_TIME, "15000", 1),
			CoreTestUtil.createLookupItem(LookupKey.UNIT_QUEUE_POOL_SIZE, "", 1)
		), Collections.emptyList());
		
		assertThat(lookups.getSingleLookupValueInt(LookupKey.XLS_MAIL_GENERATOR_SLEEP_TIME), is(15000));
		assertThat(lookups.getSingleLookupValueInt(LookupKey.UNIT_QUEUE_POOL_SIZE), is(nullValue()));
		assertThat(lookups.getSingleLookupValueInt(LookupKey.CA_STATUS), is(nullValue()));
	}
	
	@Test
	public void shouldNotGetSingleLookupValueForMultiValuedKey()
	{
		LookupContainer lookups = CoreTestUtil.getLookupContainer();
		
		thrown.expectMessage("SMC Lookup contains more than one value for key");
		lookups.getSingleLookupValue(LookupKey.SERVICE_VEHICLE_GROUP_CODE);
	}
	
	@Test
	public void shouldNotGetSingleLookupValueIntForMultiValuedKey()
	{
		LookupContainer lookups = CoreTestUtil.getLookupContainer();
		
		thrown.expectMessage("SMC Lookup contains more than one value for key");
		lookups.getSingleLookupValueInt(LookupKey.SERVICE_VEHICLE_GROUP_CODE);
	}
	
	@Test
	public void shouldNotGetSingleLookupValueIntForString()
	{
		LookupContainer lookups = CoreTestUtil.getLookupContainer();
		
		thrown.expectMessage("is not a number, but the program expects it to be");
		lookups.getSingleLookupValueInt(LookupKey.SUPPORT_PHONE_NUM);
	}
	
	@Test
	public void shouldNotGetSingleLookupValueIntForDecimal()
	{
		LookupContainer lookups = new LookupContainer(Arrays.asList(
			CoreTestUtil.createLookupItem(LookupKey.SPEC_CONFIRMATION_DAS_ID, "1.5", 1)
		), Collections.emptyList());
		
		thrown.expect(NumberFormatException.class);
		lookups.getSingleLookupValueInt(LookupKey.SPEC_CONFIRMATION_DAS_ID);
	}
	
	@Test
	public void shouldGetDocumentType()
	{
		LookupContainer lookups = CoreTestUtil.getLookupContainer();
		assertThat(lookups.getDocTypeMasterForDocumentName("foo.pdf").getDocTypeId(), is(2));
		assertThat(lookups.getDocTypeMasterForDocumentName("foo").getDocTypeId(), is(2));
		assertThat(lookups.getDocTypeMasterForDocumentName("EOOD Override").getDocTypeId(), is(14));
	}
}
