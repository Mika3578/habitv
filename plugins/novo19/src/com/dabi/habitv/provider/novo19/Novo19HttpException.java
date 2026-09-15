package com.dabi.habitv.provider.novo19;

import java.io.IOException;

final class Novo19HttpException extends IOException {

	private static final long serialVersionUID = 1L;

	private final int status;

	Novo19HttpException(final int status) {
		super("http-" + status);
		this.status = status;
	}

	int getStatus() {
		return status;
	}

}
