package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.converters.StringToRecordVOConverter;
import com.constellio.app.ui.framework.components.fields.taxonomy.TaxonomyComboBox;

@SuppressWarnings("unchecked")
public class ListAddRemoveTaxonomyComboBox extends ListAddRemoveField<RecordVO, TaxonomyComboBox> {

	private String taxonomyCode;
	
	private String schemaTypeCode;

	public ListAddRemoveTaxonomyComboBox(String taxonomyCode, String schemaTypeCode) {
		super();
		this.taxonomyCode = taxonomyCode;
		setItemConverter(new StringToRecordVOConverter(VIEW_MODE.TABLE));
	}

	@Override
	protected TaxonomyComboBox newAddEditField() {
		return new TaxonomyComboBox(taxonomyCode, schemaTypeCode);
	}

}
