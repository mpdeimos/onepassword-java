package one.password;

import java.io.IOException;
import java.util.function.Supplier;
import one.password.cli.Op;
import one.password.util.SupplierWithException;

/**
 * High-level 1password CLI Java binding that creates a new session that will be auto-extended if
 * needed. The session will be auto-closed if used with a try-with-resource block.
 */
public class OnePassword extends OnePasswordBase implements AutoCloseable {
	private final String signInAddress;
	private final String emailAddress;
	private final String secretKey;
	private final Supplier<String> password;

	/** Signs in 1password, creating a new session. */
	public OnePassword(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) throws IOException {
		this(new Config(), signInAddress, emailAddress, secretKey, password);
	}

	/** Signs in 1password, creating a new session. */
	public OnePassword(Config config, String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) throws IOException {
		this(new Op(config), signInAddress, emailAddress, secretKey, password);
	}

	private OnePassword(Op op, String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) throws IOException {
		super(op);
		this.signInAddress = signInAddress;
		this.emailAddress = emailAddress;
		this.secretKey = secretKey;
		this.password = password;
		signinOnInit();
	}

	protected void signinOnInit() throws IOException {
		signin();
	}

	private void signin() throws IOException {
		session = op.signin(signInAddress, emailAddress, secretKey, password, session);
	}

	private void signout() throws IOException {
		op.signout(session);
		session = null;
	}

	@Override
	public void close() throws IOException {
		signout();
	}

	@Override
	protected String execute(SupplierWithException<String, IOException> action) throws IOException {
		try {
			return super.execute(action);
		} catch (IOException e) {
			if (!e.getMessage().toLowerCase().contains("you are not currently signed in")) {
				throw e;
			}
			signin();
			return super.execute(action);
		}
	}

	/**
	 * High-level 1password CLI Java binding that reuses an existing session, but does not
	 * auto-renew it.
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
