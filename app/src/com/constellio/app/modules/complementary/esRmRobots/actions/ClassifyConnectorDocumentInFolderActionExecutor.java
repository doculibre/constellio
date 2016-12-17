package com.constellio.app.modules.complementary.esRmRobots.actions;

import static com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification.DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM;
import static com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification.EXCLUDE_DOCUMENTS;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.services.SmbClassifyServices;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;

public class ClassifyConnectorDocumentInFolderActionExecutor implements ActionExecutor {

	public final static String ID = ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE;

	public final static String PARAMETER_SCHEMA = ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE;

	public final static Set<String> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(ConnectorSmbDocument.SCHEMA_TYPE);
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(ClassifyConnectorDocumentInFolderActionExecutor.class);

	@Override
	public Transaction execute(String robotId, ActionParameters actionParameters, AppLayerFactory appLayerFactory,
			List<Record> records, List<Record> processedRecords) {
		String collection = actionParameters.getCollection();
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		RobotSchemaRecordServices robots = new RobotSchemaRecordServices(collection, appLayerFactory);

		ClassifyConnectorDocumentInFolderActionParameters params = ClassifyConnectorDocumentInFolderActionParameters
				.wrap(actionParameters);

		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(User.ADMIN, collection);
		SmbClassifyServices classifyServices = new SmbClassifyServices(collection, appLayerFactory, user);

		//String versions = StringUtils.defaultString(params.getVersions(), "");
		String versions = "";
		for (Record record : records) {
			ConnectorDocument connectorDocument = es.wrapConnectorDocument(record);
			try {

				boolean excludeDocumentFromConnector = params.getActionAfterClassification() == EXCLUDE_DOCUMENTS;

				String classifiedDocumentId = classifyServices.classifyDocument(connectorDocument, params.getInFolder(),
						params.getDocumentType(), params.getMajorVersions(), excludeDocumentFromConnector, versions);

				if (excludeDocumentFromConnector && isNotBlank(classifiedDocumentId)) {
					List<String> newUrlsToExclude = Arrays.asList(connectorDocument.getURL());
					connectorServices(appLayerFactory, connectorDocument)
							.addExcludedUrlsTo(newUrlsToExclude, es.getConnectorInstance(connectorDocument.getConnector()));

				} else if (params.getActionAfterClassification() == DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM
						&& isNotBlank(classifiedDocumentId)) {
					connectorServices(appLayerFactory, connectorDocument).deleteDocumentOnRemoteComponent(connectorDocument);

				}
			} catch (Throwable e) {
				LOGGER.warn("Cannot complete classification of document '" + connectorDocument.getURL() + "'", e);
				try {
					appLayerFactory.getModelLayerFactory().newRecordServices()
							.add(robots.newRobotLog().setTitle(e.getClass().getCanonicalName() + " : " + e.getMessage())
									.setRobot(robotId));
				} catch (RecordServicesException e1) {
					throw new RuntimeException("Failed to create the robot error log");
				}
			}
		}
		return new Transaction();
	}

	protected ConnectorUtilsServices connectorServices(AppLayerFactory appLayerFactory, ConnectorDocument document) {
		return ConnectorServicesFactory.forConnectorDocument(appLayerFactory, document);
	}

	public static void registerIn(RobotsManager robotsManager) {
		robotsManager.registerAction(ID, PARAMETER_SCHEMA, SUPPORTED_TYPES,
				new ClassifyConnectorDocumentInFolderActionExecutor());
	}
}
