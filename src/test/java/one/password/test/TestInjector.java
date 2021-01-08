package one.password.test;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import one.password.Config;
import one.password.OnePassword;
import one.password.OnePasswordBase;
import one.password.Session;
import one.password.cli.Op;

/**
 * Injector for objects into JUnit tests. The main aim of this injector is to share 1password
 * sessions as much as possible.
 */
public class TestInjector implements ParameterResolver {

	private enum Classes {
		CREDENTIALS(TestCredentials.class, Suppliers.memoize(TestCredentials::new)),

		CONFIG(Config.class, () -> new TestConfig(create(TestCredentials.class))),

		ONE_PASSWORD(OnePasswordBase.class, Suppliers.memoize(() -> {
			TestConfig config = create(TestConfig.class);
			return TestUtils.assertNoIOException(() -> new OnePassword(config,
					config.credentials.getSignInAddress(), config.credentials.getEmailAddress(),
					config.credentials.getSecretKey(), config.credentials::getPassword));
		})),

		OP(Op.class, Suppliers.memoize(() -> create(OnePassword.class).op())),

		SESSION(Session.class, () -> create(OnePassword.class).session());

		private final Class<?> clazz;
		private final Supplier<?> constructor;

		private <T> Classes(Class<T> clazz, Supplier<T> constructor) {
			this.clazz = clazz;
			this.constructor = constructor;
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		return find(parameterContext.getParameter().getType()).isPresent();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		return create(parameterContext.getParameter().getType());
	}

	private static Optional<Classes> find(Class<?> type) {
		return Arrays.stream(Classes.values()).filter(clazz -> clazz.clazz.isAssignableFrom(type))
				.findFirst();
	}

	@SuppressWarnings("unchecked")
	private static <T> T create(Class<T> type) {
		Optional<Classes> classes = find(type);
		Assertions.assertThat(classes).isPresent();
		return (T) classes.get().constructor.get();
	}
}
