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
package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplay;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class DisplayRetentionRuleViewImpl extends BaseViewImpl implements DisplayRetentionRuleView {

	public static final String STYLE_NAME = "display-folder";

	private RetentionRuleVO retentionRuleVO;

	private VerticalLayout mainLayout;

	private RetentionRuleDisplay recordDisplay;

	private Button editButton, deleteButton;

	private DisplayRetentionRulePresenter presenter;

	public DisplayRetentionRuleViewImpl() {
		presenter = new DisplayRetentionRulePresenter(this);
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
	public void setRetentionRule(RetentionRuleVO retentionRuleVO) {
		this.retentionRuleVO = retentionRuleVO;
	}

	@Override
	protected String getTitle() {
		return $("DisplayRetentionRuleView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		recordDisplay = new RetentionRuleDisplay(retentionRuleVO);
		recordDisplay.setWidth("100%");

		mainLayout.addComponent(recordDisplay);

		Component component = buildAdditionalComponent();
		mainLayout.addComponent(component);

		return mainLayout;
	}

	private Component buildAdditionalComponent() {
		Label foldersNumberCaptionLabel = new Label($("DisplayRetentionRuleView.foldersNumber"));
		foldersNumberCaptionLabel.setId("foldersNumber");
		foldersNumberCaptionLabel.addStyleName("foldersNumber");
		Label foldersNumberDisplayComponent = new Label(presenter.getFoldersNumber());
		foldersNumberDisplayComponent.addStyleName("display-value-foldersNumber");

		List<CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(foldersNumberCaptionLabel, foldersNumberDisplayComponent));
		return new BaseDisplay(captionsAndComponents);
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
		List<Button> actionMenuButtons = new ArrayList<Button>();

		editButton = new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}
		};

		deleteButton = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}
		};

		actionMenuButtons.add(editButton);
		actionMenuButtons.add(deleteButton);

		return actionMenuButtons;
	}

}
