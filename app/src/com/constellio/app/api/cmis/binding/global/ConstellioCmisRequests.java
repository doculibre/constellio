package com.constellio.app.api.cmis.binding.global;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.acl.ApplyAclRequest;
import com.constellio.app.api.cmis.requests.acl.GetAclRequest;
import com.constellio.app.api.cmis.requests.discovery.QueryUnsupportedRequest;
import com.constellio.app.api.cmis.requests.navigation.GetChildrenRequest;
import com.constellio.app.api.cmis.requests.navigation.GetDescendantsUnsupportedRequest;
import com.constellio.app.api.cmis.requests.navigation.GetFolderParentRequest;
import com.constellio.app.api.cmis.requests.navigation.GetObjectByPathRequest;
import com.constellio.app.api.cmis.requests.navigation.GetObjectParentsRequest;
import com.constellio.app.api.cmis.requests.object.AllowableActionsRequest;
import com.constellio.app.api.cmis.requests.object.BulkUpdatePropertiesRequest;
import com.constellio.app.api.cmis.requests.object.CreateDocumentRequest;
import com.constellio.app.api.cmis.requests.object.CreateFolderRequest;
import com.constellio.app.api.cmis.requests.object.CreateObjectRequest;
import com.constellio.app.api.cmis.requests.object.DeleteObjectRequest;
import com.constellio.app.api.cmis.requests.object.DeleteTreeRequest;
import com.constellio.app.api.cmis.requests.object.GetContentStreamRequest;
import com.constellio.app.api.cmis.requests.object.GetObjectRequest;
import com.constellio.app.api.cmis.requests.object.MoveObjectRequest;
import com.constellio.app.api.cmis.requests.object.UpdatePropertiesRequest;
import com.constellio.app.api.cmis.requests.objectType.GetTypeChildrenRequest;
import com.constellio.app.api.cmis.requests.objectType.GetTypeDefinitionRequest;
import com.constellio.app.api.cmis.requests.objectType.GetTypeDescendantsRequest;
import com.constellio.app.api.cmis.requests.repository.GetRepositoryInfoRequest;
import com.constellio.app.api.cmis.requests.versioning.CancelCheckOutUnsupportedRequest;
import com.constellio.app.api.cmis.requests.versioning.ChangeContentStreamRequest;
import com.constellio.app.api.cmis.requests.versioning.CheckInRequest;
import com.constellio.app.api.cmis.requests.versioning.CheckOutRequest;
import com.constellio.app.api.cmis.requests.versioning.GetAllVersionsRequest;
import com.constellio.app.api.cmis.requests.versioning.GetObjectOfLatestVersionUnsupportedRequest;
import com.constellio.app.api.cmis.requests.versioning.GetPropertiesOfLatestVersionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;

public class ConstellioCmisRequests extends AbstractCmisService implements CallContextAwareCmisService {

	private final AppLayerFactory appLayerFactory;
	private final CmisCacheManager repositoryManager;
	private CallContext context;

	/**
	 * The role of this class is to dispatch the service call to the correct constellio collection repository
	 */
	public ConstellioCmisRequests(AppLayerFactory appLayerFactory,
								  final CmisCacheManager repositoryManager) {

		this.appLayerFactory = appLayerFactory;
		this.repositoryManager = repositoryManager;
	}

	@Override
	public CallContext getCallContext() {
		return context;
	}

	/**
	 * This method should only be called by the service factory.
	 */
	@Override
	public void setCallContext(CallContext context) {
		this.context = context;
	}

	// --- repository service ---

	@Override
	public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		ConstellioCollectionRepository repository = getConstellioCollectionRepository(repositoryId);
		return new GetRepositoryInfoRequest(repository, appLayerFactory, repositoryId, extension, getCallContext())
				.processRequest();

	}

	@Override
	public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		SystemWideUserInfos userCredential = ConstellioCmisRequestFactory.authenticateUserFromContext(context, userServices);

		CallContext callContext = getCallContext();

		for (ConstellioCollectionRepository fsr : repositoryManager.getRepositories()) {
			String collection = fsr.getCollection();
			if (userCredential.getCollections().contains(collection)) {
				User user = userServices.getUserInCollection(userCredential.getUsername(), collection);
				if (userCredential.isSystemAdmin() || user.has(USE_EXTERNAL_APIS_FOR_COLLECTION).globally()) {
					result.add(new GetRepositoryInfoRequest(fsr, appLayerFactory, collection, extension, callContext)
							.processRequest());
				}
			}
		}

		return result;
	}

	@Override
	public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
											  BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return new GetTypeChildrenRequest(getConstellioCollectionRepository(repositoryId), getCallContext(), typeId, maxItems,
				includePropertyDefinitions, skipCount, appLayerFactory).processRequest();
	}

	@Override
	public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
		return new GetTypeDefinitionRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(),
				typeId).processRequest();
	}

	@Override
	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
															Boolean includePropertyDefinitions,
															ExtensionsData extension) {
		return new GetTypeDescendantsRequest(getConstellioCollectionRepository(repositoryId), getCallContext(), typeId, depth,
				includePropertyDefinitions, appLayerFactory).processRequest();
	}

	// --- navigation service ---

	@Override
	public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
										  Boolean includeAllowableActions, IncludeRelationships includeRelationships,
										  String renditionFilter,
										  Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
										  ExtensionsData extension) {
		return new GetChildrenRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				folderId, filter, includeAllowableActions, includePathSegment, maxItems, skipCount, this).processRequest();
	}

	@Override
	public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
														String filter,
														Boolean includeAllowableActions,
														IncludeRelationships includeRelationships,
														String renditionFilter,
														Boolean includePathSegment, ExtensionsData extension) {
		return new GetDescendantsUnsupportedRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(),
				folderId, depth, filter, includeAllowableActions, includePathSegment, this, false).processRequest();
	}

	@Override
	public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
		GetObjectParentsRequest getObjectParentsRequest = new GetObjectParentsRequest(getConstellioCollectionRepository(
				repositoryId), appLayerFactory, getCallContext(), folderId, filter, null, null, this);
		return new GetFolderParentRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getObjectParentsRequest, getCallContext()).processRequest();
	}

	@Override
	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
													   String filter,
													   Boolean includeAllowableActions,
													   IncludeRelationships includeRelationships,
													   String renditionFilter,
													   Boolean includePathSegment, ExtensionsData extension) {
		return new GetDescendantsUnsupportedRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(),
				folderId, depth, filter, includeAllowableActions, includePathSegment, this, true).processRequest();
	}

	@Override
	public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
												   Boolean includeAllowableActions,
												   IncludeRelationships includeRelationships, String renditionFilter,
												   Boolean includeRelativePathSegment, ExtensionsData extension) {
		return new GetObjectParentsRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(),
				objectId, filter, includeAllowableActions, includeRelativePathSegment, this).processRequest();
	}

	@Override
	public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
										Boolean includeAllowableActions, IncludeRelationships includeRelationships,
										String renditionFilter,
										BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		ObjectListImpl result = new ObjectListImpl();
		result.setHasMoreItems(false);
		result.setNumItems(BigInteger.ZERO);
		List<ObjectData> emptyList = Collections.emptyList();
		result.setObjects(emptyList);

		return result;
	}

	// --- object service ---

	@Override
	public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
						 VersioningState versioningState, List<String> policies, ExtensionsData extension) {
		CreateFolderRequest createFolderRequest = new CreateFolderRequest(getConstellioCollectionRepository(repositoryId),
				appLayerFactory, getCallContext(), properties, folderId);
		CreateDocumentRequest createDocumentRequest = new CreateDocumentRequest(getConstellioCollectionRepository(repositoryId),
				appLayerFactory, getCallContext(), properties, folderId, contentStream, versioningState);

		ObjectData object = new CreateObjectRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				createFolderRequest, createDocumentRequest, getCallContext(), properties, folderId, contentStream,
				versioningState, this).processRequest();

		return object.getId();
	}

	@Override
	public String createDocument(String repositoryId, Properties properties, String folderId,
								 ContentStream contentStream,
								 VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
								 ExtensionsData extension) {
		return new CreateDocumentRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				properties, folderId, contentStream, versioningState).processRequest().getDocumentId();
	}

	@Override
	public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
							   Acl addAces,
							   Acl removeAces, ExtensionsData extension) {
		return new CreateFolderRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				properties, folderId).processRequest();
	}

	@Override
	public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
											 ExtensionsData extension) {
		new DeleteObjectRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(), objectId)
				.processRequest();
	}

	@Override
	public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
										 UnfileObject unfileObjects,
										 Boolean continueOnFailure, ExtensionsData extension) {
		return new DeleteTreeRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				folderId,
				continueOnFailure).processRequest();
	}

	@Override
	public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
		return new AllowableActionsRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(),
				objectId).processRequest();
	}

	@Override
	public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
										  BigInteger length, ExtensionsData extension) {
		ContentStream contentStream = new GetContentStreamRequest(getConstellioCollectionRepository(repositoryId),
				appLayerFactory,
				getCallContext(), objectId, offset, length).processRequest();
		return contentStream;
	}

	@Override
	public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
								IncludeRelationships includeRelationships, String renditionFilter,
								Boolean includePolicyIds, Boolean includeAcl,
								ExtensionsData extension) {
		return new GetObjectRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				objectId,
				null, filter, includeAllowableActions, includeAcl, this).processRequest();
	}

	@Override
	public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
									  IncludeRelationships includeRelationships, String renditionFilter,
									  Boolean includePolicyIds, Boolean includeAcl,
									  ExtensionsData extension) {
		return new GetObjectByPathRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				path, filter, includeAllowableActions, includeAcl, this).processRequest();
	}

	@Override
	public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		ObjectData object = new GetObjectRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(), objectId, null, filter, false, false, this).processRequest();

		return object.getProperties();
	}

	@Override
	public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
											 BigInteger maxItems,
											 BigInteger skipCount, ExtensionsData extension) {
		return Collections.emptyList();
	}

	@Override
	public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
						   ExtensionsData extension) {
		new MoveObjectRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(), objectId,
				targetFolderId, this).processRequest();
	}

	@Override
	public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
								 Holder<String> changeToken,
								 ContentStream contentStream, ExtensionsData extension) {
		new ChangeContentStreamRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				objectId, overwriteFlag, contentStream, false).processRequest();
	}

	@Override
	public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
									ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
		new ChangeContentStreamRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				objectId, true, contentStream, true).processRequest();
	}

	@Override
	public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
									ExtensionsData extension) {
		new ChangeContentStreamRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				objectId, true, null, false).processRequest();
	}

	@Override
	public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
								 Properties properties,
								 ExtensionsData extension) {
		new UpdatePropertiesRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				objectId,
				properties, this).processRequest();
	}

	@Override
	public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId,
																	   List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken,
																	   Properties properties,
																	   List<String> addSecondaryTypeIds,
																	   List<String> removeSecondaryTypeIds,
																	   ExtensionsData extension) {
		UpdatePropertiesRequest updatePropertiesRequest = new UpdatePropertiesRequest(getConstellioCollectionRepository(
				repositoryId),
				appLayerFactory, getCallContext(), null, properties, this);
		return new BulkUpdatePropertiesRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				updatePropertiesRequest, getCallContext(), objectIdAndChangeToken, properties, this).processRequest();
	}

	// --- versioning service ---

	@Override
	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
										   Boolean includeAllowableActions, ExtensionsData extension) {

		return new GetAllVersionsRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				repositoryId, objectId, versionSeriesId, filter, includeAllowableActions, extension, this).processRequest();
	}

	@Override
	public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
						 Holder<Boolean> contentCopied) {
		new CheckOutRequest(getConstellioCollectionRepository(repositoryId), context, appLayerFactory, repositoryId,
				objectId,
				extension,
				contentCopied).processRequest();
	}

	@Override
	public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
		new CancelCheckOutUnsupportedRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				repositoryId, objectId, extension).processRequest();
	}

	@Override
	public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
						ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces,
						Acl removeAces,
						ExtensionsData extension) {
		new CheckInRequest(getConstellioCollectionRepository(repositoryId), getCallContext(), appLayerFactory, repositoryId,
				objectId, major, properties, contentStream, checkinComment, policies, addAces, removeAces, extension)
				.processRequest();
	}

	@Override
	public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
											   Boolean major,
											   String filter, Boolean includeAllowableActions,
											   IncludeRelationships includeRelationships, String renditionFilter,
											   Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		return new GetObjectOfLatestVersionUnsupportedRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(), objectId, versionSeriesId, major, filter, includeAllowableActions, includeRelationships,
				renditionFilter, includePolicyIds, includeAcl, extension, this).processRequest();
	}

	@Override
	public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
												   Boolean major,
												   String filter, ExtensionsData extension) {
		return new GetPropertiesOfLatestVersionRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory,
				getCallContext(), objectId, versionSeriesId, major, filter, extension).processRequest();
	}

	// --- ACL service ---

	@Override
	public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		return new GetAclRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(), objectId)
				.processRequest();
	}

	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
						AclPropagation aclPropagation,
						ExtensionsData extension) {
		return new ApplyAclRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				repositoryId, objectId, addAces, removeAces, aclPropagation, extension).processRequest();
	}

	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
		return new ApplyAclRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				repositoryId, objectId, aces, aclPropagation).processRequest();
	}

	// --- discovery service ---

	@Override
	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
							Boolean includeAllowableActions,
							IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems,
							BigInteger skipCount,
							ExtensionsData extension) {
		return new QueryUnsupportedRequest(getConstellioCollectionRepository(repositoryId), appLayerFactory, getCallContext(),
				statement,
				includeAllowableActions, maxItems, skipCount, this).processRequest();
	}

	// --- Utils ---

	private ConstellioCollectionRepository getConstellioCollectionRepository(String repositoryId) {
		return repositoryManager.getCollectionRepository(repositoryId);
	}
}
