package com.constellio.app.ui.framework.components.fields.taxonomy;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.ComboBox;

import java.util.List;

public class TaxonomyComboBox extends ComboBox implements TaxonomyField {

	private TaxonomyFieldPresenter presenter;

	public TaxonomyComboBox(String taxonomyCode, String schemaTypeCode) {
		super();
		presenter = new TaxonomyFieldPresenter(this);
		presenter.forTaxonomyAndSchemaTypeCodes(taxonomyCode, schemaTypeCode);
	}

	@Override
	public void setOptions(List<RecordVO> recordVOs) {
		for (RecordVO recordVO : recordVOs) {
			addItem(recordVO.getId());
			setItemCaption(recordVO.getId(), recordVO.getTitle());
		}
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
