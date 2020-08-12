package com.constellio.app.modules.restapi.validation;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockRuntimeException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.signature.SignatureService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.DELETE;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.GET;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.PATCH;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.POST;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.PUT;

public class ValidationService extends BaseService {

	@Inject
	private SignatureService signatureService;
	@Inject
	private ValidationDao validationDao;

	public void validateSignature(String host, String id, String serviceKey, String schemaType, String method,
								  String date, int expiration, String version, Boolean physical, String copySourceId,
								  String signature) throws Exception {
		String physicalValue = physical != null ? String.valueOf(physical) : null;
		String data = StringUtils.concat(host, id, serviceKey, schemaType, method, date, String.valueOf(expiration),
				version, physicalValue, copySourceId);

		List<String> tokens = validationDao.getUserTokens(serviceKey, false, true);
		if (isInvalidSignature(tokens, data, signature)) {
			// try while bypassing cache
			tokens = validationDao.getUserTokens(serviceKey, true, true);
			if (isInvalidSignature(tokens, data, signature)) {
				throw new InvalidSignatureException();
			}
		}
	}

	public void validateUrl(String date, int expiration) {
		LocalDateTime urlDate;
		try {
			urlDate = DateUtils.parseIsoNoMillis(date);
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException("date", date);
		}

		LocalDateTime now = TimeProvider.getLocalDateTime();
		if (now.isAfter(urlDate.plusSeconds(expiration))) {
			throw new ExpiredSignedUrlException();
		}
	}

	public void validateUserAccess(User user, Record record, String method) {
		switch (method) {
			case GET:
				if (!user.hasReadAccess().on(record)) {
					throw new UnauthorizedAccessException();
				}
				break;
			case POST:
			case PUT:
			case PATCH:
				if (!user.hasWriteAccess().on(record)) {
					throw new UnauthorizedAccessException();
				}
				break;
			case DELETE:
				if (!user.hasDeleteAccess().on(record)) {
					throw new UnauthorizedAccessException();
				}
				break;
			default:
				throw new IllegalArgumentException(String.format("Invalid method value : %s", method));
		}
	}

	public void validateUserDeleteAccessOnHierarchy(User user, Record record) {
		if (!validationDao.userHasDeleteAccessOnHierarchy(user, record)) {
			throw new UnauthorizedAccessException();
		}
	}

	public void validateAuthentication(String token, String serviceKey) {
		if (!validationDao.isUserAuthenticated(token, serviceKey)) {
			throw new UnauthenticatedUserException();
		}
	}

	public void validateAuthorizations(List<AceDto> authorizations, String collection) {
		String dateFormat = validationDao.getDateFormat();

		Set<String> principals = Sets.newHashSet();
		for (AceDto ace : ListUtils.nullToEmpty(authorizations)) {
			for (String principal : ace.getPrincipals()) {
				boolean added = principals.add(principal);
				if (added) {
					Record record = validationDao.getUserRecordByUsername(principal, collection);
					if (record == null) {
						record = validationDao.getGroupRecordByCode(principal, collection);
					}
					if (record == null) {
						throw new RecordNotFoundException(principal);
					}
					if (isLogicallyDeleted(record)) {
						throw new RecordLogicallyDeletedException(principal);
					}
				}
			}

			if (ace.getEndDate() != null && ace.getStartDate() == null) {
				throw new RequiredParameterException("ace.startDate");
			}
			if (ace.getStartDate() != null && ace.getEndDate() != null) {
				LocalDate start = DateUtils.parseLocalDate(ace.getStartDate(), dateFormat);
				LocalDate end = DateUtils.parseLocalDate(ace.getEndDate(), dateFormat);

				if (end.isBefore(start)) {
					throw new InvalidDateCombinationException(ace.getStartDate(), ace.getEndDate());
				}
			}
		}
	}

	public void validateETag(String recordId, String eTag, long recordVersion) {
		if (!eTag.equals(String.valueOf(recordVersion))) {
			throw new OptimisticLockRuntimeException(recordId, eTag, recordVersion);
		}
	}

	public void validateHost(String host) {
		if (!validationDao.getAllowedHosts().contains(host)) {
			throw new UnallowedHostException(host);
		}
	}

	public void validateToken(String token, String serviceKey) {
		Map<String, LocalDateTime> tokens = validationDao.getUserAccessTokens(serviceKey);
		if (!tokens.containsKey(token)) {
			throw new UnauthenticatedUserException();
		}

		if (tokens.get(token).isBefore(TimeProvider.getLocalDateTime())) {
			throw new ExpiredTokenException();
		}
	}

	public void validateCollection(String collection) {
		if (validationDao.getRecordById(collection) == null) {
			throw new RecordNotFoundException(collection);
		}
	}

	@Override
	protected BaseDao getDao() {
		return validationDao;
	}

	private boolean isInvalidSignature(List<String> tokens, String data, String signature) throws Exception {
		if (tokens.isEmpty()) {
			throw new UnauthenticatedUserException();
		}

		for (String token : tokens) {
			String currentSignature = signatureService.sign(token, data);

			if (currentSignature.equals(signature)) {
				return false;
			}
		}
		return true;
	}
}
