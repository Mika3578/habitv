package com.dabi.habitv.core.task;

/**
 * Listener interface for task events.
 * Implementations can be notified when tasks end or fail.
 */
public interface TaskListener {

	/**
	 * Called when a task has ended successfully.
	 */
	void onTaskEnded();

	/**
	 * Called when a task has failed.
	 */
	void onTaskFailed();

}
