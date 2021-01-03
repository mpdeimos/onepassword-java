package one.password;

import java.io.IOException;
import java.util.Properties;

import org.assertj.core.api.Assertions;

public class TestCredentials {

	private final Properties environment;

	public TestCredentials() throws IOException {
		environment = TestUtils.getTestEnvironment();

		Assertions.assertThat(getSignInAddress()).isNotEmpty();
		Assertions.assertThat(getEmailAddress()).isNotEmpty();
		Assertions.assertThat(getSecretKey()).isNotEmpty();
		Assertions.assertThat(getPassword()).isNotEmpty();
	}

	public String getSignInAddress() {
		return environment.getProperty("OP_TEST_SIGNINADDRESS");
	}

	public String getEmailAddress() {
		return environment.getProperty("OP_TEST_EMAILADDRESS");
	}

	public String getSecretKey() {
		return environment.getProperty("OP_TEST_SECRETKEY");
	}

	public String getPassword() {
		return environment.getProperty("OP_TEST_PASSWORD");
	}
}
