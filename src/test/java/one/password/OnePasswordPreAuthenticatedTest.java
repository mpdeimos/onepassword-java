package one.password;

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import one.password.test.TestConfig;

class OnePasswordPreAuthenticatedTest {
	@Test
	void withSession(TestConfig config, Session session) throws IOException {
		OnePassword.PreAuthenticated oppa = new OnePassword.PreAuthenticated(config, session);
		Assertions.assertThat(oppa.users().list()).isNotEmpty();
	}

	@Test
	void withOp(OnePassword op) throws IOException {
		OnePassword.PreAuthenticated oppa = new OnePassword.PreAuthenticated(op);
		Assertions.assertThat(oppa.users().list()).isNotEmpty();
	}

	@Test
	void withoutConfig(Session session) throws IOException {
		OnePassword.PreAuthenticated oppa = new OnePassword.PreAuthenticated(session);
		Assertions.assertThatIOException().isThrownBy(() -> oppa.users().list())
				.withMessageMatching(".*Cannot run program \"op(\\.exe)?\".*");
	}
}
