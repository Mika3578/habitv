package com.dabi.habitv.core.task;

/**
 * Exception thrown when a task fails.
 * This exception wraps the original cause of the task failure.
 */
public final class TaskFailedException extends RuntimeException {
	
	/** Serial version UID for serialization */
	private static final long serialVersionUID = -5643616531292779318L;

	/**
	 * Constructs a new TaskFailedException with the specified cause.
	 * 
	 * @param cause the cause of the task failure
	 */
	public TaskFailedException(final Throwable cause) {
		super(cause);
	}
}
