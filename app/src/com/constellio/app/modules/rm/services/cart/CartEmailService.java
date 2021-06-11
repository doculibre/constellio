package com.constellio.app.modules.rm.services.cart;

import com.constellio.app.api.extensions.params.EmailMessageParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.emails.EmailServices.MessageAttachment;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import org.apache.commons.io.IOUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CartEmailService {
	private static final String TMP_EML_FILE = "CartEmailService-emlFile";

	private final RMSchemasRecordsServices rm;
	private final IOServices ioServices;
	private final ContentManager contentManager;
	private File newTempFolder;

	public CartEmailService(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = modelLayerFactory.getContentManager();
	}

	public EmailMessage createEmailForCart(String cartOwner, List<String> cartDocuments, User requestUser) {
		try {
			newTempFolder = ioServices.newTemporaryFile(TMP_EML_FILE);
			return createEmailForCart(cartOwner, cartDocuments, newTempFolder, requestUser);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(newTempFolder);
		}
	}

	EmailMessage createEmailForCart(String cartOwner, List<String> cartDocuments, File messageFile, User requestUser) {
		try (OutputStream outputStream = ioServices.newFileOutputStream(messageFile, CartEmailService.class.getSimpleName() + ".createMessageForCart.out")) {
			User user = rm.getUser(cartOwner);
			String signature = getSignature(user);
			String subject = "";
			String from = user.getEmail();
			String userFullName = user.getFirstName() +" "+ user.getLastName();
			List<MessageAttachment> attachments = getAttachments(cartDocuments, requestUser);

			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			EmailMessageParams params = new EmailMessageParams("cart", signature, subject, from, attachments, userFullName);
			EmailMessage emailMessage = appLayerFactory.getExtensions().getSystemWideExtensions().newEmailMessage(params);
			if (emailMessage == null) {
				EmailServices emailServices = new EmailServices();
				ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
				MimeMessage message = emailServices.createMimeMessage(from, subject, signature, attachments, configs, userFullName);
				message.writeTo(outputStream);
				String filename = "cart.eml";
				InputStream inputStream = ioServices.newFileInputStream(messageFile, CartEmailService.class.getSimpleName() + ".createMessageForCart.in");
				emailMessage = new EmailMessage(filename, inputStream);
				closeAllInputStreams(attachments);
			}
			return emailMessage;
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void closeAllInputStreams(List<MessageAttachment> attachments) {
		for (MessageAttachment attachment : attachments) {
			ioServices.closeQuietly(attachment.getInputStream());
			IOUtils.closeQuietly(attachment.getInputStream());
		}
	}

	List<MessageAttachment> getAttachments(List<String> cartDocuments, User requestUser)
			throws IOException {
		//FIXME current version get only cart documents attachments
		List<MessageAttachment> returnList = new ArrayList<>();
		returnList.addAll(getDocumentsAttachments(cartDocuments, requestUser));
		return returnList;
	}

	List<MessageAttachment> getDocumentsAttachments(List<String> documentsIds, User requestUser)
			throws IOException {
		List<MessageAttachment> returnList = new ArrayList<>();
		if (documentsIds != null) {
			for (String currentDocumentId : documentsIds) {
				try {
					Document document = rm.getDocument(currentDocumentId);
					if (document.getContent() != null && !document.isLogicallyDeletedStatus() && requestUser.hasReadAccess().on(document)) {
						MessageAttachment contentFile = createAttachment(document.getContent());
						returnList.add(contentFile);
					}
				} catch (NoSuchRecordWithId e) {
					throw new CartEmlServiceRuntimeException_InvalidRecordId(e);
				}

			}
		}
		return returnList;
	}

	MessageAttachment createAttachment(Content content)
			throws IOException {
		String hash = content.getCurrentVersion().getHash();
		InputStream inputStream = contentManager.getContentInputStream(hash, content.getCurrentVersion().getFilename());
		String mimeType = content.getCurrentVersion().getMimetype();
		String attachmentName = content.getCurrentVersion().getFilename();
		return new MessageAttachment().setMimeType(mimeType).setAttachmentName(attachmentName).setInputStream(inputStream);
	}

	String getSignature(User user) {
		return user.getSignature() != null ? user.getSignature() : user.getTitle();
	}

}
