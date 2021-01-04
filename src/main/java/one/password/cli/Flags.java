package one.password.cli;

/** Common 1password CLI flags */
public enum Flags {
	VERSION, SHORTHAND, SESSION, RAW;

	/**
	 * Converts the flag to its String representation by converting it to lowercase and prepending
	 * "--", e.g. "FLAG" would become "--flag".
	 */
	@Override
	public String toString() {
		return "--" + this.name().toLowerCase();
	}

	/**
	 * Converts the flag to a String including an value argument, e.g. "--flag=value"
	 */
	public String is(String value) {
		return this + "=" + value;
	}
}
