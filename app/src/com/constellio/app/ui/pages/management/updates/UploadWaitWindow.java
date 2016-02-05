package com.constellio.app.ui.pages.management.updates;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class UploadWaitWindow extends Window {
	
	private VerticalLayout content;
	
	private Label windowMessageLabel;
	
	private Label taskLabel;
	
	private Label progressMessageLabel;
	
	public UploadWaitWindow() {
		super($("UploadWaitWindow.title"));
		center();

		content = new VerticalLayout();
		content.setSpacing(true);
		
		windowMessageLabel = new Label($("UploadWaitWindow.message"));
		taskLabel = new Label();
		progressMessageLabel = new Label();
		
		content.addComponents(windowMessageLabel, taskLabel, progressMessageLabel);
		content.setMargin(true);

		setContent(content);
		setClosable(false);
		setModal(true);
	}
	
	public void setTask(String task) {
		//taskLabel.setValue(task);
	}
	
	public void setProgressMessage(String progressMessage) {
		//progressMessageLabel.setValue(progressMessage);
	}
	
	public void setProgress(float progress) {
		if (progress >= 1) {
			taskLabel.setValue("");
			//progressMessageLabel.setValue("");
		}
	}
	
}
