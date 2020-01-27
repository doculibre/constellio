package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordCommentsDisplayPresenter implements Serializable {

	private RecordCommentsDisplay editor;

	private String recordId;

	private String metadataCode;

	private SchemaPresenterUtils presenterUtils;

	private List<Comment> comments = new ArrayList<>();

	ConstellioEIMConfigs eimConfigs;

	private AuthorizationsServices authorizationsServices;

	private Record record;

	public RecordCommentsDisplayPresenter(RecordCommentsDisplay editor) {
		this.editor = editor;
	}

	public void forRecordVO(RecordVO recordVO, String metadataCode) {
		this.recordId = recordVO.getId();
		this.metadataCode = metadataCode;
		init();
	}

	public void forRecordId(String recordId, String metadataCode) {
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		init();
	}

	private void init() {
		SessionContext sessionContext = editor.getSessionContext();
		ConstellioFactories constellioFactories = editor.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		authorizationsServices = modelLayerFactory.newAuthorizationsServices();

		record = recordServices.getDocumentById(recordId);
		String schemaCode = record.getSchemaCode();

		presenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);

		Metadata metadata = presenterUtils.getMetadata(metadataCode);
		String caption = metadata.getLabel(Language.withCode(presenterUtils.getCurrentLocale().getLanguage()));
		comments = record.get(metadata);
		if (comments == null) {
			comments = new ArrayList<>();
		}
		editor.setComments(comments);
		editor.setCaption(caption);

		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory);
		if (Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
			editor.setReadOnly(true);
		}
	}

	public boolean commentAdded(Comment newComment) {
		if (StringUtils.isBlank(newComment.getMessage())) {
			return false;
		}

		User user = presenterUtils.getCurrentUser();
		newComment.setUser(user);
		newComment.setDateTime(new LocalDateTime());

		List<Comment> newComments = new ArrayList<>(comments);
		newComments.add(0, newComment);

		updateComments(newComments);
		return true;
	}

	public void commentDeleted(Comment comment) {
		List<Comment> newComments = new ArrayList<>(comments);
		newComments.remove(comment);
		updateComments(newComments);
	}

	private void updateComments(List<Comment> newComments) {
		Metadata metadata = presenterUtils.getMetadata(metadataCode);

		Record record = presenterUtils.getRecord(recordId);

		record.set(metadata, newComments);
		if (eimConfigs.isAddCommentsWhenReadAuthorization()) {
			RecordUpdateOptions recordUpdateOptions = new RecordUpdateOptions();
			presenterUtils.addOrUpdate(record, recordUpdateOptions.setSkipUserAccessValidation(true));
		} else {
			presenterUtils.addOrUpdate(record);
		}
		editor.setComments(comments = newComments);
	}

	public boolean commentCreatedByCurrentUser(Comment comment) {
		return presenterUtils.getCurrentUser().getId().equals(comment.getUserId());
	}

	public boolean addButtonVisible() {
		User currentUser = presenterUtils.getCurrentUser();
		return authorizationsServices.canWrite(currentUser, record) || eimConfigs.isAddCommentsWhenReadAuthorization();
	}
}
