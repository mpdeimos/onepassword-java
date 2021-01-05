package one.password.cli;

/** 1password CLI commands */
public enum Commands {
	SIGNIN, SIGNOUT, LIST, CREATE, DELETE;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
