package com.dabi.habitv.core.task;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of task types.
 * Defines the different types of tasks that can be executed.
 */
public enum TaskTypeEnum {
	/** Search task type. */
	SEARCH,
	/** Retrieve task type. */
	RETRIEVE,
	/** Download task type. */
	DOWNLOAD,
	/** Export task type. */
	EXPORT,
	/** Category task type. */
	CATEGORY;

	/** Default pool size for task execution. */
	private static final int DEFAULT_POOLSIZE = 1;

	/** Map of task types to their pool sizes. */
	private static final Map<TaskTypeEnum, Integer> TYPE_TO_POOL_SIZE = 
			new HashMap<TaskTypeEnum, Integer>();

	static {
		TYPE_TO_POOL_SIZE.put(SEARCH, 1);
		TYPE_TO_POOL_SIZE.put(RETRIEVE, 1);
		TYPE_TO_POOL_SIZE.put(DOWNLOAD, 3);
		TYPE_TO_POOL_SIZE.put(EXPORT, 1);
		TYPE_TO_POOL_SIZE.put(CATEGORY, 1);
	}

	/**
	 * Gets the pool size for this task type.
	 * 
	 * @return the pool size for this task type
	 */
	public int getPoolSize() {
		Integer poolSize = TYPE_TO_POOL_SIZE.get(this);
		return poolSize != null ? poolSize : DEFAULT_POOLSIZE;
	}
}
