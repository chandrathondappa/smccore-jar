/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.typehandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * Date time handler that converts from old sql timesetamps to new Java 8 dates and times. This is a stopgap measure because as of 2022-03-28, the JTOpen driver for DB2 does not support
 * Java 8's time package (JSR 310) yet. It appears that JTOpen version 11.0 will support that, but it has not so far been released yet. 
 */
public class SmcLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime>
{
	 @Override
	  public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
	    ps.setTimestamp(i, Timestamp.valueOf(parameter));
	  }

	  @Override
	  public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
	    Timestamp timestamp = rs.getTimestamp(columnName);
	    return getLocalDateTime(timestamp);
	  }

	  @Override
	  public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
	    Timestamp timestamp = rs.getTimestamp(columnIndex);
	    return getLocalDateTime(timestamp);
	  }

	  @Override
	  public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
	    Timestamp timestamp = cs.getTimestamp(columnIndex);
	    return getLocalDateTime(timestamp);
	  }

	  private static LocalDateTime getLocalDateTime(Timestamp timestamp) {
	    if (timestamp != null) {
	      return timestamp.toLocalDateTime();
	    }
	    return null;
	  }
}