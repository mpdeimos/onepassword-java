package one.password.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.ongres.process.FluentProcess;
import com.ongres.process.FluentProcessBuilder;

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

	public Session signin(String signInAddress, String emailAddress, String secretKey, Supplier<String> password)
			throws IOException {
		Optional<String> optionalShorthand = config.getShorthand();
		if (!optionalShorthand.isPresent()) {
			optionalShorthand = Utils.getShorthand(signInAddress);
		}

		String shorthand = optionalShorthand.orElseThrow(
				() -> new IOException("Could not determine shorthand from sign in address: " + signInAddress));

		FluentProcess process = createProcess(Commands.SIGNIN, signInAddress, emailAddress, secretKey,
				Flags.SHORTHAND.is(shorthand), Flags.RAW);
		process.inputStream(Stream.of(password).map(Supplier::get));
		String session = process.get();
		return new Session(session, shorthand);
	}

	public String version() throws IOException {
		return execute(Flags.VERSION);
	}

	public String execute(Object... arguments) throws IOException {
		return createProcess(arguments).get();
	}

	private FluentProcess createProcess(Object... arguments) throws IOException {
		String executable = "op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}

		if (config.getExecutable().isPresent()) {
			executable = config.getExecutable().get().toAbsolutePath().toString();
		}

		FluentProcessBuilder builder = FluentProcess.builder(executable);
		Arrays.stream(arguments).forEach(arg -> builder.arg(arg.toString()));
		builder.environment("OP_DEVICE", config.getDevice());
		FluentProcess process = builder.start();
		if (config.getTimeout().isPresent()) {
			process.withTimeout(config.getTimeout().get());
		}

		return process;
	}
}
