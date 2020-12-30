package one.password.cli;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class OpTest {
	@Test
	void testGetVersion() throws IOException {
		Assertions.assertThat(new Op().version()).isEqualTo("1.8.0");
	}
}
