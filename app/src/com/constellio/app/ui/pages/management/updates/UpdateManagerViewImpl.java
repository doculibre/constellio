package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.admin.services.SystemAnalysisUtils;
import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.entities.system.SystemMemory;
import com.constellio.app.entities.system.SystemMemory.MemoryDetails;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.LocalDateLabel;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
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

import java.io.OutputStream;
import java.util.List;

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
	private ConfirmDialogButton reindex;
	FoldersLocator locator = new FoldersLocator();

	public UpdateManagerViewImpl() {
		presenter = new UpdateManagerPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("UpdateManagerViewImpl.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		ConfirmDialogButton restart = new ConfirmDialogButton($("UpdateManagerViewImpl.restartButton")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("UpdateManagerViewImpl.restartwarning");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.restart();
			}
		};
		restart.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		buttons.add(restart);

		reindex = new ConfirmDialogButton($("UpdateManagerViewImpl.restartAndReindexButton")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("UpdateManagerViewImpl.reindexwarning");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.restartAndReindex();
			}
		};
		reindex.setDialogMode(ConfirmDialogButton.DialogMode.WARNING);
		reindex.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		buttons.add(reindex);


		standardUpdate = new Button($("UpdateManagerViewImpl.automatic"));
		standardUpdate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.standardUpdateRequested();
			}
		});
		buttons.add(standardUpdate);

		alternateUpdate = new Button($("UpdateManagerViewImpl." + presenter.getAlternateUpdateName()));
		alternateUpdate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.alternateUpdateRequested();
			}
		});
		alternateUpdate.setVisible(presenter.isAlternateUpdateAvailable());
		buttons.add(alternateUpdate);

		license = new Button($("UpdateManagerViewImpl.licenseButton"));
		license.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.licenseUpdateRequested();
			}
		});
		buttons.add(license);

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

		showStandardUpdatePanel();
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
		panel = updatePanel;
	}

	@Override
	public void showAlternateUpdatePanel(UpdateModeHandler handler) {
		Component updatePanel = handler.buildUpdatePanel();
		layout.replaceComponent(panel, updatePanel);
		license.setEnabled(true);
		reindex.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		standardUpdate.setEnabled(presenter.isUpdateEnabled());
		alternateUpdate.setEnabled(false);
		panel = updatePanel;
	}

	@Override
	public void showLicenseUploadPanel() {
		Component licensePanel = buildLicenseUploadPanel();
		layout.replaceComponent(panel, licensePanel);
		license.setEnabled(false);
		reindex.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		boolean uploadPossible = presenter.isUpdateEnabled();
		standardUpdate.setEnabled(uploadPossible);
		alternateUpdate.setEnabled(uploadPossible);
		panel = licensePanel;
	}

	@Override
	public void showRestartRequiredPanel() {
		Component restartPanel = buildRestartRequiredPanel();
		layout.replaceComponent(panel, restartPanel);
		license.setEnabled(false);
		reindex.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		standardUpdate.setEnabled(false);
		alternateUpdate.setEnabled(false);
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
		return presenter.isAutomaticUpdateAvailable() ? buildAvailableUpdateLayout() : buildUpToDateUpdateLayout();
	}

	private Component buildAvailableUpdateLayout() {
		Label message = new Label($("UpdateManagerViewImpl.updateAvailable", presenter.getUpdateVersion()));
		message.addStyleName(ValoTheme.LABEL_BOLD);

		Button update = new LinkButton($("UpdateManagerViewImpl.updateButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				UI.getCurrent().access(new Thread(UpdateManagerViewImpl.class.getName() + "-updateFromServer") {
					@Override
					public void run() {
						presenter.updateFromServer();
					}
				});
			}
		};
		update.setVisible(presenter.isUpdateEnabled());

		HorizontalLayout updater = new HorizontalLayout(message, update);
		updater.setComponentAlignment(message, Alignment.MIDDLE_LEFT);
		updater.setComponentAlignment(update, Alignment.MIDDLE_LEFT);
		updater.setSpacing(true);

		Label changelog = new Label(presenter.getChangelog(), ContentMode.HTML);

		VerticalLayout layout = new VerticalLayout(updater, changelog);
		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
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


	private Component buildUpToDateUpdateLayout() {
		Label message = new Label($("UpdateManagerViewImpl.upToDate"));
		message.addStyleName(ValoTheme.LABEL_BOLD);
		return message;
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

}
