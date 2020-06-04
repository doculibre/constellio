package com.constellio.app.utils;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.buttons.BaseButton;
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
		ManualUploadPanel manualUploadPanel = new ManualUploadPanel();
		DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(manualUploadPanel);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(manualUploadPanel.uploadField);
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

	private class ManualUploadPanel extends VerticalLayout {

		private BaseUploadField uploadField;
		private Button uploadButton;

		private ManualUploadPanel() {
			setWidth("100%");
			setHeight("250px");
			setSpacing(true);

			uploadField = new BaseUploadField(false);
			uploadField.setCaption($("UpdateManagerViewImpl.caption"));
			uploadField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					TempFileUpload tempUpload = (TempFileUpload) uploadField.getValue();
					uploadButton.setEnabled(tempUpload != null);
				}
			});

			uploadButton = new BaseButton($("UpdateManagerViewImpl.upload")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					TempFileUpload tempUpload = (TempFileUpload) uploadField.getValue();
					if (tempUpload != null) {
						try {
							File warFile = appLayerFactory.getModelLayerFactory().getFoldersLocator()
									.getUploadConstellioWarFile();
							File uploadedFile = tempUpload.getTempFile();
							FileUtils.copyFile(uploadedFile, warFile);

							ProgressInfo progressInfo = view.openProgressPopup();
							try {
								appLayerFactory.newApplicationService().update(progressInfo);
								view.showRestartRequiredPanel();
							} catch (AppManagementServiceException ase) {
								view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
							} catch (WarFileNotFound e) {
								view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
							} finally {
								view.closeProgressPopup();
								tempUpload.delete();
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
			uploadButton.setEnabled(false);
			uploadButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

			addComponents(uploadField, uploadButton);
			setComponentAlignment(uploadButton, Alignment.MIDDLE_RIGHT);
		}

	}

}
