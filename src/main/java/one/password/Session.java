package one.password;

/**
 * Session data for authenticating with 1password. This includes the session token returned after
 * signin and the URL shorthand.
 */
public class Session {
	private final String session;
	private final String shorthand;

	public Session(String session, String shorthand) {
		this.session = session;
		this.shorthand = shorthand;
	}

	public String getSession() {
		return session;
	}

	public String getShorthand() {
		return shorthand;
	}

	public String getEnvironmentVariableName() {
		return "OP_SESSION_" + shorthand;
	}
}
