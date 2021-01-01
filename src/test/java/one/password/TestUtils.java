package one.password;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestUtils {
	public static String getOnePasswordVersion() throws IOException {
		InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream("op.version");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			return reader.readLine();
		}
	}
}
