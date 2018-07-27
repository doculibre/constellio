package com.constellio.app.modules.restapi.signature;

import com.constellio.app.modules.restapi.core.util.Algorithms;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureServiceTest {

    private String data = "localhostidserviceKeyDOCUMENTGET20500101T080000Z36001.0";
    private String key = "token";

    private String expectedSignature = "vbTrKqAtjZGVeqku99GiqzV7S3pmQaD1gt7rng4GVQ8";

    private SignatureService signatureService;

    @Before
    public void setUp() {
        signatureService = new SignatureService();
    }

    @Test
    public void testSign() throws Exception {
        String signature = signatureService.sign(key, data);

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void testSignWithAlgorithmParameter() throws Exception {
        String signature = signatureService.sign(key, data, Algorithms.HMAC_SHA_256);

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void testSignWithWrongData() throws Exception {
        String signature = signatureService.sign(key, data.concat("fake"));

        assertThat(signature).isNotEqualTo(expectedSignature);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSignWithInvalidAlgorithm() throws Exception {
        signatureService.sign(key, data, Algorithms.MD5);

    }

}
