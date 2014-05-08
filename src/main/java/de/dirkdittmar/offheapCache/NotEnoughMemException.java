package de.dirkdittmar.offheapCache;

public class NotEnoughMemException extends RuntimeException {

	private static final long serialVersionUID = -3596620565594686492L;

	public NotEnoughMemException(final String msg) {
		super(msg);
	}

	public NotEnoughMemException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
