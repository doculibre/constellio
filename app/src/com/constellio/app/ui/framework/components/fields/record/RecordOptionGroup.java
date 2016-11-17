package com.constellio.app.ui.framework.components.fields.record;

import java.util.List;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;

public class RecordOptionGroup extends ListOptionGroup implements RecordOptionField {
	
	private RecordIdToCaptionConverter captionConverter = new RecordIdToCaptionConverter();
	
	private RecordOptionFieldPresenter presenter;

	private MetadataDisplayType metadataDisplayType;
	
	public RecordOptionGroup(String schemaCode) {
		super();
		this.presenter = new RecordOptionFieldPresenter(this);
		this.presenter.forSchemaCode(schemaCode);
	}

	public RecordOptionGroup(String schemaCode, MetadataDisplayType metadataDisplayType) {
		super();
		this.presenter = new RecordOptionFieldPresenter(this);
		this.presenter.forSchemaCode(schemaCode);
		this.metadataDisplayType = metadataDisplayType;
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		initStyleName();
		int size = dataProvider.size();
		List<RecordVO> records = dataProvider.listRecordVOs(0, size);
		for (RecordVO recordVO : records) {
			String recordId = recordVO.getId();
			String itemCaption = captionConverter.convertToPresentation(recordId, String.class, getLocale());
			addItem(recordId);
			setItemCaption(recordId, itemCaption);
		}
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public void initStyleName() {
		if(metadataDisplayType != null && metadataDisplayType.equals(MetadataDisplayType.HORIZONTAL)) {
			this.addStyleName("horizontal");
		}
		else {
			this.addStyleName("vertical");
		}
	};

}
