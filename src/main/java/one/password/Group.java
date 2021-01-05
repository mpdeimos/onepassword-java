package one.password;

import java.time.ZonedDateTime;
import com.google.gson.annotations.SerializedName;

public class Group extends Entity {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SerializedName("desc")
	private String description;

	public String getDescription() {
		return description;
	}

	private ZonedDateTime createdAt;

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}
}
