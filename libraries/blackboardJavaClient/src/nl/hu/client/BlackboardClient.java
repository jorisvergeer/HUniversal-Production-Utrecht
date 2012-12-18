/**
 * @file BlackboardClient.java
 * @brief Symbolizes an blackboardclient.
 *
 * @author 1.0 Dick van der Steen
 *
 * @section LICENSE
 * License: newBSD
 * 
 * Copyright © 2012, HU University of Applied Sciences Utrecht.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/


package nl.hu.client;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoInterruptedException;
import com.mongodb.util.JSON;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client class for a mongodb blackboard.
 **/
public class BlackboardClient
{
	/**
         * @var String OR_OPERAND
         * Or operand used by MongoDB.
         **/
	private final String OR_OPERAND = "$or";

	/**
         * @var String AND_OPERAND
         * And operand used by MongoDB.
         **/
	private final String AND_OPERAND = "$and";

	/**
         * @var String OPLOG
         * Operation log collection name of MongoDB.
         **/
	private final String OPLOG = "oplog.rs";

	/**
         * @var String LOCAL
         * Local database name of MongoDB.
         **/
	private final String LOCAL = "local";
		
	/**
         * @var Mongo mongo
         * Connection object to MongoDB.
         **/
	private Mongo mongo;

	/**
         * @var HashMap<String, BasicDBObject> subscriptions
         * Link between subscribed topic name and MongoDbs BasicDBObjects
         **/
	private HashMap<String, BasicDBObject> subscriptions;

	/**
         * @var String collection
         * Name of the used MongoDB collection
         **/
	private String collection;

	/**
         * @var String database
         * Name of the used MongoDB database
         **/
	private String database;

	/**
         * @var ISubscriber callback
         * Callback object for incomming messages on subscribed topic.
         **/
	private ISubscriber callback;

	/**
         * @var DB currentDatabase
         * Database object of the currently used database
         **/
	private DB currentDatabase;

	/**
         * @var DBCollection currentCollection
         * DBCollection object of the currently used collection
         **/
	private DBCollection currentCollection;

	/**
         * @var TailedCursorThread tailableCursorThread
         * Thread for tracking tailable cursor on operation log of MongoDB
         **/
	private TailedCursorThread tailableCursorThread;

	/**
         * @var DBCursor tailedCursor
         * TailedCursor for tracking changes on the operation log of MongoDB
         **/
	private DBCursor tailedCursor;
	
	/**
         * @var DB OPLOG_DATABASE
         * Database object of the oplog database
         **/
	private DB OPLOG_DATABASE;

	/**
         * @var DBCollection OPLOG_COLLECTION
         * DBCollection object of the oplog collection
         **/
	private DBCollection OPLOG_COLLECTION;

	/**
	 * Class for tailable cursor thread within the client
	 **/
	public class TailedCursorThread extends Thread
	{
		/**
    		 * Constructor of TailedCursorThread.
     		 **/
		public TailedCursorThread()
		{		
						
			OPLOG_DATABASE = mongo.getDB(LOCAL);
			OPLOG_COLLECTION = OPLOG_DATABASE.getCollection(OPLOG);
		
			BasicDBObject where = new BasicDBObject();
			where.put("ns", database + "." + collection);
			tailedCursor = OPLOG_COLLECTION.find(where).addOption(Bytes.QUERYOPTION_TAILABLE).addOption	(Bytes.QUERYOPTION_AWAITDATA);
			tailedCursor.skip(tailedCursor.size());					
		}

		/**
     		 * Run method for the TailedCursorThread, 
		 * it will check for changes within the cursor and calls the onMessage method of its subscriber
    		 **/
		@Override 	
		public void run()
		{
			String operation;
			BasicDBObject message;
			while(true)
			{
				while(tailedCursor.hasNext())
				{	
					DBObject object = (DBObject)tailedCursor.next();
					operation = object.get("op").toString();
					switch(operation)
					{
						case "i":
									
							//BasicDBObject messageCheckObject = new BasicDBObject();
							//messageCheckObject.put(OR_OPERAND, subscriptions.values());
							//message = (BasicDBObject) currentCollection.findOne(messageCheckObject);
							//if(message != null)
							//{	
							
							BasicDBObject o = (BasicDBObject)object.get("o");
							String topic = o.get("topic").toString();
							if(subscriptions.get(topic) != null)
							{
								callback.onMessage(o.toString());
							//}
							}
							break;
					}			
				}
			}
		}
	}

	/**
    	 * Constructor of BlackboardClient.
	 *
	 * @param ip The ip of the MongoDB host.
     	 **/
	public BlackboardClient(String ip) {
		try {
			this.subscriptions = new HashMap<String, BasicDBObject>();
			this.mongo = new Mongo(ip);
			this.callback = callback;
			} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
    	 * Constructor of BlackboardClient.
	 *
	 * @param ip The ip of the MongoDB host.
	 * @param port The port of the MongoDB host.
     	 **/
	public BlackboardClient(String ip, int port) {
		try {
			this.subscriptions = new HashMap<String, BasicDBObject>();
			this.mongo = new Mongo(ip, port);
			this.callback = callback;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
      	 * Sets the callback object.
	 *
	 * @param callback The ISubscriber object to call onMessage on.
     	 **/
	public void setCallback(ISubscriber callback)
	{
		this.callback = callback;
	}

	/**
      	 * Sets the database to use.
	 *
	 * @param database The database to load from MongoDB.
     	 **/
	public void setDatabase(String database)
	{
		this.database = database;
		currentDatabase = mongo.getDB(this.database);	
	}

	/**
      	 * Sets the collection to use. Will start an TailedCursorThread
	 *
	 * @param collection The collection to load from MongoDB. 
     	 **/
	public void setCollection(String collection) throws Exception
	{
		if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		this.collection = collection;
		currentCollection = currentDatabase.getCollection(this.collection);
		this.tailableCursorThread = new TailedCursorThread();
		this.tailableCursorThread.start();		
	}

	/**
      	 * Inserts document into MongoDB using json format, will throw an exception if collection or database has not been set.
	 *
	 * @param json The json format for the MongoDB insert statement. 
     	 **/
	public void insertJson(String json) throws Exception {
		if(collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		currentCollection.insert((DBObject)JSON.parse(json));
	}	
	
	/**
      	 * Removes document from MongoDB using json format, will throw an exception if collection or database has not been set.
	 *
	 * @param json The json format for the MongoDB remove statement. 
     	 **/
	public void removeJson(String json) throws Exception {
		if(collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		currentCollection.remove((DBObject)JSON.parse(json));
	}	
	
	/**
      	 * Deprecated, do not use, use getJson instead.
	 *
     	 **/
	public ArrayList<Map> get(Map query) throws Exception {
		if(collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}	
			BasicDBObject object = new BasicDBObject();
			object.put(AND_OPERAND, query);
			List<DBObject> found = currentCollection.find(object).toArray();
			ArrayList<Map> maps  = new ArrayList<Map>();
			
			for (DBObject obj: found) 
			{
				maps.add(obj.toMap());
			}	
			return maps;
	}

	/**
      	 * Queries MongoDB using json format, will throw an exception if collection or database has not been set.
	 *
	 * @param json The json format for the MongoDB query statement. 
     	 **/
	public ArrayList<String> getJson(String json) throws Exception {
		int size =0;		
		if (collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		ArrayList<String> jsons = new ArrayList<String>();
		List<DBObject> found = currentCollection.find((DBObject)JSON.parse(json)).toArray();
		for (DBObject obj : found) 
		{
				jsons.add(obj.toString());
		}	
		return jsons;
	}	

	/**
      	 * Reads an message from the MongoDB blackboard.
	 *
	 * @param TODO 
     	 **/
	public String read(boolean blocked, String client) throws Exception {
		if (collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		} else if(subscriptions.size() == 0) {
			throw new Exception("No subscriptions has been found");
		}		 	 					
		
		BasicDBObject messageCheckObject = new BasicDBObject();
		messageCheckObject.put(OR_OPERAND, subscriptions.values());
		BasicDBObject message = (BasicDBObject) currentCollection.findOne(messageCheckObject);
		if(message!= null) 
		{
			return message.toString();
		} 
		return null;		
	}

		
	/**
      	 * Removes first message on blackboard
	 *
     	 **/
	public void removeFirst() {
		BasicDBObject messageCheckObject = new BasicDBObject();
		BasicDBObject message = (BasicDBObject) currentCollection.findOne();
		currentCollection.remove(message);
	}

	/**
      	 * Updates document from MongoDB using json format, will throw an exception if collection or database has not been set.
	 *
	 * @param query The json format for the MongoDB query statement. 
	 * @param set The json format for the MongoDB set statement. 
	 * @param unset The json format for the MongoDB unset statement. 
     	 **/
	public void updateJson(String query, String set, String unset) throws Exception {
		if(collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		if(set == null) { set = ""; }
		if(unset == null) { unset = ""; }	
		BasicDBObject setObject = new BasicDBObject();
		setObject.put("$set", (DBObject)JSON.parse(set));
		setObject.put("$unset",(DBObject)JSON.parse(unset));
		System.out.println(query);
		System.out.println(setObject);		
		currentCollection.findAndModify((DBObject)JSON.parse(query), setObject);
	}
	
	/**
      	 * Subscribes the client on a certain topic
	 *
	 * @param topic The topic to subscribe to. 
	 **/
	public void subscribe(String topic) throws Exception {
		if(collection.isEmpty() || collection == null) {
			throw new Exception("No collection selected");
		} else if (database.isEmpty() || database == null) {
			throw new Exception("No database selected");
		}
		subscriptions.put(topic, new BasicDBObject("topic", topic));
	}

	/**
      	 * Unsubscribes the client on a certain topic
	 *
	 * @param topic The topic to unsubscribe to. 
	 **/
	public void unsubscribe(String topic) {
		subscriptions.remove(topic);
	}
}
