package com.dabi.habitv.provider.novo19;

final class Novo19HttpStatus {

	private Novo19HttpStatus() {
	}

	static String summarizeFailure(final int status) {
		switch (status) {
		case 401:
			return "http-401";
		case 403:
			return "http-403";
		case 404:
			return "http-404";
		case 429:
			return "http-429";
		default:
			if (status >= 500) {
				return "http-5xx";
			}
			return "http-error";
		}
	}

}
