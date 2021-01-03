package one.password.cli;

public enum Commands {
	SIGNIN;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
