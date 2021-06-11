package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.schemas.validators.MetadataUniqueValidator;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ExternalLinkSourcePresenter extends BasePresenter<ExternalLinkSourceView> {

	private RMSchemasRecordsServices rm;

	private String source;
	private Folder folder;

	public ExternalLinkSourcePresenter(ExternalLinkSourceView view) {
		super(view);

		rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
	}

	public void forParams(String source, String folderId) {
		this.source = source;
		folder = rm.getFolder(folderId);
	}

	public String getSource() {
		return source;
	}

	public void addExternalLinks(List<ExternalLink> links) {
		for (ExternalLink link : links) {
			link.setLinkedto(folder);
			folder.addExternalLink(link.getId());
		}

		Transaction tr = new Transaction();
		tr.setRecordFlushing(RecordsFlushing.NOW());
		tr.setUser(getCurrentUser());
		tr.addAll(links);
		tr.add(folder);

		try {
			rm.executeTransaction(tr);
			view.closeWindow();
			view.navigate().to(RMViews.class).displayFolder(folder.getId());
		} catch (RecordServicesException e) {
			List<String> titles = new ArrayList<>();
			for (ExternalLink link : links) {
				folder.removeExternalLink(link.getId());
				titles.add(link.getTitle());
			}
			if (e instanceof RecordServicesException.ValidationException
				&& ((ValidationException) e).getErrors().getValidationErrors().stream().anyMatch(f -> MetadataUniqueValidator.NON_UNIQUE_METADATA.equals(f.getValidatorErrorCode()))
				&& ((ValidationException) e).getErrors().getValidationErrors().stream().anyMatch(f -> ((String) f.getParameters().get(MetadataUniqueValidator.METADATA_CODE)).startsWith("externalLink_"))) {
				view.showErrorMessage($("ExternalLinkSourcePresenter.addExternalLinkError", String.join(",", titles)));
			} else {
				view.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
}
