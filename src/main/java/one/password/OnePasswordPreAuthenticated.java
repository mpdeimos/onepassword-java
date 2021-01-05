package one.password;

import one.password.cli.Op;

/**
 * High-level 1password CLI Java binding that reuses an existing session, but does not auto-renew
 * it.
 */
public class OnePasswordPreAuthenticated extends OnePasswordBase {
	public OnePasswordPreAuthenticated(OnePasswordBase api) {
		super(api.op, api.session);
	}

	public OnePasswordPreAuthenticated(Session session) {
		this(null, session);
	}

	public OnePasswordPreAuthenticated(Config config, Session session) {
		super(new Op(config), session);
	}
}
