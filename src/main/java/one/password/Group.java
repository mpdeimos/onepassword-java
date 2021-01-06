package one.password;

import java.time.ZonedDateTime;

public class Group extends Entity.Named {

	private ZonedDateTime createdAt;

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}
}
