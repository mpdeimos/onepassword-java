package one.password.util;

import java.time.ZonedDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonTest {
	@Test
	void testZonedDateTimeSerialization() {
		String dateJson = "\"2020-12-29T10:53:35Z\"";
		ZonedDateTime date = Json.deserialize(dateJson, ZonedDateTime.class);
		Assertions.assertThat(date).isBefore(ZonedDateTime.now());
		Assertions.assertThat(Json.serialize(date)).isEqualTo(dateJson);
	}

}
