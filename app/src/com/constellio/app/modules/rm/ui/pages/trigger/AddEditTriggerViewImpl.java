package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditTriggerViewImpl extends BaseViewImpl implements AddEditTriggerView {
	private AddEditTriggerPresenter presenter;
	private String recordTitle;

	public AddEditTriggerViewImpl() {
		presenter = new AddEditTriggerPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		mainLayout.addComponent(new TriggerRecordForm(presenter.getRecordVO(), getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
				presenter.saveButtonClick(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		});

		mainLayout.setMargin(new MarginInfo(false, true, false, false));

		return mainLayout;
	}

	public void setRecordTitle(String title) {
		this.recordTitle = title;
	}

	@Override
	protected String getTitle() {
		if (presenter.isAddMode()) {
			return $("AddEditTriggerViewImpl.addTitle", recordTitle);
		} else {
			return $("AddEditTriggerViewImpl.editTitle", recordTitle);
		}
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return true;
	}
}
