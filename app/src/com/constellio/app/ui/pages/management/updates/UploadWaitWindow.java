/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		taskLabel.setValue(task);
	}
	
	public void setProgressMessage(String progressMessage) {
		progressMessageLabel.setValue(progressMessage);
	}
	
	public void setProgress(float progress) {
		if (progress >= 1) {
			taskLabel.setValue("");
			progressMessageLabel.setValue("");
		}
	}
	
}
