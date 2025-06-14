package com.dabi.habitv.core.task;

/**
 * Enumeration of task states.
 * Represents the different states a task can be in.
 */
public enum TaskState {
	/** Task has been added to the queue. */
	ADDED,
	/** Task is already in the queue. */
	ALREADY_ADD,
	/** Task has failed too many times. */
	TO_MANY_FAILED,
	/** Task encountered an error. */
	ERROR;
}

