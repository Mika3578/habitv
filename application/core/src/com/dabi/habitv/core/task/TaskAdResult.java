package com.dabi.habitv.core.task;

/**
 * Result of adding a task to the task manager.
 * Contains the state of the task after it has been added.
 */
public class TaskAdResult {

	/** The state of the task after being added. */
	private final TaskState state;

	/**
	 * Constructs a new TaskAdResult with the specified state.
	 * 
	 * @param state the state of the task
	 */
	public TaskAdResult(final TaskState state) {
		super();
		this.state = state;
	}

	/**
	 * Gets the state of the task.
	 * 
	 * @return the task state
	 */
	public TaskState getState() {
		return state;
	}

}
