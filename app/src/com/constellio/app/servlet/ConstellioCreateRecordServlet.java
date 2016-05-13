package com.constellio.app.servlet;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConstellioCreateRecordServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioCreateRecordServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("Create record called!");
		System.out.println("Create record called!");
		User user = new HttpServletRequestAuthenticator(modelLayerFactory()).authenticateSystemAdminInCollection(request);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			PrintWriter responseWriter = response.getWriter();
			responseWriter.append("Unauthorized access : Invalid collection/servicesKey/token");
		} else {
			executeRequest(user, request, response);
		}

	}

	private AppLayerFactory appLayerFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getAppLayerFactory();
	}

	private ModelLayerFactory modelLayerFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getModelLayerFactory();
	}

	private void executeRequest(User user, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		StringBuffer responseMessage = new StringBuffer();
		PrintWriter responseWriter = response.getWriter();
		responseWriter.append("<html>");

		SearchServices searchServices = modelLayerFactory().newSearchServices();
		String collection = user.getCollection();
		MetadataSchemaTypes types = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchema schema = getMetadataSchema(request, responseMessage, types, collection);

		Record record = null;
		if (schema != null) {
			RecordServices recordServices = modelLayerFactory().newRecordServices();
			record = recordServices.newRecordWithSchema(schema);

			for (Metadata metadata : schema.getMetadatas().onlyManuals()) {
				setMetadataValue(request, types, searchServices, record, metadata);
			}
			try {
				recordServices.add(record);
			} catch (RecordServicesException.ValidationException e) {
				responseMessage.append($(e.getErrors()));
			} catch (RecordServicesException e) {
				responseMessage.append($("ConstellioCreateRecordServlet.RecordServicesException", e.getMessage()));
			}
		}
		configureResponseWriter(responseMessage, responseWriter, record);
	}

	private void configureResponseWriter(StringBuffer responseMessage, PrintWriter responseWriter, Record record) {
		if (responseMessage.length() > 0 || record == null) {
			responseWriter.append(responseMessage);
		} else {
			responseWriter.append(record.getId());
		}
		responseWriter.append("</html>");
	}

	private MetadataSchema getMetadataSchema(HttpServletRequest request, StringBuffer responseMessage, MetadataSchemaTypes types,
			String collection) {
		MetadataSchema schema = null;
		if (StringUtils.isNotBlank(collection) && types != null) {
			String schemaCode = null;
			try {
				schemaCode = request.getParameter("schema");
				schema = types.getSchema(schemaCode);
			} catch (Exception e) {
				responseMessage.append($("ConstellioCreateRecordServlet.InvalidSchema", schemaCode));
			}
		} else {
			responseMessage.append($("ConstellioCreateRecordServlet.InvalidCollection", collection));
		}
		return schema;
	}

	private void setMetadataValue(HttpServletRequest request, MetadataSchemaTypes types,
			SearchServices searchServices, Record record, Metadata metadata) {
		LogicalSearchCondition condition;
		LogicalSearchQuery query = new LogicalSearchQuery();
		String metadataValue = request.getParameter(metadata.getLocalCode());
		if (StringUtils.isNotBlank(metadataValue)) {
			if (metadata.isMultivalue()) {
				List<String> values = new ArrayList<String>(Arrays.asList(metadataValue.split(",")));
				record.set(metadata, values);
			} else {
				switch (metadata.getType()) {
				case DATE:
					LocalDate localDate = LocalDate.parse(metadataValue);
					record.set(metadata, localDate);
					break;
				case DATE_TIME:
					LocalDateTime localDateTime = LocalDateTime.parse(metadataValue);
					record.set(metadata, localDateTime);
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
					MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
					Metadata whereMetadata = Schemas.CODE;
					if (schemaTypeCode.equals(User.SCHEMA_TYPE)) {
						whereMetadata = schemaType.getDefaultSchema().getMetadata(User.USERNAME);
					}
					condition = LogicalSearchQueryOperators.from(schemaType).where(whereMetadata)
							.isEqualTo(metadataValue);
					query.setCondition(condition);
					query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER));
					List<String> recordIds = searchServices.searchRecordIds(query);
					if (!recordIds.isEmpty()) {
						record.set(metadata, recordIds.get(0));
					} else {
						record.set(metadata, metadataValue);
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