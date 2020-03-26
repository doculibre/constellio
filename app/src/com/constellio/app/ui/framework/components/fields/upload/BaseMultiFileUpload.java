package com.constellio.app.ui.framework.components.fields.upload;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.ClickableNotification;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Item;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingErrorEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.MultiUpload;
import org.vaadin.easyuploads.MultiUpload.FileDetail;
import org.vaadin.easyuploads.MultiUploadHandler;
import org.vaadin.easyuploads.UploadField.FieldType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("deprecation")
public abstract class BaseMultiFileUpload extends CssLayout implements DropHandler, PollListener, ViewChangeListener {

	private CssLayout progressBars = new CssLayout();
	private CssLayout uploads = new CssLayout();
	private String uploadButtonCaption = "...";

	private BaseWindow uploadWindow;

	private VerticalLayout uploadWindowContent;

	private Label infoLabel;

	private UploadsTable uploadsTable;

	private DragAndDropWrapper dragAndDropWrapper;

	private DragAndDropWrapper dropZone;

	public static final String COMPLETE_STYLE_NAME = "base-multifileupload-completed";

	private String dropZoneCaption = $("BaseMultiFileUpload.dropZoneCaption");

	private String noDropZoneCaption = $("BaseMultiFileUpload.noDropZoneCaption");

	private int uiPollIntervalBefore = -1;

	public BaseMultiFileUpload() {
		super();
		//
		//		final CssLayout progressBars = (CssLayout) getComponent(0);
		//		progressBars.addComponentDetachListener(new ComponentDetachListener() {
		//			@Override
		//			public void componentDetachedFromContainer(ComponentDetachEvent event) {
		//				Component detachedComponent = event.getDetachedComponent();
		//				detachedComponent.addStyleName(COMPLETE_STYLE_NAME);
		//				progressBars.addComponent(detachedComponent, 0);
		//			}
		//		});

		setWidth("200px");
		uploads.setStyleName("v-multifileupload-uploads");

		if (isUploadWindow()) {
			infoLabel = new Label();
			infoLabel.addStyleName("upload-window-info-label");
			infoLabel.setVisible(false);
			infoLabel.setWidth("100%");
			infoLabel.setContentMode(ContentMode.HTML);

			uploadsTable = new UploadsTable();

			uploadWindowContent = new VerticalLayout();
			uploadWindowContent.addStyleName("upload-window-content");
			uploadWindowContent.setSpacing(true);
			uploadWindowContent.setWidth("100%");

			dragAndDropWrapper = new DragAndDropWrapper(uploadWindowContent);
			dragAndDropWrapper.setSizeFull();
			dragAndDropWrapper.setDropHandler(this);

			uploadWindow = new BaseWindow();
			uploadWindow.addStyleName("upload-window");
			uploadWindow.setClosable(false);
			uploadWindow.center();
			uploadWindow.setModal(true);
			uploadWindow.setWidth("90%");
			uploadWindow.setHeight(uploadsTable.getHeight() + "px");
			uploadWindow.setVisible(false);
			uploadWindow.addCloseListener(new CloseListener() {
				@Override
				public void windowClose(CloseEvent e) {
					infoLabel.setValue("");
					uploadWindow.setVisible(false);
					onUploadWindowClosed(e);
				}
			});

			uploadWindowContent.addComponents(infoLabel, uploadsTable);
			uploadWindow.setContent(dragAndDropWrapper);
		} else {
			addComponent(progressBars);
		}

		addComponent(uploads);

		prepareUpload();
	}

	protected boolean isUploadWindow() {
		return true;
	}

	public void setInfoMessage(String message) {
		infoLabel.setValue(message);
		infoLabel.setVisible(true);
	}

	protected String getAreaText() {
		return getDropZoneCaption();
	}

	public String getDropZoneCaption() {
		return dropZoneCaption;
	}

	public void setDropZoneCaption(String dropZoneCaption) {
		this.dropZoneCaption = dropZoneCaption;
	}

	private void addProgressIndicator(ProgressBar progressBar) {
		if (isUploadWindow()) {
			uploadsTable.addUpload(progressBar);
		} else {
			progressBars.addComponent(progressBar);
		}
	}

	private void removeProgressBar(ProgressBar progressBar) {
		if (!Toggle.PERFORMANCE_TESTING.isEnabled()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (isUploadWindow()) {
			uploadsTable.removeUpload(progressBar);
		} else {
			progressBars.removeComponent(progressBar);
		}
	}

	protected void onUploadWindowClosed(CloseEvent e) {

	}

	private void prepareUpload() {
		final FileBuffer receiver = createReceiver();

		final MultiUpload upload = new MultiUpload();
		MultiUploadHandler handler = new MultiUploadHandler() {
			private LinkedList<ProgressBar> progressBars;

			public void streamingStarted(StreamingStartEvent event) {
				if (isUploadWindow()) {
					showUploadWindowIfNotVisible();
				}
			}

			public void streamingFinished(StreamingEndEvent event) {
				if (!progressBars.isEmpty()) {
					removeProgressBar(progressBars.remove(0));
				}
				File file = receiver.getFile();
				handleFile(file, event.getFileName(), event.getMimeType(),
						event.getBytesReceived());
				receiver.setValue(null);
				if (isUploadWindow()) {
					closeUploadWindowIfAllDone();
				}
			}

			public void streamingFailed(StreamingErrorEvent event) {
				Logger.getLogger(getClass().getName()).log(Level.FINE,
						"Streaming failed", event.getException());

				for (ProgressBar progressBar : progressBars) {
					removeProgressBar(progressBar);
				}
				if (isUploadWindow()) {
					closeUploadWindowIfAllDone();
				}
			}

			public void onProgress(StreamingProgressEvent event) {
				long readBytes = event.getBytesReceived();
				long contentLength = event.getContentLength();
				float f = (float) readBytes / (float) contentLength;
				progressBars.get(0).setValue(f);
			}

			public OutputStream getOutputStream() {
				FileDetail next = upload.getPendingFileNames().iterator()
						.next();
				return receiver.receiveUpload(next.getFileName(),
						next.getMimeType());
			}

			public void filesQueued(Collection<FileDetail> pendingFileNames) {
				if (progressBars == null) {
					progressBars = new LinkedList<ProgressBar>();
				}
				for (FileDetail f : pendingFileNames) {
					ProgressBar pb = createProgressBar();
					pb.setCaption(f.getFileName());
					pb.setVisible(true);
					progressBars.add(pb);
					addProgressIndicator(pb);
				}
			}

			@Override
			public boolean isInterrupted() {
				return false;

			}
		};
		upload.setHandler(handler);
		upload.setButtonCaption(getUploadButtonCaption());
		uploads.addComponent(upload);
	}

	private ProgressIndicator createProgressBar() {
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setPollingInterval(300);
		progressIndicator.setValue(0f);
		return progressIndicator;
	}

	public String getUploadButtonCaption() {
		return uploadButtonCaption;
	}

	public void setUploadButtonCaption(String uploadButtonCaption) {
		this.uploadButtonCaption = uploadButtonCaption;
		Iterator<Component> componentIterator = uploads.getComponentIterator();
		while (componentIterator.hasNext()) {
			Component next = componentIterator.next();
			if (next instanceof MultiUpload) {
				MultiUpload upload = (MultiUpload) next;
				if (upload.isVisible()) {
					upload.setButtonCaption(getUploadButtonCaption());
				}
			}
		}
	}

	private FileFactory fileFactory;

	public FileFactory getFileFactory() {
		if (fileFactory == null) {
			fileFactory = new TempFileFactory();
		}
		return fileFactory;
	}

	public void setFileFactory(FileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}

	protected FileBuffer createReceiver() {
		FileBuffer receiver = new FileBuffer(FieldType.FILE) {
			@Override
			public FileFactory getFileFactory() {
				return BaseMultiFileUpload.this.getFileFactory();
			}
		};
		return receiver;
	}

	protected int getPollinInterval() {
		return 500;
	}

	private boolean isUploadInProgress() {
		boolean uploadInProgress;
		if (isUploadWindow()) {
			uploadInProgress = uploadsTable.size() > 0;
		} else {
			uploadInProgress = progressBars.iterator().hasNext();
		}
		return uploadInProgress;
	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		boolean allowNavigation;
		if (isUploadInProgress()) {
			String message = $("uploadInProgressNavigationBlocked");
			ClickableNotification.show(ConstellioUI.getCurrent(), "", message);
			allowNavigation = false;
		} else {
			allowNavigation = true;
		}
		return allowNavigation;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
	}

	@Override
	public void attach() {
		super.attach();
		if (supportsFileDrops()) {
			prepareDropZone();
		} else {
			prepareNoDropZone();
		}

		Navigator navigator = UI.getCurrent().getNavigator();
		if (navigator != null) {
			navigator.addViewChangeListener(this);
		}
		uiPollIntervalBefore = UI.getCurrent().getPollInterval();
		UI.getCurrent().addPollListener(this);
		UI.getCurrent().setPollInterval(getPollinInterval());
	}

	@Override
	public void detach() {
		UI.getCurrent().getNavigator().removeViewChangeListener(this);
		UI.getCurrent().removePollListener(this);
		UI.getCurrent().setPollInterval(uiPollIntervalBefore);
		super.detach();
	}

	@Override
	public void poll(PollEvent event) {
		if (isUploadWindow()) {
			uploadsTable.refreshRowCache();
		}
	}

	/**
	 * Sets up DragAndDropWrapper to accept multi file drops.
	 */
	private void prepareDropZone() {
		if (dropZone == null) {
			Component label = new Label(getAreaText(), Label.CONTENT_XHTML);
			label.setSizeUndefined();
			dropZone = new DragAndDropWrapper(label);
			dropZone.setStyleName("v-multifileupload-dropzone");
			dropZone.setSizeUndefined();
			//addComponent(dropZone, 1);
			addComponent(dropZone, 0);
			dropZone.setDropHandler(this);
			addStyleName("no-horizontal-drag-hints");
			addStyleName("no-vertical-drag-hints");
		} else {
			prepareNoDropZone();
		}
	}

	private void prepareNoDropZone() {
		Component label = new Label(noDropZoneCaption, Label.CONTENT_XHTML);
		label.setSizeUndefined();
		label.addStyleName("v-multifileupload-no-dropzone");
		//addComponent(dropZone, 1);
		addComponent(label, 0);
		addStyleName("no-horizontal-drag-hints");
		addStyleName("no-vertical-drag-hints");
	}

	protected boolean supportsFileDrops() {
		boolean supportsFileDrops;
		WebBrowser browser = getUI().getPage().getWebBrowser();
		if (ResponsiveUtils.isMobile()) {
			supportsFileDrops = false;
		} else if (browser.isChrome() || browser.isFirefox() || browser.isSafari()) {
			supportsFileDrops = true;
		} else {
			supportsFileDrops = false;
		}
		return supportsFileDrops;
	}

	abstract protected void handleFile(File file, String fileName,
									   String mimeType, long length);

	/**
	 * A helper method to set DirectoryFileFactory with given pathname as
	 * directory.
	 *
	 * @param directoryWhereToUpload
	 */
	public void setRootDirectory(String directoryWhereToUpload) {
		setFileFactory(new DirectoryFileFactory(
				new File(directoryWhereToUpload)));
	}

	public AcceptCriterion getAcceptCriterion() {
		// TODO accept only files
		// return new And(new TargetDetailIs("verticalLocation","MIDDLE"), new
		// TargetDetailIs("horizontalLoction", "MIDDLE"));
		return AcceptAll.get();
	}

	public void drop(DragAndDropEvent event) {
		DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event
				.getTransferable();
		Html5File[] files = transferable.getFiles();
		final List<String> emptyFilesName = new ArrayList<>();
		if (files != null) {
			for (final Html5File html5File : files) {
				final ProgressIndicator pi = new ProgressIndicator();
				pi.setCaption(html5File.getFileName());
				addProgressIndicator(pi);
				final FileBuffer receiver = createReceiver();
				html5File.setStreamVariable(new StreamVariable() {

					private String name;
					private String mime;
					private boolean isInterrupted;

					public OutputStream getOutputStream() {
						return receiver.receiveUpload(name, mime);
					}

					public boolean listenProgress() {
						return true;
					}

					public void onProgress(StreamingProgressEvent event) {
						float p = (float) event.getBytesReceived()
								  / (float) event.getContentLength();
						pi.setValue(p);
					}

					public void streamingStarted(StreamingStartEvent event) {
						name = event.getFileName();
						mime = event.getMimeType();
						if (isUploadWindow()) {
							showUploadWindowIfNotVisible();
						}
						isInterrupted = event.getContentLength() <= 0;
					}

					public void streamingFinished(StreamingEndEvent event) {
						removeProgressBar(pi);

						if (isInterrupted) {
							emptyFilesName.add(html5File.getFileName());
						} else {
							handleFile(receiver.getFile(), html5File.getFileName(),
									html5File.getType(), html5File.getFileSize());
							receiver.setValue(null);
						}

						if (isUploadWindow()) {
							closeUploadWindowIfAllDone(emptyFilesName);
						}
					}

					public void streamingFailed(StreamingErrorEvent event) {
						removeProgressBar(pi);
						if (isUploadWindow()) {
							closeUploadWindowIfAllDone();
						}
					}

					public boolean isInterrupted() {
						return false;
					}
				});
			}


		}
	}

	private void showUploadWindowIfNotVisible() {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				if (!uploadWindow.isVisible()) {
					uploadWindow.setVisible(true);
					UI.getCurrent().addWindow(uploadWindow);
				}
				setInfoMessage($("uploadInProgress"));
			}
		});
	}

	private void closeUploadWindowIfAllDone() {
		closeUploadWindowIfAllDone(Collections.<String>emptyList());
	}

	private void closeUploadWindowIfAllDone(final List<String> emptyFilesName) {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				if (!isUploadInProgress()) {
					UI.getCurrent().removeWindow(uploadWindow);

					if (!emptyFilesName.isEmpty()) {
						StringBuilder errorMessage = new StringBuilder(
								$("BaseMultiFileUpload.fileUploadCancel") + " :");
						for (String s : emptyFilesName) {
							errorMessage.append(" " + s);
						}

						Notification notification = new Notification(errorMessage.toString() + "<br/><br/>" +
																	 $("clickToClose"), Type.WARNING_MESSAGE);
						notification.setHtmlContentAllowed(true);
						notification.show(Page.getCurrent());
					}
				}
			}
		});
	}

}

@SuppressWarnings("deprecation")
class UploadsTable extends BaseTable {

	private static final String FILE_PROPERTY = "file";

	private static final String PROGRESS_BAR_PROPERTY = "progressBar";

	public UploadsTable() {
		super("multifileupload-uploads-table");
		addStyleName("multifileupload-uploads-table");
		addStyleName(ValoTheme.TABLE_BORDERLESS);

		setHeight("500px");
		setWidth("100%");

		addContainerProperty(FILE_PROPERTY, Component.class, "");
		addContainerProperty(PROGRESS_BAR_PROPERTY, ProgressIndicator.class, null);

		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

		setColumnWidth(PROGRESS_BAR_PROPERTY, 160);
		setColumnExpandRatio(FILE_PROPERTY, 1);

		setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String style;
				if (FILE_PROPERTY.equals(propertyId)) {
					Label filenameLabel = (Label) getItem(itemId).getItemProperty(propertyId).getValue();
					String filename = filenameLabel.getValue();
					String iconPath = FileIconUtils.getIconPath(filename);
					String extension;
					if (FileIconUtils.isDefaultIconPath(iconPath)) {
						extension = FileIconUtils.DEFAULT_VALUE;
					} else {
						extension = StringUtils.lowerCase(FilenameUtils.getExtension(filename));
					}
					style = "file-icon v-table-cell-content-file-icon-" + extension;
				} else {
					style = null;
				}
				return style;
			}
		});
	}

	@SuppressWarnings("unchecked")
	void addUpload(ProgressBar progressBar) {
		Item item = addItem(progressBar);

		String filename = progressBar.getCaption();
		Label filenameLabel = new Label(filename);
		progressBar.setCaption(null);

		item.getItemProperty(FILE_PROPERTY).setValue(filenameLabel);
		item.getItemProperty(PROGRESS_BAR_PROPERTY).setValue(progressBar);
	}

	void removeUpload(ProgressBar progressBar) {
		removeItem(progressBar);
	}

}

class TempFileFactory implements FileFactory {

	public File createFile(String fileName, String mimeType) {
		final String tempFileName = "upload_tmpfile_"
									+ System.currentTimeMillis();
		try {
			return File.createTempFile(tempFileName, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}