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
	EXPORT;

	/** Default pool size for task execution. */
	private static final int DEFAULT_POOLSIZE = 1;

	/** Map of task types to their pool sizes. */
	private static final Map<TaskTypeEnum, Integer> type2PoolSize = 
			new HashMap<TaskTypeEnum, Integer>();

	static {
		type2PoolSize.put(SEARCH, 1);
		type2PoolSize.put(RETRIEVE, 1);
		type2PoolSize.put(DOWNLOAD, 3);
		type2PoolSize.put(EXPORT, 1);
	}

	/**
	 * Gets the pool size for this task type.
	 * 
	 * @return the pool size for this task type
	 */
	public int getPoolSize() {
		Integer poolSize = type2PoolSize.get(this);
		return poolSize != null ? poolSize : DEFAULT_POOLSIZE;
	}
}
