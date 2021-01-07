package one.password.util;

/** Supplier interface that throws an exception. */
public interface SupplierWithException<R, E extends Exception> {
	/** Executes the supplier. */
	public abstract R get() throws E;
}
