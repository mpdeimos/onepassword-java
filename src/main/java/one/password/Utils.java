package one.password;

public final class Utils {
	private Utils() {
	}

	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
}
