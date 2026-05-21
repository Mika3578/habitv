package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class TaskMgrTest {

	private TaskMgr<AbstractTask<Object>, Object> taskMgr;

	private int test1 = 0;

	private int test2 = 0;

	private boolean allTreatmentDone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private void buildSimultaneousTask(final int taskNb, final String cat, final String cat2, final boolean shutdown) {
		final Map<String, Integer> poolsSize = new HashMap<String, Integer>();
		poolsSize.put(cat, 1);
		if (cat != null && !cat.equals(cat2)) {
			poolsSize.put(cat2, 2);
		}
		taskMgr = new TaskMgr<AbstractTask<Object>, Object>(taskNb, new TaskMgrListener() {

			@Override
			public void onAllTreatmentDone() {
				allTreatmentDone = true;
			}

			@Override
			public void onFailed(final Throwable throwable) {
				allTreatmentDone = false;
			}
		}, poolsSize) {
		};
		AbstractTask<Object> task = new AbstractTaskForTest() {

			@Override
			protected Object doCall() {
				test2 = 0;
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					fail();
				}
				if (test2 == 0) {
					test1++;
				} else {
					test1 = -1;
				}
				return null;
			}

			@Override
			protected void failed(final Throwable e) {
				taskMgr.shutdownNow();
				throw new TechnicalException(e);
			}

			@Override
			public String toString() {
				return "task1";
			}

			@Override
			protected void canceled() {
				
			}

		};
		if (cat == null) {
			taskMgr.addTask(task, task);
		} else {
			taskMgr.addTask(task, task, cat);
		}
		task = new AbstractTaskForTest() {

			@Override
			protected Object doCall() {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					fail();
				}
				test2++;
				return null;
			}

			@Override
			protected void failed(final Throwable e) {
				taskMgr.shutdownNow();
				throw new TechnicalException(e);
			}

			@Override
			public String toString() {
				return "task2";
			}

		};
		if (cat2 == null) {
			taskMgr.addTask(task, task);
		} else {
			taskMgr.addTask(task, task, cat2);
		}
		if (shutdown) {
			taskMgr.shutdown(1000);
		}
	}

	@Test
	public final void canRunSimultaneusAsyncTask() {
		buildSimultaneousTask(2, null, null, true);
		assertEquals(1, test2);
		assertEquals(-1, test1);
	}

	@Test
	public final void canRunOnly1TaskSimulta() {
		buildSimultaneousTask(1, null, null, true);
		assertEquals(1, test2);
		assertEquals(1, test1);
	}

	@Test
	public final void canRunOnly1TaskSimultaOnSameCategort() {
		buildSimultaneousTask(1, "cat", "cat", true);
		assertEquals(1, test2);
		assertEquals(1, test1);
	}

	@Test
	public final void canRunSimultaneusAsyncTaskOnDifferentThreadPoolExecutor() {
		// 2 differents category for 2 PoolExecutor
		buildSimultaneousTask(1, "1", "2", true);
		assertEquals(1, test2);
		assertEquals(-1, test1);
	}

	private void buildSimultaneousTaskWithError(final int taskNb, final String cat, final String cat2) {
		taskMgr = new TaskMgr<AbstractTask<Object>, Object>(taskNb, new TaskMgrListener() {

			@Override
			public void onAllTreatmentDone() {
				allTreatmentDone = true;
			}

			@Override
			public void onFailed(final Throwable throwable) {
				allTreatmentDone = false;
			}

		}, null) {
		};
		AbstractTask<Object> task = new AbstractTaskForTest() {

			@Override
			protected Object doCall() {
				throw new TechnicalException("error");
			}

			@Override
			protected void failed(final Throwable e) {
				taskMgr.shutdownNow();
				throw new TechnicalException(e);
			}

			@Override
			public String toString() {
				return "task1";
			}

		};
		if (cat == null) {
			taskMgr.addTask(task, task);
		} else {
			taskMgr.addTask(task, task, cat);
		}
		task = new AbstractTaskForTest() {

			@Override
			protected Object doCall() {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					fail();
				}
				test2++;
				return null;
			}

			@Override
			protected void failed(final Throwable e) {
				taskMgr.shutdownNow();
				throw new TechnicalException(e);
			}

			@Override
			public String toString() {
				return "task2";
			}

		};
		if (cat2 == null) {
			taskMgr.addTask(task, task);
		} else {
			taskMgr.addTask(task, task, cat2);
		}
		taskMgr.shutdown(1000);
	}

	@Test
	public final void stopOnTechnicalError() {
		buildSimultaneousTaskWithError(2, null, null);
		assertEquals(0, test2);
		assertEquals(0, test1);
	}

	@Test
	public final void enforcesMaxConcurrentDownloadsOfOne() {
		taskMgr = new TaskMgr<AbstractTask<Object>, Object>(1, new TaskMgrListener() {

			@Override
			public void onAllTreatmentDone() {
				allTreatmentDone = true;
			}

			@Override
			public void onFailed(final Throwable throwable) {
				allTreatmentDone = false;
			}
		}, null);
		taskMgr.addTask(buildSleepTask("t1", 500), buildSleepTask("t1", 500));
		taskMgr.addTask(buildSleepTask("t2", 500), buildSleepTask("t2", 500));
		try {
			Thread.sleep(50);
		} catch (final InterruptedException e) {
			fail();
		}
		assertEquals(1, taskMgr.getActiveTaskCount());
		assertTrue(taskMgr.getQueuedOrActiveTaskCount() >= 2);
		taskMgr.shutdown(2000);
	}

	@Test
	public final void enforcesMaxConcurrentDownloadsOfTwo() {
		taskMgr = new TaskMgr<AbstractTask<Object>, Object>(2, new TaskMgrListener() {

			@Override
			public void onAllTreatmentDone() {
				allTreatmentDone = true;
			}

			@Override
			public void onFailed(final Throwable throwable) {
				allTreatmentDone = false;
			}
		}, null);
		for (int i = 0; i < 3; i++) {
			final AbstractTask<Object> task = buildSleepTask("task" + i, 400);
			taskMgr.addTask(task, task);
		}
		try {
			Thread.sleep(50);
		} catch (final InterruptedException e) {
			fail();
		}
		assertTrue(taskMgr.getQueuedOrActiveTaskCount() >= 2);
		taskMgr.shutdown(2000);
	}

	private AbstractTask<Object> buildSleepTask(final String name, final long sleepMs) {
		return new AbstractTaskForTest() {

			@Override
			protected Object doCall() {
				try {
					Thread.sleep(sleepMs);
				} catch (final InterruptedException e) {
					fail();
				}
				return null;
			}

			@Override
			protected void failed(final Throwable e) {
				throw new TechnicalException(e);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	@Test
	public final void indicateWhenAllTreatmentAreDone() {
		buildSimultaneousTask(2, null, null, false);
		assertFalse(allTreatmentDone);
		assertTrue(waitForAllTreatmentDone(3000));
		taskMgr.shutdown(0);
	}

	private boolean waitForAllTreatmentDone(final long timeoutMs) {
		final long deadline = System.currentTimeMillis() + timeoutMs;
		while (!allTreatmentDone && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(25);
			} catch (final InterruptedException e) {
				fail();
			}
		}
		return allTreatmentDone;
	}
}
