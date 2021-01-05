package one.password;

import java.io.IOException;
import one.password.cli.Entities;
import one.password.cli.Op;
import one.password.util.Json;

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

	public User[] listUsers() throws IOException {
		return list(Entities.USERS, User[].class);
	}

	public Group[] listGroups() throws IOException {
		return list(Entities.GROUPS, Group[].class);
	}

	public Vault[] listVaults() throws IOException {
		return list(Entities.VAULTS, Vault[].class);
	}

	private <T> T list(Entities entity, Class<T> clazz) throws IOException {
		String json = execute(() -> op.list(session, entity));
		return Json.deserialize(json, clazz);
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
