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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class DisplayConnectorInstanceViewImpl extends BaseViewImpl implements DisplayConnectorInstanceView {
	public static final String STYLE_NAME = "display-connectorInstance";
	private RecordVO recordVO;
	private VerticalLayout mainLayout;
	private RecordDisplay recordDisplay;
	private DisplayConnectorInstancePresenter presenter;
	private Button editConnectorButton, startButton, stopButton, editSchemasButton, deleteButton;
	private TextArea lastDocumentsField;
	
	private Label documentsCountLabel;

	public DisplayConnectorInstanceViewImpl() {
		presenter = new DisplayConnectorInstancePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		recordDisplay = new RecordDisplay(recordVO, new ConnectorMetadataDisplayFactory());
		
		Component documentsCountComponent = newDocumentsCountComponent();
		lastDocumentsField = new TextArea();
		lastDocumentsField.setEnabled(false);
		lastDocumentsField.setSizeFull();

		mainLayout.addComponents(recordDisplay, documentsCountComponent, lastDocumentsField);
		return mainLayout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<>();
		editConnectorButton = new LinkButton($("DisplayConnectorInstanceView.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editConnectorInstanceButtonClicked();
			}
		};
		startButton = new LinkButton($("DisplayConnectorInstanceView.start")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.start();
			}
		};
		startButton.setVisible(presenter.isStartButtonVisible());
		stopButton = new LinkButton($("DisplayConnectorInstanceView.stop")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.stop();
			}
		};
		stopButton.setVisible(presenter.isStopButtonVisible());
		editSchemasButton = new LinkButton($("DisplayConnectorInstanceView.editSchemas")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editSchemasButtonClicked();
			}
		};
		deleteButton = new LinkButton($("DisplayConnectorInstanceView.deleteButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.deleteConnectorInstanceButtonClicked();
			}
		};

		actionMenuButtons.add(startButton);
		actionMenuButtons.add(stopButton);
		actionMenuButtons.add(editConnectorButton);
		actionMenuButtons.add(editSchemasButton);
		//Is not supported yet
		//		actionMenuButtons.add(deleteButton);

		return actionMenuButtons;
	}

	private BaseDisplay newDocumentsCountComponent() {
		String caption = $("DisplayConnectorInstanceView.docsCount");
		Label captionLabel = new Label(caption);
		String captionId = BaseDisplay.STYLE_CAPTION + "-docsCount";
		captionLabel.setId(captionId);
		captionLabel.addStyleName(captionId);
		String valueId = BaseDisplay.STYLE_VALUE + "-docsCount";
		documentsCountLabel = new Label("");
		documentsCountLabel.setId(valueId);
		documentsCountLabel.addStyleName(valueId);
		CaptionAndComponent captionsAndComponent = new CaptionAndComponent(captionLabel, documentsCountLabel);
		return new BaseDisplay(asList(captionsAndComponent));
	}

	@Override
	protected boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
	}

	@Override
	public void setDocumentsCount(long count) {
		documentsCountLabel.setValue("" + count);
	}

	@Override
	public void setLastDocuments(String lastDocuments) {
		lastDocumentsField.setValue(lastDocuments);
	}

	public class ConnectorMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			if (metadata.getCode().endsWith(ConnectorInstance.PROPERTIES_MAPPING)) {
				return null;
			} else if (metadata.getCode().endsWith(ConnectorInstance.TRAVERSAL_CODE)) {
				return null;
			} else {
				return super.buildSingleValue(recordVO, metadata, displayValue);
			}
		}
	}
}
