package com.constellio.app.modules.rm.services.sip.data.intelligid;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.sip.model.EntityRetriever;
import com.constellio.app.modules.rm.services.sip.model.SIPDocument;
import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataListFilter;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

public class ConstellioSIPObjectsProvider implements SIPObjectsProvider {


	private List<Metadata> folderMetadatas;

	private List<Metadata> documentMetadatas;

	private String categoryId;

	private String administrativeUnitId;

	private List<Document> documents;

	private String collection;

	private AppLayerFactory factory;

	private RMSchemasRecordsServices rm;

	private SIPFilter filter;

	private MetadataSchemaTypes types;

	private int currentProgress;

	private ProgressInfo progressInfo;

	public ConstellioSIPObjectsProvider(String collection, AppLayerFactory factory, SIPFilter filter,
										ProgressInfo progressInfo) {
		this.collection = collection;
		this.factory = factory;
		this.filter = filter;
		this.rm = new RMSchemasRecordsServices(collection, factory);
		this.progressInfo = progressInfo;
		this.currentProgress = 0;
		init();
	}

	@Override
	public int getStartIndex() {
		return filter.getStartIndex();
	}

	private void init() {
		if (filter.getAdministrativeUnit() != null) {
			administrativeUnitId = this.filter.getAdministrativeUnit().getId();
		}
		if (filter.getCategory() != null) {
			categoryId = filter.getCategory().getId();
		}

		System.out.println("Retrieving document list...");
		documents = filter.getDocument();
		this.progressInfo.setEnd(documents.size());
		System.out.println("Document list retrieved (" + documents.size() + ")");
	}

	@Override
	public List<SIPObject> list() {
		return new AbstractList<SIPObject>() {
			@Override
			public SIPObject get(int index) {
				System.out.println("Document " + (index + 1) + " de " + documents.size());
				Document document = documents.get(index);
				//                progressInfo.setCurrentState(++currentProgress);
				//                progressInfo.setProgressMessage("Document " + (index + 1) + " de " + documents.size());
				return new SIPDocument(document, document.getSchema().getMetadatas(), new EntityRetriever(collection, factory));
			}

			@Override
			public int size() {
				return documents.size();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, byte[]> getExtraFiles(SIPObject sipObject) {
		return null;
	}

	private class SearchableMetadataListFilter implements MetadataListFilter {
		@Override
		public boolean isReturned(Metadata metadata) {
			return metadata.isSearchable();
		}
	}

}
