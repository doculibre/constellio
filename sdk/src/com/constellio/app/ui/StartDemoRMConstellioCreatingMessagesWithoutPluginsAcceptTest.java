package com.constellio.app.ui;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.records.wrappers.MessageBodyType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices.RecordIdVersion;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;
import com.thedeanda.lorem.LoremIpsum;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

@UiTest
@MainTest
public class StartDemoRMConstellioCreatingMessagesWithoutPluginsAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SchemasRecordsServices schemas;
	RMSchemasRecordsServices rm;
	Users users = new Users();
	List<User> usersList;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();

		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withDocumentsDecommissioningList().withAllTest(users)
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Iterator<RecordIdVersion> idVersionIterator =
				getModelLayerFactory().newSearchServices().recordsIdVersionIteratorUsingSolr(rm.folder.schemaType());
		while (idVersionIterator.hasNext()) {
			System.out.println(idVersionIterator.next().getRecordId());
		}

		recordServices = getModelLayerFactory().newRecordServices();
		schemas = new SchemasRecordsServices(zeCollection, getAppLayerFactory().getModelLayerFactory());
		AppLayerFactory appLayerFactory = getAppLayerFactory();

		DataLayerFactory dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();

		UserServices userServices = getModelLayerFactory().newUserServices();
		String token = userServices.generateToken("admin");
		String serviceKey = userServices.getUserConfigs("admin").getServiceKey();
		System.out.println("Admin token : \"" + token + "\", Admin service key \"" + serviceKey + "\"");
		System.out.println("http://localhost:7070/constellio/select?token=" + token + "&serviceKey=" + serviceKey
						   + "&fq=-type_s:index" + "&q=*:*");

		usersList = rm.searchUsers(where(Schemas.IDENTIFIER).isNotNull());
	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		setup();
		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	private void setup() throws RecordServicesException {

		LocalDateTime now = LocalDateTime.now();

		for (String folderId : records.folders) {
			Transaction transaction = new Transaction();

			String conversationId = folderId + "conversation";
			Conversation conversation = schemas.newConversation();
			Folder folder = rm.getFolder(folderId);
			folder.setConversation(conversation.getId());
			transaction.addAll(conversation, folder);
			for (int messageParentNumber = 1; messageParentNumber <= 100; messageParentNumber++) {
				String messageId = conversationId + "message" + messageParentNumber;
				int messageIdHash = messageId.hashCode();

				Message messageParent = schemas.newMessage()
						.setConversation(conversation.getId());

				messageParent.setMessageAuthor(generateMessageAuthorId(messageParent, messageIdHash));
				messageParent.setMessageBody(generateMessageBody(messageParent, messageIdHash));
				messageParent.setCreatedOn(now);

				now = now.plusMinutes(Math.abs(messageIdHash % 17));

				transaction.add(messageParent);

				int replyCount = Math.abs(messageIdHash % 13);
				for (int messageNumber = 1; messageNumber <= replyCount; messageNumber++) {
					String replyId = messageId + "reply" + messageNumber;
					int replyIdHash = replyId.hashCode();

					Message message = schemas.newMessage()
							.setConversation(conversation.getId())
							.setMessageParent(messageParent.getId());

					message.setMessageAuthor(generateMessageAuthorId(message, replyIdHash));
					message.setMessageBody(generateMessageBody(message, replyIdHash));
					message.setCreatedOn(now);

					now = now.plusMinutes(Math.abs(messageIdHash % 17));

					transaction.add(message);
				}
			}
			System.out.println("Adding transaction for folder" + folderId);
			recordServices.execute(transaction);
		}
	}

	private String generateMessageAuthorId(Message message, int semiRandomNumber) {
		return usersList.get(Math.abs(semiRandomNumber % usersList.size())).getId();
	}

	private String generateMessageBody(Message message, int semiRandomNumber) {
		String messageBody = "";

		int messageType = Math.abs(semiRandomNumber % 3);
		int addOptionalThing = Math.abs(semiRandomNumber % 5);

		switch (messageType) {
			case 0:
				message.setMessageBodyType(MessageBodyType.HTML);
				messageBody = LoremIpsum.getInstance().getHtmlParagraphs(1, 4);

				switch (addOptionalThing) {
					case 1:
					case 3:
						messageBody += "<a href=\"#\">" + LoremIpsum.getInstance().getWords(5, 10) + "</a>";
						break;
				}

				break;
			case 1:
				message.setMessageBodyType(MessageBodyType.PLAIN_TEXT);
				messageBody = LoremIpsum.getInstance().getParagraphs(1, 4);
				break;
			case 2:
				message.setMessageBodyType(MessageBodyType.PLAIN_TEXT);
				messageBody = LoremIpsum.getInstance().getWords(10, 100);
				break;
		}

		return messageBody;
	}

	@Test
	public void startApplicationWithSaveState()
			throws Exception {

		givenTransactionLogIsEnabled();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(
				new File("/path/to/the/saveState.zip")).withPasswordsResetAndDisableLDAPSync();

		newWebDriver(loggedAsUserInCollection("zeUser", "myCollection"));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startOnHomePageAsChuckNorris()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsDakota()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(dakota, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsRida()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, "LaCollectionDeRida"));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsGandalf()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsBob()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(bobGratton, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsCharles()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(charlesFrancoisXavier, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsEdouard()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(edouard, zeCollection));
		waitUntilICloseTheBrowsers();
	}
}
