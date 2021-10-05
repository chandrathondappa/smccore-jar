/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.configuration.typeHandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.penske.apps.smccore.base.domain.enums.MappedEnum;

/**
 * MyBatis type handler to handle mapping Java enums to their proper database fields.
 * This type handler does not have to contain a clause for every enum type,
 * but only for enum types where the value in the database column is not identical to the name of the enum constant.
 */
public class SmcEnumTypeHandler<E extends MappedEnum> extends BaseTypeHandler<E>
{
	/** Reverse-lookup map for quickly finding enum constants by their mapped values. */
	private final Map<String, E> enumsByMappedValue = new HashMap<String, E>();
	
	/**
	 * Creates a new handler for a specific class of enum.
	 * This will be invoked by MyBatis when attempting to handle an enum it hasn't seen before.
	 * @param type The class of the enum this type handler will handle.
	 */
	public SmcEnumTypeHandler(Class<? extends E> type)
	{
		if(!type.isEnum())
			throw new IllegalArgumentException("Can not have an SMC enum type handler on a non-enum class: " + type.getName());
		
		for(E enumConstant : type.getEnumConstants())
			enumsByMappedValue.put(enumConstant.getMappedValue(), enumConstant);
	}

	/**
	 * Override: @see org.apache.ibatis.type.EnumTypeHandler#getNullableResult(java.sql.CallableStatement, int)
	 */
	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		String value = cs.getString(columnIndex);
		return enumsByMappedValue.get(value);
	}

	/**
	 * Override: @see org.apache.ibatis.type.EnumTypeHandler#getNullableResult(java.sql.ResultSet, int)
	 */
	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		String value = rs.getString(columnIndex);
		return enumsByMappedValue.get(value);
	}

	/**
	 * Override: @see org.apache.ibatis.type.EnumTypeHandler#getNullableResult(java.sql.ResultSet, java.lang.String)
	 */
	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		String value = rs.getString(columnName);
		E result = enumsByMappedValue.get(value);
		return result;
	}

	/**
	 * Override: @see org.apache.ibatis.type.EnumTypeHandler#setNonNullParameter(java.sql.PreparedStatement, int, java.lang.Enum, org.apache.ibatis.type.JdbcType)
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException
	{
		String value = parameter.getMappedValue();
		ps.setString(i, value);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "SMC handler: " + super.toString();
	}
}
