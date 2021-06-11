package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;

public class ConversationMessageExtraFieldPresenter {


	private final Supplier<List<String>> getRecordIds;

	public ConversationMessageExtraFieldPresenter(Supplier<List<String>> getRecordIds) {
		this.getRecordIds = getRecordIds;
	}

	public List<RecordVO> getLinkedRecordVOs() {
		RecordServices recordServices = ConstellioFactories.getInstanceIfAlreadyStarted().getModelLayerFactory().newRecordServices();

		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();

		return recordServices.get(getRecordIds.get(), RETURNING_SUMMARY)
				.stream().map(record -> recordToVOBuilder.build(record, VIEW_MODE.TABLE, sessionContext))
				.collect(Collectors.toList());
	}
}
