package com.dabi.habitv.framework.plugin.utils.update;

/**
 * Thrown when remote or caller-supplied update metadata would resolve outside
 * the trusted updater root or otherwise violate path safety rules.
 */
public class InvalidUpdatePathException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidUpdatePathException(final String message) {
		super(message);
	}
}
