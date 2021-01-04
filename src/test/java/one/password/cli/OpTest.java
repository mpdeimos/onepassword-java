package one.password.cli;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import one.password.Session;
import one.password.TestCredentials;
import one.password.TestUtils;
import one.password.Utils;

public class OpTest {
	@Test
	void testGetVersion() throws IOException {
		Assertions.assertThat(new Op(createConfig()).version()).isEqualTo(TestUtils.getOnePasswordVersion());
	}

	@Test
	void testSignin() throws IOException {
		TestCredentials credentials = new TestCredentials();
		Session session = new Op(createConfig()).signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		Assertions.assertThat(session.getSession()).hasLineCount(1).hasSize(43);
		Assertions.assertThat(session.getShorthand())
				.isEqualTo(URI.create(credentials.getSignInAddress()).getHost().split("\\.")[0].replace("-", "_"));
		// TODO test timeout
		// TODO test fail
		// TODO test re-login with token
	}

	private Config createConfig() {
		Config config = new Config();
		String executable = "build/bin/op";
		if (Utils.isWindowsOs()) {
			executable += ".exe";
		}
		config.setExecutable(Paths.get(executable));
		return config;
	}
}
