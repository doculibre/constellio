package com.constellio.app.ui.pages.SIP;

import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.BagInfoToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

public class BagInfoSIPPresenter extends BasePresenter<BagInfoSIPForm> {

	SearchServices searchServices;
	SchemaPresenterUtils presenterUtils;
	BagInfoToVOBuilder builder;
	private MetadataToVOBuilder metadataBuilder;

	public BagInfoSIPPresenter(BagInfoSIPForm view) {
		super(view);
		searchServices = searchServices();
		builder = new BagInfoToVOBuilder();
		presenterUtils = new SchemaPresenterUtils(BagInfo.DEFAULT_SCHEMA, view.getConstellioFactories(),
				view.getSessionContext());
		metadataBuilder = new MetadataToVOBuilder();
	}

	protected List<BagInfoVO> getAllBagInfo() {
		List<BagInfoVO> bagInfoVOS = new ArrayList<>();
		BagInfoToVOBuilder builder = new BagInfoToVOBuilder();
		List<MetadataSchema> bagInfoSchemas = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(BagInfo.SCHEMA_TYPE).getCustomSchemas();
		for (MetadataSchema bagInfoSchema : bagInfoSchemas) {
			Record bagInfoRecord = recordServices().newRecordWithSchema(bagInfoSchema);
			bagInfoRecord.set(Schemas.TITLE, bagInfoSchema.getLabel(Language.withLocale(i18n.getLocale())));
			bagInfoVOS.add(builder.build(bagInfoRecord, RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext()));
		}
		return bagInfoVOS;
	}

	protected BagInfoVO newRecord() {
		return builder.build(presenterUtils.newRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
	}

	private MetadataVO getMetadataVoFromCode(String code) {
		return presenterService().getMetadataVO(code, view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void cancelButtonClicked() {
		view.closeAllWindows();
	}
}
