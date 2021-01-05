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

	/** Returns the group with the given name or uuid. Fails if the name is not unique. */
	public Group getGroup(String nameOrUuid) throws IOException {
		return get(Group.class, nameOrUuid);
	}

	/** Returns the vault with the given name or uuid. Fails if the name is not unique. */
	public Vault getVault(String nameOrUuid) throws IOException {
		return get(Vault.class, nameOrUuid);
	}

	private <T extends Entity> T get(Class<T> entity, String nameOrUuid) throws IOException {
		String json = execute(() -> op.get(session, entity, nameOrUuid));
		return Json.deserialize(json, entity);
	}

	/** Saves modification to the given vault. */
	public void editVault(Vault vault) throws IOException {
		execute(() -> op.edit(session, Vault.class, vault.getUuid(),
				Flags.NAME.is(vault.getName())));
	}

	/** Deletes a group. */
	public void deleteGroup(Group group) throws IOException {
		delete(group);
	}

	/** Deletes a vault. */
	public void deleteVault(Vault vault) throws IOException {
		delete(vault);
	}

	public void delete(Entity entity) throws IOException {
		execute(() -> op.delete(session, entity.getClass(), entity.getUuid()));
	}

	public EntityCommand<User> users() {
		return new EntityCommand<>(User.class);
	}

	public EntityCommand<Group> groups() {
		return new EntityCommand<>(Group.class);
	}

	public EntityCommand<Vault> vaults() {
		return new EntityCommand<>(Vault.class);
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
		private Class<T> entity;

		EntityCommand(Class<T> entity) {
			this.entity = entity;
		}

		/** Creates an entity. */
		public T create(String name) throws IOException {
			return create(name, null);
		}

		/** Creates an entity. */
		public T create(String name, String description) throws IOException {
			String json = execute(
					() -> op.create(session, entity, name, Flags.DESCRIPTION.is(description)));
			return Json.deserialize(json, entity);
		}

		/** Lists all entities */
		private T[] list() throws IOException {
			String json = execute(() -> op.list(session, entity));
			return Json.deserialize(json, Utils.arrayType(entity));
		}

	}
}
