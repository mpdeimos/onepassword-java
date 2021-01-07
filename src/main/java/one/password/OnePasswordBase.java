package one.password;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import one.password.cli.Flags;
import one.password.cli.Op;
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

	// TODO create --allow-admins-to-manage
	public NamedEntityCommand<Vault> vaults() {
		return new NamedEntityCommand<>(Vault.class);
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

	protected String execute(Op.Action<String> action) throws IOException {
		return action.execute();
	}

	/** Commands for manipulating an entity. */
	public class EntityCommand<T extends Entity> {
		protected final Class<T> type;

		EntityCommand(Class<T> entity) {
			this.type = entity;
		}

		/** Returns an entity with the given uuid or other primary key. Fails if not unique. */
		public T get(String nameOrUuid) throws IOException {
			String json = execute(() -> op.get(session, type, nameOrUuid));
			return Json.deserialize(json, type);
		}

		/** Lists all entities */
		public T[] list() throws IOException {
			String json = execute(() -> op.list(session, type));
			return Json.deserialize(json, Utils.arrayType(type));
		}

		/** Saves modification to the given entity. */
		public void edit(T entity) throws IOException {
			execute(() -> op.edit(session, type, entity.getUuid(),
					entity.op_editArguments().toArray(String[]::new)));
		}

		/** Deletes an entity. */
		public void delete(T entity) throws IOException {
			execute(() -> op.delete(session, type, entity.getUuid()));
		}
	}

	/** Commands for manipulating an entity that is identified by name. */
	public class NamedEntityCommand<T extends Entity.Named> extends EntityCommand<T> {
		NamedEntityCommand(Class<T> type) {
			super(type);
		}

		/** Creates an entity. */
		public T create(String name) throws IOException {
			return create(name, null);
		}

		/** Creates an entity. */
		public T create(String name, String description) throws IOException {
			String json = execute(
					() -> op.create(session, type, name, Flags.DESCRIPTION.is(description)));
			return Json.deserialize(json, type);
		}
	}

	/** Commands for manipulating an user. */
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
			String json = execute(() -> op.create(session, type, emailAddress, name,
					Flags.LANGUAGE.is(language)));
			return Json.deserialize(json, type);
		}
	}

	/** Command for granting or revoking access to entities. */
	public class AccessCommand {
		/** Grant a access to a vault. */
		public void add(Group group, Vault vault) throws IOException {
			execute(() -> op.add(session, group.getClass(), group.getUuid(), vault.getUuid()));
		}

		/** Revoke a group's access to a vault. */
		public void remove(Group group, Vault vault) throws IOException {
			execute(() -> op.remove(session, group.getClass(), group.getUuid(), vault.getUuid()));
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
			execute(() -> op.add(session, User.class, user.getUuid(), entity.getUuid(),
					Flags.ROLE.is(Objects.toString(role, null))));
		}

		/** Lists users of the group or vault with role permissions. */
		public Map<User, Role> users(Group entity) throws IOException {
			String json = execute(() -> op.list(session, User.class,
					entity.op_listUserFlag().is(entity.getUuid())));
			User[] users = Json.deserialize(json, User[].class);
			Role.JsonWrapper[] roles = Json.deserialize(json, Role.JsonWrapper[].class);
			return IntStream.range(0, Math.min(users.length, roles.length)).boxed()
					.collect(Collectors.toMap(i -> users[i], i -> roles[i].getRole()));
		}

		/** Lists users of the group or vault with role permissions. */
		public User[] users(Vault entity) throws IOException {
			String json = execute(() -> op.list(session, User.class,
					entity.op_listUserFlag().is(entity.getUuid())));
			return Json.deserialize(json, User[].class);
		}

		/** Revoke a user's access to a group or vault. */
		public void remove(User user, Entity.UserAccessible group) throws IOException {
			execute(() -> op.remove(session, User.class, user.getUuid(), group.getUuid()));
		}
	}
}
