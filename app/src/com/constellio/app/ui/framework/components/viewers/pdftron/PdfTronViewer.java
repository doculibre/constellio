package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.BaseRequestHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.JavascriptUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.annotations.JavaScript;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

@JavaScript({"theme://jquery/jquery-2.1.4.min.js"})
public class PdfTronViewer extends VerticalLayout implements ViewChangeListener {

	private static final String CONTENT_RESOURCE_KEY_PREFIX = "document.file.";
	private static final String ANNOTATION_RESOURCE_KEY = "document.annotation";

	private Component canvas;
	private Resource documentContentResource;
	private String documentContentResourceKey;

	private Resource documentAnnotationResource;
	private String documentAnnotationResourceKey;

	private PdfTronViewerRequestHandler pdfTronViewerRequestHandler;
	private PdfTronPresenter pdfTronPresenter;

	private boolean isViewerInReadOnly;
	private boolean annotationEnabled = true;

	private Button editAnnotationBtn;
	private Label anOtherUserIdEditing;

	private VerticalLayout mainLayout;

	private boolean userHasRightToEditOtherUserAnnotation;
	private String documentContentUrl;
	private String documentAnnotationUrl;
	private String canvasId;


	public PdfTronViewer(DocumentVO documentVO, boolean userHasRightToEditOtherUserAnnotation) {

		String recordId = documentVO.getId();
		String metadataCode = Document.CONTENT;
		String filename = documentVO.getContent().getFileName();
		ConstellioUI current = ConstellioUI.getCurrent();

		this.documentContentResource = ConstellioResourceHandler.createResource(recordId, metadataCode, documentVO.getContent().getVersion(), filename);
		this.documentContentResourceKey = CONTENT_RESOURCE_KEY_PREFIX + UUID.randomUUID().toString();

		this.documentAnnotationResource = ConstellioResourceHandler.createAnnotationResource(recordId, metadataCode, documentVO.getContent().getVersion(), filename);
		this.documentAnnotationResourceKey = ANNOTATION_RESOURCE_KEY + UUID.randomUUID().toString();

		ResourceReference documentContentResourceReference = ResourceReference.create(documentContentResource, current, documentContentResourceKey);
		documentContentUrl = documentContentResourceReference.getURL();

		ResourceReference documentAnnotationResourceReference = ResourceReference.create(documentAnnotationResource, current, documentAnnotationResourceKey);
		documentAnnotationUrl = documentAnnotationResourceReference.getURL();

		canvasId = RandomStringUtils.random(13, true, true);

		this.userHasRightToEditOtherUserAnnotation = userHasRightToEditOtherUserAnnotation;

		this.pdfTronPresenter = new PdfTronPresenter(this, documentVO);

		setWidth("100%");
		setHeight("800px");
		mainLayout = new VerticalLayout();

		isViewerInReadOnly = !pdfTronPresenter.doesCurrentUserHaveAnnotationLock();


		editAnnotationBtn = new BaseButton(isViewerInReadOnly ? $("pdfTronViewer.editAnnotation") : $("pdfTronViewer.finalizeEditionAnnotation")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (isViewerInReadOnly) {
					if (pdfTronPresenter.obtainAnnotationLock()) {
						setWebViewerReadEditable();
						this.setCaption($("pdfTronViewer.finalizeEditionAnnotation"));
						isViewerInReadOnly = false;
					} else {
						addMessageIfAnOtherUserIsEditing();
						Notification.show($("pdfTronViewer.errorWhileGettingAnnotationLock"), Type.WARNING_MESSAGE);
					}
				} else {
					pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
					setWebViewerReadOnly();
					this.setCaption($("pdfTronViewer.editAnnotation"));
					isViewerInReadOnly = true;
				}
			}
		};
		editAnnotationBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		editAnnotationBtn.addStyleName(ValoTheme.BUTTON_LINK);

		Button consultAnnotation = new BaseButton($("pdfTronViewer.hideAnnotation")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (annotationEnabled) {
					com.vaadin.ui.JavaScript.eval("setEnableAnnotations(false)");
					editAnnotationBtn.addStyleName("disabled-link");
					this.setCaption($("pdfTronViewer.showAnnotation"));
					editAnnotationBtn.setEnabled(false);
					editAnnotationBtn.setImmediate(true);
					annotationEnabled = false;

				} else {
					com.vaadin.ui.JavaScript.eval("setEnableAnnotations(true)");
					editAnnotationBtn.setImmediate(true);
					editAnnotationBtn.removeStyleName("disabled-link");
					this.setCaption($("pdfTronViewer.hideAnnotation"));
					editAnnotationBtn.setEnabled(true);
					annotationEnabled = true;
				}
			}
		};

		boolean userHasWriteAccess = pdfTronPresenter.userHasWrtteAccessToDocument();

		consultAnnotation.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		consultAnnotation.addStyleName(ValoTheme.BUTTON_LINK);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");

		if (userHasWriteAccess) {
			buttonLayout.addComponent(editAnnotationBtn);
		}
		addMessageIfAnOtherUserIsEditing();

		buttonLayout.addComponent(consultAnnotation);

		buttonLayout.setComponentAlignment(consultAnnotation, Alignment.MIDDLE_RIGHT);
		mainLayout.setSizeFull();

		canvas = new Label();
		canvas.setId(canvasId);
		canvas.setHeight("100%");

		ensureRequestHandler();


		mainLayout.addComponent(buttonLayout);
		mainLayout.addComponents(canvas);
		mainLayout.setExpandRatio(canvas, 1);
		addComponent(mainLayout);
	}

	private void setWebViewerReadOnly() {
		com.vaadin.ui.JavaScript.eval("setWebViewerReadOnly(true)");
	}

	private void setWebViewerReadEditable() {
		com.vaadin.ui.JavaScript.eval("setWebViewerReadOnly(false)");
	}

	private void addMessageIfAnOtherUserIsEditing() {
		if (!pdfTronPresenter.userHasWrtteAccessToDocument()) {
			return;
		}

		String currentAnnotationLockUser = pdfTronPresenter.getCurrentAnnotationLockUser();
		boolean someOneElseIsEditingAnnotations = currentAnnotationLockUser != null && !currentAnnotationLockUser.equals(getCurrentSessionContext().getCurrentUser().getId());
		if (someOneElseIsEditingAnnotations) {
			anOtherUserIdEditing = new BaseLabel($("pdfTronViewer.someOneElseIdEditingAnnotation",
					pdfTronPresenter.getUserName(currentAnnotationLockUser)));
			mainLayout.addComponent(anOtherUserIdEditing, 0);
		}

		editAnnotationBtn.setEnabled(!someOneElseIsEditingAnnotations);
	}

	protected SessionContext getCurrentSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public AppLayerFactory getAppLayerFactory() {
		return ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
	}

	@Override
	public void attach() {
		super.attach();

		showWebViewer();
	}

	public void showWebViewer() {
		ConstellioUI current = ConstellioUI.getCurrent();
		UserVO currentUser = current.getSessionContext().getCurrentUser();
		String userFirstNameAndLastName = currentUser.getFirstName() + " " + currentUser.getLastName();


		String saveButtonCallbackURL = pdfTronViewerRequestHandler.getCallbackURL();

		StringBuilder toExecute = new StringBuilder();

		toExecute.append("canvasId='" + canvasId + "';");
		toExecute.append("isViewerReadOnlyOnInit=" + isViewerInReadOnly + ";");
		toExecute.append("documentContent='" + documentContentUrl + "';");
		toExecute.append("documentAnnotationRK='" + documentAnnotationResourceKey + "';");
		toExecute.append("documentAnnotationUrl='" + documentAnnotationUrl + "';");
		toExecute.append("documentAnnotationCallBack='" + saveButtonCallbackURL + "';");
		toExecute.append("name='" + userFirstNameAndLastName + " (" + currentUser.getUsername() + ")" + "';");
		toExecute.append("admin=" + userHasRightToEditOtherUserAnnotation + ";");

		com.vaadin.ui.JavaScript.eval(toExecute.toString());

		JavascriptUtils.loadScript("pdftron/lib/webviewer.min.js");
		JavascriptUtils.loadScript("pdftron/constellio-pdftron.js");
	}

	@Override
	public void detach() {
		super.detach();

		pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
	}

	private void ensureRequestHandler() {
		pdfTronViewerRequestHandler = new PdfTronViewerRequestHandler(documentAnnotationResourceKey);
		ConstellioUI.getCurrent().addRequestHandler(pdfTronViewerRequestHandler);
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(this);
	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		ConstellioUI.getCurrent().getNavigator().removeViewChangeListener(this);
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {

	}

	public class PdfTronViewerRequestHandler extends BaseRequestHandler {

		private String bpmnResourceKey;

		public PdfTronViewerRequestHandler(String bpmnFileResourceKey) {
			super(PdfTronViewerRequestHandler.class.getName());
			this.bpmnResourceKey = bpmnFileResourceKey;
		}

		public String getCallbackURL() {
			StringBuilder sb = new StringBuilder();
			sb.append(getPath());
			return sb.toString();
		}


		@Override
		protected boolean handleRequest(String requestURI, Map<String, String> paramsMap, User user,
										VaadinSession session, VaadinRequest request, final VaadinResponse response)
				throws IOException {
			boolean handled;
			String key = request.getParameter("resourceKey");
			if (bpmnResourceKey.equals(key)) {
				pdfTronPresenter.saveAnnotation(request.getParameter("data"));

				handled = true;
			} else {
				handled = false;
			}

			return handled;
		}

	}
}
