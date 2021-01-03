package one.password.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.ongres.process.FluentProcess;
import com.ongres.process.FluentProcessBuilder;

import one.password.Utils;

public class Op {
	private final Config config;

	public Op() {
		this(new Config());
	}

	public Op(Config config) {
		this.config = config;
	}

	public String signin(String signInAddress, String emailAddress, String secretKey, Supplier<String> password)
			throws IOException {
		FluentProcess process = createProcess(Commands.SIGNIN, signInAddress, emailAddress, secretKey, Flags.RAW);
		process.inputStream(Stream.of(password).map(Supplier::get));
		return process.get();
	}

	public String version() throws IOException {
		return execute(Flags.VERSION);
	}

	public String execute(Object... arguments) throws IOException {
		return createProcess(arguments).get();
	}

	private FluentProcess createProcess(Object... arguments) throws IOException {
		String executable = "op";
		if (Utils.IS_WINDOWS) {
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

	public static class Config {
		private static final String DEFAULT_DEVICE = Utils.randomBase32(26);
		private Path executable;
		private Duration timeout = Duration.ofSeconds(10);

		public Optional<Path> getExecutable() {
			return Optional.ofNullable(executable);
		}

		public Optional<Duration> getTimeout() {
			return Optional.ofNullable(timeout);
		}

		public String getDevice() {
			return DEFAULT_DEVICE;
		}

		public void setExecutable(Path executable) {
			this.executable = executable;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}
	}
}
