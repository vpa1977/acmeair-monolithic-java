/*******************************************************************************
 * Copyright (c) 2013-2015 IBM Corp.
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
package com.acmeair.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.acmeair.AirportCodeMapping;

public abstract class FlightService {
	protected FlightService() {
		if (useFlightDataRelatedCaching == null) {
			try {
				if (System.getenv("USE_FLIGHT_DATA_RELATED_CACHING") != null) {
					System.out.println("Found env variable USE_FLIGHT_DATA_RELATED_CACHING");
					useFlightDataRelatedCaching = Boolean
							.parseBoolean(System.getenv("USE_FLIGHT_DATA_RELATED_CACHING"));
					System.out.println("useFlightDataRelatedCaching : " + useFlightDataRelatedCaching);
				} else {
					System.out.println(
							"USE_FLIGHT_DATA_RELATED_CACHING environment variable is set. Enabling Caching. To disable caching, use Environment variable USE_FLIGHT_DATA_RELATED_CACHING=false");
					useFlightDataRelatedCaching = true;
				}
			} catch (Exception e) {
				System.out.println("USE_FLIGHT_DATA_RELATED_CACHING error. Check for below log");
				e.printStackTrace();
			}
		}
	}

	private Gson gson = new GsonBuilder().create();
	protected Logger logger = Logger.getLogger(FlightService.class.getName());

	protected static Boolean useFlightDataRelatedCaching = null;
	protected static String acmeairDir = "";

	// TODO:need to find a way to invalidate these maps
	protected static ConcurrentHashMap<String, String> originAndDestPortToSegmentCache = new ConcurrentHashMap<String, String>();
	protected static ConcurrentHashMap<String, List<String>> flightSegmentAndDataToFlightCache = new ConcurrentHashMap<String, List<String>>();
	protected static ConcurrentHashMap<String, String> flightPKtoFlightCache = new ConcurrentHashMap<String, String>();

	public String getFlightByFlightId(String flightId, String flightSegment) {
		try {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Book flights with " + flightId + " and " + flightSegment);
			}
			if (useFlightDataRelatedCaching) {
				String flight = flightPKtoFlightCache.get(flightId);
				if (flight == null) {
					flight = getFlight(flightId, flightSegment);
					if (flightId != null && flight != null) {
						flightPKtoFlightCache.putIfAbsent(flightId, flight);
					}
				}
				return flight;
			} else {
				return getFlight(flightId, flightSegment);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract String getFlight(String flightId, String flightSegment);

	public List<String> getFlightByAirportsAndDepartureDate(String fromAirport, String toAirport, Date deptDate) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Search for flights from " + fromAirport + " to " + toAirport + " on " + deptDate.toString());
		}

		String originPortAndDestPortQueryString = fromAirport + toAirport;
		String segment = null;
		if (useFlightDataRelatedCaching) {
			segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);

			if (segment == null) {
				segment = getFlightSegment(fromAirport, toAirport);
				originAndDestPortToSegmentCache.putIfAbsent(originPortAndDestPortQueryString, segment);
			}
		} else {
			segment = getFlightSegment(fromAirport, toAirport);
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Segment " + segment);
		}
		// cache flights that not available (checks against sentinel value above
		// indirectly)
		try {
			if (segment == "") {
				return new ArrayList<String>();
			}
			JsonObject segmentJson = gson.fromJson(segment, JsonObject.class);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Segment in JSON " + segmentJson);
			}
			String segId = segmentJson.get("_id").getAsString();
			if (segId == null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Segment is null");
				}
				return new ArrayList<String>();
			}

			String flightSegmentIdAndScheduledDepartureTimeQueryString = segId + deptDate.toString();
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("flightSegmentIdAndScheduledDepartureTimeQueryString "
						+ flightSegmentIdAndScheduledDepartureTimeQueryString);
			}
			if (useFlightDataRelatedCaching) {
				List<String> flights = flightSegmentAndDataToFlightCache
						.get(flightSegmentIdAndScheduledDepartureTimeQueryString);
				if (flights == null) {
					flights = getFlightBySegment(segment, deptDate);
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("flights search results if flights cache is null " + flights.toString());
					}

					flightSegmentAndDataToFlightCache.putIfAbsent(flightSegmentIdAndScheduledDepartureTimeQueryString,
							flights);
				}
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Returning " + flights);
				return flights;
			} else {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("useFlightDataRelatedCaching is false ");
				}

				List<String> flights = getFlightBySegment(segment, deptDate);
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Returning " + flights);
				return flights;
			}
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// NOTE: This is not cached
	public List<String> getFlightByAirports(String fromAirport, String toAirport) {
		String segment = getFlightSegment(fromAirport, toAirport);
		if (segment == null) {
			return new ArrayList<String>();
		}
		return getFlightBySegment(segment, null);
	}

	protected abstract String getFlightSegment(String fromAirport, String toAirport);

	protected abstract List<String> getFlightBySegment(String segment, Date deptDate);

	public abstract void storeAirportMapping(AirportCodeMapping mapping);

	public abstract AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName);

	public abstract void createNewFlight(String flightSegmentId, Date scheduledDepartureTime, Date scheduledArrivalTime,
			int firstClassBaseCost, int economyClassBaseCost, int numFirstClassSeats, int numEconomyClassSeats,
			String airplaneTypeId);

	public abstract void storeFlightSegment(String flightSeg);

	public abstract void storeFlightSegment(String flightName, String origPort, String destPort, int miles);

	public abstract Long countFlightSegments();

	public abstract Long countFlights();

	public abstract Long countAirports();

	public abstract void dropFlights();

}