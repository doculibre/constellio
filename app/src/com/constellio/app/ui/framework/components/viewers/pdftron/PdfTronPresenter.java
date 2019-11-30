package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdftron.PdfTronXMLService;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PdfTronPresenter implements CopyAnnotationsOfOtherVersionPresenter {

	private AppLayerFactory appLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private String recordId;
	private ContentVersionVO contentVersion;
	private PdfTronViewer pdfTronViewer;
	private SchemasRecordsServices schemasRecordsServices;
	private boolean doesCurrentUserHaveAnnotationLock = false;
	private Record record;
	private PdfTronXMLService pdfTronParser;
	private String xmlCurrentAnnotations;
	private MetadataSchemasManager metadataSchemasManager;
	private IOServices ioServices;
	private String metadataCode;
	private String pageRandomId;
	private boolean doesCurrnetPageHaveLock;

	public PdfTronPresenter(PdfTronViewer pdfTronViewer, String recordId, String metadataCode,
							ContentVersionVO contentVersion) {
		this.appLayerFactory = pdfTronViewer.getAppLayerFactory();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.contentVersion = contentVersion;
		this.pdfTronViewer = pdfTronViewer;
		this.recordId = recordId;
		this.schemasRecordsServices = new SchemasRecordsServices(pdfTronViewer.getCurrentSessionContext().getCurrentCollection(),
				appLayerFactory.getModelLayerFactory());
		this.record = this.schemasRecordsServices.get(recordId);
		this.pdfTronParser = new PdfTronXMLService();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.metadataCode = metadataCode;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.pageRandomId = UUID.randomUUID().toString();
		initialize();
	}

	private void initialize() {
		this.doesCurrentUserHaveAnnotationLock = doesUserHaveLock();
		this.doesCurrnetPageHaveLock = false;

		try {
			if (hasContentAnnotation()) {
				xmlCurrentAnnotations = getContentAnnotationFromVault();
			} else {
				xmlCurrentAnnotations = null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean doesCurrentPageHaveLock() {
		return doesCurrnetPageHaveLock;
	}

	private boolean hasContentAnnotation() {
		return this.contentManager.hasContentAnnotation(contentVersion.getHash(), recordId, contentVersion.getVersion());
	}


	public String getUserIdThatHaveAnnotationLock() {
		return this.contentManager.getUserIdThatHaveAnnotationLock(this.contentVersion.getHash(), recordId, contentVersion.getVersion());
	}


	public boolean doesCurrentUserHaveAnnotationLock() {
		return doesCurrentUserHaveAnnotationLock;
	}

	public User getCurrentUser() {
		return appLayerFactory.getModelLayerFactory().newUserServices()
				.getUserInCollection(getUserVO().getUsername(),
						pdfTronViewer.getCurrentSessionContext().getCurrentCollection());
	}

	private UserVO getUserVO() {
		return pdfTronViewer.getCurrentSessionContext().getCurrentUser();
	}

	public void saveAnnotation(String annotation) throws PdfTronXMLException_IOExeption {
		try {
			contentDao.add(contentVersion.getHash() + ".annotation."
						   + recordId + "." + contentVersion.getVersion(),
					IOUtils.toInputStream(annotation, (String) null));
		} catch (IOException e) {
			throw new PdfTronXMLException_IOExeption(e);
		}
	}


	public String getUserName(String userId) {
		User user = schemasRecordsServices.getUser(userId);

		return user.getFirstName() + " " + user.getLastName();
	}

	public boolean hasWrtteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public boolean hasEditAllAnnotation() {
		return getCurrentUser().has(CorePermissions.EDIT_ALL_ANNOTATION).on(record);
	}

	public boolean doesUserHaveLock() {
		return contentManager.doesUserHaveLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), getUserVO().getId());
	}

	public boolean obtainAnnotationLock() {
		boolean isLockObtained = contentManager.obtainAnnotationLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), getUserVO().getId(), pageRandomId);
		this.doesCurrentUserHaveAnnotationLock = isLockObtained;
		this.doesCurrnetPageHaveLock = isLockObtained;
		return doesCurrnetPageHaveLock;
	}

	public void releaseAnnotationLockIfUserhasIt() {
		if (!doesCurrentUserHaveAnnotationLock) {
			return;
		}

		contentManager.releaseAnnotationLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), getCurrentUser().getId(), this.pageRandomId);
		doesCurrentUserHaveAnnotationLock = false;
	}


	public String getContentAnnotationFromVault() throws IOException {
		InputStream contentAnnotationInputStream = null;
		try {
			contentAnnotationInputStream = contentManager.getContentAnnotationInputStream(contentVersion.getHash(),
					recordId, contentVersion.getVersion(), PdfTronPresenter.class.getSimpleName() + "getAnnotationsFromVault");

			return IOUtils.toString(contentAnnotationInputStream, "UTF-8");
		} finally {
			ioServices.closeQuietly(contentAnnotationInputStream);
		}
	}

	public void handleNewXml(String newXml, boolean userHasRightToEditOtherUserAnnotation, String userId)
			throws PdfTronXMLException_CannotEditOtherUsersAnnoations, PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption {
		String currenttAnnotations = xmlCurrentAnnotations;

		// Will throw if something is wrong.
		String xmlToSave = pdfTronParser.processNewXML(currenttAnnotations, newXml, userHasRightToEditOtherUserAnnotation, userId);

		if (xmlToSave != null) {
			saveAnnotation(xmlToSave);
			xmlCurrentAnnotations = xmlToSave;
			System.out.println(xmlToSave);
		}
	}

	@Override
	public List<ContentVersionVO> getAvalibleVersion() {
		MetadataSchema metadataSchema = metadataSchemasManager.getSchemaOf(record);
		Metadata contentMetadata = metadataSchema.getMetadata(metadataCode);

		Object contentValueAsObj = record.get(contentMetadata);

		Content content;
		if (contentValueAsObj instanceof Content) {
			content = (Content) contentValueAsObj;
		} else {
			throw new ImpossibleRuntimeException("Not implemented because no use case for now. (multi val)");
		}

		String currentVersion = contentVersion.getVersion();

		List<ContentVersionVO> listContentVersionVO = new ArrayList<>();

		ContentVersionToVOBuilder contentVersionToVOBuilder = new ContentVersionToVOBuilder(appLayerFactory.getModelLayerFactory());

		for (ContentVersion contentVersion : content.getHistoryVersions()) {
			if (!contentVersion.getVersion().equals(currentVersion)
				&& contentManager.hasContentAnnotation(contentVersion.getHash(), recordId, contentVersion.getVersion())) {
				listContentVersionVO.add(contentVersionToVOBuilder.build(content, contentVersion));
			}
		}

		return listContentVersionVO;
	}

	@Override
	public void addAnnotation(ContentVersion contentVErsionVO) {

	}

}
