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

	public GroupEntityCommand groups() {
		return new GroupEntityCommand();
	}

	public VaultEntityCommand vaults() {
		return new VaultEntityCommand();
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

	private static <E extends Entity> E[] list(Internal<E> internal) throws IOException {
		return listRelated(internal, null);
	}

	private static <E extends Entity, R extends Entity> E[] listRelated(Internal<E> internal,
			R related) throws IOException {
		return listRelated(internal, related,
				json -> Json.deserialize(json, Utils.arrayType(internal.type())));
	}

	private static <E extends Entity, R extends Entity, O> O listRelated(Internal<E> internal,
			R related, FunctionWithException<String, O, IOException> deserializer)
			throws IOException {
		String filterFlag = Entity.filterFlag(related);
		String json =
				internal.execute(op -> op.list(internal.session(), internal.type(), filterFlag));
		return deserializer.apply(json);
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
			return OnePasswordBase.list(internal());
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

	/**
	 * Commands for listing entities that are (transitively, e.g. via groups) accessible by other
	 * entities.
	 */
	public interface ListAccessByCommand<Accessible extends Entity, Accessor extends Entity>
			extends TypeEntityCommand<Accessible> {
		/**
		 * Lists all entities that are (transitively, e.g. via groups) accessible by other entities.
		 */
		default public Accessible[] listWithAccessBy(Accessor accessor) throws IOException {
			return listRelated(internal(), accessor);
		}
	}

	/**
	 * Commands for listing entities that have been granted direct access to other entities.
	 */
	public interface ListAccessToCommand<Accessor extends Entity, Accessible extends Entity>
			extends TypeEntityCommand<Accessor> {
		/** Lists all entities that have direct access to other entities (members of). */
		default public Accessor[] listWithDirectAccessTo(Accessible accessible) throws IOException {
			return listRelated(internal(), accessible);
		}
	}

	/**
	 * Commands for listing entities that have been granted direct access to other entities.
	 */
	public interface RoleListAccessToCommand<Accessor extends Entity, Accessible extends Entity>
			extends ListAccessToCommand<Accessor, Entity> {
		/** Lists all entities that have access to other entities (members of). */
		default Map<Accessor, Role> listRolesWithDirectAccessTo(Accessible accessible)
				throws IOException {
			return listRelated(internal(), accessible, json -> {
				Accessor[] accessors = Json.deserialize(json, Utils.arrayType(internal().type()));
				Role.JsonWrapper[] roles = Json.deserialize(json, Role.JsonWrapper[].class);
				return IntStream.range(0, Math.min(accessors.length, roles.length)).boxed()
						.collect(Collectors.toMap(i -> accessors[i], i -> roles[i].getRole()));
			});
		}
	}

	/**
	 * Commands for managing access to other entities.
	 */
	public interface AccessToCommand<Accessor extends Entity.UserOrGroup, Accessible extends Entity>
			extends ListAccessToCommand<Accessor, Entity> {
		/** Grant a access to an entity. */
		default void grantAccessTo(Accessor accessor, Accessible accessible) throws IOException {
			internal().execute(op -> op.add(internal().session(), accessor.getClass(),
					accessor.getUuid(), accessible.getUuid()));
		}

		/** Revoke access to an entity. */
		default void revokeAccessTo(Accessor accessor, Accessible accessible) throws IOException {
			internal().execute(op -> op.remove(internal().session(), accessor.getClass(),
					accessor.getUuid(), accessible.getUuid()));
		}
	}


	/**
	 * Commands for managing access to other entities using roles.
	 */
	public interface RoleAccessToCommand<Accessor extends Entity.UserOrGroup, Accessible extends Entity>
			extends AccessToCommand<Accessor, Entity>,
			RoleListAccessToCommand<Accessor, Accessible> {
		/** Grant a access to an entity with a given role. */
		default void add(Accessor accessor, Accessible accessible, Role role) throws IOException {
			internal().execute(
					op -> op.add(internal().session(), accessor.getClass(), accessor.getUuid(),
							accessible.getUuid(), Flags.ROLE.is(Objects.toString(role, null))));
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
			return createWithDescriptionAndArguments(name, description);
		}

		protected T createWithDescriptionAndArguments(String name, String description,
				String... arguments) throws IOException {
			return createWithArguments(name,
					Utils.asArray(Flags.DESCRIPTION.is(description), arguments));
		}
	}

	/** Commands for manipulating users. */
	public class UserEntityCommand extends EntityCommand<User>
			implements RoleAccessToCommand<User, Entity.UserAccessible> {
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

	/** Commands for manipulating groups. */
	public class GroupEntityCommand extends NamedEntityCommand<Group>
			implements ListAccessByCommand<Group, User>, AccessToCommand<Group, Vault> {
		GroupEntityCommand() {
			super(Group.class);
		}
	}

	/** Commands for manipulating vaults. */
	public class VaultEntityCommand extends NamedEntityCommand<Vault>
			implements ListAccessByCommand<Vault, Entity.UserOrGroup> {
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
}
