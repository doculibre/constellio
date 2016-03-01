package com.constellio.app.ui.pages.management.updates;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.LocalDateLabel;
import com.constellio.app.ui.pages.base.BaseViewImpl;
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
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class UpdateManagerViewImpl extends BaseViewImpl implements UpdateManagerView {
	private final UpdateManagerPresenter presenter;
	private UploadWaitWindow uploadWaitWindow;
	private VerticalLayout layout;
	private Component panel;
	private Button license;
	private Button standardUpdate;
	private Button alternateUpdate;
	private Button reindex;

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

		Button restart = new Button($("UpdateManagerViewImpl.restartButton"));
		restart.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.restart();
			}
		});
		buttons.add(restart);

		reindex = new Button($("UpdateManagerViewImpl.restartAndReindexButton")){
			@Override
			public boolean isEnabled() {
				return presenter.isRestartWithReindexButtonEnabled();
			}
		};
		reindex.setEnabled(presenter.isRestartWithReindexButtonEnabled());
		reindex.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.restartAndReindex();
			}
		});
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
		layout = new VerticalLayout(buildInfoItem($("UpdateManagerViewImpl.version"), presenter.getCurrentVersion()));
		layout.setSpacing(true);
		layout.setWidth("100%");

		LicenseInfo info = presenter.getLicenseInfo();
		if (info != null) {
			layout.addComponents(
					buildInfoItem($("UpdateManagerViewImpl.clientName"), info.getClientName()),
					buildInfoItem($("UpdateManagerViewImpl.expirationDate"), info.getExpirationDate()));
		}

		Component messagePanel = buildMessagePanel();
		layout.addComponent(messagePanel);
		panel = new VerticalLayout();
		layout.addComponent(panel);

		showStandardUpdatePanel();

		return layout;
	}

	private Component buildMessagePanel() {
		VerticalLayout verticalLayout = new VerticalLayout();
		UpdateRecoveryImpossibleCause cause = presenter.isUpdateWithRecoveryPossible();
		if(cause != null){
			verticalLayout.addComponent(new Label("<p style=\"color:red\">" + $("UpdateManagerViewImpl." + cause) + "</p>", ContentMode.HTML));
		}
		String exceptionDuringLastUpdate = presenter.getExceptionDuringLastUpdate();
		if(StringUtils.isNotBlank(exceptionDuringLastUpdate)){
			verticalLayout.addComponent(new Label("<p style=\"color:red\">" + $("UpdateManagerViewImpl.exceptionCausedByLastVersion")+ " " + exceptionDuringLastUpdate + "</p>", ContentMode.HTML));
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

		Label valueLabel = value instanceof LocalDate ? new LocalDateLabel((LocalDate) value) : new Label(value.toString());

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
}
