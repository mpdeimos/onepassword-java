package one.password;

import java.io.IOException;
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

	public EntityCommand<User> users() {
		return new EntityCommand<>(User.class);
	}

	public NamedEntityCommand<Group> groups() {
		return new NamedEntityCommand<>(Group.class);
	}

	public NamedEntityCommand<Vault> vaults() {
		return new NamedEntityCommand<>(Vault.class);
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

		/** Deletes an entity. */
		public void delete(Entity entity) throws IOException {
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

		/** Saves modification to the given entity. */
		public void edit(T entity) throws IOException {
			execute(() -> op.edit(session, type, entity.getUuid(),
					Flags.NAME.is(entity.getName())));
		}
	}
}
