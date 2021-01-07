package one.password.cli;

import one.password.Entity;

/** Common 1password CLI flags */
public enum Flags {
	SHORTHAND, SESSION, RAW, NAME, DESCRIPTION, LANGUAGE, ROLE, GROUP, VAULT, ALLOW_ADMINS_TO_MANAGE;

	/**
	 * Converts the flag to its String representation by converting it to lowercase and prepending
	 * "--", e.g. "MY_FLAG" would become "--my-flag".
	 */
	@Override
	public String toString() {
		return "--" + this.name().toLowerCase().replace('_', '-');
	}

	/**
	 * Converts the flag to a String including an value argument, e.g. "--flag=value". Returns null
	 * if the value is null.
	 */
	public String is(String value) {
		if (value == null) {
			return null;
		}

		return set(this.name().toLowerCase().replace('_', '-'), value);
	}

	/**
	 * Converts the provided Strings to a commandline flag, e.g. "--flag=value". Returns null if the
	 * value is null.
	 */
	private static String set(String flag, String value) {
		if (value == null) {
			return null;
		}

		return "--" + flag + "=" + value;
	}

	/**
	 * Converts the provided classe and value to a commandline flag, e.g. "--flag=value".. Returns
	 * null if the value is null.
	 */
	public static String set(Class<? extends Entity> flag, String value) {
		return set(flag.getSimpleName().toLowerCase(), value);
	}
}
