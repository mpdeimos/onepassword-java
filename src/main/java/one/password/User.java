package one.password;

import java.time.ZonedDateTime;
import java.util.stream.Stream;
import one.password.cli.Flags;

public class User extends Entity.Base implements Entity.UserOrGroup {

	private String email;

	public String getEmail() {
		return email;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String firstName;

	public String getFirstName() {
		return firstName;
	}

	private String lastName;

	public String getLastName() {
		return lastName;
	}

	private String language;

	public String getLanguage() {
		return language;
	}

	private ZonedDateTime createdAt;

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	private ZonedDateTime updatedAt;

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	private ZonedDateTime lastAuthAt;

	public ZonedDateTime getLastAuthAt() {
		return lastAuthAt;
	}

	public Stream<String> op_editArguments() {
		return Stream.of(Flags.NAME.is(name));
	}

	@Override
	public String getSecondaryId() {
		return getEmail();
	}
}
