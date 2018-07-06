package com.constellio.app.modules.restapi.validation;

import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.document.dto.AceDto;
import com.constellio.app.modules.restapi.signature.SignatureService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.Schemas;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.HttpMethod;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ValidationServiceTest {

    @Mock private ValidationDao validationDao;
    @Mock private SignatureService signatureService;

    @Mock private User user;
    @Mock private Record record;
    @Mock private UserPermissionsChecker userPermissionsChecker;

    @InjectMocks private ValidationService validationService;

    private String id = "id";
    private String serviceKey = "serviceKey";
    private String schemaType = SchemaTypes.DOCUMENT.name();
    private String method = HttpMethod.GET;
    private String date = "20500101T080000Z";
    private int expiration = 3600;
    private String version = "1.0";
    private boolean physical = false;

    private String token = "token";
    private String signature = "8OQvBGl5lu64hwRSAIh8KQkuq17W-SYLWcsJLEGMMe4";

    private String collection = "zeCollection";

    private List<AceDto> aces = singletonList(AceDto.builder().principals(singleton("1")).permissions(singleton(READ)).build());

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(signatureService.sign(anyString(), anyString())).thenReturn(signature);

        when(validationDao.getUserTokens(anyString(), anyBoolean())).thenReturn(singletonList(token));
        when(validationDao.isUserAuthenticated(anyString(), anyString())).thenReturn(true);
        when(validationDao.getDateFormat()).thenReturn("yyyy-MM-dd");
        when(validationDao.getUserByUsername(anyString(), anyString())).thenReturn(record);

        when(user.hasReadAccess()).thenReturn(userPermissionsChecker);
        when(userPermissionsChecker.on(record)).thenReturn(true);
    }

    @Test
    public void testValidateSignatureWithCorrectSignature() throws Exception {
        validationService.validateSignature(id, serviceKey, schemaType, method, date, expiration, version, physical, signature);
    }

    @Test(expected=InvalidSignatureException.class)
    public void testValidateSignatureWithInvalidSignature() throws Exception {
        String fakeSignature = "0000000000000000000000000000000000000000000";
        validationService.validateSignature(id, serviceKey, schemaType, method, date, expiration, version, physical, fakeSignature);
    }

    @Test(expected=InvalidSignatureException.class)
    public void testValidateSignatureWithInvalidToken() throws Exception {
        when(validationDao.getUserTokens(anyString(), anyBoolean())).thenReturn(Lists.newArrayList("fakeToken1", "fakeToken2"));
        when(signatureService.sign(anyString(), anyString())).thenReturn("anotherSignature");

        validationService.validateSignature(id, serviceKey, schemaType, method, date, expiration, version, physical, signature);
    }

    @Test(expected=UnauthenticatedUserException.class)
    public void testValidateSignatureWithNoToken() throws Exception {
        when(validationDao.getUserTokens(anyString(), anyBoolean())).thenReturn(Collections.<String>emptyList());

        validationService.validateSignature(id, serviceKey, schemaType, method, date, expiration, version, physical, signature);
    }

    @Test
    public void testValidateUrlWithActivePeriod() {
        validationService.validateUrl(date, expiration);
    }

    @Test(expected=ExpiredSignedUrlException.class)
    public void testValidateUrlWithExpiredPeriod() {
        validationService.validateUrl("20170101T080000Z", expiration);
    }

    @Test
    public void testValidateUserAccessWithAllowedWritePermission() {
        validationService.validateUserAccess(user, record, method);
    }

    @Test(expected=UnauthorizedAccessException.class)
    public void testValidateUserAccessWithUnallowedWritePermission() {
        when(userPermissionsChecker.on(record)).thenReturn(false);

        validationService.validateUserAccess(user, record, method);
    }

    @Test
    public void testValidateAuthentication() {
        validationService.validateAuthentication(token, serviceKey);
    }

    @Test(expected=UnauthenticatedUserException.class)
    public void testValidateAuthenticationWithInvalidToken() {
        when(validationDao.isUserAuthenticated(anyString(), anyString())).thenReturn(false);

        validationService.validateAuthentication("invalidToken", serviceKey);
    }

    @Test
    public void testValidateAuthorizations() {
        validationService.validateAuthorizations(aces, collection);
    }

    @Test(expected=RecordNotFoundException.class)
    public void testValidateAuthorizationsWithInvalidPrincipal() {
        when(validationDao.getUserByUsername(anyString(), anyString())).thenReturn(null);

        validationService.validateAuthorizations(aces, collection);
    }

    @Test(expected=RecordLogicallyDeletedException.class)
    public void testValidateAuthorizationsWithLogicallyDeletedPrincipal() {
        when(record.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

        validationService.validateAuthorizations(aces, collection);
    }

    @Test
    public void testValidateAuthorizationsWithStartDateAndNullEndDate() {
        aces.get(0).setStartDate("1970-01-01");

        validationService.validateAuthorizations(aces, collection);
    }

    @Test(expected=RequiredParameterException.class)
    public void testValidateAuthorizationsWithEndDateAndNullStartDate() {
        aces.get(0).setEndDate("1970-01-01");

        validationService.validateAuthorizations(aces, collection);
    }

    @Test(expected=InvalidDateCombinationException.class)
    public void testValidateAuthorizationsWithEndDateGreatherThanStartDate() {
        aces.get(0).setStartDate("1970-02-01");
        aces.get(0).setEndDate("1970-01-01");

        validationService.validateAuthorizations(aces, collection);
    }

    @Test
    public void testValidateETag() {
        validationService.validateETag("1", "1", 1L);
    }

    @Test(expected=OptimisticLockException.class)
    public void testValidateETagWithMismatch() {
        validationService.validateETag("1", "1", 2L);
    }

}
