package it.com.prims;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest(classes = { com.acmeair.web.AcmeAirApp.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
class EndpointNotLoggedInTest extends BaseTest {

	@BeforeEach
	public void setup() throws InterruptedException {
		super.setup();
		restTemplate = new TestRestTemplate();
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

	@Test
	public void test05_getCustomerBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;
		HttpStatus response = restTemplate.getForEntity(url, String.class).getStatusCode();
		assertEquals(HttpStatus.FORBIDDEN, response, "Incorrect response code from " + url);
	}

	@Test
	public void test06_updateCustomerBad1() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CUSTOMER_ENDPOINT + "/" + USERNAME;
		RequestEntity<String> re = RequestEntity.post(url).accept(MediaType.APPLICATION_JSON).body(CUSTOMER_UPDATE);

		ResponseEntity<String> response = restTemplate.exchange(re, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "Incorrect response code from " + url);

		String result = response.getBody();
		assertThat(result, containsString(CUSTOMER_RESPONSE_2));
	}

	@Test
	public void test08_getBookingsBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + GET_BOOKINGS_ENDPOINT + "/" + USERNAME;

		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Incorrect response code from " + url);
	}

	@Test
	public void test09_cancelBooking() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + CANCELFLIGHT_ENDPOINT;
		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("userid", USERNAME);
		form.add("number", number);
		ResponseEntity<String> response = postForm(url, form);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Incorrect response code from " + url);
	}

	@Test
	public void test11_LogoutBad() throws InterruptedException {
		String url = BASE_URL_WITH_CONTEXT_ROOT + LOGOUT_ENDPOINT + "?login=" + USERNAME;
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Incorrect response code from " + url);
	}

}