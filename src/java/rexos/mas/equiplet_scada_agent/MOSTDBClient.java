package rexos.mas.equiplet_scada_agent;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import rexos.libraries.blackboard_client.BasicOperationSubscription;
import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.FieldUpdateSubscription.MongoUpdateLogOperation;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.InvalidJSONException;
import rexos.libraries.blackboard_client.MongoOperation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MOSTDBClient {
	public class MostDbClientException extends Exception {
		public MostDbClientException(Throwable arg0) {
			super(arg0);
		}
	}

	private BlackboardClient equipletClient;
	private BlackboardClient modulesClient;
	private BlackboardClient equipletCommandsClient;

	public MOSTDBClient(EquipletScada scada) throws MostDbClientException {
		try {
			equipletClient = new BlackboardClient("145.89.191.131");
			equipletClient.setDatabase("most");
			equipletClient.setCollection("equiplet");
			
			equipletCommandsClient = new BlackboardClient("145.89.191.131");
			equipletCommandsClient.setDatabase("most");
			equipletCommandsClient.setCollection("equipletCommands");

			modulesClient = new BlackboardClient("145.89.191.131");
			modulesClient.setDatabase("most");
			modulesClient.setCollection("modules");
		} catch (UnknownHostException | GeneralMongoException
				| InvalidDBNamespaceException e) {
			throw new MostDbClientException(e);
		}
	}

	public void subscribe(BlackboardSubscriber subscriber)
			throws MostDbClientException {
		try {
			equipletClient.subscribe(new BasicOperationSubscription(
					MongoOperation.DELETE, subscriber));
			equipletClient.subscribe(new BasicOperationSubscription(
					MongoOperation.UPDATE, subscriber));
			equipletClient.subscribe(new BasicOperationSubscription(
					MongoOperation.INSERT, subscriber));
			equipletClient.subscribe(new BasicOperationSubscription(
					MongoOperation.NOOP, subscriber));
			
			modulesClient.subscribe(new BasicOperationSubscription(
					MongoOperation.DELETE, subscriber));
			modulesClient.subscribe(new BasicOperationSubscription(
					MongoOperation.UPDATE, subscriber));
			modulesClient.subscribe(new BasicOperationSubscription(
					MongoOperation.INSERT, subscriber));
			modulesClient.subscribe(new BasicOperationSubscription(
					MongoOperation.NOOP, subscriber));
		} catch (InvalidDBNamespaceException e) {
			throw new MostDbClientException(e);
		}
	}

	public List<ModuleInfo> getModules() throws MostDbClientException {
		ArrayList<ModuleInfo> result = new ArrayList<ModuleInfo>();

		try {
			List<DBObject> modules = modulesClient.findDocuments("{}");

			for (DBObject module : modules) {
				ModuleInfo moduleInfo = new ModuleInfo();
				moduleInfo.id = (int) module.get("id");
				moduleInfo.state = (int) module.get("state");
				moduleInfo.modi = (int) module.get("modi");
				result.add(moduleInfo);
			}
		} catch (InvalidJSONException | InvalidDBNamespaceException
				| GeneralMongoException e) {
			throw new MostDbClientException(e);
		}

		return result;
	}

	public EquipletInfo getEquiplet() throws MostDbClientException {
		try {
			List<DBObject> equiplets = equipletClient.findDocuments("{}");

			EquipletInfo equipletInfo = new EquipletInfo();

			for (DBObject equiplet : equiplets) {
				equipletInfo.safety = (int) equiplet.get("safety");
			}

			return equipletInfo;
		} catch (InvalidJSONException | InvalidDBNamespaceException
				| GeneralMongoException e) {
			throw new MostDbClientException(e);
		}
	}
	
	public void callEquipletCommand(String command, String... args) throws MostDbClientException{
		DBObject commandObject = new BasicDBObject();
		commandObject.put("command", command);
		commandObject.put("args", args);
		
		try {
			equipletCommandsClient.insertDocument(commandObject);
		} catch (InvalidDBNamespaceException | GeneralMongoException e) {
			throw new MostDbClientException(e);
		}
	}
}
