package com.constellio.app.modules.tasks.ui.pages.workflow;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class BetaAddEditWorkflowViewImpl extends BaseViewImpl implements BetaAddEditWorkflowView {

	private boolean addView;

	private BetaWorkflowVO workflowVO;

	private BetaAddEditWorkflowPresenter presenter;

	public BetaAddEditWorkflowViewImpl() {
		this.presenter = new BetaAddEditWorkflowPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setAddView(boolean addView) {
		this.addView = addView;
	}

	@Override
	public void setWorkflowVO(BetaWorkflowVO workflowVO) {
		this.workflowVO = workflowVO;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new RecordForm(workflowVO, getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}

	@Override
	protected String getTitle() {
		return addView ? $("AddEditWorkflowView.addViewTitle") : $("AddEditWorkflowView.editViewTitle");
	}

}
