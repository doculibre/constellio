package com.constellio.app.modules.rm.ui.pages.legalrequirement.reference;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditLegalReferenceViewImpl extends BaseViewImpl implements AddEditLegalReferenceView {

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditLegalReferenceViewImpl.class);

	private AddEditLegalReferencePresenter presenter;

	public AddEditLegalReferenceViewImpl() {
		presenter = new AddEditLegalReferencePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("LegalRequirementManagement.addEditLegalReference");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		mainLayout.addComponent(buildRecordForm());

		return mainLayout;
	}

	private Component buildRecordForm() {
		return new RecordForm(presenter.getRecordVO(), new MetadataFieldFactory(), ConstellioFactories.getInstance()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) {
				try {
					presenter.saveButtonClicked(viewObject);
				} catch (ValidationException e) {
					LOGGER.error(e.getMessage());
					showErrorMessage(MessageUtils.toMessage(e));
				} catch (RecordServicesException e) {
					LOGGER.error(e.getMessage());
					showErrorMessage(i18n.$("LegalRequirementManagement.saveRecordFailed"));
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}
}
