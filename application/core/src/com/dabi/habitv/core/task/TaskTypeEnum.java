package com.dabi.habitv.core.task;

import java.util.Map;

public enum TaskTypeEnum {
	category(5), export(2), retreive(50), search(5), download(2);

	private final int defaultPoolsize;

	private TaskTypeEnum(final int defaultPoolsize) {
		this.defaultPoolsize = defaultPoolsize;
	}

	public int getPoolSize(final Map<String, Integer> taskName2PoolSize) {
		Integer poolSize = taskName2PoolSize.get(this.toString());
		if (poolSize == null) {
			poolSize = defaultPoolsize;
		}
		return poolSize;
	}
}
