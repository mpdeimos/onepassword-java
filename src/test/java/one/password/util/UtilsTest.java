package one.password.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {
	@Test
	void testGetShorthand() {
		Assertions.assertThat(Utils.getShorthand("https://foo-bar.1password.com/")).get()
				.isEqualTo("foo_bar");
		Assertions.assertThat(Utils.getShorthand("https://foo-bar.1password.eu/")).get()
				.isEqualTo("foo_bar");
		Assertions.assertThat(Utils.getShorthand("https://Foo-Bar1.1password.eu/")).get()
				.isEqualTo("foo_bar1");
		Assertions.assertThat(Utils.getShorthand("no.url")).isEmpty();
	}

	@Test
	void testArrayType() {
		Assertions.assertThat(Utils.arrayType(String.class)).isEqualTo(String[].class);
		Assertions.assertThatThrownBy(() -> Utils.arrayType(String[].class))
				.isInstanceOf(AssertionError.class);
	}

	@Test
	void testAsArray() {
		Assertions.assertThat(Utils.asArray("a", new String[] {"b", "c", "d", "e"}))
				.containsExactly("a", "b", "c", "d", "e");
		Assertions.assertThat(Utils.asArray("a", "b", new String[] {"c", "d", "e"}))
				.containsExactly("a", "b", "c", "d", "e");
		Assertions.assertThat(Utils.asArray("a", "b", "c", new String[] {"d", "e"}))
				.containsExactly("a", "b", "c", "d", "e");
	}
}
