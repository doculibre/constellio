package com.constellio.app.ui.pages.management.sequence;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Collection;
import java.util.List;

import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SaveButton;
import com.constellio.app.ui.framework.components.fields.number.BaseLongField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class ListSequencesViewImpl extends BaseViewImpl implements ListSequencesView {
	
	private String recordId;
	
	private List<SequenceVO> sequenceVOs;
	
	private ListSequencesPresenter presenter;

	public ListSequencesViewImpl(String recordId) {
		this.recordId = recordId;
		presenter = new ListSequencesPresenter(this);
	}

	@Override
	public String getRecordId() {
		return recordId;
	}

	@Override
	public void setSequenceVOs(List<SequenceVO> sequenceVOs) {
		this.sequenceVOs = sequenceVOs;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
//		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		Table table = new Table();
		table.setWidth("100%");
		table.setPageLength(sequenceVOs.size());
		table.addContainerProperty("id", String.class, "");
		table.addContainerProperty("title", String.class, "");
		table.addContainerProperty("value", TextField.class, null);
		
		table.setColumnExpandRatio("title", 1);
		table.setColumnHeader("id", $("ListSequencesView.table.id"));
		table.setColumnHeader("title", $("ListSequencesView.table.title"));
		table.setColumnHeader("value", $("ListSequencesView.table.value"));
		
		for (SequenceVO sequenceVO : sequenceVOs) {
			TextField valueField = new BaseLongField(new MethodProperty<>(sequenceVO, "sequenceValue"));

			Item item = table.addItem(sequenceVO);
			item.getItemProperty("id").setValue(sequenceVO.getSequenceId());
			item.getItemProperty("title").setValue(sequenceVO.getSequenceTitle());
			item.getItemProperty("value").setValue(valueField);
		}
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		Button saveButton = new SaveButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked();
			}
		}; 
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		
		Button cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		};
		
		mainLayout.addComponents(table, buttonsLayout);
		mainLayout.setExpandRatio(table, 1);
		mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

		buttonsLayout.addComponents(saveButton, cancelButton);
		
		return mainLayout;
	}

	@Override
	protected String getTitle() {
		return $("ListSequencesView.viewTitle");
	}

	@Override
	public void closeWindow() {
		Collection<Window> windows = UI.getCurrent().getWindows();
		for (Window window : windows) {
			window.close();
		}
	}

}
