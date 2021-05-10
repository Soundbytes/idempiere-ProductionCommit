package de.soundbytes.utils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.adempiere.exceptions.DBException;
import org.compiere.util.DB;

public class Util extends org.compiere.util.Util{
	public static int fieldToInt(Object fieldVal) {
		if (fieldVal == null) return 0;
		int intVal = ((Integer) fieldVal).intValue();
		return intVal < 0 ?  0 : intVal;		
	}
	
	public static int fieldToInt(Object fieldVal, int nullVal) {
		if (fieldVal == null) return nullVal;
		int intVal = ((Integer) fieldVal).intValue();
		return intVal < 0 ?  nullVal : intVal;		
	}	
	
	public static boolean obj2bool(Object fieldVal) {
		if (fieldVal == null) return false;
		Boolean boolVal = (Boolean) fieldVal;
		return boolVal;
	}	
	
	public static Integer valToField(int intVal) {
		return 0 < intVal ?  Integer.valueOf(intVal) : null;
	}
	
	public static Integer valToField(int intVal, int nullVal) {
		return  nullVal < intVal ?  Integer.valueOf(intVal) : null;
	}
	
	public static String valToField(String strVal) {
		return isStrEmpty(strVal) ?  null : strVal;
	}	
	
	
	/*
	 * Compare String fields: A blank string is considered equal to a null string)
	 */
	public static boolean strEq(Object st1, Object st2) {
		if (st1 == null || ((String)st1).isBlank()) st1 = new String();
		if (st2 == null || ((String)st2).isBlank()) st2 = new String();
		return st1.equals(st2);
	}
	
	public static boolean intEq(Object v1, Object v2) {
		return ((v1 == null && v2 == null) || (v1 != null && v1.equals(v2)));
	}
	
	public static boolean isStrEmpty(Object value) {
		return value == null || ((String)value).isBlank();
	}
	
/*	public static boolean isIntEmpty(int val) {
		return val < 1;
	}	
	
	public static boolean isIntEmpty( Object val) {
		if (val == null) return true;
		return ((Integer)val).intValue() < 1;
	}
*/
	public static boolean fieldToBoolean(Object value) {
		if (value == null) return false; 
		if (value instanceof Boolean) return ((Boolean) value).booleanValue();
		if (!(value instanceof String)) return false;
		return "Y".equals((String)value) ? true : false;
	}	
	
	@SuppressWarnings("unchecked")
	private static <T extends Number> T nextSeq(T zeroVal, String tableName, String idColumnName, int idx) {
		String sql = "SELECT coalesce(trunc(max(?), -1), 0)+10 FROM ? WHERE ? = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            pstmt = DB.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
            pstmt.setString(1, zeroVal instanceof BigDecimal ? "Sequence" : "Line");
            pstmt.setString(2, tableName);
            pstmt.setString(3, idColumnName);
            pstmt.setInt(4, idx);
            
            rs = pstmt.executeQuery();
            
            if (zeroVal instanceof BigDecimal)
            	return rs.next() ? (T)rs.getBigDecimal(1) : zeroVal;
            else
            	return rs.next() ? (T)Integer.valueOf(rs.getInt(1)) : zeroVal;
        }
        catch (SQLException e)
        {
        	throw new DBException(e, sql);
        }
        finally
        {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }
	}
	
	public static BigDecimal nextSequence(String tableName, String idColumnName, int idx) {
		return nextSeq(BigDecimal.ZERO, tableName, idColumnName, idx);
	}
	
	public static int nextLine(String tableName, String idColumnName, int idx) {
		return nextSeq(Integer.valueOf(0), tableName, idColumnName, idx).intValue();
	}
	
}
