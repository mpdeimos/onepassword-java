package one.password;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import one.password.util.Utils;

/** Configuration for the 1password CLI. */
public class Config {
	private static final String DEFAULT_DEVICE = Utils.randomBase32(26);
	private String shorthand;
	private Path executable;
	private Path configDir;
	private boolean cache = false;
	private Duration timeout = Duration.ofSeconds(30);
	private String device = DEFAULT_DEVICE;

	public Optional<Path> getExecutable() {
		return Optional.ofNullable(executable);
	}

	public Config setExecutable(Path executable) {
		this.executable = executable;
		return this;
	}

	public Optional<Path> getConfigDir() {
		return Optional.ofNullable(configDir);
	}

	public Config setConfigDir(Path configDir) {
		this.configDir = configDir;
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

	public void setCache(boolean enabled) {
		this.cache = enabled;
	}

	public boolean getCache() {
		return cache;
	}

	public String getDevice() {
		return device;
	}

	/**
	 * Sets the device id. Must be a lowercase string of length 26 from the Base32 alphabet. Pass
	 * null in order to reset to an auto-generated value.
	 */
	public Config setDevice(String device) throws IllegalArgumentException {
		if (device == null) {
			device = DEFAULT_DEVICE;
		}

		if (device.length() != DEFAULT_DEVICE.length()) {
			throw new IllegalArgumentException(
					"The device id must have a string length of " + DEFAULT_DEVICE.length());
		}

		if (!Utils.isBase32(device)) {
			throw new IllegalArgumentException(
					"The device id is no valid Base32 string: " + device);
		}

		this.device = device;
		return this;
	}

}
