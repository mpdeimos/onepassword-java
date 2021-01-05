package one.password.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import one.password.Config;
import one.password.cli.Utils;

/** Utility methods for unit testing. */
public class TestUtils {
	private static final Path TEST_ENV_FILE = Paths.get(".test.env");

	/**
	 * Returns the environment that can be used to configure tests. This loads all environment
	 * variables and variables stored in the working directories ".test.env" file. The latter take
	 * precedence.
	 */
	public static Properties getTestEnvironment() throws IOException {
		Properties environment = new Properties();
		environment.putAll(System.getenv());

		if (Files.exists(TEST_ENV_FILE)) {
			environment.load(Files.newBufferedReader(TEST_ENV_FILE));
		}

		return environment;
	}

	/**
	 * Returns the version of 1password CLI that is stored in the "op.version" test resource and
	 * downloaded by Gradle during bootstrapping
	 */
	public static String getOnePasswordVersion() throws IOException {
		InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream("op.version");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			return reader.readLine();
		}
	}

	/** Creates a configuration with the 1password executable located in the build output. */
	public static Config createConfig() {
		Config config = new Config();
		String executable = "build/bin/op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}
		config.setExecutable(Paths.get(executable));
		return config;
	}
}
