package de.dirkdittmar.offheapCache;

public class NotEnoughSpaceException extends RuntimeException {

	private static final long serialVersionUID = -3596620565594686492L;

	public NotEnoughSpaceException(final String msg) {
		super(msg);
	}

	public NotEnoughSpaceException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
