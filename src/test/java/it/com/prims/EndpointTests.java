/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package it.com.prims;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpCookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest(classes = { com.acmeair.web.AcmeAirApp.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EndpointTests extends BaseTest {

	private static HttpCookie sessionCookie = null;

	class CookieInterfceptor implements ClientHttpRequestInterceptor {

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			if (sessionCookie != null) {
				request.getHeaders().add(HttpHeaders.COOKIE, sessionCookie.toString());
			}
			ClientHttpResponse response = execution.execute(request, body);
			return response;
		}

	}

	@BeforeEach
	public void setup() throws InterruptedException {
		super.setup();
		restTemplate = new TestRestTemplate(new RestTemplateBuilder().interceptors(new CookieInterfceptor()));

		if (sessionCookie == null) {
			load();
			login();
		}
	}

	@AfterEach
	public void teardown() {
	}

	private void doTest(String url, HttpStatus status, String expectedResponse) {
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(status, response.getStatusCode(), "Incorrect response code from " + url);

		if (expectedResponse != null) {
			String result = response.getBody();
			assertThat(result, containsString(expectedResponse));
		}
	}

	public void load() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOAD_ENDPOINT;
		doTest(url, HttpStatus.OK, LOAD_RESPONSE);
	}

	public void login() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGIN_ENDPOINT;

		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("login", USERNAME);
		form.add("password", PASSWORD);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(LOGIN_RESPONSE));

		// get to use cookie for future requests
		for (String header : response.getHeaders().get(org.springframework.http.HttpHeaders.SET_COOKIE)) {
			for (HttpCookie cookie : HttpCookie.parse(header)) {
				if (SESSIONID_COOKIE_NAME.equals(cookie.getName())) {
					sessionCookie = cookie;
					break;
				}
			}
			if (sessionCookie != null)
				break;
		}
		assertThat("Cookie should not be null", sessionCookie != null);
	}

	@Test
	public void test02_GetFlight() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + QUERY_ENDPOINT;

		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("fromAirport", "CDG");
		form.add("toAirport", "LHR");
		form.add("oneWay", "true");
		form.add("fromDate", date);
		form.add("returnDate", date);

		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String flightData = response.getBody();
		assertThat(flightData, containsString(QUERY_RESPONSE));
	}

	@Test
	public void test04_countSessions() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_SESSIONS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_SESSIONS_RESPONSE_1);
	}

	@Test
	public void test05_getCustomer() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;

		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(CUSTOMER_RESPONSE));
	}

	@Test
	public void test06_updateCustomer() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;
		RequestEntity<String> re = RequestEntity.post(url).contentType(MediaType.APPLICATION_JSON)
				.body(CUSTOMER_UPDATE);

		ResponseEntity<String> response = restTemplate.exchange(re, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(CUSTOMER_RESPONSE_2));
	}

	@Test
	public void test06_bookFlight() {

		String url = BASE_URL_WITH_CONTEXT_ROOT + QUERY_ENDPOINT;

		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("fromAirport", "CDG");
		form.add("toAirport", "LHR");
		form.add("oneWay", "true");
		form.add("fromDate", date);
		form.add("returnDate", date);

		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		Gson gson = new GsonBuilder().create();

		JsonObject flightData = gson.fromJson(response.getBody(), JsonObject.class);

		JsonArray arr = flightData.get("tripFlights").getAsJsonArray().get(0).getAsJsonObject().get("flightsOptions")
				.getAsJsonArray();
		JsonObject option = arr.get(0).getAsJsonObject();

		url = BASE_URL_WITH_CONTEXT_ROOT + BOOKFLIGHT_ENDPOINT;

		form = new LinkedMultiValueMap<String, String>();
		form.add("userid", USERNAME);
		form.add("toFlightSegId", option.get("flightSegmentId").getAsString());
		form.add("toFlightId", option.get("_id").getAsString());
		form.add("oneWayFlight", "true");

		response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(BOOKFLIGHT_RESPONSE));
		number = result.substring(result.indexOf("departBookingId") + 18, result.lastIndexOf("}") - 1);
	}

	@Test
	public void test07_countBookings() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_BOOKINGS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_BOOKINGS_RESPONSE_1);
	}

	@Test
	public void test08_getBookings() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + GET_BOOKINGS_ENDPOINT + "/" + USERNAME;

		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(number));
	}

	@Test
	public void test09_cancelBooking() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CANCELFLIGHT_ENDPOINT;
		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("userid", USERNAME);
		form.add("number", number);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(CANCELFLIGHT_RESPONSE));
	}

	@Test
	public void test10_countBookings() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_BOOKINGS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_BOOKINGS_RESPONSE_0);
	}

	@Test
	public void test11_Logout() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGOUT_ENDPOINT + "?login=" + USERNAME;
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);
		String result = response.getBody();
		assertThat(result, containsString(LOGOUT_RESPONSE));
	}

	@Test
	public void test12_countSessions() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_SESSIONS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_SESSIONS_RESPONSE_0);
	}

}
