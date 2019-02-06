package com.constellio.app.modules.rm.services.sip.data.intelligid;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.sip.model.EntityRetriever;
import com.constellio.app.modules.rm.services.sip.model.SIPDocument;
import com.constellio.app.modules.rm.services.sip.model.SIPFolder;
import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataListFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;

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
	public List<String> getMetadataIds(SIPObject sipObject) {
		List<String> metadataIds = new ArrayList<>();
		if (sipObject instanceof SIPFolder) {
			//metadataIds.add("numeroRegleConservation");
			//metadataIds.add("regleConservation");
		} else if (sipObject instanceof SIPDocument) {
			//			SIPDocument sipDocument = (SIPDocument) sipObject;
			//			Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
			//			boolean isCourriel = document.getSchema().getCode().equals(Email.SCHEMA);
			//			if (isCourriel) {
			//				metadataIds.add(Email.EMAIL_TO);
			//				metadataIds.add(Email.EMAIL_FROM);
			//				metadataIds.add(Email.EMAIL_IN_NAME_OF);
			//				metadataIds.add(Email.EMAIL_CC_TO);
			//				metadataIds.add(Email.EMAIL_BCC_TO);
			//				metadataIds.add(Email.EMAIL_OBJECT);
			//			} else {
			//				metadataIds.add("typeDocument");
			//			}
		}
		return metadataIds;
	}

	@Override
	public List<String> getMetadataValues(SIPObject sipObject, String metadataId) {
		List<String> metadataValues = new ArrayList<>();
		if (sipObject instanceof SIPFolder) {
			SIPFolder sipFolder = (SIPFolder) sipObject;
			Folder folder = rm.wrapFolder(sipFolder.getFicheMetadonnees());
			Folder currentFolder = folder;
			while (currentFolder.getParentFolder() != null) {
				currentFolder = rm.getFolder(currentFolder.getParentFolder());
			}
			RetentionRule retentionRule = rm.getRetentionRule(currentFolder.getRetentionRule());
			CopyRetentionRule copyRetentionRule = retentionRule.getCopyRetentionRules().get(0);
			if ("numeroRegleConservation".equals(metadataId)) {
				metadataValues.add(copyRetentionRule.getCode());
			} else if ("regleConservation".equals(metadataId)) {
				metadataValues.add(copyRetentionRule.getTitle());
			}
		} else if (sipObject instanceof SIPDocument) {
			SIPDocument sipDocument = (SIPDocument) sipObject;
			Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
			boolean isEmail = document.getSchema().getCode().equals(Email.SCHEMA);
			if (isEmail) {
				String metadataName;
				if ("emailTo".equals(metadataId)) {
					metadataName = Email.EMAIL_TO;
				} else if ("emailFrom".equals(metadataId)) {
					metadataName = Email.EMAIL_FROM;
				} else if ("emailInNameOf".equals(metadataId)) {
					metadataName = Email.EMAIL_IN_NAME_OF;
				} else if ("emailCCTo".equals(metadataId)) {
					metadataName = Email.EMAIL_CC_TO;
				} else if ("emailBCCTo".equals(metadataId)) {
					metadataName = Email.EMAIL_BCC_TO;
				} else if ("emailObject".equals(metadataId)) {
					metadataName = Email.EMAIL_OBJECT;
				} else {
					metadataName = null;
				}


				if (metadataName != null) {
					Object metadataValue = document.get(metadataName);
					if (metadataValue != null) {
						String metadataValueAsString = metadataValue instanceof List ? join(", ", (List<String>) metadataValue) : metadataValue.toString();
						if (StringUtils.isNotBlank(metadataValueAsString)) {
							metadataValues.add(metadataValueAsString);
						}
					}
				}
			} else {
				if ("typeDocument".equals(metadataId)) {
					DocumentType typeDocument = rm.getDocumentType(document.<String>get(Document.TYPE));
					if (typeDocument != null) {
						metadataValues.add(typeDocument.getCode());
					}
				}
			}
		}
		return metadataValues;
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

	@Override
	public String getCollection() {
		return this.collection;
	}

	@Override
	public AppLayerFactory getAppLayerCollection() {
		return this.factory;
	}

	private class SearchableMetadataListFilter implements MetadataListFilter {
		@Override
		public boolean isReturned(Metadata metadata) {
			return metadata.isSearchable();
		}
	}

}
