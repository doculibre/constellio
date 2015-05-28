/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.services;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;

public class FolderDocumentMetadataSyncServices {

	private RecordServices recordServices;

	private AppLayerFactory appLayerFactory;

	private ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	private String collection;

	public FolderDocumentMetadataSyncServices(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.collection = collection;
	}

	private boolean isEnteredValuesOverExtractedValues() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		return new ConstellioEIMConfigs(manager, collection).isEnteredValuesOverExtractedValues();
	}

	public void updateFolderWithContentProperties(Record documentRecord) {
		Document document = rm.wrapDocument(documentRecord);
		Content content = document.getContent();
		ContentVersion contentVersion = content.getCurrentVersion();
		ParsedContent parsedContent = modelLayerFactory.getContentManager().getParsedContent(contentVersion.getHash());
		if (parsedContent != null) {

			String extractedSubject = (String) parsedContent.getNormalizedProperty(Document.SUBJECT);
			String extractedCompany = (String) parsedContent.getNormalizedProperty(Document.COMPANY);
			String extractedAuthor = (String) parsedContent.getNormalizedProperty(Document.AUTHOR);
			String extractedTitle = (String) parsedContent.getNormalizedProperty(Schemas.TITLE_CODE);
			List<String> extractedKeywords = (List<String>) parsedContent.getNormalizedProperty(Folder.KEYWORDS);

			if (document.getSubject() == null && extractedSubject != null) {
				document.setSubject(extractedSubject);
			}

			if (document.getCompany() == null && extractedCompany != null) {
				document.setCompany(extractedCompany);
			}

			if (document.getAuthor() == null && extractedAuthor != null) {
				document.setAuthor(extractedAuthor);
			}

			if (extractedTitle != null) {
				if (!isEnteredValuesOverExtractedValues()) {
					document.setTitle(extractedTitle);
				}
			}

			if (document.getKeywords().isEmpty() && extractedKeywords != null) {
				document.setKeywords(extractedKeywords);
			}

		}
	}

}
