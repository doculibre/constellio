package com.constellio.app.modules.rm.ui.externalAccessUrl;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.base.BaseUnauthenticatedViewImpl;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;

public class ExternalUploadViewImpl extends BaseUnauthenticatedViewImpl implements ExternalUploadView, BrowserWindowResizeListener {

	private ExternalUploadPresenter presenter;
	VerticalLayout mainLayout;
	BaseUploadField uploadField;
	private BaseWindow bulkUploadWindow;

	public ExternalUploadViewImpl() {
		presenter = new ExternalUploadPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		addStyleName("external-upload-view");
		
		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("external-upload-view-main-layout");
		mainLayout.setSizeFull();

		final AtomicInteger uploadMaxCount = new AtomicInteger(0);
		final AtomicInteger streamProcessed = new AtomicInteger(0);
		uploadField = new ContentVersionUploadField(null, null) {
			@Override
			public boolean fireValueChangeWhenEqual() {
				return true;
			}

			@Override
			protected void onUploadWindowClosed(CloseEvent e) {
				presenter.uploadWindowClosed();
			}

			@Override
			public boolean mustUploadWithMinimumUIUpdatePossible(int fileToUploadCount) {
				uploadMaxCount.set(fileToUploadCount);
				return uploadMaxCount.get() > 1;
			}

			@Override
			public void streamingStarted(Html5File html5File, boolean isInterrupted) {
				super.streamingStarted(html5File, isInterrupted);
			}

			@Override
			public void streamingFinished(Html5File html5File, boolean isInterrupted) {
				super.streamingFinished(html5File, isInterrupted);

				if (streamProcessed.addAndGet(1) == 1 && uploadMaxCount.get() > 0) {
					ConstellioUI.getCurrent().addWindow(bulkUploadWindow);
				}

				if (streamProcessed.get() == uploadMaxCount.get()) {
					bulkUploadWindow.close();
					streamProcessed.set(0);
					uploadMaxCount.set(0);
				}
			}
		};
		uploadField.setSizeFull();
		uploadField.addStyleName("external-upload-view-upload-field");
		uploadField.setImmediate(true);
		uploadField.setMultiValue(false);
		uploadField.addValueChangeListener(createValueChangeListener(uploadMaxCount));

		mainLayout.addComponent(uploadField);
		
		DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(mainLayout) {
			@Override
			public void setDropHandler(DropHandler dropHandler) {
				if (ResponsiveUtils.isFileDropSupported()) {
					super.setDropHandler(dropHandler);
				}
			}
		};
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(uploadField);
		
		return dragAndDropWrapper;
	}

	@Override
	public boolean hasAccess() {
		return presenter.hasPageAccess(null, null);
	}

	@Override
	public void forParams(Map<String, String> params) {
		presenter.forParams(params);
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {

	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return true;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return null;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	private ValueChangeListener createValueChangeListener(AtomicInteger uploadMaxCount) {

		final List<ContentVersionVO> contentToAdd = new ArrayList<>();

		final String popupCssRoot = "display-folder-view-file-upload-bulk-popup";
		bulkUploadWindow = new BaseWindow();
		bulkUploadWindow.addStyleName(popupCssRoot);

		VerticalLayout windowLayout = new VerticalLayout();
		windowLayout.addStyleName(popupCssRoot + "-layout");

		final Label title = new Label();
		title.addStyleName("display-folder-view-file-upload-bulk-popup-title");
		title.setValue($("DisplayFolderView.ContentVersion.BulkUpload.title"));

		final Label uploadedFile = new Label();
		final Label remaningFiles = new Label();
		windowLayout.addComponents(title, uploadedFile, remaningFiles);


		I18NHorizontalLayout layout = new I18NHorizontalLayout();
		layout.setSizeUndefined();

		Label spinner = new Label();
		spinner.addStyleName(popupCssRoot + "-loading");

		layout.addComponents(spinner, windowLayout);

		bulkUploadWindow.setContent(layout);

		bulkUploadWindow.setModal(true);
		bulkUploadWindow.setResizable(false);

		bulkUploadWindow.addCloseListener(event -> {
			new Thread(() -> {
				presenter.contentVersionUploaded(contentToAdd);
				contentToAdd.clear();
			}).run();
		});

		return event -> {
			ContentVersionVO contentVersionVO = (ContentVersionVO) uploadField.getValue();
			contentToAdd.add(contentVersionVO);

			int uploadMaxCountValue = uploadMaxCount.get();

			ConstellioUI.getCurrent().access(() -> {
				uploadedFile.setValue($("DisplayFolderView.ContentVersion.BulkUpload.lastUploadedFileName", contentVersionVO.getFileName()));

				int remainingFiles = uploadMaxCountValue - contentToAdd.size();

				if (remainingFiles > 1) {
					remaningFiles.setValue($("DisplayFolderView.ContentVersion.BulkUpload.remaining", remainingFiles));
				} else {
					remaningFiles.setValue($("DisplayFolderView.ContentVersion.BulkUpload.remaining.single"));
				}
			});
		};
	}

	@Override
	public void clearUploadField() {

	}
}
