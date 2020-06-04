package com.constellio.app.ui.framework.components.fields.upload;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.ClickableNotification;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.easyuploads.MultiUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("deprecation")
public abstract class BaseMultiFileUpload extends CssLayout implements DropHandler, PollListener, ViewChangeListener {

	private CssLayout progressBars = new CssLayout();
	private CssLayout uploads = new CssLayout();
	private String uploadButtonCaption = "...";

	private Label infoLabel;

	private DragAndDropWrapper dragAndDropWrapper;

	private DragAndDropWrapper dropZone;

	public static final String COMPLETE_STYLE_NAME = "base-multifileupload-completed";

	private String dropZoneCaption = $("BaseMultiFileUpload.dropZoneCaption");

	private String noDropZoneCaption = $("BaseMultiFileUpload.noDropZoneCaption");

	private int uiPollIntervalBefore = -1;

	private UploadFinishedHandler uploadFinishedHandler = new UploadFinishedHandler() {
		@Override
		public void handleFile(InputStream inputStream, String fileName, String mimeType, long length,
							   int filesLeftInQueue) {
			ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
			IOServicesFactory ioServicesFactory = constellioFactories.getDataLayerFactory().getIOServicesFactory();
			FileService fileService = ioServicesFactory.newFileService();
			IOServices ioServices = ioServicesFactory.newIOServices();

			File file = fileService.newTemporaryFile(fileName);
			file.deleteOnExit();
			try (OutputStream outputStream = ioServices.newFileOutputStream(file, getClass().getName())) {
				ioServicesFactory.newIOServices().copyAndClose(inputStream, outputStream);
				BaseMultiFileUpload.this.handleFile(file, fileName, mimeType, length, filesLeftInQueue);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private UploadStateWindow uploadStateWindow = new UploadStateWindow();

	private MultiFileUpload multiFileUpload;

	public BaseMultiFileUpload() {
		this(false);
	}

	public BaseMultiFileUpload(boolean multiValue) {
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

		String uploadButtonCaption = getUploadButtonCaption();
		String dropZoneCaption = getDropZoneCaption();

		uploadStateWindow = new UploadStateWindow();
		uploadStateWindow.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				BaseMultiFileUpload.this.onUploadWindowClosed(e);
			}
		});

		multiFileUpload = new MultiFileUpload(uploadFinishedHandler, uploadStateWindow, multiValue);
		multiFileUpload.setCaption(dropZoneCaption);
		multiFileUpload.setPanelCaption("Ze Test");
		multiFileUpload.getSmartUpload().setUploadButtonCaptions(uploadButtonCaption, uploadButtonCaption);

		//dragAndDropWrapper = multiFileUpload.createDropComponent(this);

		addComponent(multiFileUpload);
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

	protected void onUploadWindowClosed(CloseEvent e) {

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

	protected int getPollinInterval() {
		return 500;
	}

	private boolean isUploadInProgress() {
		return false;
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
	}

	@Override
	public void drop(DragAndDropEvent event) {
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

	public AcceptCriterion getAcceptCriterion() {
		// TODO accept only files
		// return new And(new TargetDetailIs("verticalLocation","MIDDLE"), new
		// TargetDetailIs("horizontalLoction", "MIDDLE"));
		return AcceptAll.get();
	}

	protected abstract void handleFile(File file, String fileName, String mimeType, long length, int filesLeftInQueue);

}	