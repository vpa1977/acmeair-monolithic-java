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

import java.io.IOException;
import java.net.HttpCookie;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.process.runtime.Network;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.core.env.Environment;

@SpringBootTest(classes = {com.acmeair.web.AcmeAirApp.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EndpointTests {
    static {
        System.setProperty("spring.mongodb.embedded.version","4.0.0");
    }
	private static String BASE_URL;
	private static String BASE_URL_WITH_CONTEXT_ROOT;

	private static final String USERNAME = "uid0@email.com";
	private static final String PASSWORD = "password";
	private static final String PASSWORD_BAD = "passwordb";

	private static final String LOAD_ENDPOINT = "rest/info/loader/load";
	private static final String LOAD_RESPONSE = "Loaded flights and";

	private static final String QUERY_ENDPOINT = "rest/api/flights/queryflights";
	private static final String QUERY_RESPONSE = "\"destPort\" : \"LHR\"";

	private static final String LOGIN_ENDPOINT = "rest/api/login";
	private static final String LOGIN_RESPONSE = "logged in";

	private static final String LOGOUT_ENDPOINT = "rest/api/login/logout";
	private static final String LOGOUT_RESPONSE = "logged out";

	private static final String COUNT_SESSIONS_ENDPOINT = "rest/info/config/countSessions";
	private static final String COUNT_SESSIONS_RESPONSE_0 = "0";
	private static final String COUNT_SESSIONS_RESPONSE_1 = "1";

	private static final String CUSTOMER_ENDPOINT = "rest/api/customer/byid/";
	private static final String CUSTOMER_RESPONSE = "\"_id\" : \"" + USERNAME + "\"";
	private static final String CUSTOMER_RESPONSE_2 = "NOTNULL";

	private static final String CUSTOMER_UPDATE = "{ \"_id\" : \"" + USERNAME
			+ "\", \"status\" : \"GOLD\", \"total_miles\" : 1000000, \"miles_ytd\" : 1000, \"address\" : { \"streetAddress1\" : \"123 Main St.\", \"streetAddress2\" : \"NOTNULL\", \"city\" : \"Anytown\", \"stateProvince\" : \"NC\", \"country\" : \"USA\", \"postalCode\" : \"27617\" }, \"phoneNumber\" : \"919-123-4567\", \"phoneNumberType\" : \"BUSINESS\", \"password\" : \""
			+ PASSWORD + "\" }";
	private static final String CUSTOMER_UPDATE_BAD = "{ \"_id\" : \"" + USERNAME
			+ "\", \"status\" : \"GOLD\", \"total_miles\" : 1000000, \"miles_ytd\" : 1000, \"address\" : { \"streetAddress1\" : \"123 Main St.\", \"streetAddress2\" : \"NOTNULL\", \"city\" : \"Anytown\", \"stateProvince\" : \"NC\", \"country\" : \"USA\", \"postalCode\" : \"27617\" }, \"phoneNumber\" : \"919-123-4567\", \"phoneNumberType\" : \"BUSINESS\", \"password\" : \""
			+ PASSWORD + "b\" }";

	private static final String BOOKFLIGHT_ENDPOINT = "rest/api/bookings/bookflights";
	private static final String BOOKFLIGHT_RESPONSE = "\"oneWay\":true,\"departBookingId\"";

	private static final String COUNT_BOOKINGS_ENDPOINT = "rest/info/config/countBookings";
	private static final String COUNT_BOOKINGS_RESPONSE_0 = "0";
	private static final String COUNT_BOOKINGS_RESPONSE_1 = "1";

	private static final String CANCELFLIGHT_ENDPOINT = "rest/api/bookings/cancelbooking";
	private static final String CANCELFLIGHT_RESPONSE = "deleted.";

	private static final String GET_BOOKINGS_ENDPOINT = "rest/api/bookings/byuser";

	public static final String SESSIONID_COOKIE_NAME = "acmeair_sessionid";

	private static HttpCookie sessionCookie = null;
	private static String number = null;

	@Autowired
	Environment environment;

	private TestRestTemplate restTemplate = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES, HttpClientOption.ENABLE_REDIRECTS);

	private static MongodExecutable mongodExe;

	private static String date;

	@BeforeAll
	public static void oneTimeSetup() throws UnknownHostException, IOException {

		ImmutableMongodConfig mongodConfig = MongodConfig.builder().version(Main.V4_0)
				.net(new Net("localhost", 27017, Network.localhostIsIPv6())).build();

		MongodStarter starter = MongodStarter.getDefaultInstance();

		mongodExe = starter.prepare(mongodConfig);
		try { mongodExe.stop(); } catch (Throwable t) {}
		
		try { mongodExe.start(); }catch (Throwable t) {}

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd");

		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime nowChanged = now.toLocalDate().atStartOfDay(now.getZone());
		date = nowChanged.format(dtf) + " 00:00:00 UTC 2020";
	}

	@BeforeEach
	public void setup() {
		BASE_URL = "http://localhost:" + environment.getProperty("local.server.port") + "/";
		BASE_URL_WITH_CONTEXT_ROOT = BASE_URL;
	}

	@AfterEach
	public void teardown() {
	}

	@AfterAll
	public static void tearDownMongo() {
		if (mongodExe != null)
			mongodExe.stop();
	}
	
	private void doTest(String url, HttpStatus status, String expectedResponse) {
	    ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);	
	    assertEquals(status, response.getStatusCode(), "Incorrect response code from " + url);

	    if (expectedResponse != null) {
	      String result = response.getBody();
	      assertThat(result, containsString(expectedResponse));
	    }
	  }


	@Test
	public void test01_Load() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOAD_ENDPOINT;
		doTest(url, HttpStatus.OK, LOAD_RESPONSE);
	}
	
	private ResponseEntity<String> postForm(String url, MultiValueMap<String, String> data) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map<String,String> map = new HashMap<String, String>();
        map.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(data, headers);
        return restTemplate.postForEntity(url, request, String.class);
	}

	@Test
	public void test02_GetFlight() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + QUERY_ENDPOINT;

		LinkedMultiValueMap<String,String> form = new LinkedMultiValueMap<String,String>();
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
	public void test03_Login() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGIN_ENDPOINT;

		LinkedMultiValueMap<String,String> form = new LinkedMultiValueMap<String,String>();
		form.add("login", USERNAME);
		form.add("password", PASSWORD);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(LOGIN_RESPONSE));

		// get to use cookie for future requests
		for (String header : response.getHeaders().get(org.springframework.http.HttpHeaders.SET_COOKIE))
		{
			for (HttpCookie cookie : HttpCookie.parse(header)) 
			{
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
	public void test03_LoginBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGIN_ENDPOINT;

		LinkedMultiValueMap<String,String> form = new LinkedMultiValueMap<String,String>();
		form.add("login", USERNAME);
		form.add("password", PASSWORD_BAD);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Incorrect response code from " + url);
	}

	@Test
	public void test04_countSessions() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_SESSIONS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_SESSIONS_RESPONSE_1);
	}

	@Test
	public void test05_getCustomerBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;
		HttpStatus response = restTemplate.getForEntity(url, String.class).getStatusCode();
		assertEquals(HttpStatus.FORBIDDEN, response,"Incorrect response code from " + url);
	}

	@Test
	public void test05_getCustomer() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;
		
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(CUSTOMER_RESPONSE));
	}

	/*
	@Test
	public void test06_updateCustomer() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().cookie(sessionCookie)
				.post(Entity.entity(CUSTOMER_UPDATE, MediaType.APPLICATION_JSON), Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.OK.getStatusCode(), response.getStatus());

		String result = response.readEntity(String.class);
		assertThat(result, containsString(CUSTOMER_RESPONSE_2));

		response.close();
	}

	@Test
	public void test06_updateCustomerBad1() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().post(Entity.entity(CUSTOMER_UPDATE, MediaType.APPLICATION_JSON),
				Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test06_updateCustomerBad2() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().cookie(sessionCookie)
				.post(Entity.entity(CUSTOMER_UPDATE_BAD, MediaType.APPLICATION_JSON), Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test06_bookFlight() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + BOOKFLIGHT_ENDPOINT;

		WebTarget target = client.target(url);

		Form form = new Form();
		form.param("userid", USERNAME);
		form.param("fromAirport", "CDG");
		form.param("toAirport", "LHR");
		form.param("oneWayFlight", "true");
		form.param("fromDate", date);
		form.param("returnDate", date);

		Response response = target.request().cookie(sessionCookie)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.OK.getStatusCode(), response.getStatus());

		String result = response.readEntity(String.class);
		assertThat(result, containsString(BOOKFLIGHT_RESPONSE));
		number = result.substring(result.indexOf("departBookingId") + 18, result.lastIndexOf("}") - 1);

		response.close();
	}

	@Test
	public void test06_bookFlightBad() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + BOOKFLIGHT_ENDPOINT;

		WebTarget target = client.target(url);

		Form form = new Form();
		form.param("userid", USERNAME);
		form.param("fromAirport", "CDG");
		form.param("toAirport", "LHR");
		form.param("oneWayFlight", "true");
		form.param("fromDate", date);
		form.param("returnDate", date);

		Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
				Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test07_countBookings() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_BOOKINGS_ENDPOINT;
		doTest(url, Status.OK, COUNT_BOOKINGS_RESPONSE_1);
	}

	@Test
	public void test08_getBookings() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + GET_BOOKINGS_ENDPOINT + "/" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().cookie(sessionCookie).get();

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.OK.getStatusCode(), response.getStatus());

		String result = response.readEntity(String.class);
		assertThat(result, containsString(number));

		response.close();
	}

	@Test
	public void test08_getBookingsBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + GET_BOOKINGS_ENDPOINT + "/" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().get();

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test09_cancelBooking() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CANCELFLIGHT_ENDPOINT;

		WebTarget target = client.target(url);

		Form form = new Form();
		form.param("userid", USERNAME);
		form.param("number", number);

		Response response = target.request().cookie(sessionCookie)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.OK.getStatusCode(), response.getStatus());

		String result = response.readEntity(String.class);
		assertThat(result, containsString(CANCELFLIGHT_RESPONSE));

		response.close();
	}

	@Test
	public void test09_cancelBookingBad() throws InterruptedException, ParseException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CANCELFLIGHT_ENDPOINT;

		WebTarget target = client.target(url);

		Form form = new Form();
		form.param("userid", USERNAME);
		form.param("number", number);

		Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
				Response.class);

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test10_countBookings() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_BOOKINGS_ENDPOINT;
		doTest(url, Status.OK, COUNT_BOOKINGS_RESPONSE_0);
	}

	@Test
	public void test11_LogoutBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGOUT_ENDPOINT + "?login=" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().get();

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.FORBIDDEN.getStatusCode(), response.getStatus());

		response.close();
	}

	@Test
	public void test11_Logout() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGOUT_ENDPOINT + "?login=" + USERNAME;

		WebTarget target = client.target(url);

		Response response = target.request().cookie(sessionCookie).get();

		Thread.sleep(20);
		assertEquals("Incorrect response code from " + url, Status.OK.getStatusCode(), response.getStatus());

		String result = response.readEntity(String.class);
		assertThat(result, containsString(LOGOUT_RESPONSE));

		response.close();
	}

	@Test
	public void test12_countSessions() {
		String url = BASE_URL_WITH_CONTEXT_ROOT + COUNT_SESSIONS_ENDPOINT;
		doTest(url, HttpStatus.OK, COUNT_SESSIONS_RESPONSE_0);
	}

*/
}
