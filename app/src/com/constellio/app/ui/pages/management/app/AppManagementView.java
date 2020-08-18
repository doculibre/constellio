package com.constellio.app.ui.pages.management.app;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.themes.ValoTheme;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("serial")
public class AppManagementView extends BaseViewImpl implements AdminViewGroup {

	public static final String UPLOAD_FIELD_ID = "uploadField";
	public static final String RESTART_BUTTON_ID = "restartButton";
	public static final String UPDATE_BUTTON_ID = "updateButton";
	AppManagementPresenter presenter;
	private StreamFactory<OutputStream> warFileDestination;
	private HorizontalLayout mainLayout;
	private Button restartButton;
	private Button updateButton;
	private Upload upload;

	public AppManagementView() {
		presenter = new AppManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		return "App management view";
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent events) {

		buildMainLayout();
		presenter.enterView();

		updateButton.setCaption("Mettre à jour");
		updateButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		updateButton.setId(UPDATE_BUTTON_ID);
		updateButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.updateApplicationButtonClicked();
			}
		});

		restartButton.setCaption("Redémarrer");
		restartButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		restartButton.setId(RESTART_BUTTON_ID);
		restartButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.restartApplicationButtonClicked();
			}
		});

		upload.setId(UPLOAD_FIELD_ID);
		upload.setReceiver(new Receiver() {

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				try {
					return warFileDestination.create("AppManagementView");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		upload.addSucceededListener(new SucceededListener() {

			@Override
			public void uploadSucceeded(SucceededEvent event) {
				presenter.onSuccessfullUpload();
			}
		});

		return mainLayout;
	}

	public void setWebappName(String webappFolderName) {
		mainLayout.addComponent(new Label("Webapp running in '" + webappFolderName + "'"));
	}

	public void setDataVersion(String version) {
		mainLayout.addComponent(new Label("Data version : '" + version + "'"));
	}

	public void setWarVersion(String version) {
		mainLayout.addComponent(new Label("War version  : '" + version + "'"));
	}

	private void buildMainLayout() {
		mainLayout = new HorizontalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("-1px");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		//		setWidth("-1px");
		//		setHeight("100.0%");

		upload = new Upload();
		upload.setStyleName(ValoTheme.BUTTON_PRIMARY);
		upload.setCaption("[Upload war file]");
		upload.setImmediate(false);
		upload.setWidth("-1px");
		upload.setHeight("-1px");
		mainLayout.addComponent(upload);

		updateButton = new Button();
		updateButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		updateButton.setCaption("[Update application]");
		updateButton.setImmediate(true);
		updateButton.setWidth("-1px");
		updateButton.setHeight("-1px");

		if (presenter.hasUpdatePermission()) {
			mainLayout.addComponent(updateButton);
		}

		restartButton = new Button();
		restartButton.setCaption("[Restart application]");
		restartButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		restartButton.setImmediate(true);
		restartButton.setWidth("-1px");
		restartButton.setHeight("-1px");
		mainLayout.addComponent(restartButton);
	}

	public void setUpdateButtonVisible(boolean visible) {
		updateButton.setVisible(visible);
	}

	public void setWarFileDestination(StreamFactory<OutputStream> warFileDestination) {
		this.warFileDestination = warFileDestination;
	}

}
