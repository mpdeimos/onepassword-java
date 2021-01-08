package one.password;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import one.password.cli.Flags;

public class User extends Entity.Base implements Entity.UserOrGroup {

	private static final Set<String> INVITED_STATES = new HashSet<>(Arrays.asList( //
			"T", // pending creation
			"1", "2", // unknown, but better add these here
			"3" // via CLI
	));

	private static final Set<String> PENDING_CONFIRMATION_STATES = new HashSet<>(Arrays.asList( //
			"4", // via CLI: Pending Provision
			"P" // via self registration: Pending Confirmation
	));

	private static final Set<String> ACTIVE_STATES = new HashSet<>(Arrays.asList("A"));

	private static final Set<String> SUSPENDED_STATES = new HashSet<>(Arrays.asList("S"));

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

	private String state;

	/** Returns whether a user is active. */
	public boolean isActive() {
		return ACTIVE_STATES.contains(state.toUpperCase());
	}

	/** Returns whether a user is suspended. */
	public boolean isSuspended() {
		return SUSPENDED_STATES.contains(state.toUpperCase());
	}

	/**
	 * Returns whether a user is is invited and needs to setup its account before it can be
	 * confirmed.
	 */
	public boolean isInvited() {
		return INVITED_STATES.contains(state.toUpperCase());
	}

	private String type;

	/** Returns whether the user is a guest. */
	public boolean isGuest() {
		return "G".equals(type.toUpperCase());
	}

	/**
	 * Returns whether a user has setup its account and is pending confirmation.
	 */
	public boolean isPendingConfirmation() {
		return PENDING_CONFIRMATION_STATES.contains(state.toUpperCase());
	}

	public Stream<String> op_editArguments() {
		return Stream.of(Flags.NAME.is(name));
	}

	@Override
	public String getSecondaryId() {
		return getEmail();
	}
}
