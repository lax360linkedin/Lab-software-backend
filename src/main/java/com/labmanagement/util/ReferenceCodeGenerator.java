
package com.labmanagement.util;

import java.security.SecureRandom;
import java.util.Locale;

public final class ReferenceCodeGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();

	private ReferenceCodeGenerator() {
		// Prevent object creation
	}

	public static String generate(String prefix) {

		if (prefix == null || prefix.isBlank()) {
			throw new IllegalArgumentException("Reference code prefix cannot be blank");
		}

		int number = 10000 + RANDOM.nextInt(90000);

		return prefix.trim().toUpperCase(Locale.ROOT) + "-" + number;
	}
}
