package com.dabi.habitv.api.plugin.exception;


public class TechnicalException extends RuntimeException {

	public TechnicalException(final Throwable e) {
		super(e);
	}

	public TechnicalException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TechnicalException(final String string) {
		super(string);
	}

	private static final long serialVersionUID = -6072107997453620547L;

}
