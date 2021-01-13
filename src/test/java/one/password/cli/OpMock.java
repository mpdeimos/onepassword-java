package one.password.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import one.password.Session;

public class OpMock extends Op {
	private static final Session SESSION = new Session("session", "shorthand");
	private final List<List<String>> commands = new ArrayList<>();
	private final List<List<String>> signins = new ArrayList<>();

	public Session signin(String signInAddress, String emailAddress, String secretKey,
			Supplier<String> password, Session session) throws IOException {
		signins.add(Arrays.asList(signInAddress, emailAddress, secretKey));
		return SESSION;
	}

	public String execute(Session session, String... arguments) throws IOException {
		commands.add(
				Arrays.stream(arguments).filter(Objects::nonNull).collect(Collectors.toList()));
		return "";
	}

	public List<List<String>> getSignins() {
		List<List<String>> returnValue = new ArrayList<>(signins);
		signins.clear();
		return returnValue;
	}

	public List<List<String>> getCommands() {
		List<List<String>> returnValue = new ArrayList<>(commands);
		commands.clear();
		return returnValue;
	}
}
