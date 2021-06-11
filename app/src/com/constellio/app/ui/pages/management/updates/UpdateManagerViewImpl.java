package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.entities.system.SystemMemory;
import com.constellio.app.entities.system.SystemMemory.MemoryDetails;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LocalDateLabel;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.constellio.app.entities.system.SystemMemory.fetchSystemMemoryInfo;
import static com.constellio.app.ui.i18n.i18n.$;

public class UpdateManagerViewImpl extends BaseViewImpl implements UpdateManagerView, DropHandler {

	private UpdateManagerPresenter presenter;

	private UploadWaitWindow uploadWaitWindow;
	private VerticalLayout layout;
	private Component panel;
	private Button license;
	private Button standardUpdate;
	private Button alternateUpdate;
	private WindowButton lastAlert;
	private WindowButton myLicenseInfo;
	private ConfirmDialogButton reindexButton;
	private ConfirmDialogButton rebuildCacheButton;

	private ProgressBar downloadProgressBar;
	private ViewChangeListener viewChangeListener;

	public UpdateManagerViewImpl() {
		presenter = new UpdateManagerPresenter(this);
		downloadProgressBar = new ProgressBar(0.0f);
		downloadProgressBar.setVisible(false);
	}

	@Override
	protected String getTitle() {
		return $("UpdateManagerViewImpl.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		ConfirmDialogButton restart;

		if (presenter.isCurrentlyReindexingOnThisServer()) {
			restart = new ConfirmDialogButton($("UpdateManagerViewImpl.restartAndCancelReindexingButton")) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("UpdateManagerViewImpl.restartAndCancelReindexingWarning");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.cancelReindexing();
				}
			};
		} else {
			restart = new ConfirmDialogButton($("UpdateManagerViewImpl.restartButton")) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("UpdateManagerViewImpl.restartwarning");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.restart();
				}
			};
		}
		restart.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		buttons.add(restart);

		reindexButton = new ConfirmDialogButton($("UpdateManagerViewImpl.restartAndReindexButton")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("UpdateManagerViewImpl.reindexwarning");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.restartAndReindex(false);
			}
		};

		rebuildCacheButton = new ConfirmDialogButton($("UpdateManagerViewImpl.restartAndRebuildCacheButton")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("UpdateManagerViewImpl.rebuildCacheWarning");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.restartAndRebuildCache();
			}
		};

		reindexButton.setId(UUID.randomUUID().toString());
		reindexButton.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		reindexButton.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		buttons.add(reindexButton);

		rebuildCacheButton.setId(UUID.randomUUID().toString());
		rebuildCacheButton.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		rebuildCacheButton.setEnabled(presenter.isRestartWithCacheRebuildEnabled());
		buttons.add(rebuildCacheButton);

		standardUpdate = new Button($("UpdateManagerViewImpl.automatic"));
		standardUpdate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.standardUpdateRequested();
			}
		});
		standardUpdate.setId(UUID.randomUUID().toString());
		standardUpdate.setEnabled(false);

		if (presenter.hasUpdatePermission()) {
			buttons.add(standardUpdate);
		}

		alternateUpdate = new Button($("UpdateManagerViewImpl." + presenter.getAlternateUpdateName()));
		alternateUpdate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.alternateUpdateRequested();
			}
		});
		alternateUpdate.setId(UUID.randomUUID().toString());
		alternateUpdate.setVisible(presenter.isAlternateUpdateAvailable());
		if (presenter.hasUpdatePermission()) {
			buttons.add(alternateUpdate);
		}
		alternateUpdate.setEnabled(presenter.isUpdateEnabled());

		license = new Button($("UpdateManagerViewImpl.licenseButton"));
		license.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.licenseUpdateRequested();
			}
		});
		license.setId(UUID.randomUUID().toString());
		if (presenter.hasUpdatePermission()) {
			buttons.add(license);
		}


		lastAlert = new WindowButton($("UpdateManagerViewImpl.printLastAlertShort"), $("UpdateManagerViewImpl.printLastAlertLong"),
				WindowConfiguration.modalDialog("75%", "90%")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.addStyleName("no-scroll");
				layout.setSizeFull();

				File lastAlert = presenter.getLastAlert();

				DownloadLink downloadLink = new DownloadLink(new FileResource(lastAlert),
						$("UpdateManagerViewImpl.download") + " " + lastAlert.getName());
				DocumentViewer viewer = new DocumentViewer(lastAlert);
				viewer.setSizeFull();

				layout.addComponents(downloadLink, viewer);
				layout.setExpandRatio(viewer, 1);

				return layout;
			}
		};
		lastAlert.setVisible(presenter.hasLastAlertPermission() && presenter.getLastAlertConfigValue() != null);
		if (presenter.hasUpdatePermission()) {
			buttons.add(lastAlert);
		}

		myLicenseInfo = new WindowButton(FontAwesome.FILE_CODE_O, $("UpdateManagerViewImpl.myLicense"),
				$("UpdateManagerViewImpl.aboutMyLicense"), false,
				WindowConfiguration.modalDialog("600px", null)) {

			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSpacing(true);
				layout.addStyleName("no-scroll");

				LicenseInfo licenseInfo = presenter.getLicenseInfo();

				Map<String, Object> titleParams = new HashMap<>();
				titleParams.put("client", licenseInfo.getClientName());
				titleParams.put("plan", $(licenseInfo.getSupportPlan()));
				titleParams.put("expiration", DateFormatUtils.format(licenseInfo.getExpirationDate()));
				Label title = new Label($("UpdateManagerViewImpl.licenseTitle", titleParams), ContentMode.HTML);

				Map<String, Object> infoParams = new HashMap<>();
				String pluginListItems = licenseInfo.getPlugins().values()
						.stream().map(idAndTitleEntry -> "<li>" + idAndTitleEntry.getValue() + "</li>")
						.collect(Collectors.joining());
				infoParams.put("plugins", pluginListItems);
				infoParams.put("userAmount", Long.toString(licenseInfo.getMaxUsersAllowed()));
				infoParams.put("serverAmount", Byte.toString(licenseInfo.getMaxServersAllowed()));
				infoParams.put("serverAmount", Byte.toString(licenseInfo.getMaxServersAllowed()));
				infoParams.put("vaultQuota", Long.toString(licenseInfo.getVaultQuota()));
				Label info = new Label($("UpdateManagerViewImpl.licenseInfo", infoParams), ContentMode.HTML);

				String licenseInfoPageUrl = presenter.getLicenseInfoPageUrl();
				LinkButton learnMore = new LinkButton($("UpdateManagerViewImpl.learnMore")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Page.getCurrent().open(licenseInfoPageUrl, "_blank", false);
					}
				};
				learnMore.setVisible(licenseInfoPageUrl != null);

				layout.addComponents(title, info, learnMore);
				layout.setComponentAlignment(learnMore, Alignment.BOTTOM_LEFT);

				return layout;
			}

		};
		myLicenseInfo.setVisible(presenter.getLicenseInfo() != null);
		if (presenter.hasUpdatePermission()) {
			buttons.add(myLicenseInfo);
		}

		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		layout = new VerticalLayout(/*buildInfoItem("", "")*/);
		layout.setSpacing(true);
		layout.setWidth("100%");

		//		WindowButton allocatedMemoryButton = buildAllocatedMemoryButton();
		//		layout.addComponent(allocatedMemoryButton);

		Component messagePanel = buildMessagePanel();
		layout.addComponent(messagePanel);
		panel = new VerticalLayout();
		layout.addComponent(panel);
		layout.setSpacing(true);

		if (presenter.hasUpdatePermission()) {
			showStandardUpdatePanel();
		}

		//		layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.currentVersionofConstellio"), presenter.getCurrentVersion()));
		//
		//		MemoryDetails allocatedMemoryForConstellio = SystemAnalysisUtils.getAllocatedMemoryForConstellio();
		//		if (allocatedMemoryForConstellio != null) {
		//			layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.allocatedMemoryForConstellio"), allocatedMemoryForConstellio));
		//		} else {
		//			layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.allocatedMemoryForConstellio"), $("UpdateManagerViewImpl.statut")));
		//		}
		//
		//		LicenseInfo info = presenter.getLicenseInfo();
		//		if (info != null) {
		//			layout.addComponents(
		//					buildInfoItem($("UpdateManagerViewImpl.clientName"), info.getClientName()),
		//					buildInfoItem($("UpdateManagerViewImpl.expirationDate"), info.getExpirationDate()));
		//		} else {
		//			layout.addComponents(
		//					buildInfoItemRed($("UpdateManagerViewImpl.clientName"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.expirationDate"), $("UpdateManagerViewImpl.statut")));
		//		}
		//
		//		if (locator.getFoldersLocatorMode() != FoldersLocatorMode.WRAPPER) {
		//			layout.addComponents(
		//					buildInfoItemRed($("UpdateManagerViewImpl.versionofKernel"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.privatedirectoryinstalled"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.javaversionofwrapper"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.javaversionoflinux"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.versionofSolr"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.UserrunningSolr"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.UserrunningConstellio"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.diskUsageOpt"), $("UpdateManagerViewImpl.statut")),
		//					buildInfoItemRed($("UpdateManagerViewImpl.diskUsageSolr"), $("UpdateManagerViewImpl.statut")));
		//		} else {
		//			String linuxVersion = presenter.getLinuxVersion();
		//			if (presenter.isLinuxVersionDeprecated(linuxVersion)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.versionofKernel"), linuxVersion));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.versionofKernel"), linuxVersion));
		//			}
		//
		//			if (!presenter.isPrivateRepositoryInstalled()) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.privatedirectoryinstalled"), $("no")));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.privatedirectoryinstalled"), $("yes")));
		//			}
		//
		//			String wrapperJavaVersion = presenter.getWrapperJavaVersion();
		//			if (presenter.isJavaVersionDeprecated(wrapperJavaVersion)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.javaversionofwrapper"), wrapperJavaVersion));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.javaversionofwrapper"), wrapperJavaVersion));
		//			}
		//
		//			String javaVersion = presenter.getJavaVersion();
		//			if (presenter.isJavaVersionDeprecated(javaVersion)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.javaversionoflinux"), javaVersion));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.javaversionoflinux"), javaVersion));
		//			}
		//
		//			String solrVersion = presenter.getSolrVersion();
		//			if (presenter.isSolrVersionDeprecated(solrVersion)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.versionofSolr"), solrVersion));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.versionofSolr"), solrVersion));
		//			}
		//
		//			String solrUser = presenter.getSolrUser();
		//			if (presenter.isSolrUserRoot(solrUser)) {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.UserrunningSolr"), solrUser));
		//			} else {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.UserrunningSolr"), solrUser));
		//			}
		//
		//			String constellioUser = presenter.getConstellioUser();
		//			if (presenter.isConstellioUserRoot(constellioUser)) {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.UserrunningConstellio"), constellioUser));
		//			} else {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.UserrunningConstellio"), constellioUser));
		//			}
		//
		//			String diskUsageOpt = presenter.getDiskUsage("/opt");
		//			if (presenter.isDiskUsageProblematic(diskUsageOpt)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.diskUsageOpt"), diskUsageOpt));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.diskUsageOpt"), diskUsageOpt));
		//			}
		//
		//			String diskUsageSolr = presenter.getDiskUsage("/var/solr");
		//			if (presenter.isDiskUsageProblematic(diskUsageSolr)) {
		//				layout.addComponents(buildInfoItemRed($("UpdateManagerViewImpl.diskUsageSolr"), diskUsageSolr));
		//			} else {
		//				layout.addComponents(buildInfoItem($("UpdateManagerViewImpl.diskUsageSolr"), diskUsageSolr));
		//			}
		//		}
		return layout;
	}

	private WindowButton buildAllocatedMemoryButton() {
		SystemMemory systemMemory = fetchSystemMemoryInfo();
		final MemoryDetails totalSystemMemory = systemMemory.getTotalSystemMemory();
		final MemoryDetails allocatedMemoryForConstellio = systemMemory.getConstellioAllocatedMemory();
		final MemoryDetails allocatedMemoryForSolr = systemMemory.getSolrAllocatedMemory();

		Double percentageOfAllocatedMemory = systemMemory.getPercentageOfAllocatedMemory();

		WindowButton allocatedMemoryButton = new WindowButton("", $("UpdateManagerViewImpl.allocatedMemoryButtonCaption")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();
				if (totalSystemMemory != null) {
					mainLayout.addComponents(buildInfoItem($("UpdateManagerViewImpl.totalSystemMemory"), totalSystemMemory));
				}

				if (allocatedMemoryForConstellio != null) {
					mainLayout.addComponents(buildInfoItem($("UpdateManagerViewImpl.allocatedMemoryForConstellio"), allocatedMemoryForConstellio));
				}

				if (allocatedMemoryForSolr != null) {
					mainLayout.addComponents(buildInfoItem($("UpdateManagerViewImpl.allocatedMemoryForSolr"), allocatedMemoryForSolr));
				}
				return mainLayout;
			}
		};

		StringBuilder buttonCaption = new StringBuilder($("UpdateManagerViewImpl.allocatedMemoryButtonCaption"));
		if (percentageOfAllocatedMemory != null) {
			buttonCaption.append(" : " + percentageOfAllocatedMemory * 100 + " %");
			if (percentageOfAllocatedMemory >= 0.8) {
				allocatedMemoryButton.addStyleName("button-caption-error");
			} else {
				allocatedMemoryButton.addStyleName("button-caption-important");
			}
		} else {
			allocatedMemoryButton.addStyleName("button-caption-error");
			buttonCaption.append(" : " + $("UpdateManagerViewImpl.missingInfoForMemoryAnalysis"));
		}
		allocatedMemoryButton.setCaption(buttonCaption.toString());
		return allocatedMemoryButton;
	}

	private Component buildMessagePanel() {
		VerticalLayout verticalLayout = new VerticalLayout();
		UpdateRecoveryImpossibleCause cause = presenter.isUpdateWithRecoveryPossible();
		if (cause != null) {
			verticalLayout.addComponent(
					new Label("<p style=\"color:red\">" + $("UpdateManagerViewImpl." + cause) + "</p>", ContentMode.HTML));
		} else {
			UpdateNotRecommendedReason updateNotRecommendedReason = presenter.getUpdateNotRecommendedReason();
			if (updateNotRecommendedReason != null) {
				verticalLayout.addComponent(
						new Label("<p style=\"color:red\">" + $("UpdateManagerViewImpl." + updateNotRecommendedReason) + "</p>",
								ContentMode.HTML));
			}
		}
		final String exceptionDuringLastUpdate = presenter.getExceptionDuringLastUpdate();
		if (StringUtils.isNotBlank(exceptionDuringLastUpdate)) {
			verticalLayout.addComponent(new Label(
					"<p style=\"color:red\">" + $("UpdateManagerViewImpl.exceptionCausedByLastVersion") + "</p>", ContentMode.HTML));
			WindowButton windowButton = new WindowButton($("details"), $("details"), WindowConfiguration.modalDialog("90%", "90%")) {
				@Override
				protected Component buildWindowContent() {
					TextArea textArea = new TextArea();
					textArea.setSizeFull();
					textArea.setValue(exceptionDuringLastUpdate);
					return textArea;
				}
			};
			windowButton.addStyleName(ValoTheme.BUTTON_LINK);
			verticalLayout.addComponent(windowButton);
			verticalLayout.addComponent(new Label(
					"<p style=\"color:red\">" + "" + "</p>", ContentMode.HTML));
		}
		return verticalLayout;
	}

	@Override
	public void showStandardUpdatePanel() {
		Component updatePanel = presenter.isLicensedForAutomaticUpdate() ? buildAutomaticUpdateLayout() : buildUnlicensedLayout();
		layout.replaceComponent(panel, updatePanel);
		license.setEnabled(true);
		standardUpdate.setEnabled(false);
		alternateUpdate.setEnabled(presenter.isUpdateEnabled());
		updateMenuActionBasedOnButton(license);
		updateMenuActionBasedOnButton(standardUpdate);
		updateMenuActionBasedOnButton(alternateUpdate);
		panel = updatePanel;
	}

	@Override
	public void showAlternateUpdatePanel(UpdateModeHandler handler) {
		Component updatePanel = handler.buildUpdatePanel();
		layout.replaceComponent(panel, updatePanel);
		license.setEnabled(true);
		reindexButton.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		rebuildCacheButton.setEnabled(presenter.isRestartWithCacheRebuildEnabled());
		standardUpdate.setEnabled(presenter.isUpdateEnabled());
		alternateUpdate.setEnabled(false);
		updateMenuActionBasedOnButton(license);
		updateMenuActionBasedOnButton(reindexButton);
		updateMenuActionBasedOnButton(rebuildCacheButton);
		updateMenuActionBasedOnButton(standardUpdate);
		updateMenuActionBasedOnButton(alternateUpdate);
		panel = updatePanel;
	}

	@Override
	public void showLicenseUploadPanel() {
		Component licensePanel = buildLicenseUploadPanel();
		layout.replaceComponent(panel, licensePanel);
		license.setEnabled(false);
		reindexButton.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		rebuildCacheButton.setEnabled(presenter.isRestartWithCacheRebuildEnabled());
		boolean uploadPossible = presenter.isUpdateEnabled();
		standardUpdate.setEnabled(uploadPossible);
		alternateUpdate.setEnabled(uploadPossible);
		updateMenuActionBasedOnButton(license);
		updateMenuActionBasedOnButton(reindexButton);
		updateMenuActionBasedOnButton(rebuildCacheButton);
		updateMenuActionBasedOnButton(standardUpdate);
		updateMenuActionBasedOnButton(alternateUpdate);
		panel = licensePanel;
	}

	@Override
	public void showRestartRequiredPanel() {
		Component restartPanel = buildRestartRequiredPanel();
		layout.replaceComponent(panel, restartPanel);
		license.setEnabled(false);
		reindexButton.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		rebuildCacheButton.setEnabled(presenter.isRestartWithCacheRebuildEnabled());
		standardUpdate.setEnabled(false);
		alternateUpdate.setEnabled(false);
		updateMenuActionBasedOnButton(license);
		updateMenuActionBasedOnButton(reindexButton);
		updateMenuActionBasedOnButton(rebuildCacheButton);
		updateMenuActionBasedOnButton(standardUpdate);
		updateMenuActionBasedOnButton(alternateUpdate);
		panel = restartPanel;
	}

	private Component buildInfoItem(String caption, Object value) {
		Label captionLabel = new Label(caption);
		captionLabel.addStyleName(ValoTheme.LABEL_BOLD);

		Label valueLabel = value instanceof LocalDate ? new LocalDateLabel((LocalDate) value) : new Label(value == null ? "" : value.toString());

		HorizontalLayout layout = new HorizontalLayout(captionLabel, valueLabel);
		layout.setSpacing(true);

		return layout;
	}

	private Component buildInfoItemRed(String caption, Object value) {
		Label captionLabel = new Label(caption);
		captionLabel.addStyleName(ValoTheme.LABEL_BOLD);

		if (value == null || StringUtils.isEmpty(value.toString())) {
			value = $("UpdateManagerViewImpl.statut");
		}
		Label valueLabel = value instanceof LocalDate ?
						   new LocalDateLabel((LocalDate) value) :
						   new Label(value.toString());
		valueLabel.setStyleName("important-label");

		HorizontalLayout layout = new HorizontalLayout(captionLabel, valueLabel);
		layout.setSpacing(true);
		return layout;
	}

	private Component buildAutomaticUpdateLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addStyleName(BaseForm.BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);

		downloadProgressBar.setWidth("100%");

		AtomicReference<String> selectedVersion = new AtomicReference<>();
		Button downloadReleaseNote = new WindowButton($("UpdateManagerViewImpl.downloadReleaseNote"),
				$("UpdateManagerViewImpl.releaseNote"), WindowConfiguration.modalDialog("50%", "80%")) {
			@Override
			protected Component buildWindowContent() {
				Label releaseNote = null;
				String releaseNoteValue = presenter.downloadReleaseNoteRequested(selectedVersion.get());

				if (releaseNoteValue != null) {
					releaseNote = new Label(releaseNoteValue, ContentMode.HTML);
					releaseNote.setSizeFull();
				} else {
					getWindow().close();
				}

				return releaseNote;
			}
		};
		downloadReleaseNote.setEnabled(false);
		downloadReleaseNote.addStyleName(BaseForm.CANCEL_BUTTON);

		ConfirmDialogButton downloadWarAndInstall = new ConfirmDialogButton($("UpdateManagerViewImpl.downloadWarAndInstall")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("UpdateManagerViewImpl.downloadWarAndInstall.confirmDialog", selectedVersion.get());
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				downloadReleaseNote.setEnabled(false);
				this.setEnabled(false);
				presenter.downloadWarAndInstalledClicked(selectedVersion.get());
			}
		};
		downloadWarAndInstall.setVisible(presenter.isDistributedEnvironment());
		downloadWarAndInstall.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		downloadWarAndInstall.setEnabled(false);
		downloadWarAndInstall.addStyleName(BaseForm.SAVE_BUTTON);
		downloadWarAndInstall.addStyleName(ValoTheme.BUTTON_PRIMARY);

		buttonsLayout.addComponents(downloadWarAndInstall, downloadReleaseNote);

		ComboBox versions = new ComboBox($("UpdateManagerViewImpl.versions"));
		versions.addItems(presenter.getAvailableLtsVersions());
		versions.setEnabled(presenter.hasAvailableLtsVersions());
		versions.setNullSelectionAllowed(false);
		versions.addValueChangeListener(event -> {
			selectedVersion.set((String) event.getProperty().getValue());
			downloadReleaseNote.setEnabled(true);
			downloadWarAndInstall.setEnabled(presenter.isAutomaticUpdateAvailable(selectedVersion.get()));
		});

		VerticalLayout layout = new VerticalLayout(versions, downloadProgressBar, buttonsLayout);
		layout.setWidth("100%");
		layout.setSpacing(true);
		layout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

		return layout;
	}

	@Override
	public ProgressBar getDownloadProgressBar() {
		return downloadProgressBar;
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	public ProgressInfo openProgressPopup() {
		uploadWaitWindow = new UploadWaitWindow();
		final ProgressInfo progressInfo = new ProgressInfo() {
			@Override
			public void setTask(String task) {
				uploadWaitWindow.setTask(task);
			}

			@Override
			public void setProgressMessage(String progressMessage) {
				uploadWaitWindow.setProgressMessage(progressMessage);
			}
		};
		UI.getCurrent().addWindow(uploadWaitWindow);
		return progressInfo;
	}

	@Override
	public void closeProgressPopup() {
		uploadWaitWindow.close();
	}

	private Component buildUnlicensedLayout() {
		Label message = new Label($("UpdateManagerViewImpl.unlicensed"));
		message.addStyleName(ValoTheme.LABEL_BOLD);

		Link request = new Link($("UpdateManagerViewImpl.requestLicense"),
				new ExternalResource("mailto:sales@constellio.com?Subject=Demande de license Constellio"));

		VerticalLayout layout = new VerticalLayout(message, request);
		layout.setSpacing(true);

		return layout;
	}

	private Component buildLicenseUploadPanel() {
		Upload upload = new Upload($("UpdateManagerViewImpl.uploadLicenseCaption"), new Receiver() {
			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				return presenter.getLicenseOutputStream();
			}
		});
		upload.addSucceededListener(new SucceededListener() {
			@Override
			public void uploadSucceeded(SucceededEvent event) {
				presenter.licenseUploadSucceeded();
			}
		});
		upload.setButtonCaption($("UpdateManagerViewImpl.uploadLicense"));

		Button cancel = new LinkButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.licenseUploadCancelled();
			}
		};

		VerticalLayout layout = new VerticalLayout(upload, cancel);
		layout.setWidth("100%");
		layout.setSpacing(true);

		return layout;
	}

	private Component buildRestartRequiredPanel() {
		return new Label("<p style=\"color:red\">" + $("UpdateManagerViewImpl.restart") + "</p>", ContentMode.HTML);
	}

	@Override
	public void drop(DragAndDropEvent event) {
		DropHandler childDropHandler = ComponentTreeUtils.getFirstChild((Component) panel, DropHandler.class);
		if (panel instanceof DropHandler) {
			((DropHandler) panel).drop(event);
		} else if (childDropHandler != null) {
			childDropHandler.drop(event);
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}


	@Override
	protected ClickListener getBackButtonClickListener() {
		return (ClickListener) event -> presenter.backButtonClicked();
	}

	@Override
	public void attach() {
		super.attach();

		if (UI.getCurrent().getNavigator() != null && viewChangeListener == null) {
			viewChangeListener = new ViewChangeListener() {
				@Override
				public boolean beforeViewChange(ViewChangeEvent event) {
					return true;
				}

				@Override
				public void afterViewChange(ViewChangeEvent event) {
					presenter.onPageExit();
					UI.getCurrent().getNavigator().removeViewChangeListener(viewChangeListener);
				}
			};
			UI.getCurrent().getNavigator().addViewChangeListener(viewChangeListener);
		}
	}
}
