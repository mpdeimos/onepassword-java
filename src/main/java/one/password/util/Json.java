package one.password.util;

import java.io.IOException;
import java.time.ZonedDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/** Utilities for converting Java objects from/to Json. */
public class Json {
	private static final TypeAdapter<ZonedDateTime> ZONED_DATE_TIME_ADAPTER =
			new TypeAdapter<ZonedDateTime>() {

				@Override
				public void write(JsonWriter out, ZonedDateTime value) throws IOException {
					out.value(value.toString());
				}

				@Override
				public ZonedDateTime read(JsonReader in) throws IOException {
					return ZonedDateTime.parse(in.nextString());
				}

			};


	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ZonedDateTime.class, ZONED_DATE_TIME_ADAPTER).create();

	/**
	 * Deserializes a JSON String to a Java Object.
	 */
	public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
		try {
			return GSON.fromJson(json, clazz);
		} catch (JsonParseException e) {
			throw new IOException(e.getMessage() + "\nJson:\n" + json, e);
		}
	}

	/** Serializes a Java Object to Json. */
	public static String serialize(Object object) {
		return GSON.toJson(object);
	}
}
