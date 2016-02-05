package com.constellio.app.ui.framework.components.fields.taxonomy;

import java.util.List;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.pages.base.SessionContext;

public class TaxonomyOptionGroup extends ListOptionGroup implements TaxonomyField {

	private TaxonomyFieldPresenter presenter;

	public TaxonomyOptionGroup(String taxonomyCode, String schemaTypeCode) {
		super();
		presenter = new TaxonomyFieldPresenter(this);
		presenter.forTaxonomyAndSchemaTypeCodes(taxonomyCode, schemaTypeCode);
	}

	@Override
	public void setOptions(List<RecordVO> recordVOs) {
		for (RecordVO recordVO : recordVOs) {
			addItem(recordVO);
		}
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
