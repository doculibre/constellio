package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;

import java.util.List;

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
			folder.addExternalLink(link.getId());
		}

		Transaction tr = new Transaction();
		tr.setUser(getCurrentUser());
		tr.addAll(links);
		tr.add(folder);

		try {
			rm.executeTransaction(tr);
			view.closeWindow();
			// TODO::JOLA --> Refresh isn't right, no new link displayed?
		} catch (RecordServicesException e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
}
