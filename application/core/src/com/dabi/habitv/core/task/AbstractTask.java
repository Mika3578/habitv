package com.dabi.habitv.core.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Abstract base class for all tasks.
 * Provides common functionality for task execution and lifecycle management.
 * 
 * @param <R> the result type of the task
 */
abstract class AbstractTask<R> implements Callable<R> {

    /** Logger for this class. */
    protected static final Logger LOG = Logger.getLogger(AbstractTask.class);

    /** The category this task belongs to. */
    private String category = null;

    /** Listener for task events. */
    private TaskListener listener;

    /** Future representing the task execution. */
    private Future<R> future;

    /** Whether the task is currently running. */
    private boolean running = false;

    /** Whether the task has been canceled. */
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
                if (canceled) {
                    canceled();
                }
            }
            return result;
        } else {
            canceled();
            return null;
        }
    }

    /**
     * Called before the task starts execution.
     */
    protected abstract void adding();

    /**
     * Called when the task fails.
     * 
     * @param e the exception that caused the failure
     */
    protected abstract void failed(Throwable e);

    /**
     * Called when the task ends successfully.
     */
    protected abstract void ended();

    /**
     * Called when the task is canceled.
     */
    protected abstract void canceled();

    /**
     * Called when the task starts execution.
     */
    protected abstract void started();

    /**
     * Performs the actual task execution.
     * 
     * @return the result of the task
     * @throws Exception if the task execution fails
     */
    protected abstract R doCall() throws Exception;

    /**
     * Sets the category and future for this task.
     * 
     * @param taskCategory the category this task belongs to
     * @param taskFuture the future representing this task
     */
    final void addedTo(final String taskCategory, final Future<R> taskFuture) {
        this.category = taskCategory;
        this.future = taskFuture;
    }

    /**
     * Waits for the task to complete.
     */
    void waitEndOfTreatment() {
        getResult();
    }

    /**
     * Gets the result of the task execution.
     * 
     * @return the task result, or null if not available
     */
    public R getResult() {
        try {
            return future == null ? null : future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new TechnicalException(e);
        }
    }

    /**
     * Gets the category this task belongs to.
     * 
     * @return the task category
     */
    public final String getCategory() {
        return category;
    }

    /**
     * Sets the listener for task events.
     * 
     * @param taskListener the listener to set
     */
    public void setListener(final TaskListener taskListener) {
        this.listener = taskListener;
    }

    @Override
    public abstract String toString();

    /**
     * Checks if the task is currently running.
     * 
     * @return true if the task is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Cancels the task execution.
     */
    public void cancel() {
        canceled = true;
        if (future != null) {
            future.cancel(true);
        }
    }
} 