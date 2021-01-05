package one.password.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.ongres.process.FluentProcess;
import com.ongres.process.FluentProcessBuilder;
import com.ongres.process.Output;
import one.password.Config;
import one.password.Session;
import one.password.util.Utils;

/** Wraps execution of the 1password CLI using FluentProcess and strict exception handling. */
class OpProcess {
	private final FluentProcess process;

	private OpProcess(FluentProcess process) {
		this.process = process;
	}

	/** Starts the op executable with environment set from the configuration. */
	public static OpProcess start(Config config, Session session, Object... arguments)
			throws IOException {
		return wrapExceptions(() -> {
			String executable = getExecutable(config);
			FluentProcessBuilder builder = FluentProcess.builder(executable);

			builder.environment("OP_DEVICE", config.getDevice());
			if (session != null) {
				builder.environment(session.getEnvironmentVariableName(), session.getSession());
			}

			Arrays.stream(arguments).filter(Objects::nonNull)
					.forEach(arg -> builder.arg(arg.toString()));

			builder.allowedExitCode(1);
			FluentProcess process = builder.start();
			if (config.getTimeout().isPresent()) {
				process = process.withTimeout(config.getTimeout().get());
			}

			return new OpProcess(process);
		});
	}

	private static String getExecutable(Config config) {
		String executable = "op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}

		if (config.getExecutable().isPresent()) {
			executable = config.getExecutable().get().toAbsolutePath().toString();
		}
		return executable;
	}

	/** Connects a stream of input strings. */
	public OpProcess input(Stream<String> input) throws IOException {
		wrapExceptions(() -> process.inputStream(input));
		return this;
	}

	/** Returns the process output or throws an {@link IOException} if execution failed. */
	public String output() throws IOException {

		Output output = process.tryGet();
		Optional<String> error = output.error();
		if (error.isPresent() && !error.get().isEmpty()) {
			throw new IOException(error.get());
		}

		Optional<Exception> exception = output.exception();
		if (exception.isPresent()) {
			throw new IOException(exception.get().getMessage(), exception.get());
		}

		return output.output().orElseThrow(() -> new IOException("Invalid process output"));
	}

	private static <T> T wrapExceptions(Supplier<T> action) throws IOException {
		try {
			return action.get();
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
