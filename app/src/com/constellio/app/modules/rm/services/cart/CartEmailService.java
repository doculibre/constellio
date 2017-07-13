package com.constellio.app.modules.rm.services.cart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId;
import com.constellio.app.modules.rm.wrappers.Cart;
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
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;

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

	public EmailMessage createEmailForCart(Cart cart) {
		try {
			newTempFolder = ioServices.newTemporaryFile(TMP_EML_FILE);
			return createEmailForCart(cart, newTempFolder);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(newTempFolder);
		}
	}

	EmailMessage createEmailForCart(Cart cart, File messageFile) {
		try (OutputStream outputStream = ioServices.newFileOutputStream(messageFile, CartEmailService.class.getSimpleName() + ".createMessageForCart.out")) {
			User user = rm.getUser(cart.getOwner());
			String signature = getSignature(user);
			String subject = "";
			String from = user.getEmail();
			List<MessageAttachment> attachments = getAttachments(cart);
			
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			EmailMessage emailMessage = appLayerFactory.getExtensions().getSystemWideExtensions().newEmailMessage("cart", signature, subject, from, attachments);
			if (emailMessage == null) {
				EmailServices emailServices = new EmailServices();
				MimeMessage message = emailServices.createMimeMessage(from, subject, signature, attachments);
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

	List<MessageAttachment> getAttachments(Cart cart)
			throws IOException {
		//FIXME current version get only cart documents attachments
		List<MessageAttachment> returnList = new ArrayList<>();
		returnList.addAll(getDocumentsAttachments(cart.getDocuments()));
		return returnList;
	}

	List<MessageAttachment> getDocumentsAttachments(List<String> documentsIds)
			throws IOException {
		List<MessageAttachment> returnList = new ArrayList<>();
		if (documentsIds != null) {
			for (String currentDocumentId : documentsIds) {
				try {
					Document document = rm.getDocument(currentDocumentId);
					if (document.getContent() != null) {
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
