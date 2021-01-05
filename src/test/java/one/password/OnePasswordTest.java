package one.password;

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import one.password.test.TestConfig;
import one.password.test.TestCredentials;

class OnePasswordTest {

	@Test
	void testListUsers(OnePasswordPreAuthenticated op, TestCredentials credentials)
			throws IOException {
		Assertions.assertThat(op.listUsers())
				.anyMatch(user -> user.email.equals(credentials.getEmailAddress()));
	}

	@Test
	void testAutoReSignIn(TestConfig config) throws IOException {
		try (OnePassword op = new OnePassword(config, config.credentials.getSignInAddress(),
				config.credentials.getEmailAddress(), config.credentials.getSecretKey(),
				config.credentials::getPassword)) {
			Session initialSession = op.session;
			op.op.signout(initialSession);
			User[] users = op.listUsers();
			Assertions.assertThat(users).isNotEmpty();
			Assertions.assertThat(initialSession.getSession())
					.isNotEqualTo(op.session.getSession());
		}
	}
}
