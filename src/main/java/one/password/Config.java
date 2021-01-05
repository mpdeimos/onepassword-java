package one.password;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import one.password.cli.Utils;

/** Configuration for the 1password CLI. */
public class Config {
	// TODO make configurable
	private static final String DEFAULT_DEVICE = Utils.randomBase32(26);
	private String shorthand;
	private Path executable;
	private Duration timeout = Duration.ofSeconds(10);

	public Optional<Path> getExecutable() {
		return Optional.ofNullable(executable);
	}

	public Config setExecutable(Path executable) {
		this.executable = executable;
		return this;
	}

	public Optional<String> getShorthand() {
		return Optional.ofNullable(shorthand);
	}

	public Config setShorthand(String shorthand) {
		this.shorthand = shorthand;
		return this;
	}

	public Optional<Duration> getTimeout() {
		return Optional.ofNullable(timeout);
	}

	public Config setTimeout(Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	public String getDevice() {
		return DEFAULT_DEVICE;
	}
}
