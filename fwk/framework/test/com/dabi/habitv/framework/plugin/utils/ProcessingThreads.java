package com.dabi.habitv.framework.plugin.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class ProcessingThreads {

	private static boolean blocker;

	private ProcessingThreads() {

	}

	private static final List<Process> PROCESSING_LIST = Collections.synchronizedList(new LinkedList<Process>());

	public static void addProcessing(final Process process) {
		if (!blocker) {
			PROCESSING_LIST.add(process);
		}
	}

	public static void removeProcessing(final Process process) {
		if (!blocker) {
			PROCESSING_LIST.remove(process);
		}
	}

	public static void killAllProcessing() {
		blocker = true;
		Iterator<Process> it = PROCESSING_LIST.iterator();
		while (it.hasNext()) {
			it.next().destroy();
		}
	}

}