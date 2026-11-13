package com.dabi.habitv.provider.rtbfauvio;

import java.io.IOException;

final class RtbfAuvioHttpException extends IOException {

	private static final long serialVersionUID = 1L;

	private final int status;

	RtbfAuvioHttpException(final int status) {
		super("http-" + status);
		this.status = status;
	}

	int getStatus() {
		return status;
	}

}
