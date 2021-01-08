package one.password.cli;

/** 1password CLI commands */
public enum Commands {
	VERSION("--version"), SIGNIN, SIGNOUT, GET, LIST, CREATE, EDIT, DELETE, ADD, REMOVE, CONFIRM, REACTIVATE, SUSPEND;

	private String name;

	private Commands() {
		this.name = name().toLowerCase();
	}

	private Commands(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
