package one.password.cli;

import java.io.IOException;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import one.password.TestUtils;
import one.password.Utils;

public class OpTest {
	@Test
	void testGetVersion() throws IOException {
		Op.Config config = new Op.Config();
		String executable = "build/bin/op";
		if (Utils.IS_WINDOWS) {
			executable += ".exe";
		}
		config.setExecutable(Paths.get(executable));
		Assertions.assertThat(new Op(config).version()).isEqualTo(TestUtils.getOnePasswordVersion());
	}
}
