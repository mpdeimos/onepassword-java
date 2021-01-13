package one.password;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import one.password.cli.Op;
import one.password.util.SupplierWithException;

/**
 * High-level 1password CLI Java binding that creates a new session on first request that will be
 * auto-extended if needed. The session will be auto-closed if used with a try-with-resource block.
 */
public class OnePassword extends OnePasswordBase implements AutoCloseable {
	private static final List<String> SIGNIN_ERRORS = Arrays.asList("session expired",
			"sign in to create a new session", "you are not currently signed in");
	private final String signInAddress;
	private final String emailAddress;
	private final String secretKey;
	private final Supplier<String> password;

	/** Signs in 1password, creating a new session. */
	public OnePassword(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) {
		this(new Config(), signInAddress, emailAddress, secretKey, password);
	}

	/** Signs in 1password, creating a new session. */
	public OnePassword(Config config, String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) {
		this(new Op(config), signInAddress, emailAddress, secretKey, password);
	}

	OnePassword(Op op, String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) {
		super(op);
		this.signInAddress = signInAddress;
		this.emailAddress = emailAddress;
		this.secretKey = secretKey;
		this.password = password;
	}

	public OnePassword signin() throws IOException {
		session = op.signin(signInAddress, emailAddress, secretKey, password, session);
		return this;
	}

	private OnePassword signout() throws IOException {
		if (session != null) {
			op.signout(session);
			session = null;
		}

		return this;
	}

	@Override
	public void close() throws IOException {
		signout();
	}

	@Override
	protected String execute(SupplierWithException<String, IOException> action) throws IOException {
		try {
			if (session == null) {
				signin();
			}

			return super.execute(action);
		} catch (IOException e) {
			if (SIGNIN_ERRORS.stream()
					.noneMatch(error -> e.getMessage().toLowerCase().contains(error))) {
				throw e;
			}

			signin();
			return super.execute(action);
		}
	}

	/**
	 * High-level 1password CLI Java binding that reuses an existing session, but does not
	 * auto-renew it or auto-login.
	 */
	public static class PreAuthenticated extends OnePasswordBase {
		public PreAuthenticated(OnePasswordBase api) {
			super(api.op, api.session);
		}

		public PreAuthenticated(Session session) {
			this(new Config(), session);
		}

		public PreAuthenticated(Config config, Session session) {
			super(new Op(config), session);
		}
	}
}
