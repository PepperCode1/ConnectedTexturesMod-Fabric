package team.chisel.ctm.client.util;

public class BitUtil {
	public static boolean getBit(byte map, int bit) {
		return ((map >> bit) & 1) == 1;
	}

	public static boolean getBit(long map, int bit) {
		return ((map >> bit) & 1) == 1;
	}

	public static byte setBit(byte map, int bit) {
		return (byte) (map | (1 << bit));
	}

	public static long setBit(long map, int bit) {
		return (long) (map | (1 << bit));
	}

	public static byte clearBit(byte map, int bit) {
		return (byte) (map & ~(1 << bit));
	}

	public static long clearBit(long map, int bit) {
		return (long) (map & ~(1 << bit));
	}

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
