package one.password.cli;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import one.password.Config;
import one.password.Session;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;

public class OpTest {
	@Test
	void testGetVersion(Op op) throws IOException {
		Assertions.assertThat(op.version()).isEqualTo(TestUtils.getOnePasswordVersion());
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
	@EnumSource(Entities.class)
	void smokeTestList(Entities entity, Op op, Session session) throws IOException {
		op.list(session, entity);
	}

	@Test
	void testListUsers(Op op, Session session, TestCredentials credentials) throws IOException {
		String users = op.list(session, Entities.USERS);
		Assertions.assertThat(users).contains(credentials.getEmailAddress());
	}

	@Test
	void testListGroups(Op op, Session session) throws IOException {
		String groups = op.list(session, Entities.GROUPS);
		Assertions.assertThat(groups).contains("Administrators");
	}

	@Test
	void testListVaults(Op op, Session session) throws IOException {
		String groups = op.list(session, Entities.VAULTS);
		Assertions.assertThat(groups).contains("Shared");
	}
}
