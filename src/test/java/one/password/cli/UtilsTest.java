package one.password.cli;

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
}
