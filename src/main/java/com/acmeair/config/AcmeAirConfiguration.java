package com.acmeair.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.service.AuthService;
import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


@RestController
@RequestMapping("/info/config")
public class AcmeAirConfiguration {
    
	private static Logger logger = Logger.getLogger(AcmeAirConfiguration.class.getName());

	@Autowired
	private BookingService bs;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AuthService authService;

	@Autowired
	private FlightService flightService;
	

	
    public AcmeAirConfiguration() {
        super();
    }	
		
	
	@RequestMapping(value = "/activeDataService", method = RequestMethod.GET, produces = "text/html")
	public ResponseEntity<String> getActiveDataServiceInfo() {
		try {		
			logger.fine("Get active Data Service info");
			return new ResponseEntity<>(bs.getServiceType(), HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Unknown", HttpStatus.OK);
		}
	}
	
	
	private static final JsonObject makeDescriptionElement(String name, String description) {
		JsonObject js = new JsonObject();
		js.add("name", new JsonPrimitive(name));
		js.add("description", new JsonPrimitive(description));
		return js;
	}
	
	/**
    *  Get runtime info.
    */
    @RequestMapping(value = "/runtime", method = RequestMethod.GET, produces = "application/json")
    public String getRuntimeInfo() {
      Gson gson = new GsonBuilder().create();
      JsonArray array = new JsonArray();
      array.add(makeDescriptionElement("Runtime", "Java"));
      array.add(makeDescriptionElement("Version", System.getProperty("java.version")));
      array.add(makeDescriptionElement("Vendor", System.getProperty("java.vendor")));
      return gson.toJson(array);
    }
	
	@RequestMapping(value = "/countBookings", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long> countBookings() {
		try {
			Long count = bs.count();			
			return new ResponseEntity<>(count, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/countCustomers", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long> countCustomers() {
		try {
			Long customerCount = customerService.count();
			return new ResponseEntity<>(customerCount, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
	
	@RequestMapping(value = "/countSessions", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long> countSessions() {
		try {
			Long customerCount = authService.countSessions();
			return new ResponseEntity<>(customerCount, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
	
	@RequestMapping(value = "/countFlights", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long>  countFlights() {
		try {
			Long count = flightService.countFlights();			
			return new ResponseEntity<>(count, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/countFlightSegments", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long>  countFlightSegments() {
		try {
			Long count = flightService.countFlightSegments();			
			return new ResponseEntity<>(count, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/countAirports", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Long>  countAirports() {
		try {			
			Long count = flightService.countAirports();	
			return new ResponseEntity<>(count, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>((long)-1, HttpStatus.OK);
		}
	}
	
}
