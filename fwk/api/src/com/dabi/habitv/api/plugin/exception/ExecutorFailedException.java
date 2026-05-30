package com.dabi.habitv.api.plugin.exception;

import org.apache.log4j.Logger;

public class ExecutorFailedException extends RuntimeException {

	protected static final Logger LOG = Logger
			.getLogger(ExecutorFailedException.class);

	private static final long serialVersionUID = -3244886187302237470L;

	private final String fullOuput;

	private final String lastLine;

	private final String cmd;

	public ExecutorFailedException(final String cmd, final String fullOuput,
			final String lastLine, final Throwable throwable) {
		super(lastLine, throwable);
		this.fullOuput = fullOuput;
		this.cmd = cmd;
		this.lastLine = lastLine;
		LOG.error("External command failed: " + ExecutorOutputSanitizer.sanitizeCommand(cmd));
		final String sanitizedOutput = ExecutorOutputSanitizer.sanitizeOutput(fullOuput);
		if (sanitizedOutput != null && !sanitizedOutput.isEmpty()) {
			LOG.error("External command output snippet: " + sanitizedOutput);
		}
	}

	public String getFullOuput() {
		return fullOuput;
	}

	public String getCmd() {
		return cmd;
	}

	public String getLastLine() {
		return lastLine;
	}

}
