package it.com.prims;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
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

public abstract class BaseTest {
	static {
		System.setProperty("spring.mongodb.embedded.version", "4.0.0");
	}

	protected static String BASE_URL;
	protected static String BASE_URL_WITH_CONTEXT_ROOT;

	protected static final String USERNAME = "uid0@email.com";
	protected static final String PASSWORD = "password";
	protected static final String PASSWORD_BAD = "passwordb";

	protected static final String LOAD_ENDPOINT = "rest/info/loader/load";
	protected static final String LOAD_RESPONSE = "Loaded flights and";

	protected static final String QUERY_ENDPOINT = "rest/api/flights/queryflights";
	protected static final String QUERY_RESPONSE = "\"destPort\": \"LHR\"";

	protected static final String LOGIN_ENDPOINT = "rest/api/login";
	protected static final String LOGIN_RESPONSE = "logged in";

	protected static final String LOGOUT_ENDPOINT = "rest/api/login/logout";
	protected static final String LOGOUT_RESPONSE = "logged out";

	protected static final String COUNT_SESSIONS_ENDPOINT = "rest/info/config/countSessions";
	protected static final String COUNT_SESSIONS_RESPONSE_0 = "0";
	protected static final String COUNT_SESSIONS_RESPONSE_1 = "1";

	protected static final String CUSTOMER_ENDPOINT = "rest/api/customer/byid";
	protected static final String CUSTOMER_RESPONSE = "{\"_id\": \"" + USERNAME + "\"";
	protected static final String CUSTOMER_RESPONSE_2 = "NOTNULL";

	protected static final String CUSTOMER_UPDATE = "{ \"_id\": \"" + USERNAME
			+ "\", \"status\": \"GOLD\", \"total_miles\": 1000000, \"miles_ytd\": 1000, \"address\": { \"streetAddress1\": \"123 Main St.\", \"streetAddress2\": \"NOTNULL\", \"city\": \"Anytown\", \"stateProvince\": \"NC\", \"country\": \"USA\", \"postalCode\": \"27617\" }, \"phoneNumber\": \"919-123-4567\", \"phoneNumberType\": \"BUSINESS\", \"password\": \""
			+ PASSWORD + "\" }";
	protected static final String CUSTOMER_UPDATE_BAD = "{ \"_id\": \"" + USERNAME
			+ "\", \"status\"  \"GOLD\", \"total_miles\" : 1000000, \"miles_ytd\" : 1000, \"address\" : { \"streetAddress1\" : \"123 Main St.\", \"streetAddress2\" : \"NOTNULL\", \"city\" : \"Anytown\", \"stateProvince\" : \"NC\", \"country\" : \"USA\", \"postalCode\" : \"27617\" }, \"phoneNumber\" : \"919-123-4567\", \"phoneNumberType\" : \"BUSINESS\", \"password\" : \""
			+ PASSWORD + "b\" }";

	protected static final String BOOKFLIGHT_ENDPOINT = "rest/api/bookings/bookflights";
	protected static final String BOOKFLIGHT_RESPONSE = "\"oneWay\":true,\"departBookingId\"";

	protected static final String COUNT_BOOKINGS_ENDPOINT = "rest/info/config/countBookings";
	protected static final String COUNT_BOOKINGS_RESPONSE_0 = "0";
	protected static final String COUNT_BOOKINGS_RESPONSE_1 = "1";

	protected static final String CANCELFLIGHT_ENDPOINT = "rest/api/bookings/cancelbooking";
	protected static final String CANCELFLIGHT_RESPONSE = "deleted.";

	protected static final String GET_BOOKINGS_ENDPOINT = "rest/api/bookings/byuser";

	public static final String SESSIONID_COOKIE_NAME = "acmeair_sessionid";

	protected TestRestTemplate restTemplate;

	@Autowired
	protected Environment environment;

	protected static String number = null;
	protected static String date;

	private static MongodExecutable mongodExe;

	@BeforeAll
	public static void oneTimeSetup() throws UnknownHostException, IOException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd");

		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime nowChanged = now.toLocalDate().atStartOfDay(now.getZone());
		date = nowChanged.format(dtf) + " 00:00:00 UTC 2020";

		ImmutableMongodConfig mongodConfig = MongodConfig.builder().version(Main.V4_0)
				.net(new Net("localhost", 27017, Network.localhostIsIPv6())).build();

		MongodStarter starter = MongodStarter.getDefaultInstance();

		mongodExe = starter.prepare(mongodConfig);
		mongodExe.start();

	}

	@AfterAll
	public static void tearDownMongo() {
		if (mongodExe != null)
			mongodExe.stop();
	}

	protected ResponseEntity<String> postForm(String url, MultiValueMap<String, String> data) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		headers.setAll(map);
		HttpEntity<?> request = new HttpEntity<>(data, headers);
		return restTemplate.postForEntity(url, request, String.class);
	}

	@Test
	public void test03_LoginBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGIN_ENDPOINT;

		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("login", USERNAME);
		form.add("password", PASSWORD_BAD);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Incorrect response code from " + url);
	}

	protected void setup() throws InterruptedException {
		BASE_URL = "http://localhost:" + environment.getProperty("local.server.port") + "/";
		BASE_URL_WITH_CONTEXT_ROOT = BASE_URL;
	}

}
