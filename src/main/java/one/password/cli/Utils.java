package one.password.cli;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** General utility methods. */
public final class Utils {
	private Utils() {
	}

	private static final boolean IS_WINDOWS =
			System.getProperty("os.name").toLowerCase().startsWith("windows");

	private static final char[] BASE32_ALPHABET =
			{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
					'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7'};

	private static final Pattern SHORTHAND_PATTERN = Pattern.compile(".+//([^\\.]+)\\..+");

	/** Returns whether the current Java process is running on a Windows OS. */
	public static boolean isWindowsOs() {
		return IS_WINDOWS;
	}

	/** Returns a random string with a specific length from the Base32 alphabet. */
	public static String randomBase32(int length) {
		return new SecureRandom().ints(length, 0, BASE32_ALPHABET.length)
				.mapToObj(i -> Character.toString(BASE32_ALPHABET[i]))
				.collect(Collectors.joining());
	}

	/**
	 * Extracts the shorthand from the sign in address. Returns an empty optional if the shorthand
	 * could not be extracted.
	 */
	public static Optional<String> getShorthand(String signInAddress) {
		Matcher matcher = SHORTHAND_PATTERN.matcher(signInAddress);
		if (!matcher.matches()) {
			return Optional.empty();
		}

		String subdomain = matcher.group(1);
		String shorthand = subdomain.replace('-', '_').toLowerCase();
		return Optional.of(shorthand);
	}
}
