package one.password.util;

/** BiConsumer interface that throws an exception. */
public interface BiConsumerWithException<T, U, E extends Exception> {
	/** Executes the consumer. */
	public abstract void accept(T t, U u) throws E;
}
