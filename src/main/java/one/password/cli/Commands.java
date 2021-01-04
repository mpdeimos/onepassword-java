package one.password.cli;

public enum Commands {
	SIGNIN, SIGNOUT;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
