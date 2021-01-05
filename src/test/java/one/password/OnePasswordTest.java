package one.password;

import java.io.IOException;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

	@Nested
	class Groups {
		@BeforeEach
		void cleanup(OnePassword op) {
			Arrays.stream(TestUtils.assertNoIOException(() -> op.listGroups()))
					.filter(group -> group.getName().startsWith(TEST_ITEM_PREFIX))
					.forEach(group -> TestUtils.assertNoIOException(() -> {
						op.deleteGroup(group);
						return null;
					}));
		}

		@Test
		void createListGetDelete(OnePassword op) throws IOException {
			Group group = op.createGroup(withTestPrefix("group"));
			Assertions.assertThat(group.getName()).isEqualTo(withTestPrefix("group"));
			Assertions.assertThat(group.getDescription()).isEmpty();

			Assertions.assertThat(op.listGroups())
					.anyMatch(v -> v.getUuid().equals(group.getUuid()));

			Assertions.assertThat(op.getGroup(group.getName()))
					.matches(v -> v.getUuid().equals(group.getUuid()));
			Assertions.assertThat(op.getGroup(group.getUuid()))
					.matches(v -> v.getUuid().equals(group.getUuid()));

			op.deleteGroup(group);
			Assertions.assertThat(op.listGroups())
					.noneMatch(v -> v.getUuid().equals(group.getUuid()));

			Assertions.assertThatIOException().isThrownBy(() -> op.deleteGroup(group));
		}

		@Test
		void withDescription(OnePassword op) throws IOException {
			Group group = op.createGroup(withTestPrefix("group"), "some description");
			Assertions.assertThat(group.getName()).isEqualTo(withTestPrefix("group"));
			Assertions.assertThat(group.getDescription()).isEqualTo("some description");
			op.deleteGroup(group);
		}

		// @Test
		// void edit(OnePassword op) throws IOException {
		// Group vault = op.createVault(withTestPrefix("vault"));
		// vault.setName(withTestPrefix("edited"));
		// op.editVault(vault);
		// Assertions.assertThat(op.getVault(vault.getUuid()).getName())
		// .isEqualTo(withTestPrefix("edited"));
		// op.deleteVault(vault);
		// Assertions.assertThatIOException().isThrownBy(() -> op.editVault(vault));
		// }

		@Test
		void getByNameFailsWithIdenticalName(OnePassword op) throws IOException {
			Group group = op.createGroup(withTestPrefix("group"));
			Group group2 = op.createGroup(withTestPrefix("group"));
			Assertions.assertThatIOException().isThrownBy(() -> op.getGroup(group.getName()));
			op.deleteGroup(group);
			op.deleteGroup(group2);
		}
	}

	@Nested
	class Vaults {
		@BeforeEach
		void cleanup(OnePassword op) {
			Arrays.stream(TestUtils.assertNoIOException(() -> op.listVaults()))
					.filter(vault -> vault.getName().startsWith(TEST_ITEM_PREFIX))
					.forEach(vault -> TestUtils.assertNoIOException(() -> {
						op.deleteVault(vault);
						return null;
					}));
		}

		@Test
		void createListGetDelete(OnePassword op) throws IOException {
			Vault vault = op.createVault(withTestPrefix("vault"));
			Assertions.assertThat(vault.getName()).isEqualTo(withTestPrefix("vault"));
			Assertions.assertThat(vault.getDescription()).isEmpty();

			Assertions.assertThat(op.listVaults())
					.anyMatch(v -> v.getUuid().equals(vault.getUuid()));

			Assertions.assertThat(op.getVault(vault.getName()))
					.matches(v -> v.getUuid().equals(vault.getUuid()));
			Assertions.assertThat(op.getVault(vault.getUuid()))
					.matches(v -> v.getUuid().equals(vault.getUuid()));

			op.deleteVault(vault);
			Assertions.assertThat(op.listVaults())
					.noneMatch(v -> v.getUuid().equals(vault.getUuid()));

			Assertions.assertThatIOException().isThrownBy(() -> op.deleteVault(vault));
		}

		@Test
		void withDescription(OnePassword op) throws IOException {
			Vault vault = op.createVault(withTestPrefix("vault"), "some description");
			Assertions.assertThat(vault.getName()).isEqualTo(withTestPrefix("vault"));
			Assertions.assertThat(vault.getDescription()).isEqualTo("some description");
			op.deleteVault(vault);
		}

		@Test
		void edit(OnePassword op) throws IOException {
			Vault vault = op.createVault(withTestPrefix("vault"));
			vault.setName(withTestPrefix("edited"));
			op.editVault(vault);
			Assertions.assertThat(op.getVault(vault.getUuid()).getName())
					.isEqualTo(withTestPrefix("edited"));
			op.deleteVault(vault);
			Assertions.assertThatIOException().isThrownBy(() -> op.editVault(vault));
		}

		@Test
		void getByNameFailsWithIdenticalName(OnePassword op) throws IOException {
			Vault vault = op.createVault(withTestPrefix("vault"));
			Vault vault2 = op.createVault(withTestPrefix("vault"));
			Assertions.assertThatIOException().isThrownBy(() -> op.getVault(vault.getName()));
			op.deleteVault(vault);
			op.deleteVault(vault2);
		}
	}

	private String withTestPrefix(String name) {
		return TEST_ITEM_PREFIX + name;
	}
}
