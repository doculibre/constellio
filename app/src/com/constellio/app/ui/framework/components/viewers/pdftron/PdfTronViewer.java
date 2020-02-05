package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.BaseRequestHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler.ResourceType;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.JavascriptUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditAnnotationWithoutLock;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
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
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

@JavaScript({"theme://jquery/jquery-2.1.4.min.js"})
@Slf4j
public class PdfTronViewer extends VerticalLayout implements ViewChangeListener {

	public static final String[] SUPPORTED_EXTENTION = {"pdf", "pdf/a", "xfdf", "fdf", "docx", "xlsx", "pptx", "jpg", "png", "mp4"};
	private static final String CONTENT_RESOURCE_KEY_PREFIX = "document.file.";
	private static final String ANNOTATION_RESOURCE_KEY = "document.annotation";
	public static final String PDFTRON_CANVAS_ID = "pdftron-canvas";

	public static final String ENGLISH_CODE = "en";
	public static final String ARABIC_CODE = "ar";
	public static final String FRENCH_CODE = "fr";

	private Component canvas;
	private Resource documentContentResource;
	private String documentContentResourceKey;

	private Resource documentAnnotationResource;
	private String documentAnnotationResourceKey;

	private PdfTronViewerRequestHandler pdfTronViewerRequestHandler;
	private PdfTronPresenter pdfTronPresenter;

	private boolean isViewerInReadOnly;
	private boolean annotationEnabled = true;

	private GetAnnotationsOfOtherVersionWindowButton getAnnotationOfOtherVersionWindowButton;
	private HorizontalLayout getAnnotationOfOtherVersionLayout;
	private Button editAnnotationBtn;
	private Label anOtherUserIdEditing;
	private Label anOtherPageIsEdtting;

	private VerticalLayout mainLayout;

	private boolean userHasRightToEditOtherUserAnnotation;
	private String documentContentUrl;
	private String documentAnnotationUrl;
	private String canvasId;

	private String recordId;
	private String pdfTronLicense;
	private ThreadState threadState = null;
	private Button topRightButton = null;

	private String searchTerm = null;


	public PdfTronViewer(String recordId, ContentVersionVO contentVersion, String metadataCode, boolean readOnlyMode,
						 String license) {

		this.recordId = recordId;
		String filename = contentVersion.getFileName();
		ConstellioUI current = ConstellioUI.getCurrent();

		if (ArrayUtils.contains(PdfTronViewer.SUPPORTED_EXTENTION, FilenameUtils.getExtension(filename))) {
			this.documentContentResource = ConstellioResourceHandler.createResource(recordId, metadataCode, contentVersion.getVersion(), filename, ResourceType.NORMAL, false, contentVersion.getContentId());
		} else {
			ContentManager contentManager = getAppLayerFactory().getModelLayerFactory().getContentManager();
			if (contentManager.hasContentPreview(contentVersion.getHash())) {
				this.documentContentResource = ConstellioResourceHandler.createPreviewResource(recordId, metadataCode, contentVersion.getVersion(), filename);
			} else {
				this.setVisible(false);
				return;
			}
		}

		this.documentContentResourceKey = CONTENT_RESOURCE_KEY_PREFIX + UUID.randomUUID().toString();
		ResourceReference documentContentResourceReference = ResourceReference.create(documentContentResource, current, documentContentResourceKey);
		documentContentUrl = documentContentResourceReference.getURL();

		this.pdfTronLicense = license;

		this.documentAnnotationResource = ConstellioResourceHandler.createAnnotationResource(recordId, metadataCode, contentVersion.getVersion(), filename, contentVersion.getContentId());
		this.documentAnnotationResourceKey = ANNOTATION_RESOURCE_KEY + UUID.randomUUID().toString();

		ResourceReference documentAnnotationResourceReference = ResourceReference.create(documentAnnotationResource, current, documentAnnotationResourceKey);
		documentAnnotationUrl = documentAnnotationResourceReference.getURL();

		canvasId = PDFTRON_CANVAS_ID + RandomStringUtils.random(13, true, true);

		this.pdfTronPresenter = new PdfTronPresenter(this, this.recordId, metadataCode, contentVersion);

		this.userHasRightToEditOtherUserAnnotation = pdfTronPresenter.hasEditAllAnnotation();

		setWidth("100%");
		setHeight("800px");
		mainLayout = new VerticalLayout();

		isViewerInReadOnly = !pdfTronPresenter.doesCurrentPageHaveLock();

		this.getAnnotationOfOtherVersionWindowButton = new GetAnnotationsOfOtherVersionWindowButton(pdfTronPresenter, new Refresh() {
			@Override
			public void refresh() {
				rePullAnnotationsInPdfTron();
			}
		});

		this.getAnnotationOfOtherVersionLayout = new HorizontalLayout();
		this.getAnnotationOfOtherVersionLayout.setWidth("100%");
		this.getAnnotationOfOtherVersionLayout.addComponent(getAnnotationOfOtherVersionWindowButton);
		this.getAnnotationOfOtherVersionLayout.setVisible(false);

		editAnnotationBtn = new BaseButton(isViewerInReadOnly ? $("pdfTronViewer.editAnnotation") : $("pdfTronViewer.finalizeEditionAnnotation")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (isViewerInReadOnly) {
					if (pdfTronPresenter.obtainAnnotationLock()) {
						startAliveCallBack();
						setWebViewerReadEditable();
						this.setCaption($("pdfTronViewer.finalizeEditionAnnotation"));
						hideGetAnnotationFromPreviousVersion(userHasRightToEditOtherUserAnnotation && pdfTronPresenter.getAvailableVersion().size() > 0);
						isViewerInReadOnly = false;
					} else {
						addMessageIfAnOtherUserOrAnOtherPageIsEditing(true);
					}
				} else {
					stopThreadAndDontReleaseLock();
					pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
					setWebViewerReadOnly();
					this.setCaption($("pdfTronViewer.editAnnotation"));
					isViewerInReadOnly = true;
					hideGetAnnotationFromPreviousVersion(false);
				}
			}
		};
		editAnnotationBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		editAnnotationBtn.addStyleName(ValoTheme.BUTTON_LINK);

		Button enableDisableAnnotation = new BaseButton($("pdfTronViewer.hideAnnotation")) {
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

		boolean userHasWriteAccess = pdfTronPresenter.hasWriteAccessToDocument() && !readOnlyMode;

		hideGetAnnotationFromPreviousVersion(false);

		enableDisableAnnotation.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		enableDisableAnnotation.addStyleName(ValoTheme.BUTTON_LINK);


		HorizontalLayout buttonLayout2 = new HorizontalLayout();
		buttonLayout2.setWidth("100%");

		if (userHasWriteAccess) {
			buttonLayout2.addComponent(editAnnotationBtn);
		}
		addMessageIfAnOtherUserOrAnOtherPageIsEditing(false);

		buttonLayout2.addComponent(enableDisableAnnotation);

		buttonLayout2.setComponentAlignment(enableDisableAnnotation, Alignment.MIDDLE_RIGHT);
		mainLayout.setSizeFull();

		canvas = new Label();
		canvas.setId(canvasId);
		canvas.setHeight("100%");

		ensureRequestHandler();

		mainLayout.addComponent(getAnnotationOfOtherVersionLayout);
		mainLayout.addComponent(buttonLayout2);
		mainLayout.addComponents(canvas);
		mainLayout.setExpandRatio(canvas, 1);
		addComponent(mainLayout);
	}


	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	private void rePullAnnotationsInPdfTron() {
		com.vaadin.ui.JavaScript.eval("rePullAnnotations()");
	}

	private void hideGetAnnotationFromPreviousVersion(boolean visible) {
		if (topRightButton != null) {
			getAnnotationOfOtherVersionWindowButton.setVisible(visible);
			getAnnotationOfOtherVersionLayout.setVisible(true);
		} else {
			getAnnotationOfOtherVersionLayout.setVisible(visible);
			getAnnotationOfOtherVersionWindowButton.setVisible(visible);
		}
	}

	public void setTopRightButton(Button button) {
		this.getAnnotationOfOtherVersionLayout.addComponent(button);
		this.getAnnotationOfOtherVersionLayout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
		this.getAnnotationOfOtherVersionLayout.setVisible(true);
		this.topRightButton = button;
	}

	public void releaseRessource() {
		pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
		stopThreadAndDontReleaseLock();
	}


	private void stopThreadAndDontReleaseLock() {
		if (this.threadState != null) {
			PdfTronViewer.this.threadState.stopAndDontRealseLock();
		}
	}

	private class ThreadState {
		public ThreadState(long maxTimeWhenNoCall) {
			lastReceiveCall = null;
			initialStart = System.currentTimeMillis();
			maxTimeWhenNocall = maxTimeWhenNoCall;
		}

		private boolean keepLooping = true;
		private Long lastReceiveCall;
		private Long initialStart;
		private long maxTimeWhenNocall;
		private boolean dontReleaseLock = false;

		public void lastReceiveCall(long currentTime) {
			lastReceiveCall = currentTime;
		}

		public void evaluate(long maxDelai) {
			long currentTime = System.currentTimeMillis();
			if ((lastReceiveCall == null && initialStart < (currentTime - maxTimeWhenNocall)) || lastReceiveCall != null && lastReceiveCall < (currentTime - maxDelai)) {
				this.stop();
			}
		}

		public boolean isKeepLooping() {
			return keepLooping;
		}

		public boolean isDontReleaseLock() {
			return dontReleaseLock;
		}

		public void stop() {
			keepLooping = false;
		}

		public void stopAndDontRealseLock() {
			dontReleaseLock = true;
			stop();
		}
	}

	public void startAliveCallBack() {

		this.threadState = new ThreadState(20000); // 20 seconds if no call is sucessfull
		final ThreadState threadState = this.threadState;
		ConstellioUI.getCurrent().runAsync(new Runnable() {
			@Override
			public void run() {
				while (threadState != null && threadState.isKeepLooping()) {
					ConstellioUI.getCurrent().access(new Runnable() {
						@Override
						public void run() {
							final String functionId = "hearthBeatCallBack";
							com.vaadin.ui.JavaScript.getCurrent().addFunction(functionId,
									new JavaScriptFunction() {
										@Override
										public void call(JsonArray arguments) {
											threadState.lastReceiveCall(System.currentTimeMillis());
										}
									});
							StringBuilder js = new StringBuilder();
							js.append("    setTimeout(function() {");
							js.append("        " + functionId + "();");
							js.append("    }, 10000);");
							threadState.evaluate(26000);

							com.vaadin.ui.JavaScript.getCurrent().execute(js.toString());
						}
					});
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				if (!threadState.isDontReleaseLock()) {
					pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
				}
			}
		}, 10, this);
	}


	public static String getPdfTronKey(AppLayerFactory appLayerFactory) {
		return appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.PDFTRON_LICENSE);
	}

	public static String getPdfTronKey() {
		return getPdfTronKey(getAppLayerFactory());
	}

	private void setWebViewerReadOnly() {
		com.vaadin.ui.JavaScript.eval("setWebViewerReadOnly(true)");
	}

	private void setWebViewerReadEditable() {
		com.vaadin.ui.JavaScript.eval("setWebViewerReadOnly(false)");
	}

	private void addMessageIfAnOtherUserOrAnOtherPageIsEditing(boolean showNotification) {
		if (!pdfTronPresenter.hasWriteAccessToDocument()) {
			return;
		}

		String currentAnnotationLockUser = pdfTronPresenter.getUserIdThatHaveAnnotationLock();
		boolean someOneElseIsEditingAnnotations = currentAnnotationLockUser != null && !currentAnnotationLockUser.equals(getCurrentSessionContext().getCurrentUser().getId());

		if (someOneElseIsEditingAnnotations) {
			anOtherUserIdEditing = new BaseLabel($("pdfTronViewer.someOneElseIdEditingAnnotation",
					pdfTronPresenter.getUserName(currentAnnotationLockUser)));
			mainLayout.addComponent(anOtherUserIdEditing, 0);

			if (showNotification) {
				Notification.show($("pdfTronViewer.errorAnOtherUserHasAnnotationLock"), Type.WARNING_MESSAGE);
			}

			editAnnotationBtn.setEnabled(false);
		} else if (pdfTronPresenter.doesUserHaveLock() && !pdfTronPresenter.doesCurrentPageHaveLock()) {
			anOtherPageIsEdtting = new BaseLabel($("pdfTronViewer.anOtherPageIsEditting"));
			mainLayout.addComponent(anOtherPageIsEdtting, 0);
			editAnnotationBtn.setEnabled(false);

			if (showNotification) {
				Notification.show($("pdfTronViewer.errorAnOtherPageHaveAnnotationLock"), Type.WARNING_MESSAGE);
			}
		}

	}

	protected SessionContext getCurrentSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public static AppLayerFactory getAppLayerFactory() {
		return ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
	}

	@Override
	public void attach() {
		super.attach();
		showWebViewer();
	}

	public void showWebViewer() {
		pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
		stopThreadAndDontReleaseLock();

		ConstellioUI current = ConstellioUI.getCurrent();
		UserVO currentUser = current.getSessionContext().getCurrentUser();
		String userFirstNameAndLastName = currentUser.getFirstName() + " " + currentUser.getLastName();


		String saveButtonCallbackURL = pdfTronViewerRequestHandler.getCallbackURL();

		StringBuilder toExecute = new StringBuilder();

		toExecute.append("canvasId='" + canvasId + "';");
		toExecute.append("ignoreAnnotationChange='" + true + "';");
		toExecute.append("documentContent='" + documentContentUrl + "';");
		toExecute.append("documentAnnotationRK='" + documentAnnotationResourceKey + "';");
		toExecute.append("documentAnnotationUrl='" + documentAnnotationUrl + "';");
		toExecute.append("documentAnnotationCallBack='" + saveButtonCallbackURL + "';");
		toExecute.append("name='" + userFirstNameAndLastName + " (" + currentUser.getUsername() + ")" + "';");
		toExecute.append("admin=" + userHasRightToEditOtherUserAnnotation + ";");
		toExecute.append("license=" + pdfTronLicense + ";");
		toExecute.append("isReadOnly=" + isViewerInReadOnly + ";");
		toExecute.append("language='" + getPdfTronLanguageCode() + "';");

		if (searchTerm != null) {
			toExecute.append("searchTerm='" + searchTerm + "';");
		} else {
			toExecute.append("searchTerm=undefined;");
		}

		com.vaadin.ui.JavaScript.eval(toExecute.toString());

		JavascriptUtils.loadScript("pdftron/lib/webviewer.min.js");
		JavascriptUtils.loadScript("pdftron/constellio-pdftron.js");
	}

	public String getPdfTronLanguageCode() {
		String locale = getCurrentSessionContext().getCurrentLocale().getLanguage();

		switch (locale) {
			case ENGLISH_CODE:
			case ARABIC_CODE:
				return "en";
			case FRENCH_CODE:
				return "fr";
			default:
				throw new IllegalStateException("Language not supported for PDFtron");
		}
	}

	@Override
	public void detach() {
		super.detach();
		pdfTronPresenter.releaseAnnotationLockIfUserhasIt();
		stopThreadAndDontReleaseLock();
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
				try {
					pdfTronPresenter.handleNewXml(request.getParameter("data"), userHasRightToEditOtherUserAnnotation, user.getId());
				} catch (PdfTronXMLException_CannotEditOtherUsersAnnoations pdfTronXMLException_cannotEditOtherUsersAnnoations) {
					log.error("do not have permission to edit other user annotation parsing error", pdfTronXMLException_cannotEditOtherUsersAnnoations);
					response.getWriter().write(
							createErrorJSONResponse("Could not update. Some modifications are " +
													"invalid"));

				} catch (PdfTronXMLException_XMLParsingException e) {
					log.error("unexpected xml parsing error", e);
					response.getWriter().write(
							createErrorJSONResponse("Invalid xml"));

				} catch (PdfTronXMLException_IOExeption pdfTronXMLException_ioExeption) {
					log.error("unexpected io error", pdfTronXMLException_ioExeption);
					response.getWriter().write(
							createErrorJSONResponse("Unexpected IO error"));
				} catch (PdfTronXMLException_CannotEditAnnotationWithoutLock pdfTronXMLException_cannotEditAnnotationWithoutLock) {
					log.error("cannot edit while not having the page lock");
					response.getWriter().write(
							createErrorJSONResponse("cannot edit while not having the page lock"));
				}
				handled = true;
			} else {
				handled = false;
			}

			return handled;
		}

		private String createErrorJSONResponse(String errorMessage) {
			JSONObject rootJsonObject = new JSONObject();
			rootJsonObject.put("error", errorMessage);

			return rootJsonObject.toJSONString();
		}

	}
}
