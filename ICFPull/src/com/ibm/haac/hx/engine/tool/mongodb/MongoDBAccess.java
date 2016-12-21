/****************************************************************************** 
 * Licensed Materials - Property of IBM 
 * "Restricted Materials of IBM"
 * © Copyright IBM Corp. 2014 All Rights Reserved. 
 * 
 * Copying, redistribution and/or modification is prohibited. 
 * U.S. Government Users Restricted Rights - Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
 *****************************************************************************/
package com.ibm.haac.hx.engine.tool.mongodb;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

/**
 * Class to handle MongoDB use
 * 
 * @author antonioj
 * 
 */
public class MongoDBAccess implements IDBAccess {

	public static int readLimit;
	private static String dbName;
	private static String serverIP;
	private static int serverPort;
	private static String userName;
	private static String pwd;
	private static String mongoUri;
	private MongoClient mongoClient;
	private DB db;

	private boolean mongoConnected = false;

	static {
		try {
			Properties appProperties = new Properties();
			appProperties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("res/db.properties"));

			readLimit = Integer.parseInt(appProperties
					.getProperty("mongo.readLimit"));
			dbName = appProperties.getProperty("mongo.dbName");
			serverIP = appProperties.getProperty("mongo.serverIP");
			String port = appProperties.getProperty("mongo.serverPort");
			if (port == null || port.trim().equals(""))
				serverPort = 27017;
			else
				serverPort = Integer.parseInt(port.trim());

			userName = appProperties.getProperty("mongo.user");
			pwd = appProperties.getProperty("mongo.pwd");
			mongoUri = appProperties.getProperty("mongo.connectionURI");

			System.out.println("mongo uri=" + mongoUri + ", dbName=" + dbName
					+ ", userName=" + userName + ", serverIP=" + serverIP
					+ ", serverPort=" + serverPort);
			if (mongoUri == null
					&& (dbName == null || serverIP == null || userName == null || pwd == null))
				throw new Exception(
						"Neither mongo uri nor database property is configured");

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Could not read mongo db properties. " + e.getMessage());
		}

	}

	/**
	 * https://github.com/mongodb/morphia/releases
	 * https://github.com/jmkgreen/morphia/wiki
	 * http://www.ibm.com/developerworks
	 * /java/library/j-morphia/index.html?ca=dat
	 * http://www.javacodegeeks.com/2011/11/using-mongodb-with-morphia.html
	 * http:
	 * //www.slideshare.net/scotthernandez/mongodb-easy-java-persistence-with
	 * -morphia
	 */
	private Morphia morphia;
	private static Datastore morphiaDatastore;

	public MongoDBAccess() throws Exception {

		try {
			
			if (mongoUri != null && !mongoUri.trim().equals("")) {
				System.out.println("Use mongo connection uri");
				mongoUri = mongoUri.trim();
				// http://stackoverflow.com/questions/15052074/connecting-a-mongodb-created-in-mongolab-through-a-java-application
				MongoClientURI uri = new MongoClientURI(mongoUri);
				this.mongoClient = new MongoClient(uri);
				int index = mongoUri.lastIndexOf("/");
				dbName = mongoUri.substring(index + 1);
			} else {
				System.out.println("Use mongo database connection properties");
				MongoCredential credential = MongoCredential
						.createMongoCRCredential(userName, dbName,
								pwd.toCharArray());

				// connect to the server
				this.mongoClient = new MongoClient(new ServerAddress(serverIP,
						serverPort), Arrays.asList(credential));
			}
			// attach to the database
			this.db = this.mongoClient.getDB(dbName);
           
			morphia = new Morphia();
			if (morphiaDatastore == null)
				morphiaDatastore = morphia.createDatastore(this.mongoClient,
						dbName);
           
			ensureEntities();

			this.mongoConnected = true;

		} catch (MongoException e) {
			throw new Exception("MongoDB exception: " + e.getMessage());
		} catch (UnknownHostException e) {
			throw new Exception("MongoDB UnknownHostException exception: "
					+ e.getMessage());
		}
	}

	@Override
	public void mapEntity(Class<?>... clazz) {
		morphia.map(clazz);

		ensureEntities();
	}

	public void ensureEntities() {
		// creates indexes from @Index annotations in your entities
		morphiaDatastore.ensureIndexes();
		// creates capped collections from @Entity
		morphiaDatastore.ensureCaps();
	}

	@Override
	public Datastore getDatastore() {
		return morphiaDatastore;
	}

	public boolean isConnected() {
		return this.mongoConnected;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public String getDbName() {
		return dbName;
	}

	public DB getDb() {
		return db;
	}

	public boolean isMongoConnected() {
		return mongoConnected;
	}

	public Morphia getMorphia() {
		return morphia;
	}

}
