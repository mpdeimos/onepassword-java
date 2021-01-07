package one.password;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import one.password.cli.Flags;

public class Group extends Entity.Named implements Entity.UserOrGroup {

	public void setDescription(String description) {
		this.description = description;
	}

	private ZonedDateTime createdAt;

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	private ZonedDateTime updatedAt;

	public Optional<ZonedDateTime> getUpdatedAt() {
		return Optional.ofNullable(updatedAt);
	}

	@Override
	public Stream<String> saveArguments() {
		return Stream.concat(super.saveArguments(), Stream.of(Flags.DESCRIPTION.is(description)));
	}
}
