package one.password.test;

import java.nio.file.Paths;
import one.password.Config;
import one.password.util.Utils;

/** Common configuration used for tests. */
public class TestConfig extends Config {
	public final TestCredentials credentials;

	public TestConfig(TestCredentials credentials) {
		this.credentials = credentials;

		String executable = "build/bin/op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}
		setExecutable(Paths.get(executable));
	}

}
