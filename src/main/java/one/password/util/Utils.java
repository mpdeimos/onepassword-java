package one.password.util;

import java.security.SecureRandom;
import java.util.Arrays;
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

	private static final int[] BASE32_ALPHABET =
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
				.mapToObj(i -> Character.toString((char) BASE32_ALPHABET[i]))
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

	/** Tests whether the generated device id is from the Base32 alphabet. */
	public static boolean isBase32(String string) {
		return string.chars().allMatch(c -> Arrays.stream(BASE32_ALPHABET).anyMatch(a -> a == c));
	}

	/** Returns the array type of a class. */
	@SuppressWarnings("unchecked")
	public static <T> Class<T[]> arrayType(Class<T> type) {
		try {

			return (Class<T[]>) Class.forName("[L" + type.getCanonicalName() + ";");
		} catch (ClassNotFoundException e) {
			throw new AssertionError(
					"Cannot create array type from class " + type.getCanonicalName());
		}
	}

	public static String[] asArray(String a, String... more) {
		return combineSwitched(more, a);
	}

	public static String[] asArray(String a, String b, String... more) {
		return combineSwitched(more, combine(a, b));
	}

	public static String[] asArray(String a, String b, String c, String... more) {
		return combineSwitched(more, combine(a, b, c));
	}

	private static String[] combine(String... more) {
		return more;
	}

	private static String[] combineSwitched(String[] after, String... before) {
		String[] copy = Arrays.copyOf(before, before.length + after.length);
		System.arraycopy(after, 0, copy, before.length, after.length);
		return copy;
	}
}
