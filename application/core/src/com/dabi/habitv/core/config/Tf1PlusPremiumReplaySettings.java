package com.dabi.habitv.core.config;

/**
 * TF1+ premium-replay credentials and tool paths from {@code configuration.xml}.
 */
public final class Tf1PlusPremiumReplaySettings {

	private final String email;

	private final String password;

	private final String devicePath;

	private final String nM3u8DlRePath;

	private final String mediaflowUrl;

	private final String mediaflowPassword;

	private final String pythonCommand;

	public Tf1PlusPremiumReplaySettings(final String email, final String password, final String devicePath,
			final String nM3u8DlRePath, final String mediaflowUrl, final String mediaflowPassword,
			final String pythonCommand) {
		this.email = email;
		this.password = password;
		this.devicePath = devicePath;
		this.nM3u8DlRePath = nM3u8DlRePath;
		this.mediaflowUrl = mediaflowUrl;
		this.mediaflowPassword = mediaflowPassword;
		this.pythonCommand = pythonCommand;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getDevicePath() {
		return devicePath;
	}

	public String getNM3u8DlRePath() {
		return nM3u8DlRePath;
	}

	public String getMediaflowUrl() {
		return mediaflowUrl;
	}

	public String getMediaflowPassword() {
		return mediaflowPassword;
	}

	public String getPythonCommand() {
		return pythonCommand;
	}
}
