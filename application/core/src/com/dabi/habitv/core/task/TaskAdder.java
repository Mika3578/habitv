package com.dabi.habitv.core.task;

/**
 * Interface for adding tasks to the task manager.
 * Provides methods to add different types of tasks to the system.
 */
public interface TaskAdder {

	/**
	 * Adds a retrieve task to the task manager.
	 * 
	 * @param retreiveTask the retrieve task to add
	 * @return the result of adding the task
	 */
	TaskAdResult addRetreiveTask(RetrieveTask retreiveTask);

	/**
	 * Adds an export task to the task manager.
	 * 
	 * @param exportTask the export task to add
	 * @param category the category for the export task
	 * @return the result of adding the task
	 */
	TaskAdResult addExportTask(ExportTask exportTask, String category);

	/**
	 * Adds a download task to the task manager.
	 * 
	 * @param downloadTask the download task to add
	 * @param channel the channel for the download task
	 * @return the result of adding the task
	 */
	TaskAdResult addDownloadTask(DownloadTask downloadTask, String channel);

}
