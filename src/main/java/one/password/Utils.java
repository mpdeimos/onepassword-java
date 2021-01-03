package one.password;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public final class Utils {
	private Utils() {
	}

	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

	private static final char[] BASE32_ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7' };

	public static String randomBase32(int length) {
		return new SecureRandom().ints(length, 0, BASE32_ALPHABET.length)
				.mapToObj(i -> Character.toString(BASE32_ALPHABET[i])).collect(Collectors.joining());
	}
}
