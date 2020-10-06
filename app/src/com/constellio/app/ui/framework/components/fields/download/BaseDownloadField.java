package com.constellio.app.ui.framework.components.fields.download;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;

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
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;

@SuppressWarnings("serial")
public class BaseDownloadField extends CustomField<Object> {

	public static final String STYLE_NAME = "base-download-field";

	private static final String CAPTION_PROPERTY_ID = "caption";

	private VerticalLayout mainLayout;

	private ProgressBar downloadProgressBar;

	private BaseTextField downloadTextField;

	private BaseButton downloadButton;
	
	private Label downloadedFileNameLabel;

	private I18NHorizontalLayout downloadedFileNameLayout;

	private ViewChangeListener viewChangeListener;

	private boolean isViewOnly;

	private DownloadThread dlThread;

	public BaseDownloadField() {
		super();

		setWidth("100%");

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_NAME + "d-layout");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		downloadProgressBar = new ProgressBar();
		downloadProgressBar.setWidth("100%");
		downloadProgressBar.addStyleName(STYLE_NAME + "-filedownload");
		downloadProgressBar.setVisible(false);

		downloadedFileNameLayout = new I18NHorizontalLayout();
		downloadedFileNameLayout.setSpacing(true);
		downloadedFileNameLayout.setVisible(false);
		
		downloadedFileNameLabel = new Label();

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
				downloadProgressBar.setValue(0.0f);
				if (dlThread != null) {
					dlThread.interrupt();
					dlThread = null;
				}
			}
		};
		deleteButton.setVisible(false);

		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				TempFileDownload newValue = (TempFileDownload) BaseDownloadField.this.getValue();
				if (newValue != null) {
					downloadedFileNameLabel.setValue(newValue.getFileName());
					downloadedFileNameLayout.setVisible(true);
					deleteButton.setVisible(true);
				} else {
					downloadedFileNameLayout.setVisible(false);
					deleteButton.setVisible(false);
				}
				if (dlThread != null) {
					dlThread.interrupt();
					dlThread = null;
				}
			}
		});

		downloadedFileNameLayout.addComponents(downloadedFileNameLabel, deleteButton);

		downloadTextField = new BaseTextField();
		downloadTextField.setInputPrompt("https://www.example.com/constellio.war");
		downloadTextField.setImmediate(true);
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
				downloadProgressBar.setVisible(true);
				try {
					if (dlThread == null) {
						URL testingUrl = new URL(downloadTextField.getValue());

						TempFileDownload tempFileDownload = new TempFileDownload(testingUrl);
						URLConnection successOnHeaders = tempFileDownload.getCon();

						downloadProgressBar.setIndeterminate(false);
						downloadProgressBar.setValue(0.0f);

						dlThread = new DownloadThread(downloadProgressBar, successOnHeaders, tempFileDownload, BaseDownloadField.this);
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

		mainLayout.addComponents(downloadTextField, downloadButton, downloadProgressBar, downloadedFileNameLayout);
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

	protected void deleteTempFile(Object itemId) {

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
