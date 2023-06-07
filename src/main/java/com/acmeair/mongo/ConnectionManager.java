package com.acmeair.mongo;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClients;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class ConnectionManager implements MongoConstants {

	private static AtomicReference<ConnectionManager> connectionManager = new AtomicReference<ConnectionManager>();

	private final static Logger logger = Logger.getLogger(ConnectionManager.class.getName());

	protected MongoClient mongoClient;
	protected MongoDatabase db;

	public static ConnectionManager getConnectionManager() {
		if (connectionManager.get() == null) {
			synchronized (connectionManager) {
				if (connectionManager.get() == null) {
					connectionManager.set(new ConnectionManager());
				}
			}
		}
		return connectionManager.get();
	}

	private ConnectionManager() {

		// Set default client options, and then check if there is a properties
		// file.
		String hostname = "localhost";
		int port = 27017;
		String dbname = "acmeair";

		String mongoManual = System.getenv("MONGO_MANUAL");
		Boolean isManual = Boolean.parseBoolean(mongoManual);

		String mongoHost = System.getenv("MONGO_HOST");
		if (mongoHost != null) {
			hostname = mongoHost;
		}

		String mongoPort = System.getenv("MONGO_PORT");
		if (mongoPort != null) {
			port = Integer.parseInt(mongoPort);
		}

		String mongoDbName = System.getenv("MONGO_DBNAME");
		if (mongoDbName != null) {
			dbname = mongoDbName;
		}

		String mongoUser = System.getenv("MONGO_USER");

		String mongoPassword = System.getenv("MONGO_PASSWORD");
		ConnectionString mongoURI = new ConnectionString("mongodb://" + hostname + ":" + port);
		try {

			// If MONGO_MANUAL is set to true, it will set up the DB connection
			// right away
			if (isManual) {
				if (mongoUser != null) {
					MongoCredential credential = MongoCredential.createCredential(mongoUser, dbname,
							mongoPassword.toCharArray());
					MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(mongoURI)
							.credential(credential).build();
					mongoClient = MongoClients.create(settings);
				} else {
					mongoClient = MongoClients.create(mongoURI);
				}
			} else {
				// Check if VCAP_SERVICES exist, and if it does, look up the url
				// from the credentials.
				String vcapJSONString = System.getenv("VCAP_SERVICES");
				if (vcapJSONString != null) {
					logger.info("Reading VCAP_SERVICES");
					Gson gson = new GsonBuilder().create();
					JsonObject vcapServices = gson.fromJson(vcapJSONString, JsonObject.class);

					JsonArray mongoServiceArray = null;
					for (String key : vcapServices.keySet()) {
						if (key.startsWith("user-provided")) {
							mongoServiceArray = vcapServices.getAsJsonArray(key);
							logger.info("Service Type : MongoDB by Compost - " + key);
							break;
						}
					}
					JsonObject mongoService = (JsonObject) mongoServiceArray.get(0);
					JsonObject credentials = (JsonObject) mongoService.get("credentials");
					String url = credentials.get("url").getAsString();
					logger.fine("service url = " + url);
					mongoURI = new ConnectionString(url);
					mongoClient = MongoClients.create(mongoURI);
					dbname = mongoURI.getDatabase();

				} else {
					mongoClient = MongoClients.create(mongoURI);
				}
			}
			logger.fine("#### Mongo DB Database Name " + dbname + " ####");
			db = mongoClient.getDatabase(dbname);

			for (String host : mongoURI.getHosts()) {
				logger.info("#### Mongo DB Server " + host + " ####");
			}
			logger.info("#### Mongo DB is created with DB name " + dbname + " ####");
		} catch (Exception e) {
			logger.severe("Caught Exception : " + e.getMessage());
			throw e;
		}

	}

	public MongoDatabase getDB() {
		return db;
	}
}
