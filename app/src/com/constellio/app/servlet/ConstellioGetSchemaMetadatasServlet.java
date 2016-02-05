package com.constellio.app.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioGetSchemaMetadatasServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		executeRequest(request, response);

	}

	private ModelLayerFactory modelLayerFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getModelLayerFactory();
	}

	private Document buildResponseDocument(MetadataSchema schema, String language) {
		Document document = new Document();
		Element schemaElement = new Element("schema");
		schemaElement.setAttribute("code", schema.getCode());
		schemaElement.setAttribute("collection", schema.getCollection());
		schemaElement.setAttribute("language", language);
		schemaElement.setAttribute("search-field", "search_txt_" + language);
		document.addContent(schemaElement);

		for (Metadata metadata : schema.getMetadatas()) {
			Element metadataElement = new Element("metadata");
			schemaElement.addContent(metadataElement);
			metadataElement.setAttribute("code", metadata.getLocalCode());
			metadataElement.setAttribute("title", metadata.getLabel());
			metadataElement.setAttribute("multivalue", "" + metadata.isMultivalue());
			metadataElement.setAttribute("type", metadata.getType().name());
			metadataElement.setAttribute("solr-field", metadata.getDataStoreCode());
			if (metadata.isSearchable()) {
				String solrAnalyzedField = metadata.getAnalyzedField(language).getDataStoreCode();
				metadataElement.setAttribute("solr-analyzed-field", solrAnalyzedField);
			}
		}

		return document;
	}

	private void executeRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String collection = request.getParameter("collection");
		String schemaCode = request.getParameter("schema");
		MetadataSchema schema = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
		String language = modelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0);

		Document document = buildResponseDocument(schema, language);

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.getFormat().setEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		outputter.output(document, response.getWriter());
	}

}