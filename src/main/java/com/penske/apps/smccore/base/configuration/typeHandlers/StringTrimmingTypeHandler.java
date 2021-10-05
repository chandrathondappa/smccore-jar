/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.configuration.typeHandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * Type handler to trim leading and trailing whitespace off of Strings.
 * Should probably not be registered globally, but only used for specific mappings.
 */
//FIXME: test
public class StringTrimmingTypeHandler extends BaseTypeHandler<String>
{
	/** {@inheritDoc} */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException
	{
		ps.setString(i, StringUtils.trim(parameter));
	}

	/** {@inheritDoc} */
	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		return StringUtils.trim(rs.getString(columnName));
	}

	/** {@inheritDoc} */
	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		return StringUtils.trim(rs.getString(columnIndex));
	}

	/** {@inheritDoc} */
	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		return StringUtils.trim(cs.getString(columnIndex));
	}

}