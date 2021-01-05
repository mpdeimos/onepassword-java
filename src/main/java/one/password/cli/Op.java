package one.password.cli;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import one.password.Config;
import one.password.Session;

/** Porcelain wrapper around the 1password CLI. */
public class Op {
	private final Config config;

	public Op() {
		this(new Config());
	}

	public Op(Config config) {
		this.config = config;
	}

	/** Signs in 1password creating a new session. */
	public Session signin(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) throws IOException {
		return signin(signInAddress, emailAddress, secretKey, password, null);
	}

	/** Signs in 1password reusing the specified session or creating a new one. */
	public Session signin(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password, Session session) throws IOException {
		String shorthand = getShorthand(signInAddress);

		// The session cannot be passed as env var for login
		String sessionFlag = null;
		if (session != null) {
			sessionFlag = Flags.SESSION.is(session.getSession());
		}
		OpProcess process = OpProcess.start(config, null, Commands.SIGNIN, signInAddress,
				emailAddress, secretKey, Flags.SHORTHAND.is(shorthand), sessionFlag, Flags.RAW);
		process.input(Stream.of(password).map(Supplier::get));
		return new Session(process.output(), shorthand);
	}

	/** Signs out the current session. */
	public void signout(Session session) throws IOException {
		execute(session, Commands.SIGNOUT);
	}

	/** Lists all items of a given entity type. */
	public String list(Session session, Entities entity) throws IOException {
		return execute(session, Commands.LIST, entity);
	}

	/** Prints the version number of the installed 1password CLI. */
	public String version() throws IOException {
		return execute(null, Flags.VERSION);
	}

	/**
	 * Executes an arbitrary 1password CLI command. Arguments are converted to strings using
	 * {@link #toString()}. The session may be null in order to use a not use authentication or
	 * manually handle it via {@link Flags#SESSION}.
	 */
	public String execute(Session session, Object... arguments) throws IOException {
		return OpProcess.start(config, session, arguments).output();
	}

	private String getShorthand(String signInAddress) throws IOException {
		Optional<String> optionalShorthand = config.getShorthand();
		if (!optionalShorthand.isPresent()) {
			optionalShorthand = Utils.getShorthand(signInAddress);
		}

		return optionalShorthand.orElseThrow(() -> new IOException(
				"Could not determine shorthand from sign in address: " + signInAddress));
	}

	/** An action of the op executable. */
	public interface Action<T> {
		/** Executes the action. */
		T execute() throws IOException;
	}

}
