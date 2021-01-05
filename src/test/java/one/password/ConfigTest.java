package one.password;

import com.google.common.base.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {
	@Test
	void testSetDevice() {
		Config config = new Config();
		Assertions.assertThat(config.getDevice()).hasSize(26);
		Assertions.assertThatIllegalArgumentException().isThrownBy(() -> config.setDevice(""));
		Assertions.assertThatIllegalArgumentException().isThrownBy(() -> config.setDevice("a"));
		Assertions.assertThatCode(() -> config.setDevice(Strings.repeat("a", 26)))
				.doesNotThrowAnyException();
		Assertions.assertThat(config.getDevice()).isEqualTo(Strings.repeat("a", 26));
		Assertions.assertThatCode(() -> config.setDevice(null)).doesNotThrowAnyException();
		Assertions.assertThat(config.getDevice()).isNotEqualTo(Strings.repeat("a", 26)).hasSize(26);
	}

}
