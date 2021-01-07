package one.password;

/** Permission roles */
public enum Role {
	MEMBER, MANAGER;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	/** Role wrapper object in Json. */
	static class JsonWrapper {
		private Role role;

		public Role getRole() {
			return role;
		}
	}
}
