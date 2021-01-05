package one.password.cli;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import one.password.Session;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;

public class OpTest {
	private final TestCredentials credentials;

	OpTest() throws IOException {
		credentials = new TestCredentials();
	}

	@Test
	void testGetVersion() throws IOException {
		Assertions.assertThat(new Op(TestUtils.createConfig()).version())
				.isEqualTo(TestUtils.getOnePasswordVersion());
	}

	@Test
	void testTimeout() {
		Assertions.assertThatIOException().isThrownBy(
				() -> new Op(TestUtils.createConfig().setTimeout(Duration.ofNanos(1))).version());
	}

	@Test
	void testSignin() throws IOException {
		Session session = new Op(TestUtils.createConfig()).signin(credentials.getSignInAddress(),
				credentials.getEmailAddress(), credentials.getSecretKey(),
				credentials::getPassword);
		Assertions.assertThat(session.getSession()).hasLineCount(1).hasSize(43);
		Assertions.assertThat(session.getShorthand())
				.isEqualTo(URI.create(credentials.getSignInAddress()).getHost().split("\\.")[0]
						.replace("-", "_"));
	}

	@Test
	void testSigninTwiceYieldsSameSession() throws IOException {
		Op op = new Op(TestUtils.createConfig());
		Session session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		Session session2 = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword, session);
		Assertions.assertThat(session2).usingRecursiveComparison().isEqualTo(session);
	}

	@Test
	void testSigninWithWrongCredentials() throws IOException {
		Assertions.assertThatIOException()
				.isThrownBy(() -> new Op(TestUtils.createConfig()).signin(
						credentials.getSignInAddress(), "foo@bar.com", credentials.getSecretKey(),
						() -> "xxx"));
	}

	@Test
	void testSignout() throws IOException {
		Op op = new Op(TestUtils.createConfig());
		Session session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		op.signout(session);
		Session session2 = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		Assertions.assertThat(session2).usingRecursiveComparison().isNotEqualTo(session);
	}

	@Nested
	class WithSession {
		private Op op;
		private Session session;

		@BeforeEach
		void login() throws IOException {
			op = new Op(TestUtils.createConfig());
			session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
					credentials.getSecretKey(), credentials::getPassword);
		}

		@Nested
		class Do {

			@ParameterizedTest
			@EnumSource(Entities.class)
			void smokeTestList(Entities entity) throws IOException {
				op.list(session, entity);
			}

			@Test
			void testListUsers() throws IOException {
				String users = op.list(session, Entities.USERS);
				Assertions.assertThat(users).contains(credentials.getEmailAddress());
			}
		}
	}
}
