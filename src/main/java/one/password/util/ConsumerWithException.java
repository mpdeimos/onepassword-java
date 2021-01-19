package one.password.util;

/** Consumer interface that throws an exception. */
public interface ConsumerWithException<T, E extends Exception> {
	/** Executes the consumer. */
	public abstract void accept(T t) throws E;
}
