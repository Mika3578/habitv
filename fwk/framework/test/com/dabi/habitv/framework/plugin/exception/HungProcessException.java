package com.dabi.habitv.framework.plugin.exception;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;

public class HungProcessException extends ExecutorFailedException {

	private static final long serialVersionUID = 1L;
	private final long hungTime;
	
	public HungProcessException(String cmd, String fullOuput, String lastLine, long hungTime) {
		super(cmd, fullOuput, lastLine, null);
		this.hungTime = hungTime;
	}

	public long getHungTime() {
		return hungTime;
	}

}