/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore;

import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * Contains generic dummy test data for various unit tests.
 */
public class TestData
{
	public static final String SSO = "600555555";
	
	public final User userPenske = CoreTestUtil.createUser(1234, SSO, "Joe", "Test", "joe.test@penske.com", UserType.PENSKE);
	public final User userVendor = CoreTestUtil.createUser(1234, "test@morgan.com", "Morgan", "Test", "test@morgan.com", UserType.VENDOR);
	
	public TestData()
	{
		CoreTestUtil.addAssociatedVendors(userVendor, 100, 101);
	}
}
