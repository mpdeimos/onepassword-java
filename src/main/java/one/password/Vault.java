package one.password;

import one.password.cli.Flags;

public class Vault extends Entity.Named implements Entity.UserAccessible {

	@Override
	public Flags op_listUserFlag() {
		return Flags.VAULT;
	}

}
