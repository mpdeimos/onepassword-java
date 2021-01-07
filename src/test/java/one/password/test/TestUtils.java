package one.password.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import one.password.util.SupplierWithException;

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

	/** Asserts that the action does not throw an {@link java.io.IOException}. */
	public static <T> T assertNoIOException(SupplierWithException<T, IOException> action) {
		try {
			return action.get();
		} catch (IOException e) {
			Assertions.assertThat(e).doesNotThrowAnyException();
			return null;
		}
	}

	/** Waits for one second. */
	public static void waitOneSecond() {
		Assertions.assertThatCode(() -> Thread.sleep(Duration.ofSeconds(1).toMillis()))
				.doesNotThrowAnyException();
	}
}
