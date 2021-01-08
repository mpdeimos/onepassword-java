package one.password.test;

import java.nio.file.Paths;
import one.password.Config;
import one.password.util.Utils;

/**
 * Common configuration used for tests. Uses the downloaded op executable during bootstrap and sets
 * the device ID to a fixed value.
 */
public class TestConfig extends Config {
	public final TestCredentials credentials;

	public TestConfig(TestCredentials credentials) {
		this.credentials = credentials;

		String executable = "build/bin/op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}
		setExecutable(Paths.get(executable));
		setCache(true);
		TestUtils.assertNoIOException(() -> setDevice(
				TestUtils.getTestEnvironment().getProperty("OP_TEST_DEVICE", null)));
	}


}
