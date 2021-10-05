package com.penske.apps.smccore.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods in DAOs that should not be wrapped in SQL to filter be vendor ID.
 * It is assumed that all queries will filter by vendor ID unless explicitly marked with this annotation.
 * Thus:<ul>
 *   <li>All queries not marked with this annotation must return a field called VENDOR_ID</li>
 *   <li>If the user is not a vendor user (vendor users are userType = 2), the query will not be wrapped with any SQL</li>
 *   <li>If the user is a vendor user, any query not marked with this annotation will be wrapped with the following SQL:
 *   	<p><pre>SELECT * FROM ({query SQL here}) WHERE VENDOR_ID IN ({user's associated vendor list here})</pre></p>
 *   </li>
 * </ul>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NonVendorQuery
{
	/**
	 * Can optionally add a comment for why this query does not require vendor filtering
	 */
	public String value() default "";
}