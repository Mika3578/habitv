package com.dabi.habitv.core.task;

public enum EnqueueSkipReason {
	ALREADY_DOWNLOADED,
	ALREADY_QUEUED,
	ALREADY_DOWNLOADING,
	DUPLICATE_IN_BATCH
}
