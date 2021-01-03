package one.password.cli;

public enum Flags {
	VERSION, RAW;

	@Override
	public String toString() {
		return "--" + this.name().toLowerCase();
	}
}
