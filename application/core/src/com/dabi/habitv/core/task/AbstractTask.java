package com.dabi.habitv.core.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

abstract class AbstractTask<R> implements Callable<R> {

	protected static final Logger LOG = Logger.getLogger(AbstractTask.class);

	private String category = null;

	private TaskListener listener;

	private Future<R> future;

	private boolean running = false;

	private boolean canceled = false;

	@Override
	public final R call() { // NO_UCD (test only)
		if (!canceled) {
			R result;
			running = true;
			try {
				started();
				Thread.currentThread().setName(toString());
				result = doCall();
				if (listener != null) {
					listener.onTaskEnded();
				}
				ended();
			} catch (final Throwable e) {
				if (listener != null) {
					listener.onTaskFailed();
				}
				failed(e);
				throw new TaskFailedException(e);
			} finally {
				running = false;
				if (canceled){
					canceled();
				}
			}
			return result;
		} else {
			canceled();
			return null;
		}
	}

	protected abstract void adding();// appelé avant le démarrage du thread

	protected abstract void failed(Throwable e);

	protected abstract void ended();
	
	protected abstract void canceled();

	protected abstract void started();

	protected abstract R doCall() throws Exception;

	final void addedTo(final String category, final Future<R> future) {
		this.category = category;
		this.future = future;
	}

	void waitEndOfTreatment() {
		getResult();
	}

	public R getResult() {
		try {
			return future == null ? null : future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new TechnicalException(e);
		}
	}

	public final String getCategory() {
		return category;
	}

	public void setListener(final TaskListener listener) {
		this.listener = listener;
	}

	@Override
	public abstract String toString();

	public boolean isRunning() {
		return running;
	}

	public boolean isFinished() {
		return future != null && future.isDone();
	}

	public boolean isQueuedOrActive() {
		return future != null && !future.isDone();
	}

	public void cancel() {
		canceled = true;
		if (future != null) {
			future.cancel(true);
		}
	}

}
