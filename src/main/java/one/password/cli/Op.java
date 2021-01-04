package one.password.cli;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import one.password.Session;
import one.password.Utils;

public class Op {
	private final Config config;

	public Op() {
		this(new Config());
	}

	public Op(Config config) {
		this.config = config;
	}

	public Session signin(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password) throws IOException {
		return signin(signInAddress, emailAddress, secretKey, password, null);
	}

	public Session signin(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password, Session session) throws IOException {
		String shorthand = getShorthand(signInAddress);

		String sessionFlag = null;
		if (session != null) {
			sessionFlag = Flags.SESSION.is(session.getSession());
		}
		OpProcess process = OpProcess.start(config, null, Commands.SIGNIN, signInAddress,
				emailAddress, secretKey, Flags.SHORTHAND.is(shorthand), sessionFlag, Flags.RAW);
		process.input(Stream.of(password).map(Supplier::get));
		return new Session(process.output(), shorthand);
	}

	public void signout(Session session) throws IOException {
		execute(session, Commands.SIGNOUT);
	}

	public String version() throws IOException {
		return execute(Flags.VERSION);
	}

	private String getShorthand(String signInAddress) throws IOException {
		Optional<String> optionalShorthand = config.getShorthand();
		if (!optionalShorthand.isPresent()) {
			optionalShorthand = Utils.getShorthand(signInAddress);
		}

		return optionalShorthand.orElseThrow(() -> new IOException(
				"Could not determine shorthand from sign in address: " + signInAddress));
	}

	public String execute(Object... arguments) throws IOException {
		return execute(config, (Session) null, arguments);
	}

	public String execute(Session session, Object... arguments) throws IOException {
		return OpProcess.start(config, session, arguments).output();
	}
}
