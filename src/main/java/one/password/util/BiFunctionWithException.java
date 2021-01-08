package one.password.util;

/** Function interface that throws an exception. */
public interface BiFunctionWithException<A1, A2, R, E extends Exception> {
	/** Executes the function. */
	public abstract R apply(A1 first, A2 second) throws E;
}
