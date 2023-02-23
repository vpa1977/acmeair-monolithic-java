/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.service.FlightService;

@RestController
@RequestMapping("/api/flights")
public class FlightsREST {

	@Autowired
	private FlightService flightService;

	// TODO: Consider a pure GET implementation of this service, but maybe not much
	// value due to infrequent similar searches
	@RequestMapping(value = "/queryflights", method = RequestMethod.POST, produces = "text/plain", consumes = "application/x-www-form-urlencoded")
	public String getTripFlights(@RequestParam("fromAirport") String fromAirport,
			@RequestParam("toAirport") String toAirport, @RequestParam("fromDate") DateParam fromDate,
			@RequestParam("returnDate") DateParam returnDate, @RequestParam("oneWay") boolean oneWay) {

		String options = "";

		List<String> toFlights = flightService.getFlightByAirportsAndDepartureDate(fromAirport, toAirport,
				fromDate.getDate());

		if (!oneWay) {
			List<String> retFlights = flightService.getFlightByAirportsAndDepartureDate(toAirport, fromAirport,
					returnDate.getDate());

			options = "{\"tripFlights\":" + "[{\"numPages\":1,\"flightsOptions\": " + toFlights
					+ ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}, "
					+ "{\"numPages\":1,\"flightsOptions\": " + retFlights
					+ ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}], " + "\"tripLegs\":2}";
		} else {
			options = "{\"tripFlights\":" + "[{\"numPages\":1,\"flightsOptions\": " + toFlights
					+ ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}], " + "\"tripLegs\":1}";
		}

		return options;
	}

}