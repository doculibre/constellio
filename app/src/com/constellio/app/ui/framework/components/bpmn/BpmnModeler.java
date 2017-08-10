package com.constellio.app.ui.framework.components.bpmn;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseRequestHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.util.JavascriptUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
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
import com.vaadin.ui.VerticalLayout;

@JavaScript({ "theme://jquery/jquery-2.1.4.min.js", "theme://bpmn/bpmn-modeler.js" }) 
@StyleSheet({"theme://bpmn/assets/diagram-js.css", "theme://bpmn/assets/bpmn-font/css/bpmn-embedded.css"})
public class BpmnModeler extends VerticalLayout {
	
	private static final String RESOURCE_KEY_PREFIX = "BpmnModeler.file.";
	
	private Component canvas;
	private Button saveButton;
	private BpmnSaveRequestHandler saveRequestHandler;
	private Resource bpmnResource;
	private String bpmnResourceKey;
	
	static Resource defaultResource() {
		File bpmnFile = new File("C:\\git\\constellio-dev2\\constellio\\app\\WebContent\\VAADIN\\themes\\constellio\\bpmn\\constellio-test.bpmn");
		return ConstellioResourceHandler.createResource(bpmnFile);
	}
	
	public BpmnModeler() {
		this(defaultResource());
	}
	
	public BpmnModeler(Resource bpmnFileResource) {
		this.bpmnResource = bpmnFileResource;

		bpmnResourceKey = RESOURCE_KEY_PREFIX + UUID.randomUUID().toString();
		ensureRequestHandler();
		
		setWidth("100%");
		setHeight("600px");
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();
		
		canvas = new Label();
		canvas.setId("bpmn-canvas");
		canvas.setHeight("100%");
		
		saveButton = new Button("Save Diagram");
		saveButton.setId("bpmn-save-button");
		
		mainLayout.addComponents(canvas, saveButton);
		mainLayout.setExpandRatio(canvas, 1);
		mainLayout.setComponentAlignment(saveButton, Alignment.TOP_RIGHT);
		addComponent(mainLayout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		ResourceReference bpmnResourceReference = ResourceReference.create(bpmnResource, ConstellioUI.getCurrent(), bpmnResourceKey);
		String bpmnFileURL = bpmnResourceReference.getURL();
		
		String saveButtonCallbackURL = saveRequestHandler.getCallbackURL();
		com.vaadin.ui.JavaScript.eval("bpmnDiagramURL='" + bpmnFileURL + "';");
		com.vaadin.ui.JavaScript.eval("saveButtonCallbackURL='" + saveButtonCallbackURL + "';");
		com.vaadin.ui.JavaScript.eval("bpmnResourceKey='" + bpmnResourceKey + "';");
//		com.vaadin.ui.JavaScript.eval("$('canvas').height='500px';");
		
        JavascriptUtils.loadScript("bpmn/bpmn-modeler.js");
        JavascriptUtils.loadScript("bpmn/constellio-modeler.js");
	}

	private void ensureRequestHandler() {
		saveRequestHandler = new BpmnSaveRequestHandler(bpmnResourceKey);
		if (!ConstellioUI.getCurrent().getRequestHandlers().contains(saveRequestHandler)) {
			ConstellioUI.getCurrent().addRequestHandler(saveRequestHandler);
		}
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				ConstellioUI.getCurrent().removeRequestHandler(saveRequestHandler);
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
			}
		});
	}
	
	protected void saveBpmn(String xml) {
		System.out.println(xml);
	}
	
	public class BpmnSaveRequestHandler extends BaseRequestHandler {
		
		private String bpmnResourceKey;
		
		public BpmnSaveRequestHandler(String bpmnFileResourceKey) {
			super(BpmnSaveRequestHandler.class.getName());
			this.bpmnResourceKey = bpmnFileResourceKey;
		}
		
		public String getCallbackURL() {
			StringBuilder sb = new StringBuilder();
			sb.append(getPath());
			return sb.toString();
		}

		@Override
		protected boolean handleRequest(String requestURI, Map<String, String> paramsMap, User user, VaadinSession session, VaadinRequest request, VaadinResponse response)
				throws IOException {
			boolean handled;
			String key = request.getParameter("bpmnResourceKey");
			if (bpmnResourceKey.equals(key)) {
				String xml = request.getParameter("xml");
				saveBpmn(xml);
				handled = true;
			} else {
				handled = false;
			}
			return handled;
		}
		
	}
	
}
