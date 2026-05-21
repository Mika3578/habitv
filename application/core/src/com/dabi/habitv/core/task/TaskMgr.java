package com.dabi.habitv.core.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class TaskMgr<T extends AbstractTask<R>, R> {

	private static final Logger LOG = Logger.getLogger(TaskMgr.class);

	private static final int DEFAULT_KEEP_ALIVE_TIME_SEC = 10;

	private static final String DEFAULT = "default";

	private final Map<String, ExecutorService> category2ExecutorService = new HashMap<String, ExecutorService>();

	private final TaskMgrListener taskMgrListener;

	private final int defaultPoolSize;

	private final Map<String, Integer> category2PoolSize;

	private final Map<Object, T> object2Task = new HashMap<>();

	public TaskMgr(final int defaultPoolSize, final TaskMgrListener taskMgrListener, final Map<String, Integer> category2PoolSize) {
		super();
		this.defaultPoolSize = defaultPoolSize;
		this.taskMgrListener = taskMgrListener;
		this.category2PoolSize = category2PoolSize;
	}

	public void addTask(final Object object, final T task) {
		addTask(object, task, DEFAULT);
	}

	public synchronized void addTask(final Object object, final T task, final String category) {
		task.adding();
		ExecutorService executorService = category2ExecutorService.get(category);
		if (executorService == null) {
			executorService = initExecutor(category);
			category2ExecutorService.put(category, executorService);
		}
		final ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
		if (pool.getActiveCount() >= pool.getMaximumPoolSize()) {
			LOG.info("Queue waiting for " + task + " because concurrency limit "
					+ pool.getMaximumPoolSize() + " was reached on pool "
					+ category);
		}
		task.addedTo(category, executorService.submit(task));
		object2Task.put(object, task);
	}

	public synchronized boolean hasQueuedOrActiveTask(final Object object) {
		final T task = object2Task.get(object);
		return task != null && task.isQueuedOrActive();
	}

	public synchronized int getActiveTaskCount() {
		int active = 0;
		for (final T task : object2Task.values()) {
			if (task.isRunning()) {
				active++;
			}
		}
		return active;
	}

	public synchronized int getQueuedOrActiveTaskCount() {
		return object2Task.size();
	}

	private ExecutorService initExecutor(final String category) {
		final int poolSize = findPoolSizeByCategory(category);
		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize, DEFAULT_KEEP_ALIVE_TIME_SEC, TimeUnit.SECONDS,
		        new LinkedBlockingQueue<Runnable>()) {

			@Override
			public void afterExecute(final Runnable r, final Throwable t) {
				super.afterExecute(r, t);
				if (getActiveCount() <= 1) {
					taskMgrListener.onAllTreatmentDone();
				}

				synchronized (TaskMgr.this) {
					Iterator<Entry<Object, T>> it = object2Task.entrySet().iterator();
					while (it.hasNext()) {
						if (it.next().getValue().isFinished()) {
							it.remove();
						}
					}
				}
			}
		};
		threadPoolExecutor.allowCoreThreadTimeOut(true);
		return threadPoolExecutor;
	}

	private int findPoolSizeByCategory(final String category) {
		Integer ret;
		if (DEFAULT.equals(category) || category2PoolSize == null || !category2PoolSize.containsKey(category)) {
			ret = defaultPoolSize;
		} else {
			ret = category2PoolSize.get(category);
		}
		return ret;
	}

	void shutdown(final int timeoutMs) {
		for (final ExecutorService executorService : category2ExecutorService.values()) {
			try {
				executorService.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
			} catch (final InterruptedException e) {
				throw new TechnicalException(e);
			}
		}
	}

	public void shutdownNow() {
		for (final ExecutorService executorService : category2ExecutorService.values()) {
			executorService.shutdownNow();
		}
	}

	public void cancelTask(Object object) {
		T task = object2Task.get(object);
		if (task != null) {
			task.cancel();
		}

	}
}
