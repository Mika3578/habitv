package com.dabi.habitv.tray.view;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent.UpdatablePluginStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.framework.plugin.utils.DownloadFailureDiagnostics;
import com.dabi.habitv.tray.controller.ViewController;
import com.dabi.habitv.tray.subscriber.CoreSubscriber;

public final class HabiTvTrayView implements CoreSubscriber {

	private static final Logger LOG = Logger.getLogger(HabiTvTrayView.class);

	private final ViewController controller;

	private final TrayIcon trayIcon;

	private final Image fixImage;

	private final Image animatedImage;

	private boolean retreiveInProgress = false;

	private boolean checkInProgress = false;

	private boolean updateInProgress = false;

	private EventHandler<WindowEvent> closingMainViewHandler = new EventHandler<WindowEvent>() {

		@Override
		public void handle(WindowEvent event) {
			if (firstClose) {
				trayIcon.displayMessage("habiTv", "habiTv est encore en cours d'exécution.", TrayIcon.MessageType.INFO);
				firstClose = false;
			}
		}
	};

	private boolean firstClose = true;

	public HabiTvTrayView(final ViewController controller, Stage primaryStage) {
		this.controller = controller;
		primaryStage.setOnHidden(closingMainViewHandler);
		primaryStage.setOnCloseRequest(closingMainViewHandler);
		fixImage = getImage("fixe.gif"); //$NON-NLS-1$
		animatedImage = getImage("anim.gif"); //$NON-NLS-1$
		trayIcon = new TrayIcon(fixImage, Messages.getString("HabiTvTrayView.2")); //$NON-NLS-1$
	}

	private Image getImage(final String image) {
		return Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(image));
	}

	public void init() throws AWTException {

		final SystemTray tray = SystemTray.getSystemTray();
		trayIcon.setPopupMenu(new TrayMenu(controller, closingMainViewHandler));

		final MouseListener mouseListener = new MouseListener() {

			@Override
			public void mouseReleased(final MouseEvent mouseEvent) {
				// nothing
			}

			@Override
			public void mousePressed(final MouseEvent mouseEvent) {
				// nothing
			}

			@Override
			public void mouseExited(final MouseEvent mouseEvent) {
				// nothing
			}

			@Override
			public void mouseEntered(final MouseEvent mouseEvent) {
				// nothing
			}

			@Override
			public void mouseClicked(final MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 2) {
					controller.openMainView(closingMainViewHandler);
				}
				// } else {
				// if (!controller.getManager().getProgressionModel()
				// .getEpisodeName2ActionProgress().isEmpty()) {
				// trayIcon.displayMessage(
				//								Messages.getString("HabiTvTrayView.3"), progressionToText(controller.getManager().getProgressionModel().getEpisodeName2ActionProgress()), //$NON-NLS-1$
				// TrayIcon.MessageType.INFO);
				// }
				// }
			}
		};
		trayIcon.addMouseListener(mouseListener);

		tray.add(trayIcon);
	}

	@Override
	public void update(final SearchEvent event) {
		switch (event.getState()) {
		case ALL_RETREIVE_DONE:
			retreiveInProgress = false;
			changeAnimation();
			break;
		case ALL_SEARCH_DONE:
			checkInProgress = false;
			changeAnimation();
			break;
		case BUILD_INDEX:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.12"), Messages.getString("HabiTvTrayView.13") + event.getChannel() + " " + event.getCategory(), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case CHECKING_EPISODES:
			checkInProgress = true;
			changeAnimation();
			break;
		case RESUME_EXPORT:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.15"), Messages.getString("HabiTvTrayView.16"), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$
			retreiveInProgress = true;
			changeAnimation();
			break;
		case DONE:

			break;
		case ERROR:
			checkInProgress = false;
			changeAnimation();
			LOG.error("", event.getException());
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.17"), Messages.getString("HabiTvTrayView.18") + " " + event.getChannel() + " : " + event.getException().getMessage(), TrayIcon.MessageType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case IDLE:

			break;
		default:
			break;
		}
	}

	private void changeAnimation() {
		if (retreiveInProgress || checkInProgress || updateInProgress) {
			trayIcon.setImage(animatedImage);
		} else {
			trayIcon.setImage(fixImage);
		}
	}

	@Override
	public void update(final RetreiveEvent event) {
		switch (event.getState()) {
		case BUILD_INDEX:

			break;
		case DOWNLOAD_FAILED:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.19"),
					Messages.getString("HabiTvTrayView.20") + event.getEpisode().getCategory() + " "
							+ event.getEpisode().getName() + " "
							+ buildLocalizedDownloadFailureDetail(event),
					TrayIcon.MessageType.WARNING);
			break;
		case DOWNLOADED:

			break;
		case DOWNLOAD_STARTING:
			break;
		case EXPORT_FAILED:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.22"), Messages.getString("HabiTvTrayView.23") + event.getEpisode().getCategory() + " " + event.getEpisode().getName() + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							+ event.getException().getMessage(), TrayIcon.MessageType.WARNING);
			break;
		case EXPORT_STARTING:

			break;
		case FAILED:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.26"), Messages.getString("HabiTvTrayView.27") + event.getException().getMessage() + Messages.getString("HabiTvTrayView.28") + event.getEpisode(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					TrayIcon.MessageType.WARNING);
			break;
		case READY:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.29"), Messages.getString("HabiTvTrayView.30") + event.getEpisode().getCategory() + " " + event.getEpisode().getName(), //$NON-NLS-1$ //$NON-NLS-2$
					TrayIcon.MessageType.INFO);
			break;
		case TO_DOWNLOAD:
			retreiveInProgress = true;
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.32"), Messages.getString("HabiTvTrayView.33") + event.getEpisode().getCategory() + " " + event.getEpisode().getName(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					TrayIcon.MessageType.INFO);
			break;
		case TO_EXPORT:

			break;
		case STOPPED:

			break;
		default:
			break;
		}
		changeAnimation();
	}

	@Override
	public void update(final SearchCategoryEvent event) {
		switch (event.getState()) {
		case BUILDING_CATEGORIES:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.35"), Messages.getString("HabiTvTrayView.36") + event.getPlugin(), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$
			trayIcon.setImage(animatedImage);
			break;
		case DONE:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.37"), Messages.getString("HabiTvTrayView.38") + event.getInfo(), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$
			changeAnimation();
			break;
		case ERROR:

			break;
		case IDLE:

			break;
		default:
			break;
		}
	}

	@Override
	public void update(final UpdatePluginEvent event) {
		switch (event.getState()) {
		case STARTING_ALL:
			updateInProgress = true;
			changeAnimation();
			break;
		case CHECKING:
			break;
		case DOWNLOADING:
			break;
		case ERROR:
			break;
		case DONE:
			updateInProgress = false;
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.maj"), Messages.getString("HabiTvTrayView.majpluginfini", event.getPlugin(), event.getVersion()), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case ALL_DONE:
			changeAnimation();
			break;
		default:
			break;
		}
	}

	@Override
	public void update(UpdatablePluginEvent event) {
		UpdatablePluginStateEnum state = event.getState();
		switch (state) {
		case CHECKING:

			break;
		case DOWNLOADING:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.maj"), Messages.getString("HabiTvTrayView.automajplugin", event.getPlugin(), event.getVersion()), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			break;
		case ERROR:

			break;
		case DONE:
			trayIcon.displayMessage(
					Messages.getString("HabiTvTrayView.maj"), Messages.getString("HabiTvTrayView.automajpluginfini", event.getPlugin(), event.getVersion()), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			break;
		default:
			break;
		}
	}

	private static String buildLocalizedDownloadFailureDetail(final RetreiveEvent event) {
		if (event.getException() == null) {
			return Messages.getString("HabiTvTrayView.downloadFailure.generic");
		}
		final String classificationKey = DownloadFailureDiagnostics.getClassificationKey(event.getException());
		if (classificationKey != null) {
			final String localized = Messages.getString("HabiTvTrayView.downloadFailure." + classificationKey);
			if (!localized.startsWith("!")) {
				return localized;
			}
		}
		return Messages.getString("HabiTvTrayView.downloadFailure.generic");
	}

}
