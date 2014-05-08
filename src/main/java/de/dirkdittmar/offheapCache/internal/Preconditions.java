package de.dirkdittmar.offheapCache.internal;


public final class Preconditions {

	private Preconditions() {
	}

	public static void checkArgument(final boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException();
		}
	}

	public static void checkArgument(final boolean expression,
			final String errorMessage) {
		if (!expression) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	public static void checkArgument(final boolean expression,
			final String errorMessageTemplate, final Object... errorMessageArgs) {
		if (!expression) {
			throw new IllegalArgumentException(String.format(
					errorMessageTemplate, errorMessageArgs));
		}
	}

}
