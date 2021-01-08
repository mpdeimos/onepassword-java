package one.password;

import java.util.stream.Stream;
import com.google.gson.annotations.SerializedName;
import one.password.cli.Flags;

/** Base class for 1password entities. */
public interface Entity {
	/** Returns the entities Uuid. */
	public String getUuid();

	/** For internal use: Arguments for editing this entity via the 1password CLI. */
	Stream<String> op_editArguments();

	/** Returns the singular name of the entity class. */
	public static String singular(Class<? extends Entity> clazz) {
		return clazz.getSimpleName().toLowerCase();
	}

	/** Returns the plural name of the entity class. */
	public static String plural(Class<? extends Entity> clazz) {
		return singular(clazz) + "s";
	}

	/**
	 * Converts the provided entity to a filter flag with the entity uuid, e.g. "--user=xxx".
	 * Returns null if the entity is null.
	 */
	public static String filterFlag(Entity entity) {
		if (entity == null) {
			return null;
		}

		return Flags.set(entity.getClass().getSimpleName().toLowerCase(), entity.getUuid());
	}

	/** Base class for 1password entities. */
	public abstract static class Base implements Entity {
		private String uuid;

		@Override
		public String getUuid() {
			return uuid;
		}
	}

	/** Base class for entities identified by name. */
	public abstract static class Named extends Base {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@SerializedName("desc")
		protected String description;

		public String getDescription() {
			return description;
		}

		public Stream<String> op_editArguments() {
			return Stream.of(Flags.NAME.is(name));
		}
	}

	/** Marker interface for users and groups to grant access. */
	public static interface UserOrGroup extends Entity {
		// TODO rename to accessor?
	}

	/** Marker interface for entities users may have access to. */
	public static interface UserAccessible extends Entity {
		// TODO rename to accessible?
	}
}
