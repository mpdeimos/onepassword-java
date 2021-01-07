package one.password;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import one.password.Entity.UserOrGroup;
import one.password.OnePasswordBase.EntityCommand;
import one.password.OnePasswordBase.NamedEntityCommand;
import one.password.OnePasswordBase.UserEntityCommand;
import one.password.OnePasswordBase.VaultEntityCommand;
import one.password.test.TestConfig;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;
import one.password.util.Utils;

class OnePasswordTest {

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
	void withoutConfig(TestCredentials credentials) throws IOException {
		OnePassword op =
				new OnePassword(credentials.getSignInAddress(), credentials.getEmailAddress(),
						credentials.getSecretKey(), credentials::getPassword) {
					@Override
					protected void signinOnInit() throws IOException {
						// This would fail too early as we usually sign in during construct
					}
				};
		Assertions.assertThatIOException().isThrownBy(() -> op.users().list())
				.withMessageMatching(".*Cannot run program \"op(\\.exe)?\".*");
	}

	@Nested
	class Users extends EntityCommandTest<User, UserEntityCommand> {
		private final String emailTemplatePrefix;
		private final String emailTemplateSuffix;

		protected Users(OnePassword op) {
			super(User.class, op.users());
			Properties environment =
					TestUtils.assertNoIOException(() -> TestUtils.getTestEnvironment());
			emailTemplatePrefix = environment.getProperty("OP_TEST_EMAILADDRESS_TEMPLATE_PREFIX");
			emailTemplateSuffix = environment.getProperty("OP_TEST_EMAILADDRESS_TEMPLATE_SUFFIX");
			Assertions.assertThat(emailTemplatePrefix).isNotEmpty();
			Assertions.assertThat(emailTemplateSuffix).isNotEmpty();
		}

		@Override
		protected User createTestEntity(String name) throws IOException {
			String emailAddress = secondaryIdentifier(name);
			return command.create(emailAddress, name);
		}

		@Override
		protected String secondaryIdentifier(String name) {
			return emailTemplatePrefix + name.replaceAll("[\\W]|_", "_") + emailTemplateSuffix;
		}

		@Override
		protected String getSecondaryIdentifier(User entity) {
			return entity.getEmail();
		}

		@Override
		protected boolean isTestEntity(User entity) {
			return entity.getEmail().startsWith(emailTemplatePrefix)
					&& entity.getEmail().endsWith(emailTemplateSuffix);
		}

		@Test
		void listExisting(OnePassword op, TestCredentials credentials) throws IOException {
			Assertions.assertThat(op.users().list())
					.anyMatch(user -> user.getEmail().equals(credentials.getEmailAddress()));
		}

		@Test
		void createUserWithAdditionalFields() throws IOException {
			User user = command.create(secondaryIdentifier("email"), "user name");
			Assertions.assertThat(user.getEmail()).isEqualTo(secondaryIdentifier("email"));
			Assertions.assertThat(user.getName()).isEqualTo("user name");
			Assertions.assertThat(user.getFirstName()).isEqualTo("user name");
			Assertions.assertThat(user.getLastName()).isEmpty();
			Assertions.assertThat(user.getLanguage()).isEqualTo("en");
			Assertions.assertThat(user.getCreatedAt())
					.isCloseTo(ZonedDateTime.now(), Assertions.within(5, ChronoUnit.SECONDS))
					.isEqualTo(user.getUpdatedAt());
			Assertions.assertThat(user.getLastAuthAt()).isBefore(user.getCreatedAt());
			command.delete(user);

			user = command.create(secondaryIdentifier("email2"), "user name", "de");
			Assertions.assertThat(user.getLanguage()).isEqualTo("de");
		}

		@Test
		void editName() throws IOException {
			User user = command.create(secondaryIdentifier("email"), "user name");
			TestUtils.waitOneSecond();
			user.setName("new name");
			command.edit(user);
			User editedUser = command.get(user.getUuid());
			Assertions.assertThat(editedUser.getName()).isEqualTo("new name");
			Assertions.assertThat(editedUser.getFirstName()).isEqualTo("new name");
			Assertions.assertThat(editedUser.getLastName()).isEmpty();
			Assertions.assertThat(editedUser.getUpdatedAt()).isAfter(user.getUpdatedAt());
			command.delete(editedUser);
			Assertions.assertThatIOException().isThrownBy(() -> command.edit(editedUser));
		}

		@Test
		void cannotCreateUserWithSameMailTwice() throws IOException {
			User user = createTestEntity("twice");
			Assertions.assertThatIOException().isThrownBy(() -> createTestEntity("twice"));
			command.delete(user);
		}
	}

	@Nested
	class Groups extends NamedEntityCommandTest<Group, NamedEntityCommand<Group>> {
		private static final String ADMINISTRATORS = "Administrators";

		protected Groups(OnePassword op) {
			super(Group.class, op.groups());
		}

		@Test
		void listExisting(OnePassword op) throws IOException {
			Assertions.assertThat(op.groups().list()).extracting(Group::getName)
					.contains("Recovery", ADMINISTRATORS, "Owners");
		}

		@Test
		void createdAt() throws IOException {
			Assertions.assertThat(createTestEntity().getCreatedAt()).isCloseTo(ZonedDateTime.now(),
					Assertions.within(5, ChronoUnit.SECONDS));
		}

		@Test
		void setDescription() throws IOException {
			Group group = command.create(secondaryIdentifier("group"), "description");
			Assertions.assertThat(group.getDescription()).isEqualTo("description");
			Assertions.assertThat(group.getUpdatedAt()).isEmpty();
			group.setDescription("new description");
			TestUtils.waitOneSecond();
			command.edit(group);
			Group editedGroup = command.get(group.getUuid());
			Assertions.assertThat(editedGroup.getDescription()).isEqualTo("new description");
			Assertions.assertThat(editedGroup.getUpdatedAt()).isPresent();
			Assertions.assertThat(editedGroup.getUpdatedAt().get())
					.isAfter(editedGroup.getCreatedAt());
		}
	}

	@Nested
	class Vaults extends NamedEntityCommandTest<Vault, VaultEntityCommand> {
		protected Vaults(OnePassword op) {
			super(Vault.class, op.vaults());
		}

		@Test
		void listExisting(OnePassword op) throws IOException {
			Assertions.assertThat(op.vaults().list()).extracting(Vault::getName).contains("Private",
					"Shared");
		}

		@Test
		void withRestrictedAccess(OnePassword op) throws IOException {
			Access access = new Access(op);
			Group admins = op.groups().get(Groups.ADMINISTRATORS);

			Vault vault = command.create("vault");
			access.assertAccess(vault, admins, true);
			command.delete(vault);

			vault = command.create("vault", "description", false);
			access.assertAccess(vault, admins, false);
			command.delete(vault);
		}
	}

	@Nested
	class Access {
		private OnePassword op;
		final Users users;
		final Groups groups;
		final Vaults vaults;

		protected Access(OnePassword op) {
			this.op = op;
			users = new Users(op);
			groups = new Groups(op);
			vaults = new Vaults(op);
		}

		@BeforeEach
		void cleanup() {
			vaults.cleanup();
			users.cleanup();
			vaults.cleanup();
		}

		@Test
		void addUserToGroup() throws IOException {
			User user = users.createTestEntity();
			Group group = groups.createTestEntity();
			Assertions.assertThat(op.access().users(group).keySet()).extracting(User::getUuid)
					.doesNotContain(user.getUuid());

			op.access().add(user, group);
			Assertions.assertThat(op.access().users(group).entrySet())
					.anyMatch(entry -> (entry.getKey().getUuid().equals(user.getUuid())
							&& entry.getValue() == Role.MEMBER));

			op.access().add(user, group, Role.MANAGER);
			Assertions.assertThat(op.access().users(group).entrySet())
					.anyMatch(entry -> (entry.getKey().getUuid().equals(user.getUuid())
							&& entry.getValue() == Role.MANAGER));

			op.access().remove(user, group);
			Assertions.assertThat(op.access().users(group).keySet()).extracting(User::getUuid)
					.doesNotContain(user.getUuid());

			Assertions.assertThatIOException().isThrownBy(() -> op.access().remove(user, group));

			op.users().delete(user);
			op.groups().delete(group);
		}

		@Test
		void addUserToVault() throws IOException {
			User user = users.createTestEntity();
			Vault vault = vaults.createTestEntity();
			assertAccess(vault, user, false);

			op.access().add(user, vault);
			assertAccess(vault, user, true);

			op.access().remove(user, vault);
			assertAccess(vault, user, false);

			Assertions.assertThatIOException().isThrownBy(() -> op.access().remove(user, vault));

			op.users().delete(user);
			op.vaults().delete(vault);
		}


		@Test
		void addGroupToVault() throws IOException {
			User user = users.createTestEntity();
			Group group = groups.createTestEntity();
			Vault vault = vaults.createTestEntity();
			assertAccess(vault, user, false, group, false);

			op.access().add(group, vault);
			assertAccess(vault, user, false, group, true);

			// Adding user to group, does not list the user
			op.access().add(user, group);
			assertAccess(vault, user, false, group, true);

			op.access().add(user, vault);
			assertAccess(vault, user, true, group, true);

			op.access().remove(group, vault);
			assertAccess(vault, user, true, group, false);

			op.access().remove(user, vault);
			assertAccess(vault, user, false, group, false);

			op.users().delete(user);
			op.groups().delete(group);
			op.vaults().delete(vault);
		}

		private void assertAccess(Vault vault, User user, boolean userAccess, Group group,
				boolean groupAccess) throws IOException {
			assertAccess(vault, user, userAccess);
			assertAccess(vault, group, groupAccess);
		}

		private void assertAccess(Vault vault, Entity.UserOrGroup userOrGroup, boolean hasAccess)
				throws IOException {
			UserOrGroup[] members = members(vault, userOrGroup.getClass());
			if (hasAccess) {
				Assertions.assertThat(members).extracting(Entity::getUuid)
						.contains(userOrGroup.getUuid());
			} else {
				Assertions.assertThat(members).extracting(Entity::getUuid)
						.doesNotContain(userOrGroup.getUuid());
			}
		}

		private Entity.UserOrGroup[] members(Vault vault, Class<? extends Entity.UserOrGroup> type)
				throws IOException {
			if (type.equals(User.class)) {
				return op.access().users(vault);
			}

			if (type.equals(Group.class)) {
				return op.access().groups(vault);
			}

			return Assertions.fail("Unknown type: " + type);
		}
	}

	abstract static class EntityCommandTest<E extends Entity, C extends EntityCommand<E>> {
		protected final Class<E> type;
		protected final C command;

		protected EntityCommandTest(Class<E> type, C command) {
			this.type = type;
			this.command = command;
		}

		@BeforeEach
		void cleanup() {
			Arrays.stream(TestUtils.assertNoIOException(() -> command.list()))
					.filter(this::isTestEntity)
					.forEach(entity -> TestUtils.assertNoIOException(() -> {
						command.delete(entity);
						return null;
					}));
		}

		protected abstract boolean isTestEntity(E entity);

		protected E createTestEntity() throws IOException {
			return createTestEntity(Utils.randomBase32(8));
		}

		protected abstract E createTestEntity(String name) throws IOException;

		protected abstract String secondaryIdentifier(String name);

		protected abstract String getSecondaryIdentifier(E entity);

		@Test
		void createListGetDelete() throws IOException {
			E entity = createTestEntity();

			Assertions.assertThat(command.list())
					.anyMatch(e -> e.getUuid().equals(entity.getUuid()));
			Assertions.assertThat(command.list()).extracting(this::getSecondaryIdentifier)
					.anyMatch(secondaryId -> secondaryId.equals(getSecondaryIdentifier(entity)));

			Assertions.assertThat(command.get(entity.getUuid()))
					.matches(e -> e.getUuid().equals(entity.getUuid()));
			Assertions.assertThat(command.get(getSecondaryIdentifier(entity)))
					.matches(e -> e.getUuid().equals(entity.getUuid()));

			command.delete(entity);
			Assertions.assertThat(command.list())
					.noneMatch(e -> e.getUuid().equals(entity.getUuid()));

			Assertions.assertThatIOException().isThrownBy(() -> command.delete(entity));
		}
	}

	abstract static class NamedEntityCommandTest<E extends Entity.Named, C extends NamedEntityCommand<E>>
			extends EntityCommandTest<E, C> {
		private static final String TEST_ITEM_PREFIX = "__test__";

		protected NamedEntityCommandTest(Class<E> type, C command) {
			super(type, command);
		}

		@Override
		protected boolean isTestEntity(E entity) {
			return entity.getName().startsWith(TEST_ITEM_PREFIX);
		}

		@Override
		protected E createTestEntity(String name) throws IOException {
			return command.create(secondaryIdentifier(name));
		}

		@Override
		protected String secondaryIdentifier(String name) {
			return TEST_ITEM_PREFIX + name;
		}

		@Override
		protected String getSecondaryIdentifier(E entity) {
			return entity.getName();
		}

		@Test
		void nameAndDescription() throws IOException {
			E entity = command.create(secondaryIdentifier("name"));
			Assertions.assertThat(entity.getName()).isEqualTo(secondaryIdentifier("name"));
			Assertions.assertThat(entity.getDescription()).isEmpty();
			command.delete(entity);

			entity = command.create(secondaryIdentifier("name"), "some description");
			Assertions.assertThat(entity.getName()).isEqualTo(secondaryIdentifier("name"));
			Assertions.assertThat(entity.getDescription()).isEqualTo("some description");
			command.delete(entity);
		}

		@Test
		void edit() throws IOException {
			E entity = createTestEntity("name");
			entity.setName(secondaryIdentifier("edited"));
			command.edit(entity);
			Assertions.assertThat(command.get(entity.getUuid()).getName())
					.isEqualTo(secondaryIdentifier("edited"));
			command.delete(entity);
			Assertions.assertThatIOException().isThrownBy(() -> command.edit(entity));
		}

		@Test
		void getByNameFailsWithIdenticalName() throws IOException {
			E entity = createTestEntity("name");
			E entity2 = createTestEntity("name");
			Assertions.assertThatIOException().isThrownBy(() -> command.get(entity.getName()));
			command.delete(entity);
			command.delete(entity2);
		}
	}
}
