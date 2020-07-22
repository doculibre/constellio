package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditDecommissioningListViewImpl extends BaseViewImpl implements EditDecommissioningListView {
	private final EditDecommissioningListPresenter presenter;
	private RecordVO decommissioningList;

	public EditDecommissioningListViewImpl() {
		presenter = new EditDecommissioningListPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		decommissioningList = presenter.forRecordId(event.getParameters()).getDecommissioningList();
	}

	@Override
	protected String getTitle() {
		return $("EditDecommissioningListView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked(decommissioningList);
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new RecordForm(decommissioningList, EditDecommissioningListViewImpl.this.getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO recordVO)
					throws ValidationException {
				presenter.saveButtonClicked(recordVO);
			}

			@Override
			protected void cancelButtonClick(RecordVO recordVO) {
				presenter.cancelButtonClicked(recordVO);
			}
		};
	}
}
