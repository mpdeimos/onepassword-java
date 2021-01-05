package one.password;

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;

class OnePasswordTest {

	@Test
	void testListUsers() throws IOException {
		TestCredentials credentials = new TestCredentials();
		try (OnePassword op = new OnePassword(TestUtils.createConfig(),
				credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword)) {
			User[] users = op.listUsers();
			Assertions.assertThat(users).isNotEmpty();
		}
	}

	@Test
	void testAutoReSignIn() throws IOException {
		TestCredentials credentials = new TestCredentials();
		Config config = TestUtils.createConfig();
		try (OnePassword op = new OnePassword(config, credentials.getSignInAddress(),
				credentials.getEmailAddress(), credentials.getSecretKey(),
				credentials::getPassword)) {
			Session initialSession = op.session;
			op.op.signout(initialSession);
			User[] users = op.listUsers();
			Assertions.assertThat(users).isNotEmpty();
			Assertions.assertThat(initialSession.getSession())
					.isNotEqualTo(op.session.getSession());
		}
	}
}
