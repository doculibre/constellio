package com.constellio.app.ui.pages.management.sequence;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

public class SequenceServices {
	
	private ConstellioFactories constellioFactories;
	
	private SessionContext sessionContext;
	
	public SequenceServices(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.constellioFactories = constellioFactories;
		this.sessionContext = sessionContext;
	}
	
	public List<AvailableSequence> getAvailableSequences(String recordId) {
		final List<AvailableSequence> availableSequences = new ArrayList<>();

		String collection = sessionContext.getCurrentCollection();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record record;
		if (recordId != null) {
			record = recordServices.getDocumentById(recordId);
			AppLayerCollectionExtensions collectionExtensions = appLayerFactory.getExtensions().forCollection(collection);
			List<AvailableSequence> collectionAvailableSequences = collectionExtensions.getAvailableSequencesForRecord(record);
			availableSequences.addAll(collectionAvailableSequences);
			
//			AppLayerSystemExtensions systemExtensions = appLayerFactory.getExtensions().getSystemWideExtensions();
//			List<AvailableSequence> systemAvailableSequences = systemExtensions.getAvailableSequences();
//			availableSequences.addAll(systemAvailableSequences);
		}
		return availableSequences;
	}
	
	private SequencesManager sequencesManager() {
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		return dataLayerFactory.getSequencesManager();
	}

	public void set(String sequenceId, long value) {
		SequencesManager sequencesManager = sequencesManager();
		sequencesManager.set(sequenceId, value);
	}

	public long getLastSequenceValue(String sequenceId) {
		SequencesManager sequencesManager = sequencesManager();
		return sequencesManager.getLastSequenceValue(sequenceId);
	}

	long next(String sequenceId) {
		SequencesManager sequencesManager = sequencesManager();
		return sequencesManager.next(sequenceId);
	}

}
