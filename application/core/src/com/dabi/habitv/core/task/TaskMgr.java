package com.dabi.habitv.core.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Generic task manager for handling task execution.
 * Manages task execution across different categories with configurable thread pools.
 * 
 * @param <T> the type of task to manage
 * @param <R> the result type of the task
 */
public class TaskMgr<T extends AbstractTask<R>, R> {

	/** Default keep alive time for threads in seconds */
	private static final int DEFAULT_KEEP_ALIVE_TIME_SEC = 10;

	/** Default category name */
	private static final String DEFAULT = "default";

	/** Map of category names to executor services */
	private final Map<String, ExecutorService> category2ExecutorService = 
			new HashMap<String, ExecutorService>();

	/** Listener for task manager events */
	private final TaskMgrListener taskMgrListener;

	/** Default pool size for thread pools */
	private final int defaultPoolSize;

	/** Map of category names to pool sizes */
	private final Map<String, Integer> category2PoolSize;

	/** Map of objects to their associated tasks */
	private final Map<Object, T> object2Task = new HashMap<>();

	/**
	 * Constructs a new TaskMgr with the specified parameters.
	 * 
	 * @param defaultPoolSize the default thread pool size
	 * @param taskMgrListener the listener for task manager events
	 * @param category2PoolSize map of category names to pool sizes
	 */
	public TaskMgr(final int defaultPoolSize, final TaskMgrListener taskMgrListener, 
			final Map<String, Integer> category2PoolSize) {
		super();
		this.defaultPoolSize = defaultPoolSize;
		this.taskMgrListener = taskMgrListener;
		this.category2PoolSize = category2PoolSize;
	}

	/**
	 * Adds a task to the default category.
	 * 
	 * @param object the object associated with the task
	 * @param task the task to add
	 */
	public void addTask(final Object object, final T task) {
		addTask(object, task, DEFAULT);
	}

	/**
	 * Adds a task to the specified category.
	 * 
	 * @param object the object associated with the task
	 * @param task the task to add
	 * @param category the category to add the task to
	 */
	public synchronized void addTask(final Object object, final T task, final String category) {
		task.adding();
		ExecutorService executorService = category2ExecutorService.get(category);
		if (executorService == null) {
			executorService = initExecutor(category);
			category2ExecutorService.put(category, executorService);
		}
		task.addedTo(category, executorService.submit(task));
		object2Task.put(object, task);
	}

	/**
	 * Initializes an executor service for the specified category.
	 * 
	 * @param category the category name
	 * @return the initialized executor service
	 */
	private ExecutorService initExecutor(final String category) {
		final int poolSize = findPoolSizeByCategory(category);
		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
				poolSize, poolSize, DEFAULT_KEEP_ALIVE_TIME_SEC, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>()) {

			@Override
			public void afterExecute(final Runnable r, final Throwable t) {
				super.afterExecute(r, t);
				if (getActiveCount() <= 1) {
					taskMgrListener.onAllTreatmentDone();
				}

				Iterator<Entry<Object, T>> it = object2Task.entrySet().iterator();
				while (it.hasNext()) {
					if (!it.next().getValue().isRunning()) {
						it.remove();
					}
				}
			}
		};
		threadPoolExecutor.allowCoreThreadTimeOut(true);
		return threadPoolExecutor;
	}

	/**
	 * Finds the pool size for the specified category.
	 * 
	 * @param category the category name
	 * @return the pool size for the category
	 */
	private int findPoolSizeByCategory(final String category) {
		Integer ret;
		if (DEFAULT.equals(category) || category2PoolSize == null 
				|| !category2PoolSize.containsKey(category)) {
			ret = defaultPoolSize;
		} else {
			ret = category2PoolSize.get(category);
		}
		return ret;
	}

	/**
	 * Shuts down all executor services with the specified timeout.
	 * 
	 * @param timeoutMs the timeout in milliseconds
	 */
	void shutdown(final int timeoutMs) {
		for (final ExecutorService executorService : category2ExecutorService.values()) {
			try {
				executorService.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
			} catch (final InterruptedException e) {
				throw new TechnicalException(e);
			}
		}
	}

	/**
	 * Shuts down all executor services immediately.
	 */
	public void shutdownNow() {
		for (final ExecutorService executorService : category2ExecutorService.values()) {
			executorService.shutdownNow();
		}
	}

	/**
	 * Cancels the task associated with the specified object.
	 * 
	 * @param object the object whose task should be cancelled
	 */
	public void cancelTask(Object object) {
		T task = object2Task.get(object);
		if (task != null) {
			task.cancel();
		}
	}
}
