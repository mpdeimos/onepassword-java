package one.password;

import java.io.IOException;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import one.password.test.TestConfig;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;

class OnePasswordTest {
	private static final String TEST_ITEM_PREFIX = "__test__";

	@Test
	void testAutoReSignIn(TestConfig config) throws IOException {
		try (OnePassword op = new OnePassword(config, config.credentials.getSignInAddress(),
				config.credentials.getEmailAddress(), config.credentials.getSecretKey(),
				config.credentials::getPassword)) {
			Session initialSession = op.session;
			op.op.signout(initialSession);
			Assertions.assertThat(op.listUsers()).isNotEmpty();
			Assertions.assertThat(initialSession.getSession())
					.isNotEqualTo(op.session.getSession());
		}
	}

	@Test
	void testListUsers(OnePassword op, TestCredentials credentials) throws IOException {
		Assertions.assertThat(op.listUsers())
				.anyMatch(user -> user.getEmail().equals(credentials.getEmailAddress()));
	}


	@Test
	void testListGroups(OnePassword op) throws IOException {
		Assertions.assertThat(op.listGroups()).extracting(Group::getName).contains("Recovery",
				"Administrators", "Owners", "Administrators");
	}

	@Test
	void testListVaults(OnePassword op) throws IOException {
		Assertions.assertThat(op.listVaults()).extracting(Vault::getName).contains("Private",
				"Shared");
	}

	@Test
	void testModifyVault(OnePassword op) throws IOException {
		Vault vault = op.createVault(withTestPrefix("vault"));
		Assertions.assertThat(vault.getName()).isEqualTo(withTestPrefix("vault"));
		Assertions.assertThat(vault.getDescription()).isEmpty();;
		Assertions.assertThat(op.listVaults()).anyMatch(v -> v.getUuid().equals(vault.getUuid()));
		op.deleteVault(vault);
		Assertions.assertThat(op.listVaults()).noneMatch(v -> v.getUuid().equals(vault.getUuid()));
		Assertions.assertThatIOException().isThrownBy(() -> op.deleteVault(vault));
	}

	@Test
	void testModifyVaultWithDescription(OnePassword op) throws IOException {
		Vault vault = op.createVault(withTestPrefix("vault"), "some description");
		Assertions.assertThat(vault.getName()).isEqualTo(withTestPrefix("vault"));
		Assertions.assertThat(vault.getDescription()).isEqualTo("some description");
		op.deleteVault(vault);
	}

	@AfterAll
	static void cleanup(OnePassword op) {
		Arrays.stream(TestUtils.assertNoIOException(() -> op.listVaults()))
				.filter(vault -> vault.getName().startsWith(TEST_ITEM_PREFIX))
				.forEach(vault -> TestUtils.assertNoIOException(() -> {
					op.deleteVault(vault);
					return null;
				}));
	}

	private String withTestPrefix(String name) {
		return TEST_ITEM_PREFIX + name;
	}

}
