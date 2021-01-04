package one.password.cli;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import one.password.Session;
import one.password.TestCredentials;
import one.password.TestUtils;
import one.password.Utils;

public class OpTest {
	private final TestCredentials credentials;

	OpTest() throws IOException {
		credentials = new TestCredentials();
	}

	@Test
	void testGetVersion() throws IOException {
		Assertions.assertThat(new Op(createConfig()).version())
				.isEqualTo(TestUtils.getOnePasswordVersion());
	}

	@Test
	void testTimeout() {
		Assertions.assertThatIOException()
				.isThrownBy(() -> new Op(createConfig().setTimeout(Duration.ofNanos(1))).version());
	}

	@Test
	void testSignin() throws IOException {
		Session session = new Op(createConfig()).signin(credentials.getSignInAddress(),
				credentials.getEmailAddress(), credentials.getSecretKey(),
				credentials::getPassword);
		Assertions.assertThat(session.getSession()).hasLineCount(1).hasSize(43);
		Assertions.assertThat(session.getShorthand())
				.isEqualTo(URI.create(credentials.getSignInAddress()).getHost().split("\\.")[0]
						.replace("-", "_"));
	}

	@Test
	void testSigninTwiceYieldsSameSession() throws IOException {
		Op op = new Op(createConfig());
		Session session = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword);
		Session session2 = op.signin(credentials.getSignInAddress(), credentials.getEmailAddress(),
				credentials.getSecretKey(), credentials::getPassword, session);
		Assertions.assertThat(session2).usingRecursiveComparison().isEqualTo(session);
	}

	@Test
	void testSigninWithWrongCredentials() throws IOException {
		Assertions.assertThatIOException()
				.isThrownBy(() -> new Op(createConfig()).signin(credentials.getSignInAddress(),
						"foo@bar.com", credentials.getSecretKey(), () -> "xxx"));
	}

	@Test
	void testSignout() throws IOException {
		Op op = new Op(createConfig());
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
			op = new Op(createConfig());
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
