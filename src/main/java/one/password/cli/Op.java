package one.password.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

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

	public String version() throws IOException {
		return execute("-v");
	}

	private String execute(String... arguments) throws IOException {
		String executable = "op";
		if (Utils.IS_WINDOWS) {
			executable += ".exe";
		}

		if (config.getExecutable().isPresent()) {
			executable = config.getExecutable().get().toAbsolutePath().toString();
		}

		FluentProcessBuilder builder = FluentProcess.builder(executable, arguments);
		return builder.start().get();
	}

	public static class Config {
		private Path executable;

		public Optional<Path> getExecutable() {
			return Optional.ofNullable(executable);
		}

		public void setExecutable(Path executable) {
			this.executable = executable;
		}
	}
}
