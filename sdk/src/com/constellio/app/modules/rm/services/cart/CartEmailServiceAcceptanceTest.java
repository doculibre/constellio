package com.constellio.app.modules.rm.services.cart;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServices.MessageAttachment;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class CartEmailServiceAcceptanceTest extends ConstellioTest {

	private static final String TEST_ID = "CartEmlServiceAcceptanceTest-inputStreams";
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	Users users = new Users();
	String title1 = "Chevreuil.odt";
	String title2 = "Grenouille.odt";
	Content content1_title1, content1_title2, content2_title1, content2_title2;
	File content1File, content2File;
	Document document11WithContent1HavingTitle1, document12WithContent1HavingTitle2, document21WithContent2HavingTitle1,
			document22WithContent2HavingTitle2, documentWithoutContent;

	Cart cart;
	private IOServices ioServices;
	private ContentManager contentManager;
	private CartEmailService cartEmlService;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		users.setUp(getModelLayerFactory().newUserServices());
		contentManager = getModelLayerFactory().getContentManager();
		ioServices = getDataLayerFactory().getIOServicesFactory().newIOServices();
		cartEmlService = new CartEmailService(zeCollection, getModelLayerFactory());

		initTestData();
	}

	private void initTestData()
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		content1_title1 = createContent(title1, title1);
		content1_title2 = createContent(title1, title2);
		content2_title1 = createContent(title2, title1);
		content2_title2 = createContent(title2, title2);
		File folder = newTempFolder();
		content1File = createFileFromContent(content1_title1, folder.getPath() + "/1");
		content2File = createFileFromContent(content2_title2, folder.getPath() + "/2");

		document11WithContent1HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1)
				.setFolder(records.getFolder_A01().getId());
		transaction.add(document11WithContent1HavingTitle1.setContent(content1_title1).setTitle("11"));

		document12WithContent1HavingTitle2 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document12WithContent1HavingTitle2.setContent(content1_title2).setTitle("12"));

		document21WithContent2HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle("21"));

		document22WithContent2HavingTitle2 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document22WithContent2HavingTitle2.setContent(content2_title2).setTitle("22"));

		documentWithoutContent = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(documentWithoutContent.setTitle("withoutContent"));

		cart = rm.getOrCreateUserCart(users.aliceIn(zeCollection));
		cart.setTitle("Ou est mon panier!!!");
		List<Record> documents = asList(document11WithContent1HavingTitle1.getWrappedRecord(),
				document12WithContent1HavingTitle2.getWrappedRecord(),
				document21WithContent2HavingTitle1.getWrappedRecord(),
				document22WithContent2HavingTitle2.getWrappedRecord(),
				documentWithoutContent.getWrappedRecord());
		addDocumentsToCart(cart, documents);
		transaction.add(cart);
		recordServices.execute(transaction);
		recordServices.execute(new Transaction(documents));
	}

	private void addDocumentsToCart(Cart cart, List<Record> documents) {
		for (Record record : documents) {
			rm.wrapDocument(record).addFavorite(cart.getId());
		}
	}

	@Test
	public void whenCreateAttachmentThenOk()
			throws Exception {
		MessageAttachment attachment = cartEmlService
				.createAttachment(content1_title1);
		assertThat(attachment.getMimeType()).isEqualTo(content1_title1.getCurrentVersion().getMimetype());
		assertThat(attachment.getAttachmentName()).isEqualTo(content1_title1.getCurrentVersion().getFilename());
		assertThat(attachment.getInputStream()).hasContentEqualTo(new FileInputStream(content1File));
	}

	@Test
	public void givenDocumentWithContentWhenGetDocumentsAttachmentsThenOk()
			throws Exception {
		List<MessageAttachment> attachments = cartEmlService
				.getDocumentsAttachments(asList(document22WithContent2HavingTitle2.getId()), users.adminIn(zeCollection));
		assertThat(attachments.size()).isEqualTo(1);

		MessageAttachment attachment = attachments.get(0);
		assertThat(attachment.getMimeType()).isEqualTo(content2_title2.getCurrentVersion().getMimetype());
		assertThat(attachment.getAttachmentName()).isEqualTo(content2_title2.getCurrentVersion().getFilename());
		assertThat(attachment.getInputStream()).hasContentEqualTo(new FileInputStream(content2File));
	}

	@Test(expected = CartEmlServiceRuntimeException_InvalidRecordId.class)
	public void givenNonExistingDocumentWhenGetDocumentsAttachmentsThenEmptyAttachments()
			throws Exception {
		cartEmlService
				.getDocumentsAttachments(asList("invalidId"), users.adminIn(zeCollection));
	}

	@Test
	public void givenDocumentWithoutContentWhenGetDocumentsAttachmentsThenOk()
			throws Exception {
		List<MessageAttachment> attachments = cartEmlService
				.getDocumentsAttachments(asList(documentWithoutContent.getId()), users.adminIn(zeCollection));
		assertThat(attachments).isEmpty();
	}

	@Test
	public void givenCartWithTestDocumentsWhenGetAttachmentsThenOk()
			throws Exception {
		List<MessageAttachment> attachments = cartEmlService.getAttachments(getCartDocumentIds(cart), users.adminIn(zeCollection));
		validateAttachments(attachments);
	}

	private void validateAttachments(List<MessageAttachment> attachments)
			throws FileNotFoundException {
		assertThat(attachments.size()).isEqualTo(4);
		MessageAttachment attachment0 = attachments.get(0);
		MessageAttachment attachment1 = attachments.get(1);
		MessageAttachment attachment2 = attachments.get(2);
		MessageAttachment attachment3 = attachments.get(3);

		assertThat(attachment0.getMimeType()).isEqualTo(content1_title1.getCurrentVersion().getMimetype());
		assertThat(attachment0.getAttachmentName()).isEqualTo(content1_title1.getCurrentVersion().getFilename());
		assertThat(attachment0.getInputStream()).hasContentEqualTo(new FileInputStream(content1File));

		assertThat(attachment1.getMimeType()).isEqualTo(content1_title2.getCurrentVersion().getMimetype());
		assertThat(attachment1.getAttachmentName()).isEqualTo(content1_title2.getCurrentVersion().getFilename());
		assertThat(attachment1.getInputStream()).hasContentEqualTo(new FileInputStream(content1File));

		assertThat(attachment2.getMimeType()).isEqualTo(content2_title1.getCurrentVersion().getMimetype());
		assertThat(attachment2.getAttachmentName()).isEqualTo(content2_title1.getCurrentVersion().getFilename());
		assertThat(attachment2.getInputStream()).hasContentEqualTo(new FileInputStream(content2File));

		assertThat(attachment3.getMimeType()).isEqualTo(content2_title2.getCurrentVersion().getMimetype());
		assertThat(attachment3.getAttachmentName()).isEqualTo(content2_title2.getCurrentVersion().getFilename());
		assertThat(attachment3.getInputStream()).hasContentEqualTo(new FileInputStream(content2File));
	}

	@Test
	public void givenCartWithTestDocumentsWhenCreateEmlForCartThenOk()
			throws Exception {
		InputStream emlStreamFactory = cartEmlService.createEmailForCart(cart.getOwner(), getCartDocumentIds(cart), users.adminIn(zeCollection)).getInputStream();
		validateEml(emlStreamFactory);
		IOUtils.closeQuietly(emlStreamFactory);
	}

	@Test
	public void givenCartWithTestDocumentsWhenCreateEmlForCartThenHasAdequateMimeType()
			throws Exception {
		InputStream emlStreamFactory = cartEmlService.createEmailForCart(cart.getOwner(), getCartDocumentIds(cart), users.adminIn(zeCollection)).getInputStream();
		File tempFolder = newTempFolder();
		File file = new File(tempFolder, "test.eml");
		FileUtils.copyInputStreamToFile(emlStreamFactory, file);

		assertThat(MimeTypes.getMIMEType(file)).isEqualTo("message/rfc822");
		FileUtils.deleteQuietly(tempFolder);
		IOUtils.closeQuietly(emlStreamFactory);
	}

	@Test
	public void givenEmptyCartWithTestDocumentsWhenCreateEmlForCartThenOk()
			throws Exception {
		for (Document document : getCartDocuments(cart)) {
			document.removeFavorite(cart.getId());
		}
		InputStream emlStreamFactory = cartEmlService.createEmailForCart(cart.getOwner(), getCartDocumentIds(cart), users.adminIn(zeCollection)).getInputStream();
		IOUtils.closeQuietly(emlStreamFactory);
	}

	private void validateEml(InputStream eml)
			throws MessagingException, IOException {

		Session mailSession = Session.getInstance(System.getProperties());
		MimeMessage message = new MimeMessage(mailSession, eml);
		assertThat(message.getFrom()).containsOnly(new InternetAddress(users.alice().getEmail()));

		EmailServices emailService = new EmailServices();

		assertThat(emailService.getBody(message)).contains(cartEmlService.getSignature(users.aliceIn(zeCollection)));

		List<MessageAttachment> attachments = emailService.getAttachments(message);
		assertThat(attachments.size()).isEqualTo(4);
		MessageAttachment attachment0 = attachments.get(0);
		MessageAttachment attachment1 = attachments.get(1);
		MessageAttachment attachment2 = attachments.get(2);
		MessageAttachment attachment3 = attachments.get(3);

		assertThat(attachment0.getMimeType()).contains(content1_title1.getCurrentVersion().getMimetype());
		assertThat(attachment0.getAttachmentName()).isEqualTo(content1_title1.getCurrentVersion().getFilename());
		assertThat(attachment0.getInputStream()).hasContentEqualTo(new FileInputStream(content1File));

		assertThat(attachment1.getMimeType()).contains(content1_title2.getCurrentVersion().getMimetype());
		assertThat(attachment1.getAttachmentName()).isEqualTo(content1_title2.getCurrentVersion().getFilename());
		assertThat(attachment1.getInputStream()).hasContentEqualTo(new FileInputStream(content1File));

		assertThat(attachment2.getMimeType()).contains(content2_title1.getCurrentVersion().getMimetype());
		assertThat(attachment2.getAttachmentName()).isEqualTo(content2_title1.getCurrentVersion().getFilename());
		assertThat(attachment2.getInputStream()).hasContentEqualTo(new FileInputStream(content2File));

		assertThat(attachment3.getMimeType()).contains(content2_title2.getCurrentVersion().getMimetype());
		assertThat(attachment3.getAttachmentName()).isEqualTo(content2_title2.getCurrentVersion().getFilename());
		assertThat(attachment3.getInputStream()).hasContentEqualTo(new FileInputStream(content2File));
	}

	private Content createContent(String resource, String title) {
		User user = users.adminIn(zeCollection);
		ContentVersionDataSummary version01 = upload("Minor_" + resource);
		Content content = contentManager.createMinor(user, title, version01);
		ContentVersionDataSummary version10 = upload("Major_" + resource);
		content.updateContent(user, version10, true);
		return content;
	}

	private ContentVersionDataSummary upload(String resource) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("RMTestRecords_" + resource);
		return contentManager.upload(inputStream);
	}

	private File createFileFromContent(Content content, String filePath) {
		InputStream inputStream = null;
		try {
			inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), TEST_ID);
			FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
			return new File(filePath);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	public List<String> getCartDocumentIds(Cart cart) {
		List<Document> documents = getCartDocuments(cart);
		List<String> documentsIds = new ArrayList<>();
		for (Document document : documents) {
			documentsIds.add(document.getId());
		}
		return documentsIds;
	}

	private List<Document> getCartDocuments(Cart cart) {
		final Metadata metadata = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.document.schemaType()).where(metadata).isContaining(asList(cart.getId())));
		return rm.searchDocuments(logicalSearchQuery);
	}
}
