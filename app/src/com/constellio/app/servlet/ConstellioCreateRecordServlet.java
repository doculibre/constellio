package com.constellio.app.servlet;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConstellioCreateRecordServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioCreateRecordServlet.class);

	private static final String SCHEMA_QUERY_PARAM_NAME = "schema";

	private static final String MULTIVALUE_SEPARATOR = ",";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("Create record called!");

		createRecords(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("Create records called!");

		createRecords(request, response);
	}

	private void createRecords(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter responseWriter = response.getWriter();

		final User user = new HttpServletRequestAuthenticator(modelLayerFactory()).authenticateSystemAdminInCollection(request);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			responseWriter.append("Unauthorized access : Invalid collection/servicesKey/token");
		} else {
			responseWriter.append("<html>");

			String collection = user.getCollection();
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
			String schemaCode = request.getParameter(SCHEMA_QUERY_PARAM_NAME);
			MetadataSchema metadataSchema = getMetadataSchema(schemaCode, metadataSchemaTypes, collection, responseWriter);

			if (metadataSchema != null) {
				if (HttpMethod.GET.equalsIgnoreCase(request.getMethod())) {
					createRecordFromGetRequest(metadataSchema, metadataSchemaTypes, request, responseWriter);
				} else if (HttpMethod.POST.equalsIgnoreCase(request.getMethod())) {
					createRecordsFromPostRequest(metadataSchema, metadataSchemaTypes, request, response, responseWriter);
				} else {
					response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
			}

			responseWriter.append("</html>");
		}
	}

	private void createRecordFromGetRequest(MetadataSchema metadataSchema, MetadataSchemaTypes metadataSchemaTypes,
											HttpServletRequest request, PrintWriter responseWriter) {
		RecordServices recordServices = modelLayerFactory().newRecordServices();

		Record record = recordServices.newRecordWithSchema(metadataSchema);

		for (Metadata metadata : metadataSchema.getMetadatas().onlyManualsOrAutomaticWithEvaluator()) {
			String metadataValue = request.getParameter(metadata.getLocalCode());
			setRecordMetadata(record, metadata, metadataValue, metadataSchemaTypes);
		}

		boolean error = false;
		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			responseWriter.append($(e.getErrors()));
			error = true;

		} catch (ValidationRuntimeException e) {
			responseWriter.append($(e.getValidationErrors()));
			error = true;

		} catch (RecordServicesException e) {
			responseWriter.append($("ConstellioCreateRecordServlet.RecordServicesException", e.getMessage()));
			error = true;
		}

		if (!error) {
			responseWriter.append(record.getId());
		}
	}

	private void createRecordsFromPostRequest(MetadataSchema metadataSchema, MetadataSchemaTypes metadataSchemaTypes,
											  HttpServletRequest request, HttpServletResponse response,
											  PrintWriter responseWriter)
			throws IOException {
		if (MediaType.APPLICATION_XML.equalsIgnoreCase(request.getContentType())) {
			Transaction transaction = new Transaction();
			RecordServices recordServices = modelLayerFactory().newRecordServices();

			Document domDocument;
			try {
				domDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(request.getInputStream());
			} catch (ParserConfigurationException | SAXException e) {
				responseWriter.append(e.getMessage());
				return;
			}

			NodeList recordsNodeList = domDocument.getDocumentElement().getChildNodes();
			for (int i = 0, recordsCount = recordsNodeList.getLength(); i < recordsCount; i++) {
				Record record = recordServices.newRecordWithSchema(metadataSchema);

				NamedNodeMap recordNamedNodeMap = recordsNodeList.item(i).getAttributes();
				for (Metadata metadata : metadataSchema.getMetadatas().onlyManualsOrAutomaticWithEvaluator()) {
					Node recordMetadataValue = recordNamedNodeMap.getNamedItem(metadata.getLocalCode());
					if (recordMetadataValue != null) {
						setRecordMetadata(record, metadata, recordMetadataValue.getNodeValue(), metadataSchemaTypes);
					}
				}

				transaction.add(record);
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException.ValidationException e) {
				responseWriter.append($(e.getErrors()));
			} catch (RecordServicesException e) {
				responseWriter.append($("ConstellioCreateRecordServlet.RecordServicesException", e.getMessage()));
			}

			responseWriter.append(Joiner.on(" ").join(transaction.getRecordIds()));
		} else {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		}
	}

	private MetadataSchema getMetadataSchema(String schemaCode, MetadataSchemaTypes types,
											 String collection, PrintWriter responseWriter) {
		MetadataSchema schema = null;
		if (StringUtils.isNotBlank(collection) && types != null) {
			try {
				schema = types.getSchema(schemaCode);
			} catch (Exception e) {
				responseWriter.append($("ConstellioCreateRecordServlet.InvalidSchema", schemaCode));
			}
		} else {
			responseWriter.append($("ConstellioCreateRecordServlet.InvalidCollection", collection));
		}
		return schema;
	}

	private ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	private void setRecordMetadata(Record record, Metadata metadata, String metadataValue,
								   MetadataSchemaTypes metadataSchemaTypes) {
		if (StringUtils.isNotBlank(metadataValue)) {
			if (metadata.isMultivalue()) {
				List<String> values = new ArrayList<>(Arrays.asList(metadataValue.split(MULTIVALUE_SEPARATOR)));
				record.set(metadata, values);
			} else {
				switch (metadata.getType()) {
					case DATE:
						record.set(metadata, LocalDate.parse(metadataValue));
						break;
					case DATE_TIME:
						record.set(metadata, LocalDateTime.parse(metadataValue));
						break;
					case STRING:
					case TEXT:
						record.set(metadata, metadataValue);
					case INTEGER:
						break;
					case NUMBER:
						break;
					case BOOLEAN:
						break;
					case REFERENCE:
						String schemaTypeCode = metadata.getAllowedReferences().getAllowedSchemaType();

						MetadataSchemaType metadataSchemaType = metadataSchemaTypes.getSchemaType(schemaTypeCode);
						Metadata whereMetadata;
						if (schemaTypeCode.equals(User.SCHEMA_TYPE)) {
							whereMetadata = metadataSchemaType.getDefaultSchema().getMetadata(User.USERNAME);
						} else {
							whereMetadata = Schemas.CODE;
						}
						LogicalSearchCondition logicalSearchCondition = LogicalSearchQueryOperators.from(metadataSchemaType)
								.where(whereMetadata).isEqualTo(metadataValue);
						LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition)
								.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER));
						List<String> foundRecordIds = modelLayerFactory().newSearchServices().searchRecordIds(logicalSearchQuery);

						if (foundRecordIds.isEmpty()) {
							record.set(metadata, metadataValue);
						} else {
							record.set(metadata, foundRecordIds.get(0));
						}
						break;
					case CONTENT:
						break;
					case STRUCTURE:
						break;
					case ENUM:
						break;
				}
			}
		}
	}

}