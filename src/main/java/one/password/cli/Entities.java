package one.password.cli;

/** 1password CLI entities */
public enum Entities {
	DOCUMENTS, EVENTS, GROUPS, ITEMS, TEMPLATES, USERS, VAULTS;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
