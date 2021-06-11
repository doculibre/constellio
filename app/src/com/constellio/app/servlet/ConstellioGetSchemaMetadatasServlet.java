package com.constellio.app.servlet;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.data.utils.AccentApostropheCleaner.cleanAll;

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

	private Element buildElementFromSchema(MetadataSchema schema, String language) {
		Element schemaElement = new Element("schema");
		schemaElement.setAttribute("code", schema.getCode());
		schemaElement.setAttribute("collection", schema.getCollection());
		schemaElement.setAttribute("language", language);
		schemaElement.setAttribute("search-field", "search_txt_" + language);
		schemaElement.setAttribute("label", schema.getLabel(Language.withCode(language)));

		for (Metadata metadata : schema.getMetadatas()) {
			Element metadataElement = new Element("metadata");
			schemaElement.addContent(metadataElement);
			metadataElement.setAttribute("code", metadata.getLocalCode());
			metadataElement.setAttribute("title", metadata.getLabel(Language.withCode(language)));
			metadataElement.setAttribute("multivalue", "" + metadata.isMultivalue());
			metadataElement.setAttribute("type", metadata.getType().name());
			metadataElement.setAttribute("solr-field", metadata.getDataStoreCode());
			metadataElement.setAttribute("label", metadata.getLabel(Language.withCode(language)));
			if (metadata.getType() == MetadataValueType.ENUM) {
				metadataElement.setAttribute("allowed-values",
						Arrays.stream(metadata.getEnumClass().getEnumConstants()).map(Enum::name)
								.collect(Collectors.joining(",")));
			}
			if (metadata.isSearchable()) {
				String solrAnalyzedField = metadata.getAnalyzedField(language).getDataStoreCode();
				metadataElement.setAttribute("solr-analyzed-field", solrAnalyzedField);
			}
		}

		return schemaElement;
	}

	private void executeRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String type = request.getParameter("type");
		String label = request.getParameter("label");
		String collection = request.getParameter("collection");
		String schemaCode = request.getParameter("schema");
		String language = modelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0);

		if (type == null) {
			executeRequestWithSchemaCode(response, collection, schemaCode, language);
		} else {
			executeRequestWithType(response, collection, type, label, language);
		}
	}

	private void executeRequestWithSchemaCode(HttpServletResponse response, String collection, String schemaCode,
											  String language)
			throws IOException, ServletException {

		MetadataSchema schema = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
		Document document = new Document().addContent(buildElementFromSchema(schema, language));
		outputDocument(response, document);
	}

	private void executeRequestWithType(HttpServletResponse response, String collection, String type, String label,
										String language)
			throws IOException, ServletException {
		List<MetadataSchema> schemaList = new ArrayList<>();

		Document document = new Document();
		Element rootElement = new Element("collection");
		rootElement.setAttribute("code", collection);
		document.addContent(rootElement);

		if (type.equals("ddv")) {
			for (MetadataSchemaType schemaType : modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes()) {
				if (schemaType.getCode().contains("ddv")) {
					schemaList.addAll(schemaType.getAllSchemas());
				}
			}
		} else {
			for (MetadataSchemaType schemaType : modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes()) {
				if (cleanAll(schemaType.getCode()).equals(cleanAll(type))) {
					schemaList.addAll(schemaType.getAllSchemas());
				}
			}
		}

		if (label == null) {
			for (MetadataSchema schema : schemaList) {
				rootElement.addContent(buildElementFromSchema(schema, language));
			}
		} else {
			for (MetadataSchema schema : schemaList) {
				if (cleanAll(schema.getLabel(Language.withCode(language))).equals(cleanAll(label))) {
					rootElement.addContent(buildElementFromSchema(schema, language));
					break;
				}
			}
		}

		outputDocument(response, document);
	}

	private void outputDocument(HttpServletResponse response, Document document)
			throws IOException, ServletException {
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.getFormat().setEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		outputter.output(document, response.getWriter());
	}

}