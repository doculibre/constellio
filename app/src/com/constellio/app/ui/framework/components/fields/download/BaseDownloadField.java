package com.constellio.app.ui.framework.components.fields.download;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class BaseDownloadField extends CustomField<Object> {

	public static final String STYLE_NAME = "base-download-field";

	private static final String CAPTION_PROPERTY_ID = "caption";

	private VerticalLayout mainLayout;

	private BaseFileDownload fileDownload;

	private BaseTextField downloadTextField;

	private BaseButton downloadButton;

	private ButtonsContainer<IndexedContainer> fileUploadsContainer;

	private Table fileDownloadsTable;

	private Map<Object, Component> itemCaptions = new HashMap<>();

	private ViewChangeListener viewChangeListener;

	private boolean isViewOnly;

	private DownloadingThread dlThread;

	public BaseDownloadField() {
		this(true, false);
	}

	public BaseDownloadField(boolean haveDeleteButton, boolean isViewOnly) {
		super();

		this.isViewOnly = isViewOnly;

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_NAME + "d-layout");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		fileDownload = new BaseFileDownload() {
			@Override
			protected void onDownloadWindowClosed(CloseEvent e) {
				BaseDownloadField.this.onDownloadWindowClosed(e);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void handleFile(File file, String fileName, String mimeType, long length) {
				try {
					file.deleteOnExit();
					TempFileDownload newTempFileDownload = new TempFileDownload(fileName, mimeType, length, file);
					Object newConvertedValue;
					Converter<Object, Object> converter = BaseDownloadField.this.getConverter();
					if (converter != null) {
						newConvertedValue = converter.convertToModel(newTempFileDownload, Object.class, getLocale());
					} else {
						newConvertedValue = newTempFileDownload;
					}

					closeUploadWindow();
					if (!newConvertedValue.equals(getConvertedValue())) {
						deleteTempFiles();
						BaseDownloadField.this.setValue(newConvertedValue);
					} else if (fireValueChangeWhenEqual()) {
						fireValueChange(true);
					}

				} catch (Throwable t) {
					t.printStackTrace();

					throw t;
				}
			}

		};
		fileDownload.setWidth("100%");
		fileDownload.addStyleName(STYLE_NAME + "-filedownload");

		addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				List<Object> listValue;
				Object newValue = BaseDownloadField.this.getValue();
				if (newValue instanceof List) {
					listValue = (List<Object>) newValue;
				} else {
					listValue = new ArrayList<Object>();
					if (newValue != null) {
						listValue.add(newValue);
					}
				}

				itemCaptions.clear();
				fileUploadsContainer.removeAllItems();
				for (Object listValueElement : listValue) {
					Object itemId = listValueElement;
					fileUploadsContainer.addItem(itemId);
					Item listValueElementItem = fileUploadsContainer.getItem(itemId);
					Component itemCaption = newItemCaption(itemId);
					listValueElementItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(itemCaption);
					itemCaptions.put(itemId, itemCaption);
				}
			}
		});

		fileUploadsContainer = new ButtonsContainer<>(new IndexedContainer() {
			public Property<?> getContainerProperty(final Object itemId, Object propertyId) {
				if (itemId != null && CAPTION_PROPERTY_ID.equals(propertyId)) {
					Component itemCaption = newItemCaption(itemId);
					return new ObjectProperty<Component>(itemCaption, Component.class);
				} else {
					return super.getContainerProperty(itemId, propertyId);
				}
			}
		});
		fileUploadsContainer.addContainerProperty(CAPTION_PROPERTY_ID, Component.class, null);

		if (haveDeleteButton) {
			fileUploadsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					DeleteButton deleteButton = new DeleteButton() {
						@SuppressWarnings("unchecked")
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							if (itemId instanceof TempFileDownload) {
								TempFileDownload tempFileDownload = (TempFileDownload) itemId;
								tempFileDownload.delete();
							} else {
								deleteTempFile(itemId);
							}
							BaseDownloadField.this.setValue(null);
						}
					};
					deleteButton.setReadOnly(BaseDownloadField.this.isReadOnly());
					deleteButton.setEnabled(BaseDownloadField.this.isEnabled());
					deleteButton.setVisible(isDeleteLink(itemId));
					return deleteButton;
				}
			});
		}
		fileDownloadsTable = new BaseTable(getClass().getName());
		fileDownloadsTable.setContainerDataSource(fileUploadsContainer);
		fileDownloadsTable.setPageLength(0);
		fileDownloadsTable.setWidth("100%");
		fileDownloadsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		fileDownloadsTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 47);
		fileDownloadsTable.addStyleName(STYLE_NAME + "-table");


		downloadTextField = new BaseTextField();
		downloadTextField.setInputPrompt("https://www.example.com/constellio.war");
		downloadTextField.setEnabled(!isViewOnly);
		downloadTextField.setValue("");
		downloadTextField.setWidth("100%");
		downloadTextField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (downloadTextField.getValue() != null && !downloadTextField.getValue().equals("")) {
					downloadButton.setEnabled(true && !isViewOnly);
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

						fileDownload.getHandler().filesQueued(asList(tempFileDownload));
						fileDownload.getHandler().downloadStarted(new DownloadEvent(tempFileDownload));

						dlThread = new DownloadingThread(fileDownload.getHandler(), successOnHeaders, tempFileDownload, BaseDownloadField.this.getUI(), fileDownload.getUI());
						dlThread.start();
					}
				} catch (InvalidWarUrl | IOException ex) {
					Logger.getLogger(getClass().getName()).log(Level.FINE,
							"Download failed", ex);
				}

			}
		};
		downloadButton.setEnabled(false);
		downloadButton.setWidth("33%");

		downloadTextField.setVisible(!isViewOnly);
		downloadButton.setVisible(!isViewOnly);
		fileDownload.setVisible(false);
		fileDownloadsTable.setVisible(!isViewOnly);

		mainLayout.addComponents(downloadTextField, downloadButton, fileDownload, fileDownloadsTable);
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
			itemCaption = tempFileDownload.getFileName();
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

	public final String getDropZoneCaption() {
		return fileDownload.getDropZoneCaption();
	}

	public final void setDropZoneCaption(String dropZoneCaption) {
		fileDownload.setDropZoneCaption(dropZoneCaption);
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
