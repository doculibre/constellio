package com.constellio.app.ui.framework.components.bpmn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseRequestHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.util.JavascriptUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

//@JavaScript({ "theme://bpmn/bpmn-modeler.js", "theme://bpmn/demo-modeler.js" })
@JavaScript({ "theme://jquery/jquery-2.1.4.min.js", "theme://bpmn/bpmn-modeler.js" }) 
@StyleSheet({"theme://bpmn/assets/diagram-js.css", "theme://bpmn/assets/bpmn-font/css/bpmn-embedded.css"})
public class BpmnModeler extends VerticalLayout {
	
	private Component canvas;
	private Button saveButton;
	private BpmnSaveRequestHandler saveRequestHandler;
	
	public BpmnModeler() {
		ensureRequestHandler();
		
		setWidth("100%");
		setHeight("600px");
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();
		canvas = new Label();
		canvas.setId("canvas");
		
		saveButton = new Button("Save Diagram");
		saveButton.setId("save-button");
		
		mainLayout.addComponents(canvas, saveButton);
		mainLayout.setExpandRatio(canvas, 1);
		mainLayout.setComponentAlignment(saveButton, Alignment.TOP_RIGHT);
		addComponent(mainLayout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		File bpmnFile = new File("C:\\git\\constellio-dev\\constellio\\app\\WebContent\\VAADIN\\themes\\constellio\\bpmn\\constellio-test.bpmn");
		Resource bpmnFileResource = ConstellioResourceHandler.createResource(bpmnFile);
		ResourceReference bpmnFileResourceReference = ResourceReference.create(bpmnFileResource, this, "BpmnModeler.file");
		String bpmnFileURL = bpmnFileResourceReference.getURL();
		
		String saveButtonCallbackURL = saveRequestHandler.getCallbackURL();
		com.vaadin.ui.JavaScript.eval("bpmnDiagramURL='" + bpmnFileURL + "';");
		com.vaadin.ui.JavaScript.eval("saveButtonCallbackURL='" + saveButtonCallbackURL + "';");
		
        JavascriptUtils.loadScript("bpmn/constellio-modeler.js");
	}

	private void ensureRequestHandler() {
		saveRequestHandler = (BpmnSaveRequestHandler) ConstellioUI.getCurrent().getRequestHandler(BpmnSaveRequestHandler.class);
		if (saveRequestHandler == null) {
			saveRequestHandler = new BpmnSaveRequestHandler();
			VaadinSession session = VaadinSession.getCurrent();
			session.addRequestHandler(saveRequestHandler);
		} 
	}
	
	public void save(BpmnSaveHandler saveHandler) {
		
	}
	
	public static class BpmnSaveRequestHandler extends BaseRequestHandler {
		
		public BpmnSaveRequestHandler() {
			super(BpmnSaveRequestHandler.class.getName());
		}
		
		public String getCallbackURL() {
			StringBuilder sb = new StringBuilder();
			sb.append(getPath());
			return sb.toString();
		}

		@Override
		protected boolean handleRequest(String requestURI, Map<String, String> paramsMap, User user, VaadinSession session, VaadinRequest request, VaadinResponse response)
				throws IOException {
			String xml = request.getParameter("xml");
			System.out.println(xml);
			return true;
		}
		
	}
	
	public static interface BpmnSaveHandler {
		
		void save(InputStream in);
		
	}
	
	public static class SaveButtonListener implements Button.ClickListener {

		@Override
		public void buttonClick(ClickEvent event) {
			com.vaadin.ui.JavaScript.eval("bpmnSave()");
		}
		
	}
	
}
