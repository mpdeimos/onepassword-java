package one.password;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import one.password.Entity.UserOrGroup;
import one.password.OnePasswordBase.EntityCommand;
import one.password.OnePasswordBase.NamedEntityCommand;
import one.password.OnePasswordBase.UserCommand;
import one.password.OnePasswordBase.VaultCommand;
import one.password.test.TestConfig;
import one.password.test.TestCredentials;
import one.password.test.TestUtils;
import one.password.util.Utils;

class OnePasswordTest {
	private static final Comparator<Entity> ID_COMPARATOR = Comparator.comparing(Entity::getId);
	private static final Comparator<Entity.SecondaryId> ID2_COMPARATOR =
			Comparator.comparing(Entity.SecondaryId::getSecondaryId);

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
	class Users extends EntityCommandTest<User, UserCommand> {
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
			String emailAddress = secondaryId(name);
			return command.create(emailAddress, name);
		}

		@Override
		protected String secondaryId(String name) {
			return emailTemplatePrefix + name.replaceAll("[\\W]|_", "_") + emailTemplateSuffix;
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
			User user = command.create(secondaryId("email"), "user name");
			Assertions.assertThat(user.getEmail()).isEqualTo(secondaryId("email"));
			Assertions.assertThat(user.getName()).isEqualTo("user name");
			Assertions.assertThat(user.getFirstName()).isEqualTo("user name");
			Assertions.assertThat(user.getLastName()).isEmpty();
			Assertions.assertThat(user.getLanguage()).isEqualTo("en");
			Assertions.assertThat(user.getCreatedAt())
					.isCloseTo(ZonedDateTime.now(), Assertions.within(5, ChronoUnit.SECONDS))
					.isEqualTo(user.getUpdatedAt());
			Assertions.assertThat(user.getLastAuthAt()).isBefore(user.getCreatedAt());
			command.delete(user);

			user = command.create(secondaryId("email2"), "user name", "de");
			Assertions.assertThat(user.getLanguage()).isEqualTo("de");
		}

		@Test
		void editName() throws IOException {
			User user = command.create(secondaryId("email"), "user name");
			TestUtils.waitOneSecond();
			user.setName("new name");
			command.edit(user);
			User editedUser = command.get(user.getId());
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
			Group group = command.create(secondaryId("group"), "description");
			Assertions.assertThat(group.getDescription()).isEqualTo("description");
			Assertions.assertThat(group.getUpdatedAt()).isEmpty();
			group.setDescription("new description");
			TestUtils.waitOneSecond();
			command.edit(group);
			Group editedGroup = command.get(group.getId());
			Assertions.assertThat(editedGroup.getDescription()).isEqualTo("new description");
			Assertions.assertThat(editedGroup.getUpdatedAt()).isPresent();
			Assertions.assertThat(editedGroup.getUpdatedAt().get())
					.isAfter(editedGroup.getCreatedAt());
		}
	}

	@Nested
	class Vaults extends NamedEntityCommandTest<Vault, VaultCommand> {
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
			access.assertGrantedAccessTo(vault, admins, true);
			command.delete(vault);

			vault = command.create("vault", "description", false);
			access.assertGrantedAccessTo(vault, admins, false);
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
			assertContainsEntity(op.users().listGrantedAccessTo(group), user, false);
			assertContainsEntity(op.groups().listAccessibleBy(user), group, false);

			op.users().grantAccessTo(user, group);
			Assertions.assertThat(op.users().listGrantedRolesTo(group).entrySet())
					.anyMatch(entry -> ID_COMPARATOR.compare(entry.getKey(), user) == 0
							&& entry.getValue() == Role.MEMBER);
			assertContainsEntity(op.groups().listAccessibleBy(user), group, true);

			op.users().add(user, group, Role.MANAGER);
			Assertions.assertThat(op.users().listGrantedRolesTo(group).entrySet())
					.anyMatch(entry -> ID_COMPARATOR.compare(entry.getKey(), user) == 0
							&& entry.getValue() == Role.MANAGER);
			assertContainsEntity(op.groups().listAccessibleBy(user), group, true);

			op.users().revokeAccessTo(user, group);
			assertContainsEntity(op.users().listGrantedAccessTo(group), user, false);
			assertContainsEntity(op.groups().listAccessibleBy(user), group, false);

			Assertions.assertThatIOException()
					.isThrownBy(() -> op.users().revokeAccessTo(user, group));

			op.users().delete(user);
			op.groups().delete(group);
		}

		@Test
		void addUserToVault() throws IOException {
			User user = users.createTestEntity();
			Vault vault = vaults.createTestEntity();
			assertGrantedAccessTo(vault, user, false);
			assertAccessiblyBy(vault, user, false);

			op.users().grantAccessTo(user, vault);
			assertGrantedAccessTo(vault, user, true);
			assertAccessiblyBy(vault, user, true);

			op.users().revokeAccessTo(user, vault);
			assertGrantedAccessTo(vault, user, false);
			assertAccessiblyBy(vault, user, false);

			Assertions.assertThatIOException()
					.isThrownBy(() -> op.users().revokeAccessTo(user, vault));

			op.users().delete(user);
			op.vaults().delete(vault);
		}


		@Test
		void addGroupToVault() throws IOException {
			User user = users.createTestEntity();
			Group group = groups.createTestEntity();
			Vault vault = vaults.createTestEntity();
			assertGrantedAccessTo(vault, user, false, group, false);
			assertAccessiblyBy(vault, user, false, group, false);

			op.groups().grantAccessTo(group, vault);
			assertGrantedAccessTo(vault, user, false, group, true);
			assertAccessiblyBy(vault, user, false, group, true);

			op.users().grantAccessTo(user, group);
			// Adding user to group, does not list the user
			assertGrantedAccessTo(vault, user, false, group, true);
			// But transitive access is granted
			assertAccessiblyBy(vault, user, true, group, true);

			op.users().grantAccessTo(user, vault);
			assertGrantedAccessTo(vault, user, true, group, true);
			assertAccessiblyBy(vault, user, true, group, true);

			op.groups().revokeAccessTo(group, vault);
			assertGrantedAccessTo(vault, user, true, group, false);
			assertAccessiblyBy(vault, user, true, group, false);

			op.users().revokeAccessTo(user, vault);
			assertGrantedAccessTo(vault, user, false, group, false);
			assertAccessiblyBy(vault, user, false, group, false);

			op.users().delete(user);
			op.groups().delete(group);
			op.vaults().delete(vault);
		}

		private void assertGrantedAccessTo(Vault vault, User user, boolean userAccess, Group group,
				boolean groupAccess) throws IOException {
			assertGrantedAccessTo(vault, user, userAccess);
			assertGrantedAccessTo(vault, group, groupAccess);
		}

		private void assertGrantedAccessTo(Vault vault, Entity.UserOrGroup userOrGroup,
				boolean hasAccess) throws IOException {
			UserOrGroup[] members = listGrantedAccessTo(userOrGroup.getClass(), vault);
			assertContainsEntity(members, userOrGroup, hasAccess);
		}

		private void assertAccessiblyBy(Vault vault, User user, boolean userAccess, Group group,
				boolean groupAccess) throws IOException {
			assertAccessiblyBy(vault, user, userAccess);
			assertAccessiblyBy(vault, group, groupAccess);
		}

		private void assertAccessiblyBy(Vault vault, Entity.UserOrGroup userOrGroup,
				boolean hasAccess) throws IOException {
			Vault[] members = op.vaults().listAccessibleBy(userOrGroup);
			assertContainsEntity(members, vault, hasAccess);
		}

		private void assertContainsEntity(Entity[] entities, Entity entity, boolean contained) {
			if (contained) {
				Assertions.assertThat(entities).usingElementComparator(ID_COMPARATOR)
						.contains(entity);
			} else {
				Assertions.assertThat(entities).usingElementComparator(ID_COMPARATOR)
						.doesNotContain(entity);
			}
		}

		private Entity.UserOrGroup[] listGrantedAccessTo(
				Class<? extends Entity.UserOrGroup> accessorType, Vault accessible)
				throws IOException {
			if (accessorType.equals(User.class)) {
				return op.users().listGrantedAccessTo(accessible);
			}

			if (accessorType.equals(Group.class)) {
				return op.groups().listGrantedAccessTo(accessible);
			}

			return Assertions.fail("Unknown type: " + accessorType);
		}
	}

	abstract static class EntityCommandTest<E extends Entity & Entity.SecondaryId, C extends EntityCommand<E>> {
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
			return createTestEntity(type.getSimpleName() + "_" + Utils.randomBase32(8));
		}

		protected abstract E createTestEntity(String name) throws IOException;

		protected abstract String secondaryId(String name);

		@Test
		void createListGetDelete() throws IOException {
			E entity = createTestEntity();

			Assertions.assertThat(command.list()).usingElementComparator(ID_COMPARATOR)
					.contains(entity);
			Assertions.assertThat(command.list()).usingElementComparator(ID2_COMPARATOR)
					.contains(entity);

			Assertions.assertThat(command.get(entity.getId())).usingComparator(ID_COMPARATOR)
					.isEqualTo(entity);
			Assertions.assertThat(command.get(entity.getSecondaryId()))
					.usingComparator(ID2_COMPARATOR).isEqualTo(entity);

			command.delete(entity);
			Assertions.assertThat(command.list()).usingElementComparator(ID_COMPARATOR)
					.doesNotContain(entity);

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
			return command.create(secondaryId(name));
		}

		@Override
		protected String secondaryId(String name) {
			return TEST_ITEM_PREFIX + name;
		}

		@Test
		void nameAndDescription() throws IOException {
			E entity = command.create(secondaryId("name"));
			Assertions.assertThat(entity.getName()).isEqualTo(secondaryId("name"));
			Assertions.assertThat(entity.getDescription()).isEmpty();
			command.delete(entity);

			entity = command.create(secondaryId("name"), "some description");
			Assertions.assertThat(entity.getName()).isEqualTo(secondaryId("name"));
			Assertions.assertThat(entity.getDescription()).isEqualTo("some description");
			command.delete(entity);
		}

		@Test
		void edit() throws IOException {
			E entity = createTestEntity("name");
			entity.setName(secondaryId("edited"));
			command.edit(entity);
			Assertions.assertThat(command.get(entity.getId()).getName())
					.isEqualTo(secondaryId("edited"));
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
