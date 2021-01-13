package one.password;

import java.util.List;
import one.password.cli.OpMock;

public class OnePasswordMock extends OnePassword {
	private OpMock opMock;

	public OnePasswordMock() {
		this(new OpMock());
	}

	public OnePasswordMock(OpMock op) {
		super(op, "user", "email", "key", () -> "password");
		this.opMock = op;
	}

	public List<List<String>> getCommands() {
		return opMock.getCommands();
	}

	public List<List<String>> getSignins() {
		return opMock.getSignins();
	}
}
