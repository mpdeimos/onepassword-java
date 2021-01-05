package one.password;

public class Entity {
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
}
