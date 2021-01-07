package one.password.util;

/** Function interface that throws an exception. */
public interface FunctionWithException<T, R, E extends Exception> {
	/** Executes the function. */
	public abstract R apply(T argument) throws E;
}
