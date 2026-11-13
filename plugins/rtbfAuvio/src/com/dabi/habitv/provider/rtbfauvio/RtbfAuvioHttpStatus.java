package com.dabi.habitv.provider.rtbfauvio;

final class RtbfAuvioHttpStatus {

	private RtbfAuvioHttpStatus() {
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
