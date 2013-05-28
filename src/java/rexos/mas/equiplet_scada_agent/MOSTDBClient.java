package rexos.mas.equiplet_scada_agent;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.FieldUpdateSubscription.MongoUpdateLogOperation;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.InvalidJSONException;

import com.mongodb.DBObject;

public class MOSTDBClient {
	public class MostDbClientException extends Exception {
		public MostDbClientException(Throwable arg0) {
			super(arg0);
		}
	}

	private BlackboardClient equipletClient;
	private BlackboardClient modulesClient;

	public MOSTDBClient(EquipletScada scada) throws MostDbClientException {
		try {
			equipletClient = new BlackboardClient("145.89.191.131");
			equipletClient.setDatabase("most");
			equipletClient.setCollection("equiplet");

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
			FieldUpdateSubscription equipletSubscription = new FieldUpdateSubscription(
					"id", subscriber);
			equipletSubscription.addOperation(MongoUpdateLogOperation.REPLACE);
			equipletSubscription.addOperation(MongoUpdateLogOperation.SET);
			equipletSubscription.addOperation(MongoUpdateLogOperation.UNSET);
			
			FieldUpdateSubscription modulesSubscription = new FieldUpdateSubscription(
					"id", subscriber);
			modulesSubscription.addOperation(MongoUpdateLogOperation.REPLACE);
			modulesSubscription.addOperation(MongoUpdateLogOperation.SET);
			modulesSubscription.addOperation(MongoUpdateLogOperation.UNSET);

			equipletClient.subscribe(equipletSubscription);
			modulesClient.subscribe(modulesSubscription);
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
}
