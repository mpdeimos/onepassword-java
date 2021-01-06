package one.password;

import java.io.IOException;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import one.password.OnePasswordBase.NamedEntityCommand;
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
			Assertions.assertThat(op.users().list()).isNotEmpty();
			Assertions.assertThat(initialSession.getSession())
					.isNotEqualTo(op.session.getSession());
		}
	}

	@Test
	void testListUsers(OnePassword op, TestCredentials credentials) throws IOException {
		Assertions.assertThat(op.users().list())
				.anyMatch(user -> user.getEmail().equals(credentials.getEmailAddress()));
	}


	@Test
	void testListGroups(OnePassword op) throws IOException {
		Assertions.assertThat(op.groups().list()).extracting(Group::getName).contains("Recovery",
				"Administrators", "Owners", "Administrators");
	}

	@Test
	void testListVaults(OnePassword op) throws IOException {
		Assertions.assertThat(op.vaults().list()).extracting(Vault::getName).contains("Private",
				"Shared");
	}

	@Nested
	class Groups extends NamedEntityCommandTest<Group> {
		protected Groups(OnePassword op) {
			super(Group.class, op.groups());
		}
	}

	@Nested
	class Vaults extends NamedEntityCommandTest<Vault> {
		protected Vaults(OnePassword op) {
			super(Vault.class, op.vaults());
		}
	}

	abstract static class NamedEntityCommandTest<T extends Entity.Named> {
		private final Class<T> type;
		private NamedEntityCommand<T> command;

		protected NamedEntityCommandTest(Class<T> type, NamedEntityCommand<T> command) {
			this.type = type;
			this.command = command;
		}

		@BeforeEach
		void cleanup() {
			Arrays.stream(TestUtils.assertNoIOException(() -> command.list()))
					.filter(entity -> entity.getName().startsWith(TEST_ITEM_PREFIX))
					.forEach(entity -> TestUtils.assertNoIOException(() -> {
						command.delete(entity);
						return null;
					}));
		}

		protected String entityName() {
			return withTestPrefix(type.getSimpleName());
		}

		@Test
		void createListGetDelete() throws IOException {
			T entity = command.create(entityName());
			Assertions.assertThat(entity.getName()).isEqualTo(entityName());
			Assertions.assertThat(entity.getDescription()).isEmpty();

			Assertions.assertThat(command.list())
					.anyMatch(v -> v.getUuid().equals(entity.getUuid()));

			Assertions.assertThat(command.get(entity.getName()))
					.matches(v -> v.getUuid().equals(entity.getUuid()));
			Assertions.assertThat(command.get(entity.getUuid()))
					.matches(v -> v.getUuid().equals(entity.getUuid()));

			command.delete(entity);
			Assertions.assertThat(command.list())
					.noneMatch(v -> v.getUuid().equals(entity.getUuid()));

			Assertions.assertThatIOException().isThrownBy(() -> command.delete(entity));
		}

		@Test
		void withDescription() throws IOException {
			T entity = command.create(entityName(), "some description");
			Assertions.assertThat(entity.getName()).isEqualTo(entityName());
			Assertions.assertThat(entity.getDescription()).isEqualTo("some description");
			command.delete(entity);
		}

		@Test
		void edit() throws IOException {
			T entity = command.create(entityName());
			entity.setName(entityName() + " edited");
			command.edit(entity);
			Assertions.assertThat(command.get(entity.getUuid()).getName())
					.isEqualTo(entityName() + " edited");
			command.delete(entity);
			Assertions.assertThatIOException().isThrownBy(() -> command.edit(entity));
		}

		@Test
		void getByNameFailsWithIdenticalName() throws IOException {
			T entity = command.create(entityName());
			T entity2 = command.create(entityName());
			Assertions.assertThatIOException().isThrownBy(() -> command.get(entity.getName()));
			command.delete(entity);
			command.delete(entity2);
		}
	}

	private static String withTestPrefix(String name) {
		return TEST_ITEM_PREFIX + name;
	}
}
