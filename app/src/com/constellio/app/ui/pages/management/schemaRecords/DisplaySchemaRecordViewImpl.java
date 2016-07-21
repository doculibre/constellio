package com.constellio.app.ui.pages.management.schemaRecords;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.ListSequencesButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class DisplaySchemaRecordViewImpl extends BaseViewImpl implements DisplaySchemaRecordView {

	DisplaySchemaRecordPresenter presenter;

	RecordVO recordVO;

	RecordDisplay recordDisplay;

	public DisplaySchemaRecordViewImpl() {
		this.presenter = new DisplaySchemaRecordPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		String id = event.getParameters();
		recordVO = presenter.getRecordVO(id);
		presenter.forSchema(recordVO.getSchema().getCode());
	}

	@Override
	protected String getTitle() {
		return $("DisplaySchemaRecordView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		recordDisplay = new RecordDisplay(recordVO);
		return recordDisplay;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		if (presenter.isSequenceTable(recordVO)) {
			actionMenuButtons.add(new ListSequencesButton(recordVO.getId(), $("DisplaySchemaRecordView.sequences")));
		}
		return actionMenuButtons;
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

}
