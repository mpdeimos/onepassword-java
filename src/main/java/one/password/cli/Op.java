package one.password.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import com.ongres.process.FluentProcess;

public class Op {
	private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

	private final Config config;

	public Op() {
		this(new Config());
	}

	public Op(Config config) {
		this.config = config;
	}

	public String version() throws IOException {
		return execute("-v");
	}

	private String execute(String... arguments) throws IOException {
		String executable = "op";
		if (WINDOWS) {
			executable += ".exe";
		}

		return FluentProcess.start(executable, arguments).get();
	}

	public static class Config {
		private Path binary;

		public Optional<Path> getBinary() {
			return Optional.of(binary);
		}

		public void setBinary(Path binary) {
			this.binary = binary;
		}
	}
}
