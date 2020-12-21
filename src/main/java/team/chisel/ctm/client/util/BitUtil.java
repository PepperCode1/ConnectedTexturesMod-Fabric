package team.chisel.ctm.client.util;

public class BitUtil {
	/**
	 * Casts to int but keeps the bits the same.
	 */
	public static int bitIntCast(byte b) {
		return ((int) b) & 0xFF;
	}
	
	/**
	 * Casts to int but keeps the bits the same.
	 */
	public static int bitIntCast(short s) {
		return ((int) s) & 0xFFFF;
	}
}
