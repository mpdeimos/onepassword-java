package one.password.cli;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import one.password.Config;
import one.password.Entity;
import one.password.Group;
import one.password.Session;
import one.password.User;
import one.password.Vault;
import one.password.test.TestConfig;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;

public class OpTest {
	@Test
	void testGetVersion(Op op) throws IOException {
		Assertions.assertThat(op.version()).isEqualTo(TestUtils.getOnePasswordVersion());
	}

	@Test
	void testGetVersionWithoutConfig() throws IOException {
		Assertions.assertThatIOException().isThrownBy(() -> new Op().version())
				.withMessageMatching(".*Cannot run program \"op(\\.exe)?\".*");
	}

	@Test
	void testTimeout(Config config) {
		Assertions.assertThatIOException()
				.isThrownBy(() -> new Op(config.setTimeout(Duration.ofNanos(1))).version());
	}

	@Test
	void testSignin(Op op, TestCredentials credentials) throws IOException {
		Session session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		Assertions.assertThat(session.getSession()).hasLineCount(1).hasSize(43);
		Assertions.assertThat(session.getShorthand())
				.isEqualTo(URI.create(credentials.getSignInAddress()).getHost().split("\\.")[0]
						.replace("-", "_"));
	}


	@Test
	void testSigninWithShorthand(TestConfig config) throws IOException {
		config.setShorthand("myshorthand");
		Op op = new Op(config);
		Session session = op.signin(config.credentials.getSignInAddress(),
				config.credentials.getEmailAddress(), config.credentials.getSecretKey(),
				config.credentials::getPassword);
		Assertions.assertThat(session.getShorthand()).isEqualTo("myshorthand");
	}

	@Test
	void testSigninTwiceYieldsSameSession(Op op, TestCredentials credentials, Session session)
			throws IOException {
		Session newSession =
				op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
						credentials.getSecretKey(), credentials::getPassword, session);
		Assertions.assertThat(newSession).usingRecursiveComparison().isEqualTo(session);
	}

	@Test
	void testSigninWithWrongCredentials(Op op, TestCredentials credentials) throws IOException {
		Assertions.assertThatIOException()
				.isThrownBy(() -> op.signin(credentials.getSignInAddress(), "foo@bar.com",
						credentials.getSecretKey(), () -> "xxx"));
	}

	@Test
	void testSignout(Op op, TestCredentials credentials) throws IOException {
		Session session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		op.signout(session);
		Session newSession =
				op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
						credentials.getSecretKey(), credentials::getPassword);
		Assertions.assertThat(newSession).usingRecursiveComparison().isNotEqualTo(session);
	}


	@ParameterizedTest
	@MethodSource("entityClasses")
	void smokeTestList(Class<? extends Entity> entity, Op op, Session session) throws IOException {
		op.list(session, entity);
	}

	private static Stream<Class<? extends Entity>> entityClasses() {
		return Stream.of(User.class, Group.class, Vault.class);
	}

	@Test
	void testListUsers(Op op, Session session, TestCredentials credentials) throws IOException {
		String users = op.list(session, User.class);
		Assertions.assertThat(users).contains(credentials.getEmailAddress());
	}

	@Test
	void testListGroups(Op op, Session session) throws IOException {
		String groups = op.list(session, Group.class);
		Assertions.assertThat(groups).contains("Administrators");
	}

	@Test
	void testListVaults(Op op, Session session) throws IOException {
		String groups = op.list(session, Vault.class);
		Assertions.assertThat(groups).contains("Shared");
	}
}
