package com.dabi.habitv.core.task;

/**
 * Listener interface for task manager events.
 * Implementations can be notified when all treatments are done or when failures occur.
 */
public interface TaskMgrListener {
	
	/**
	 * Called when all treatments are completed.
	 */
	void onAllTreatmentDone();

	/**
	 * Called when a failure occurs during task processing.
	 * 
	 * @param t the throwable that caused the failure
	 */
	void onFailed(Throwable t);
}
