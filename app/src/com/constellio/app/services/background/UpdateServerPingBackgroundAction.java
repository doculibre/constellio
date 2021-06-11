package com.constellio.app.services.background;

import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.FontAwesome;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class UpdateServerPingBackgroundAction implements Runnable {
	private String oldAlertHash;
	private String newAlertHash;
	private String expectedNewAlertHash;
	private File newAlert;
	private boolean currentVersionNotALts;
	private LocalDate currentVersionEndOfLife;

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private AppManagementService appManagementService;
	private SystemConfigurationsManager manager;
	private HashingService hashingService;
	private UserServices userServices;

	public static Set<String> newAvailableVersions = new HashSet<>();

	// TODO : TEMP USE ONLY REMOVE WHEN REFACTORING NOTIFICATIONS ALSO MAKE SURE IT IS THREAD SAFE
	public static Map<String, Notification> notifications = new HashMap<>();
	private static Set<NotificationsChangeListener> listeners = new HashSet<>();

	public UpdateServerPingBackgroundAction(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appManagementService = appLayerFactory.newApplicationService();
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
		this.hashingService = modelLayerFactory.getDataLayerFactory().getIOServicesFactory()
				.newHashingService(HashingEncoding.BASE64_URL_ENCODED);
		this.userServices = modelLayerFactory.newUserServices();

		try {
			getOldAlertHash();
		} catch (IOException | HashingServiceException e) {
			log.warn("No previous alert found", e);
		}
	}

	private void getOldAlertHash() throws IOException, HashingServiceException {
		File lastAlertFromValue = manager.getFileFromValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_ALERT, "lastAlert.pdf");

		if (null != lastAlertFromValue) {
			oldAlertHash = calculateFileHash(lastAlertFromValue);
		}
	}

	@Override
	public void run() {
		if (FoldersLocator.usingAppWrapper() && Boolean.TRUE.equals(manager.getValue(ConstellioEIMConfigs.UPDATE_SERVER_PING_ENABLED))) {
			try {
				UpdateServerPingUpdates updates = appManagementService.getUpdateServerPingUpdates();
				downloadLatestAlert(updates.getLatestAlertHash());
				downloadNewLicense(updates.getNewLicenseHash());
				notifyNewVersionsAvailable(updates.getNewVersionsAvailable());
				notifyCurrentLtsEndOfLife(updates.getCurrentLtsEndOfLife());
				notifyUpdateToALTS(updates.isCurrentVersionNotALtsButALtsExists());

				fireNotificationsAdded();
			} catch (AppManagementServiceException | AppManagementServiceRuntimeException e) {
				log.warn("Unable to receive daily ping updates", e);
			}
		}
	}

	public static void addNotification(String key, Notification notification, boolean fireNotificationAddedEvent) {
		// TODO : TEMP USE ONLY REMOVE WHEN REFACTORING NOTIFICATIONS

		notifications.put(key, notification);

		if (fireNotificationAddedEvent) {
			fireNotificationsAdded();
		}
	}

	private void downloadLatestAlert(String latestAlertHash) throws CannotConnectToServer {
		expectedNewAlertHash = latestAlertHash;
		if (expectedNewAlertHash != null && (oldAlertHash == null || !oldAlertHash.equals(expectedNewAlertHash))) {
			newAlert = appManagementService.getLastAlertFromServer();

			if (!newAlert.exists()) {
				newAlert = null;
				expectedNewAlertHash = null;
			}
		}

		if (null != newAlert) {
			try {
				newAlertHash = calculateFileHash(newAlert);
			} catch (IOException | HashingServiceException e) {
				log.error("Unable to calculate new alert hash", e);
			}

			if (!newAlertHash.equals(oldAlertHash)) {
				if (expectedNewAlertHash.equals(newAlertHash)) {
					modelLayerFactory.newUserServices().resetHasReadLastAlertMetadataOnUsers();
					copyNewAlertFileToConfigValue();
					oldAlertHash = newAlertHash;
				} else {
					log.error("Downloaded alert doesn't match expected one");
				}
				newAlertHash = null;
				expectedNewAlertHash = null;
			}
		}
	}

	private void downloadNewLicense(String expectedNewLicenseHash) throws CannotConnectToServer {
		if (StringUtils.isNotBlank(expectedNewLicenseHash)) {
			boolean newLicenseCorrectlyDownloaded = false;
			File newLicense = null;
			try {
				for (int i = 0; i < 3; i++) {
					newLicense = appManagementService.getNewLicenseFromServer();
					if (newLicense != null && newLicense.exists()) {
						try {
							if (newLicenseCorrectlyDownloaded = calculateFileHash(newLicense).equals(expectedNewLicenseHash)) {
								break;
							}
						} catch (IOException | HashingServiceException e) {
							log.error("Unable to calculate new license hash try " + i + 1 + " of 3", e);
						}

					}
				}
				if (newLicenseCorrectlyDownloaded) {
					appManagementService.storeLicense(newLicense);
					notifyNewLicenseInstalled();
				} else {
					SystemLogger.error("Client was unable to download the latest license from the server");
				}
			} finally {
				appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService().deleteQuietly(newLicense);
			}
		}
	}

	private void notifyNewLicenseInstalled() {
		userServices.resetLicenseNotificationViewDateForAllUsers();
		addNotification("Notifications.licenseInstalled", new Notification($("Notifications.licenseInstalled")) {
			@Override
			public String getIconName() {
				return FontAwesome.FILE_TEXT_O.name();
			}
		}, false);
	}

	private void notifyNewVersionsAvailable(List<String> newAvailableVersions) {
		if (newAvailableVersions != null && this.newAvailableVersions.addAll(newAvailableVersions)) {
			userServices.resetNewVersionsNotificationViewDateForAllUsers();
			if (newAvailableVersions.size() > 1) {
				addNotification("Notifications.newVersionsAvailable", new Notification($("Notifications.newVersionsAvailable", StringUtils.join(newAvailableVersions, ", "))) {
					@Override
					public String getIconName() {
						return FontAwesome.ARROW_CIRCLE_O_DOWN.name();
					}
				}, false);

			} else {
				addNotification("Notifications.newVersionAvailable", new Notification($("Notifications.newVersionAvailable", newAvailableVersions.get(0))) {
					@Override
					public String getIconName() {
						return FontAwesome.ARROW_CIRCLE_O_DOWN.name();
					}
				}, false);
			}
		}
	}

	private void notifyCurrentLtsEndOfLife(LocalDate eol) {
		if (eol != null && (currentVersionEndOfLife == null || !eol.isEqual(currentVersionEndOfLife))) {
			currentVersionEndOfLife = eol;
			userServices.resetLtsEndOfLifeNotificationViewDateForAllUsers();
			if (TimeProvider.getLocalDate().isBefore(eol)) {
				addNotification("Notifications.currentLtsEndsOn", new Notification($("Notifications.currentLtsEndsOn", DateFormatUtils.format(eol))) {
					@Override
					public String getIconName() {
						return FontAwesome.CALENDAR_MINUS_O.name();
					}
				}, false);
			} else {
				addNotification("Notifications.currentLtsIsExpired", new Notification($("Notifications.currentLtsIsExpired")) {
					@Override
					public String getIconName() {
						return FontAwesome.CALENDAR_TIMES_O.name();
					}
				}, false);
			}
		}
	}

	private void notifyUpdateToALTS(boolean notALTS) {
		if (!currentVersionNotALts && notALTS) {
			currentVersionNotALts = true;
			userServices.resetNotALtsNotificationViewDateForAllUsers();

			addNotification("Notifications.currentVersionNotALtsButALtsExists", new Notification($("Notifications.currentVersionNotALtsButALtsExists")) {
				@Override
				public String getIconName() {
					return FontAwesome.TIMES_CIRCLE_O.name();
				}
			}, false);
		} else if (!notALTS && currentVersionNotALts) {
			currentVersionNotALts = false;
		}
	}

	private String calculateFileHash(File fileToHash) throws IOException, HashingServiceException {
		return hashingService.getHashFromFile(fileToHash);
	}

	private void copyNewAlertFileToConfigValue() {
		StreamFactory<InputStream> streamFactory = name -> new FileInputStream(newAlert.getPath());

		manager.setValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_ALERT, streamFactory);
		newAlert = null;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UpdateServerPingUpdates {
		String latestAlertHash;
		String newLicenseHash;
		List<String> newVersionsAvailable;
		LocalDate currentLtsEndOfLife;
		boolean currentVersionNotALtsButALtsExists;
	}

	// TODO : TEMP USE ONLY REMOVE WHEN REFACTORING NOTIFICATIONS
	public static void subscribeToNotifications(NotificationsChangeListener listener) {
		listeners.add(listener);
	}

	// TODO : TEMP USE ONLY REMOVE WHEN REFACTORING NOTIFICATIONS
	public static void unsubscribeFromNotifications(NotificationsChangeListener listener) {
		listeners.remove(listener);
	}

	// TODO : TEMP USE ONLY REMOVE WHEN REFACTORING NOTIFICATIONS
	private static void fireNotificationsAdded() {
		listeners.forEach(NotificationsChangeListener::notificationsAdded);
	}

	public interface NotificationsChangeListener extends Serializable {

		void notificationsAdded();

		void notificationsRemoved();

		void clearNotifications();
	}

	public static class Notification implements Serializable {
		private final String caption;

		public Notification(String caption) {
			this.caption = caption;
		}

		public String getCaption() {
			return caption != null ? caption : "";
		}

		public String getIconName() {
			return null;
		}

		public final boolean hasActionToExecuteOnClick() {
			return getActionToExecuteOnClick() != null;
		}

		public Runnable getActionToExecuteOnClick() {
			return null;
		}
	}
}
