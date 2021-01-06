package one.password;

import com.google.gson.annotations.SerializedName;

/** Base class for 1password entities. */
public abstract class Entity {
	private String uuid;

	public String getUuid() {
		return uuid;
	}

	/** Returns the singular name of the entity class. */
	public static String singular(Class<? extends Entity> clazz) {
		return clazz.getSimpleName().toLowerCase();
	}

	/** Returns the plural name of the entity class. */
	public static String plural(Class<? extends Entity> clazz) {
		return singular(clazz) + "s";
	}

	/** Base class for entities identified by name. */
	public abstract static class Named extends Entity {
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
}
