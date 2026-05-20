package com.dabi.habitv.framework.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorStoppedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.exception.HungProcessException;

public class CmdExecutor implements ProcessHolder {

	private static final Logger LOG = Logger.getLogger(CmdExecutor.class);

	protected static final int PERCENTAGE = 100;

	private String fullOutput = null;

	private final String cmdProcessor;

	private static final String CMD_TOKEN = "#CMD#";

	private final long maxHungTime;

	private boolean hungThread;

	private boolean ended = false;

	private Process process;
	private boolean stopped = false;
	private final String cmd;
	private String lastOutputLine = null;
	private String progression;

	private Thread killThread;

	public CmdExecutor(final String cmdProcessor, final String cmd, final long maxHungTime) {
		super();
		this.cmd = cmd;
		this.cmdProcessor = cmdProcessor;
		this.maxHungTime = maxHungTime;
		this.hungThread = false;
	}

	@Override
	public void stop() {
		stopped = true;
		ended = true;
		if (process != null) {
			process.destroy();
			ProcessingThreads.removeProcessing(process);
		}
	}

	public String getProgression() {
		return progression;
	}

	public void start() {
		init();
		final StringBuffer fullOutput = new StringBuffer();

		try {
			process = buildProcess();

			ProcessingThreads.addProcessing(process);
			// consume standard output in a thread
			final Thread outputThread = treatCmdOutput(process.getInputStream(), fullOutput);
			outputThread.start();

			// consume error output in a thread
			final Thread errorThread = treatCmdOutput(process.getErrorStream(), fullOutput);
			errorThread.start();

			if (getHungProcessTime() != -1) {
				killThread = buildKillerThread(fullOutput, process);
				killThread.start();
			}
			// wait for both thread
			errorThread.join();
			outputThread.join();
			ended = true;
			if (hungThread) {
				process.destroy();
				throw new HungProcessException(cmd, fullOutput.toString(), lastOutputLine, maxHungTime);
			} else {
				process.waitFor();
			}
		} catch (final InterruptedException e) {
			throw new ExecutorFailedException(cmd, fullOutput.toString(), lastOutputLine, e);
		} finally {
			if (process != null) {
				ProcessingThreads.removeProcessing(process);
			}
		}

		if (stopped) {
			throw new ExecutorStoppedException(cmd);
		}

		if (process.exitValue() != 0 || (getLastOutputLine() != null && !isSuccess(fullOutput.toString()))) {
			throw buildFailureException(cmd, fullOutput.toString(), lastOutputLine, null);
		}
		this.fullOutput = fullOutput.toString();
	}

	private void init() {
		fullOutput = null;
		hungThread = false;
		ended = false;
		process = null;
		stopped = false;
		lastOutputLine = null;
		progression = null;

	}

	private Thread buildKillerThread(final StringBuffer fullOutput, final Process process) {
		final Thread tread = new Thread() {
			@Override
			public void run() {
				setName("KillerThread" + cmd);

				String oldOutPut = fullOutput.toString();
				String newOutPut = fullOutput.toString();
				while (!hungThread && !ended) {
					try {
						Thread.sleep(getHungProcessTime());
					} catch (final InterruptedException e) {
						throw new TechnicalException(e);
					}
					newOutPut = fullOutput.toString();
					if (oldOutPut != null && oldOutPut.equals(newOutPut)) {
						hungThread = true;
						process.destroy();
					} else {
						oldOutPut = fullOutput.toString();
					}
				}
			}
		};
		return tread;
	}

	protected long getHungProcessTime() {
		return -1;
	}

	protected Map<String, String> getProcessEnvironment() {
		return null;
	}

	protected ExecutorFailedException buildFailureException(final String failedCmd, final String fullOutput,
			final String lastLine, final Throwable cause) {
		return new ExecutorFailedException(failedCmd, fullOutput, lastLine, cause);
	}

	protected Process buildProcess() throws ExecutorFailedException {
		try {
			final Map<String, String> envOverrides = getProcessEnvironment();
			final boolean hasEnvOverrides = envOverrides != null && !envOverrides.isEmpty();
			if (cmdProcessor == null || cmdProcessor.isEmpty()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("cmd : " + cmd);
				}
				if (!hasEnvOverrides) {
					return Runtime.getRuntime().exec(cmd);
				}
				final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
				applyEnvironmentOverrides(processBuilder, envOverrides);
				return processBuilder.start();
			} else {
				final String[] cmdArgs = cmdProcessor.split(" ");
				for (int i = 0; i < cmdArgs.length; i++) {
					if (cmdArgs[i].contains(CMD_TOKEN)) {
						cmdArgs[i] = cmdArgs[i].replace(CMD_TOKEN, cmd);
					}
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("cmd : " + cmdArgs);
				}
				if (!hasEnvOverrides) {
					return Runtime.getRuntime().exec(cmdArgs);
				}
				final ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
				applyEnvironmentOverrides(processBuilder, envOverrides);
				return processBuilder.start();
			}
		} catch (final IOException e) {
			throw new ExecutorFailedException(cmd, e.getMessage(), e.getMessage(), e);
		}
	}

	private static void applyEnvironmentOverrides(final ProcessBuilder processBuilder,
			final Map<String, String> envOverrides) {
		final Map<String, String> environment = processBuilder.environment();
		for (final Map.Entry<String, String> entry : envOverrides.entrySet()) {
			environment.put(entry.getKey(), entry.getValue());
		}
	}

	private Thread treatCmdOutput(final InputStream inputStream, final StringBuffer fullOutput) {
		final Thread tread = new Thread() {
			@Override
			public void run() {
				setName("ThreadOut" + cmd);
				try {
					final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line = "";
					String lastHandledLine;
					try {
						long lastTime = 0;
						while ((line = reader.readLine()) != null && !hungThread) {
							fullOutput.append(line);
							fullOutput.append("\n");
							lastOutputLine = line;
							lastHandledLine = progression;
							String newProgression = handleProgression(line);
							if (newProgression != null) {
								progression = newProgression;
								LOG.debug(line);
								final long now = System.currentTimeMillis();
								if (progression != null && (now - lastTime) > FrameworkConf.TIME_BETWEEN_LOG) {
									hungThread = isHungProcess(lastHandledLine, progression, now, lastTime, maxHungTime);
									lastTime = now;
								}
							}
						}
					} finally {
						reader.close();
					}
				} catch (final IOException ioe) {
					throw new TechnicalException(ioe);
				}
			}
		};
		return tread;
	}

	private boolean isHungProcess(final String lastHandledLine, final String currentHandledLine, final long now, final long lastTime,
	        final long maxHungTime) {
		LOG.debug("lastHandledLine" + lastHandledLine);
		LOG.debug("currentHandledLine" + currentHandledLine);
		LOG.debug("now" + now);
		LOG.debug("lastTime" + lastTime);
		LOG.debug("maxHungTime" + maxHungTime);
		return lastHandledLine != null && currentHandledLine != null && (currentHandledLine.equals(lastHandledLine))
		        && (now - lastTime) > maxHungTime;
	}

	protected String getLastOutputLine() {
		return lastOutputLine;
	}

	protected boolean isSuccess(final String fullOutput) {
		return true;
	}

	protected String handleProgression(final String line) {
		return null;
	}

	public String getFullOutput() {
		return fullOutput;
	}

}
