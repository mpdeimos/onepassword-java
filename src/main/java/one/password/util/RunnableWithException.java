package one.password.util;

/** Runnable interface that throws an exception. */
public interface RunnableWithException<E extends Exception> {
	/** Executes the supplier. */
	public abstract void run() throws E;
}
