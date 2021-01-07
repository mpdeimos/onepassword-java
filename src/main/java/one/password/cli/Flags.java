package one.password.cli;

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
	 * Converts the flag to a String including an value argument, e.g. "--flag=value"
	 */
	public String is(String value) {
		if (value == null) {
			return null;
		}

		return this.toString() + "=" + value;
	}
}
