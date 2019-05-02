package com.constellio.app.modules.restapi.validation;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockException;
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
import java.util.Collection;
import java.util.List;
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
								  String date, int expiration,
								  String version, Boolean physical, String signature) throws Exception {
		String physicalValue = physical != null ? String.valueOf(physical) : null;
		String data = StringUtils.concat(host, id, serviceKey, schemaType, method, date, String.valueOf(expiration), version, physicalValue);

		Collection<String> tokens = validationDao.getUserTokens(serviceKey, true);
		if (tokens.isEmpty()) {
			throw new UnauthenticatedUserException();
		}

		for (String token : tokens) {
			String currentSignature = signatureService.sign(token, data);

			if (currentSignature.equals(signature)) {
				return;
			}
		}
		throw new InvalidSignatureException();
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
					Record record = validationDao.getUserByUsername(principal, collection);
					if (record == null) {
						record = validationDao.getGroupByCode(principal, collection);
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
			throw new OptimisticLockException(recordId, eTag, recordVersion);
		}
	}

	public void validateHost(String host) {
		if (!validationDao.getAllowedHosts().contains(host)) {
			throw new UnallowedHostException(host);
		}
	}

	@Override
	protected BaseDao getDao() {
		return validationDao;
	}
}
