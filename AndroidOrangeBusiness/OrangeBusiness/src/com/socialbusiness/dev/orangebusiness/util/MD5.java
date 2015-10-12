package com.socialbusiness.dev.orangebusiness.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {

	private static final String ALGORITHM = "MD5";

	private static char sHexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static MessageDigest sDigest;

	static {
		try {
			sDigest = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Get MD5 Digest failed.");
			e.printStackTrace();
		}
	}

	private MD5() {
	}

	final public static String encode(String source) {
		if (source == null) {
			return null;
		}
		byte[] btyes = source.getBytes();
		byte[] encodedBytes = sDigest.digest(btyes);

		return hexString(encodedBytes);
	}

	public static String hexString(byte[] source) {
		if (source == null || source.length <= 0) {
			return "";
		}

		final int size = source.length;
		final char str[] = new char[size * 2];
		int index = 0;
		byte b;
		for (int i = 0; i < size; i++) {
			b = source[i];
			str[index++] = sHexDigits[b >>> 4 & 0xf];
			str[index++] = sHexDigits[b & 0xf];
		}
		return new String(str);
	}

	// public static void main(String[] args) {
	//
	// System.out.println(MD5.encode("a"));
	// // System.out.println(MD5.encode("abc"));
	// }
}
