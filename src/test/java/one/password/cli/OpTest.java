package one.password.cli;

import java.io.IOException;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
		Assertions.assertThat(new Op(createConfig()).signin(credentials.getSignInAddress(),
				credentials.getEmailAddress(), credentials.getSecretKey(), credentials::getPassword)).hasLineCount(1)
				.hasSize(43);
		// TODO test timeout
		// TODO test fail
		// TODO test re-login with token
	}

	private Op.Config createConfig() {
		Op.Config config = new Op.Config();
		String executable = "build/bin/op";
		if (Utils.IS_WINDOWS) {
			executable += ".exe";
		}
		config.setExecutable(Paths.get(executable));
		return config;
	}
}
