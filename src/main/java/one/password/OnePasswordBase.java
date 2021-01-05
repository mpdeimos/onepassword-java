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

	/** Lists all users. */
	public User[] listUsers() throws IOException {
		return list(User.class);
	}

	/** Lists all groups. */
	public Group[] listGroups() throws IOException {
		return list(Group.class);
	}

	/** Lists all vaults. */
	public Vault[] listVaults() throws IOException {
		return list(Vault.class);
	}

	private <T extends Entity> T[] list(Class<T> entity) throws IOException {
		String json = execute(() -> op.list(session, entity));
		return Json.deserialize(json, Utils.arrayType(entity));
	}

	/** Creates a new vault with name. */
	public Vault createVault(String name) throws IOException {
		String json = execute(() -> op.create(session, Vault.class, name));
		return Json.deserialize(json, Vault.class);
	}

	/** Creates a new vault with name and description. */
	public Vault createVault(String name, String description) throws IOException {
		String json = execute(
				() -> op.create(session, Vault.class, name, Flags.DESCRIPTION.is(description)));
		return Json.deserialize(json, Vault.class);
	}

	/** Returns the vault with the given name or uuid. Fails if the name is not unique. */
	public Vault getVault(String nameOrUuid) throws IOException {
		String json = execute(() -> op.get(session, Vault.class, nameOrUuid));
		return Json.deserialize(json, Vault.class);
	}

	/** Saves modification to the given vault. */
	public void editVault(Vault vault) throws IOException {
		execute(() -> op.edit(session, Vault.class, vault.getUuid(),
				Flags.NAME.is(vault.getName())));
	}

	/** Deletes the vault. */
	public void deleteVault(Vault vault) throws IOException {
		execute(() -> op.delete(session, Vault.class, vault.getUuid()));
	}

	protected String execute(Op.Action<String> action) throws IOException {
		return action.execute();
	}

	public Op getOp() {
		return op;
	}

	public Session getSession() {
		return session;
	}
}
