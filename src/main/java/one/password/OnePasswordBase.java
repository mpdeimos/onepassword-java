package one.password;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import one.password.cli.Flags;
import one.password.cli.Op;
import one.password.util.FunctionWithException;
import one.password.util.Json;
import one.password.util.Utils;

/** Base class for high-level 1password CLI Java bindings. */
public abstract class OnePasswordBase {
	protected final Op op;
	protected Session session;

	protected OnePasswordBase(Op op) {
		this.op = op;
	}

	protected OnePasswordBase(Op op, Session session) {
		this(op);
		this.session = session;
	}

	public UserEntityCommand users() {
		return new UserEntityCommand();
	}

	public NamedEntityCommand<Group> groups() {
		return new NamedEntityCommand<>(Group.class);
	}

	public VaultEntityCommand vaults() {
		return new VaultEntityCommand();
	}

	public AccessCommand access() {
		return new AccessCommand();
	}

	public Op op() {
		return op;
	}

	public Session session() {
		return session;
	}

	protected String execute(FunctionWithException<Op, String, IOException> action)
			throws IOException {
		return action.apply(op);
	}

	public interface Internal<E extends Entity> {
		/** Returns the entity type of this command. */
		Class<E> type();

		/** Returns the current session. */
		Session session();

		/** Executes an function with {@link Op}. */
		String execute(FunctionWithException<Op, String, IOException> action) throws IOException;
	}

	public interface TypeEntityCommand<E extends Entity> {
		/** Returns internal methods not meant for public use. */
		Internal<E> internal();
	}

	/** Commands for manipulating an entity. */
	public class EntityCommand<E extends Entity> implements TypeEntityCommand<E> {
		private final Internal<E> internal;

		EntityCommand(Class<E> entity) {
			this.internal = new Internal<E>() {
				@Override
				public Class<E> type() {
					return entity;
				}

				@Override
				public Session session() {
					return session;
				}

				@Override
				public String execute(FunctionWithException<Op, String, IOException> action)
						throws IOException {
					return OnePasswordBase.this.execute(action);
				}
			};
		}

		@Override
		public Internal<E> internal() {
			return internal;
		}

		/** Returns an entity with the given uuid or other primary key. Fails if not unique. */
		public E get(String nameOrUuid) throws IOException {
			String json = internal()
					.execute(op -> op.get(internal().session(), internal().type(), nameOrUuid));
			return Json.deserialize(json, internal().type());
		}

		/** Lists all entities */
		public E[] list() throws IOException {
			String json =
					internal().execute(op -> op.list(internal().session(), internal().type()));
			return Json.deserialize(json, Utils.arrayType(internal().type()));
		}

		/** Saves modification to the given entity. */
		public void edit(E entity) throws IOException {
			internal().execute(op -> op.edit(internal().session(), internal().type(),
					entity.getUuid(), entity.op_editArguments().toArray(String[]::new)));
		}

		/** Deletes an entity. */
		public void delete(E entity) throws IOException {
			internal().execute(
					op -> op.delete(internal().session(), internal().type(), entity.getUuid()));
		}

		protected E createWithArguments(String name, String... arguments) throws IOException {
			String json = internal().execute(
					op -> op.create(internal().session(), internal().type(), name, arguments));
			return Json.deserialize(json, internal().type());
		}
	}

	/** Commands for listing entities with access to given objects. */
	public interface ListWithAccessEntityCommand<E extends Entity, A extends Entity>
			extends TypeEntityCommand<E> {
		/** Lists all entities with access by another entity. */
		default public E[] listWithAccessBy(A accessible) throws IOException {
			String filterFlag = Flags.set(accessible.getClass(), accessible.getUuid());
			String json = internal()
					.execute(op -> op.list(internal().session(), internal().type(), filterFlag));
			return Json.deserialize(json, Utils.arrayType(internal().type()));
		}
	}

	/** Commands for manipulating an entity that is identified by name. */
	public class NamedEntityCommand<T extends Entity.Named> extends EntityCommand<T> {
		NamedEntityCommand(Class<T> type) {
			super(type);
		}

		/** Creates an entity. */
		public T create(String name) throws IOException {
			return create(name, (String) null);
		}

		/** Creates an entity. */
		public T create(String name, String description) throws IOException {
			return createWithArguments(name, description);
		}

		protected T createWithArguments(String name, String description, String... arguments)
				throws IOException {
			return createWithArguments(name,
					Utils.asArray(Flags.DESCRIPTION.is(description), arguments));
		}
	}

	/** Commands for manipulating users. */
	public class UserEntityCommand extends EntityCommand<User> {
		UserEntityCommand() {
			super(User.class);
		}

		/** Creates an user. */
		public User create(String emailAddress, String name) throws IOException {
			return create(emailAddress, name, null);
		}

		/** Creates an user and specifies its language, e.g. "en" or "de". */
		public User create(String emailAddress, String name, String language) throws IOException {
			return createWithArguments(emailAddress, name, Flags.LANGUAGE.is(language));
		}
	}

	/** Commands for manipulating vaults. */
	public class VaultEntityCommand extends NamedEntityCommand<Vault>
			implements ListWithAccessEntityCommand<Vault, Entity.UserOrGroup> {
		VaultEntityCommand() {
			super(Vault.class);
		}

		/**
		 * Creates a new vault optionally restricting access for admins. Default is that admins have
		 * access.
		 */
		public Vault create(String name, String description, boolean adminAccess)
				throws IOException {
			return createWithArguments(name, description,
					Flags.ALLOW_ADMINS_TO_MANAGE.is(Boolean.toString(adminAccess)));
		}
	}

	/** Command for granting or revoking access to entities. */
	public class AccessCommand {
		/** Grant a access to a vault. */
		public void add(Group group, Vault vault) throws IOException {
			execute(op -> op.add(session, group.getClass(), group.getUuid(), vault.getUuid()));
		}

		/** Revoke a group's access to a vault. */
		public void remove(Group group, Vault vault) throws IOException {
			execute(op -> op.remove(session, group.getClass(), group.getUuid(), vault.getUuid()));
		}

		/** Lists users of the vault. */
		public User[] users(Vault entity) throws IOException {
			return members(entity, User.class);
		}

		/** Lists groups of the vault. */
		public Group[] groups(Vault entity) throws IOException {
			return members(entity, Group.class);
		}

		private <M extends Entity.UserOrGroup> M[] members(Vault entity, Class<M> type)
				throws IOException {
			String json = execute(
					op -> op.list(session, type, entity.op_listUserFlag().is(entity.getUuid())));
			return Json.deserialize(json, Utils.arrayType(type));
		}

		/** Grant a user access to a group. */
		public void add(User user, Entity.UserAccessible entity) throws IOException {
			add(user, entity, null);
		}

		/** Grant a user access to a group or vault with given permission role. */
		public void add(User user, Group group, Role role) throws IOException {
			add(user, (Entity.UserAccessible) group, role);
		}

		private void add(User user, Entity.UserAccessible entity, Role role) throws IOException {
			execute(op -> op.add(session, User.class, user.getUuid(), entity.getUuid(),
					Flags.ROLE.is(Objects.toString(role, null))));
		}

		/** Lists users of the group with role permissions. */
		public Map<User, Role> users(Group entity) throws IOException {
			String json = execute(op -> op.list(session, User.class,
					entity.op_listUserFlag().is(entity.getUuid())));
			User[] users = Json.deserialize(json, User[].class);
			Role.JsonWrapper[] roles = Json.deserialize(json, Role.JsonWrapper[].class);
			return IntStream.range(0, Math.min(users.length, roles.length)).boxed()
					.collect(Collectors.toMap(i -> users[i], i -> roles[i].getRole()));
		}

		/** Revoke a user's access to a group or vault. */
		public void remove(User user, Entity.UserAccessible group) throws IOException {
			execute(op -> op.remove(session, User.class, user.getUuid(), group.getUuid()));
		}
	}
}
