package one.password;

import java.io.IOException;
import com.google.gson.Gson;
import one.password.cli.Entities;
import one.password.cli.Op;

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
		String json = execute(() -> op.list(session, Entities.USERS));
		return new Gson().fromJson(json, User[].class);
	}

	protected String execute(Op.Action<String> action) throws IOException {
		return action.execute();
	}
}
