/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.penske.apps.smccore.CoreTestUtil;
import com.penske.apps.smccore.base.dao.UserDAO;
import com.penske.apps.smccore.base.domain.User;
import com.penske.apps.smccore.base.domain.enums.UserType;

/**
 * Class under test: {@link UserService}
 */
public class UserServiceTest
{
	private final UserDAO userDAO = mock(UserDAO.class);
	private final UserService service = new UserService(userDAO);
	
	@Test
	public void shouldGetUser()
	{
		User user = CoreTestUtil.createUser(1234, "600013219", "JOHN", "PAPAVASILION", "test@penske.com", UserType.PENSKE);
		when(userDAO.getUsers("600013219", null, null, true, false)).thenReturn(Arrays.asList(user));

		User actualUser = service.getUser("600013219", false);

		assertThat(actualUser, is(user));
	}
}
