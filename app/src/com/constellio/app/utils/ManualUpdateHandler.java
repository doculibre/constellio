package com.constellio.app.utils;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFoundException;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.download.BaseDownloadField;
import com.constellio.app.ui.framework.components.fields.download.TempFileDownload;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class ManualUpdateHandler implements UpdateModeHandler {

	private final ManualUpdateHandlerView view;
	private transient AppLayerFactory appLayerFactory;

	public ManualUpdateHandler(AppLayerFactory appLayerFactory, ManualUpdateHandlerView view) {
		this.view = view;
		init(appLayerFactory);
	}

	@Override
	public Component buildUpdatePanel() {
		ManualUpdatePanel manualUpdatePanel = new ManualUpdatePanel();
		DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(manualUpdatePanel);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(manualUpdatePanel.uploadField);
		return dragAndDropWrapper;

	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getAppLayerFactory());
	}

	private void init(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	private class ManualUpdatePanel extends VerticalLayout {

		private BaseUploadField uploadField;
		private Button updateButton;
		private BaseDownloadField downloadField;

		private ManualUpdatePanel() {
			setWidth("100%");
			setHeight("600px");
			setSpacing(false);

			uploadField = new BaseUploadField();
			uploadField.setMultiValue(false);
			uploadField.setCaption($("UpdateManagerViewImpl.caption"));
			uploadField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					TempFileUpload tempUpload = (TempFileUpload) uploadField.getValue();
					TempFileDownload tempDownload = (TempFileDownload) downloadField.getValue();
					if (tempUpload != null) {
						downloadField.setValue(null);
					}
					updateButton.setEnabled(tempUpload != null || tempDownload != null);
				}
			});

			downloadField = new BaseDownloadField();
			downloadField.setCaption($("UpdateManagerViewImpl.useUrl"));
			downloadField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					TempFileDownload tempDownload = (TempFileDownload) downloadField.getValue();
					TempFileUpload tempUpload = (TempFileUpload) uploadField.getValue();
					if (tempDownload != null) {
						uploadField.setValue(null);
					}
					updateButton.setEnabled(tempUpload != null || tempDownload != null);
				}
			});

			updateButton = new BaseButton($("UpdateManagerViewImpl.updateButton")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					TempFileUpload tempUpload = (TempFileUpload) uploadField.getValue();
					TempFileDownload tempDownload = (TempFileDownload) downloadField.getValue();
					if (tempUpload != null || tempDownload != null) {
						try {
							File uploadedFile = null;
							if (tempUpload != null) {
								uploadedFile = tempUpload.getTempFile();
							} else if (tempDownload != null) {
								uploadedFile = tempDownload.getTempFile();
							} else {
								throw new WarFileNotFoundException();
							}

							File warFile = appLayerFactory.getModelLayerFactory().getFoldersLocator()
									.getUploadConstellioWarFile();

							FileUtils.copyFile(uploadedFile, warFile);

							ProgressInfo progressInfo = view.openProgressPopup();
							try {
								appLayerFactory.newApplicationService().update(progressInfo);
								view.showRestartRequiredPanel();
							} catch (AppManagementServiceException ase) {
								view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
							} catch (WarFileNotFoundException e) {
								view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
							} finally {
								view.closeProgressPopup();

								if (tempUpload != null) {
									tempUpload.delete();
								}
								if (tempDownload != null) {
									tempDownload.delete();
								}
								//view.
							}
						} catch (FileNotFoundException fnfe) {
							view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
						} catch (IOException e1) {
							view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
						}
					}
				}
			};
			updateButton.setEnabled(false);
			updateButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

			addComponents(uploadField, downloadField, updateButton);
			setComponentAlignment(updateButton, Alignment.MIDDLE_RIGHT);
		}

	}

}
