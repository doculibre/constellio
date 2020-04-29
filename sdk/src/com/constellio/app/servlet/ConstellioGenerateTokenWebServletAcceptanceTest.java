package com.constellio.app.servlet;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.constellio.sdk.tests.TestUtils.assertThatException;
import static org.assertj.core.api.Assertions.assertThat;

public class ConstellioGenerateTokenWebServletAcceptanceTest extends ConstellioTest {

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withAllTest(users));
		startApplication();
	}

	@After
	public void tearDown()
			throws Exception {
		stopApplication();
	}

	@Test
	public void validateWebService()
			throws Exception {

		givenTimeIs(dateTime(2014, 1, 2, 3, 0, 3));
		getModelLayerFactory().newUserServices().addUpdateUserCredential(getModelLayerFactory().newUserServices()
				.getUser(admin).setServiceKey("adminkey"));
		getModelLayerFactory().newAuthenticationService().changePassword(admin, "1qaz2wsx");
		getModelLayerFactory().newAuthenticationService().changePassword(aliceWonderland, "mouhahaha");
		getModelLayerFactory().newAuthenticationService().changePassword(bobGratton, "1qaz2wsx");
		getModelLayerFactory().newAuthenticationService().changePassword(dakota, "wololo");

		for (boolean usingHeader : new boolean[]{false, true}) {

			assertThat(callWebservice("admin", "1qaz2wsx", "2d", null, usingHeader))
					.is(returningValidCredentialUntil(dateTime(2014, 1, 4, 3, 0, 3)))
					.has(returnedServiceKey("adminkey"));

			assertThat(callWebservice("bob", "1qaz2wsx", "3j", null, usingHeader))
					.is(returningValidCredentialUntil(dateTime(2014, 1, 5, 3, 0, 3)))
					.has(returnedServiceKey("agent_bob"));

			assertThat(callWebservice("dakota", "wololo", "49h", null, usingHeader))
					.is(returningValidCredentialUntil(dateTime(2014, 1, 4, 4, 0, 3)))
					.has(returnedServiceKey("agent_dakota"));

			assertThat(callWebservice("admin", "1qaz2wsx", "26", "alice", usingHeader))
					.is(returningValidCredentialUntil(dateTime(2014, 1, 3, 5, 0, 3)))
					.has(returnedServiceKey("agent_alice"));

			assertThatException(() -> callWebservice("", "wololo", "12h", null, usingHeader)).hasMessageContaining("401 Bad username/password");
			assertThatException(() -> callWebservice("admin", "", "12h", null, usingHeader)).hasMessageContaining("401 Bad username/password");
			assertThatException(() -> callWebservice("admin", "1qaz", "", null, usingHeader)).hasMessageContaining("422 Parameter 'duration' required");
			assertThatException(() -> callWebservice("admin", "wololo", "12h", null, usingHeader)).hasMessageContaining("401 Bad username/password");
			assertThatException(() -> callWebservice("dakota", "wololo", "12h", "alice", usingHeader)).hasMessageContaining("401 asUser requires system admin rights");
			assertThatException(() -> callWebservice("admin", "1qaz2wsx", "12h", "johny", usingHeader)).hasMessageContaining("401 Bad asUser value");
			assertThatException(() -> callWebservice("admin", "1qaz2wsx", "12w", "alice", usingHeader)).hasMessageContaining("422 Bad Duration. Example : 14d or 24h");
		}
	}

	private Condition<? super String> returnedServiceKey(final String key) {
		return new Condition<String>() {
			@Override
			public boolean matches(String value) {
				SAXBuilder builder = new SAXBuilder();
				File testFile = new File(newTempFolder(), "test.xml");
				System.out.println(value);
				try {
					FileUtils.write(testFile, value);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					Document document = builder.build(testFile);

					String serviceKey = document.getRootElement().getChild("serviceKey").getText();

					assertThat(serviceKey).isEqualTo(key);

				} catch (JDOMException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}

	private Condition<? super String> returningValidCredentialUntil(final LocalDateTime dateTime) {
		return new Condition<String>() {
			@Override
			public boolean matches(String value) {
				SAXBuilder builder = new SAXBuilder();
				File testFile = new File(newTempFolder(), "test.xml");
				System.out.println(value);
				try {
					FileUtils.write(testFile, value);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					Document document = builder.build(testFile);

					String serviceKey = document.getRootElement().getChild("serviceKey").getText();
					String token = document.getRootElement().getChild("token").getText();

					String user = getModelLayerFactory().newUserServices().getUserCredentialByServiceKey(serviceKey);
					assertThat(getModelLayerFactory().newUserServices().isAuthenticated(serviceKey, token))
							.describedAs("can be authentified with given credentials").isTrue();
					assertThat(getModelLayerFactory().newUserServices().getUser(user).getAccessTokens())
							.describedAs("token end date").containsEntry(token, dateTime);

				} catch (JDOMException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}


	private String callWebservice(String username, String password, String duration, String asUser, boolean usingHeader)
			throws Exception {
		WebClient webClient = new WebClient();
		WebRequest webRequest;

		if (usingHeader) {
			String url = "http://localhost:7070/constellio/generateToken";

			webRequest = new WebRequest(new URL(url));

			webRequest.setAdditionalHeader("username", username);
			webRequest.setAdditionalHeader("password", password);
			webRequest.setAdditionalHeader("duration", duration);
			if (asUser != null) {
				webRequest.setAdditionalHeader("asUser", asUser);
			}
		} else {
			String url =
					"http://localhost:7070/constellio/generateToken?username=" + username + "&password=" + password + "&duration="
					+ duration + "&asUser=" + asUser;

			webRequest = new WebRequest(new URL(url));
		}

		Page page = webClient.getPage(webRequest);
		String html = page.getWebResponse().getContentAsString();
		return html;
	}

}
