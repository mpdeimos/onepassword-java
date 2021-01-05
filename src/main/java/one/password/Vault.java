package one.password;

import com.google.gson.annotations.SerializedName;

public class Vault extends Entity {
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
}
