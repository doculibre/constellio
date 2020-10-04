package com.constellio.app.ui.framework.components.fields.download;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class BaseDownloadField extends CustomField<Object> {

	public static final String STYLE_NAME = "base-download-field";

	private static final String CAPTION_PROPERTY_ID = "caption";

	private VerticalLayout mainLayout;

	private com.vaadin.ui.ProgressBar fileDownload;

	private BaseTextField downloadTextField;

	private BaseButton downloadButton;

	private I18NHorizontalLayout downloadedFileName;

	private Map<Object, Component> itemCaptions = new HashMap<>();

	private ViewChangeListener viewChangeListener;

	private boolean isViewOnly;

	private DownloadThread dlThread;

	public BaseDownloadField() {
		super();

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_NAME + "d-layout");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(false);

		fileDownload = new com.vaadin.ui.ProgressBar();
		fileDownload.setWidth("100%");
		fileDownload.addStyleName(STYLE_NAME + "-filedownload");

		downloadedFileName = new I18NHorizontalLayout();

		addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				TempFileDownload newValue = (TempFileDownload) BaseDownloadField.this.getValue();
				itemCaptions.clear();
				downloadedFileName.setCaption("");
				if (newValue != null) {
					downloadedFileName.setCaption(newValue.getFileName());
					downloadedFileName.getComponent(0).setVisible(true);
				} else {
					downloadedFileName.getComponent(0).setVisible(false);
				}
				if (dlThread != null) {
					dlThread.interrupt();
					dlThread = null;
				}
			}
		});

		DeleteButton deleteButton = new DeleteButton() {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				Object itemId = BaseDownloadField.this.getValue();

				if (itemId instanceof TempFileDownload) {
					TempFileDownload tempFileDownload = (TempFileDownload) itemId;
					tempFileDownload.delete();
				} else {
					deleteTempFile(itemId);
				}
				BaseDownloadField.this.setValue(null);
				fileDownload.setValue(0.0f);
				if (dlThread != null) {
					dlThread.interrupt();
					dlThread = null;
				}
			}
		};

		deleteButton.setReadOnly(BaseDownloadField.this.isReadOnly());
		deleteButton.setEnabled(BaseDownloadField.this.isEnabled());
		deleteButton.setVisible(false);

		downloadedFileName.addComponent(deleteButton);

		downloadTextField = new BaseTextField();
		downloadTextField.setInputPrompt("https://www.example.com/constellio.war");
		downloadTextField.setEnabled(true);
		downloadTextField.setValue("");
		downloadTextField.setWidth("100%");
		downloadTextField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (downloadTextField.getValue() != null && !downloadTextField.getValue().equals("")) {
					downloadButton.setEnabled(true);
				}
			}
		});

		downloadButton = new BaseButton($("UpdateManagerViewImpl.downloadWar")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					if (dlThread == null) {
						URL testingUrl = new URL(downloadTextField.getValue());

						TempFileDownload tempFileDownload = new TempFileDownload(testingUrl);
						URLConnection successOnHeaders = tempFileDownload.getCon();

						fileDownload.setIndeterminate(false);
						fileDownload.setValue(0.0f);

						dlThread = new DownloadThread(fileDownload, successOnHeaders, tempFileDownload, BaseDownloadField.this);
						dlThread.start();
					}
				} catch (InvalidWarUrlException | IOException ex) {
					Logger.getLogger(getClass().getName()).log(Level.FINE,
							"Download failed", ex);
					if (dlThread != null) {
						dlThread.interrupt();
						dlThread = null;
					}
				}

			}
		};
		downloadButton.setEnabled(false);
		downloadButton.setWidth("33%");

		downloadTextField.setVisible(true);
		downloadButton.setVisible(true);
		fileDownload.setVisible(true);

		mainLayout.addComponents(downloadTextField, downloadButton, fileDownload, downloadedFileName);
	}

	protected void onDownloadWindowClosed(CloseEvent e) {
		if (dlThread != null) {
			dlThread.interrupt();
			dlThread = null;
		}
	}

	@Override
	public void detach() {
		if (dlThread != null) {
			dlThread.interrupt();
			dlThread = null;
		}
		super.detach();
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
					deleteTempFiles();
					UI.getCurrent().getNavigator().removeViewChangeListener(viewChangeListener);
				}
			};
			UI.getCurrent().getNavigator().addViewChangeListener(viewChangeListener);
		}
	}

	protected Object getItemId(TempFileDownload tempFileDownload) {
		return tempFileDownload;
	}

	protected Component getItemCaption(Object itemId) {
		return itemCaptions.get(itemId);
	}

	protected Map<Object, Component> getItemCaptions() {
		return Collections.unmodifiableMap(itemCaptions);
	}

	protected Component newItemCaption(Object itemId) {
		String itemCaption;
		if (itemId instanceof TempFileDownload) {
			TempFileDownload tempFileDownload = (TempFileDownload) itemId;
			itemCaption = tempFileDownload.getFilePath();
		} else {
			itemCaption = itemId.toString();
		}
		return new Label(itemCaption);
	}

	@Override
	public void setInternalValue(Object newValue) {
		super.setInternalValue(newValue);
	}

	protected void deleteTempFile(Object itemId) {

	}

	protected boolean isDeleteLink(Object itemId) {
		return true;
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

	@SuppressWarnings("unchecked")
	protected final void deleteTempFiles() {
		Object currentValue = getInternalValue();
		if (currentValue instanceof TempFileDownload) {
			TempFileDownload tempFileDownload = (TempFileDownload) currentValue;
			tempFileDownload.delete();
		} else if (currentValue instanceof List) {
			List<Object> currentListValue = (List<Object>) currentValue;
			for (Object currentListElement : currentListValue) {
				if (currentListElement instanceof TempFileDownload) {
					TempFileDownload tempFileDownload = (TempFileDownload) currentListElement;
					tempFileDownload.delete();
				} else {
					deleteTempFile(currentListElement);
				}
			}
		} else if (currentValue != null) {
			deleteTempFile(currentValue);
		}
	}

	@Override
	public void discard()
			throws SourceException {
		super.discard();
		deleteTempFiles();
	}

	public boolean fireValueChangeWhenEqual() {
		return false;
	}

	@Override
	public Class<? extends Object> getType() {
		Class<?> type;
		type = Object.class;
		return type;
	}

	@Override
	public void setValue(Object newFieldValue)
			throws com.vaadin.data.Property.ReadOnlyException, ConversionException {
		super.setValue(newFieldValue);
	}

}
