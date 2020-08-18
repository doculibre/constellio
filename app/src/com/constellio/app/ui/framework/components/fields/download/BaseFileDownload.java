package com.constellio.app.ui.framework.components.fields.download;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.ClickableNotification;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Item;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("deprecation")
public abstract class BaseFileDownload extends CssLayout implements PollListener, ViewChangeListener {

	private CssLayout progressBars = new CssLayout();
	private CssLayout download = new CssLayout();
	private FileDownloadHandler handler;
	private String downloadButtonCaption = "...";

	private BaseWindow downloadWindow;

	private VerticalLayout downloadWindowContent;

	private Label infoLabel;

	private DownloadTable downloadTable;

	public static final String COMPLETE_STYLE_NAME = "base-filedownload-completed";

	private String dropZoneCaption = $("BaseFileDownload.dropZoneCaption");

	private String noDropZoneCaption = $("BaseFileDownload.noDropZoneCaption");

	private int uiPollIntervalBefore = -1;

	public BaseFileDownload() {
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
		download.setStyleName("v-multifileupload-download");

		if (getDownloadWindow()) {
			infoLabel = new Label();
			infoLabel.addStyleName("upload-window-info-label");
			infoLabel.setVisible(false);
			infoLabel.setWidth("100%");
			infoLabel.setContentMode(ContentMode.HTML);

			downloadTable = new DownloadTable();

			downloadWindowContent = new VerticalLayout();
			downloadWindowContent.addStyleName("upload-window-content");
			downloadWindowContent.setSpacing(true);
			downloadWindowContent.setWidth("100%");

			downloadWindow = new BaseWindow();
			downloadWindow.addStyleName("upload-window");
			downloadWindow.setClosable(false);
			downloadWindow.center();
			downloadWindow.setModal(true);
			downloadWindow.setWidth("90%");
			downloadWindow.setHeight(downloadTable.getHeight() + "px");
			downloadWindow.setVisible(false);
			downloadWindow.addCloseListener(new CloseListener() {
				@Override
				public void windowClose(CloseEvent e) {
					infoLabel.setValue("");
					downloadWindow.setVisible(false);
					onDownloadWindowClosed(e);
				}
			});

			downloadWindowContent.addComponents(infoLabel, downloadTable);
			downloadWindow.setContent(downloadWindowContent);
		} else {
			addComponent(progressBars);
		}

		addComponent(download);

		prepareDownload();
	}

	public FileDownloadHandler getHandler() {
		return handler;
	}

	protected boolean getDownloadWindow() {
		return true;
	}

	public void setInfoMessage(String message) {
		infoLabel.setValue(message);
		infoLabel.setVisible(true);
	}

	public String getDropZoneCaption() {
		return dropZoneCaption;
	}

	public void setDropZoneCaption(String dropZoneCaption) {
		this.dropZoneCaption = dropZoneCaption;
	}

	private void addProgressIndicator(ProgressBar progressBar) {
		if (getDownloadWindow()) {
			downloadTable.addDownload(progressBar);
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
		if (getDownloadWindow()) {
			downloadTable.removeDownload(progressBar);
		} else {
			progressBars.removeComponent(progressBar);
		}
	}

	protected void onDownloadWindowClosed(CloseEvent e) {

	}

	private void prepareDownload() {
		handler = new FileDownloadHandler() {
			private LinkedList<ProgressBar> progressBars;

			public void downloadStarted(DownloadEvent event) {
				if (getDownloadWindow()) {
					showUploadWindowIfNotVisible();
				}
			}

			public void downloadFinished(DownloadEvent event) {
				if (!progressBars.isEmpty()) {
					removeProgressBar(progressBars.remove(0));
				}
				File file = event.getTempFileDownload().getTempFile();
				handleFile(file, event.getTempFileDownload().getFileName(), event.getTempFileDownload().getMimeType(),
						event.getTempFileDownload().getLength());

				if (getDownloadWindow()) {
					closeUploadWindowIfAllDone();
				}
			}

			public void downloadFailed(DownloadEvent event) {
				Logger.getLogger(getClass().getName()).log(Level.FINE,
						"Streaming failed", event.getException());

				for (ProgressBar progressBar : progressBars) {
					removeProgressBar(progressBar);
				}
				if (getDownloadWindow()) {
					closeUploadWindowIfAllDone();
				}
			}

			public void onProgress(DownloadEvent event) {
				long readBytes = event.getProgressDownload();
				long contentLength = event.getTempFileDownload().getLength();
				float f = (float) readBytes / (float) contentLength;
				progressBars.get(0).setValue(f);
			}

			public void filesQueued(Collection<TempFileDownload> pendingFileNames) {
				if (progressBars == null) {
					progressBars = new LinkedList<>();
				}
				for (TempFileDownload f : pendingFileNames) {
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
	}

	private ProgressIndicator createProgressBar() {
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setPollingInterval(300);
		progressIndicator.setValue(0f);
		return progressIndicator;
	}

	public String getDownloadButtonCaption() {
		return downloadButtonCaption;
	}

	protected int getPollinInterval() {
		return 500;
	}

	private boolean isDownloadInProgress() {
		boolean downloadInProgress;
		if (getDownloadWindow()) {
			downloadInProgress = downloadTable.size() > 0;
		} else {
			downloadInProgress = progressBars.iterator().hasNext();
		}
		return downloadInProgress;
	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		boolean allowNavigation;
		if (isDownloadInProgress()) {
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

		prepareNoDropZone();

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
		closeUploadWindow();
		UI.getCurrent().getNavigator().removeViewChangeListener(this);
		UI.getCurrent().removePollListener(this);
		UI.getCurrent().setPollInterval(uiPollIntervalBefore);
		super.detach();
	}

	@Override
	public void poll(PollEvent event) {
		if (getDownloadWindow()) {
			downloadTable.refreshRowCache();
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

	abstract protected void handleFile(File file, String fileName,
									   String mimeType, long length);

	public AcceptCriterion getAcceptCriterion() {
		// TODO accept only files
		// return new And(new TargetDetailIs("verticalLocation","MIDDLE"), new
		// TargetDetailIs("horizontalLoction", "MIDDLE"));
		return AcceptAll.get();
	}

	private void showUploadWindowIfNotVisible() {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				if (!downloadWindow.isVisible()) {
					downloadWindow.setVisible(true);
					UI.getCurrent().addWindow(downloadWindow);
				}
				setInfoMessage($("uploadInProgress"));
			}
		});
	}

	protected void closeUploadWindow() {
		UI.getCurrent().removeWindow(downloadWindow);
	}

	private void closeUploadWindowIfAllDone() {
		closeUploadWindowIfAllDone(Collections.<String>emptyList());
	}

	private void closeUploadWindowIfAllDone(final List<String> emptyFilesName) {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				if (!isDownloadInProgress()) {
					closeUploadWindow();
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
class DownloadTable extends BaseTable {

	private static final String FILE_PROPERTY = "file";

	private static final String PROGRESS_BAR_PROPERTY = "progressBar";

	public DownloadTable() {
		super("multifileupload-download-table");
		addStyleName("multifileupload-download-table");
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
	void addDownload(ProgressBar progressBar) {
		Item item = addItem(progressBar);

		String filename = progressBar.getCaption();
		Label filenameLabel = new Label(filename);
		progressBar.setCaption(null);

		item.getItemProperty(FILE_PROPERTY).setValue(filenameLabel);
		item.getItemProperty(PROGRESS_BAR_PROPERTY).setValue(progressBar);
	}

	void removeDownload(ProgressBar progressBar) {
		removeItem(progressBar);
	}

}