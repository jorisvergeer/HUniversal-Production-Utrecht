package rexos.mas.service_agent;

import jade.core.AID;
import jade.core.Agent;

import java.net.UnknownHostException;
import java.util.HashMap;

import org.bson.types.ObjectId;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.MongoOperation;
import rexos.libraries.blackboard_client.OplogEntry;
import rexos.mas.data.DbData;
import rexos.mas.equiplet_agent.ProductStepMessage;
import rexos.mas.equiplet_agent.StepStatusCode;
import rexos.mas.service_agent.behaviour.CanDoProductStep;
import rexos.mas.service_agent.behaviour.GetProductStepDuration;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * This agent manages services and oversees generation and scheduling of
 * serviceSteps.
 * 
 * @author Peter
 * 
 */
public class ServiceAgent extends Agent implements BlackboardSubscriber {
	private static final long serialVersionUID = 1L;

	private BlackboardClient productStepBBClient, serviceStepBBClient;
	private FieldUpdateSubscription statusSubscription = new FieldUpdateSubscription(
			"status", this);
	private HashMap<String, Integer> services;
	private HashMap<Integer, String[]> stepTypes;
	private DbData dbData;
	private AID equipletAgentAID, hardwareAgentAID, logisticsAID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jade.core.Agent#setup()
	 */
	@Override
	public void setup() {
		System.out.println("I spawned as a service agent.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			dbData = (DbData) args[0];
			equipletAgentAID = (AID) args[1];
			hardwareAgentAID = (AID) args[2];
			logisticsAID = (AID) args[3];
		}

		try {
			productStepBBClient = new BlackboardClient(dbData.getIp());
			serviceStepBBClient = new BlackboardClient(dbData.getIp());

			productStepBBClient.setDatabase(dbData.getName());
			productStepBBClient.setCollection("ProductStepsBlackBoard");
			// Needs to react on state changes of production steps to WAITING
			productStepBBClient.subscribe(statusSubscription);

			serviceStepBBClient.setDatabase(dbData.getName());
			serviceStepBBClient.setCollection("ServiceStepsBlackBoard");
			// Needs to react on status changes
			serviceStepBBClient.subscribe(statusSubscription);
		} catch (UnknownHostException | GeneralMongoException
				| InvalidDBNamespaceException e) {
			e.printStackTrace();
			doDelete();
		}

		services = new HashMap<>();
		services.put("Drill", 15);
		services.put("Glue", 20);
		services.put("Pick", 5);
		services.put("Place", 5);

		stepTypes = new HashMap<>();
		stepTypes.put(0, new String[] { "Pick", "Place" }); // Pick&Place
		stepTypes.put(1, new String[] { "Glue", "Pick", "Place" }); // Attach
		stepTypes.put(2, new String[] { "Drill", "Pick", "Place" }); // Screw
		stepTypes.put(3, new String[] { "Drill", "Pick", "Place" }); // Screw

		// create serviceFactory
		// addBehaviour(new AnswerBehaviour(this));
		addBehaviour(new CanDoProductStep(this));
		addBehaviour(new GetProductStepDuration(this));

		// receive behaviours from EA
		// add EvaluateProductionStep receiveBehaviour --> conversation with HA
		// add ScheduleProductStep receiveBehaviour --> conversation with LA
		// add ScheduleStep receiveBehaviour
		// add StepDuration receiveBehaviour
		// add StepDuration receiveBehaviour
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	public void takeDown() {
		productStepBBClient.unsubscribe(statusSubscription);
		serviceStepBBClient.unsubscribe(statusSubscription);
		try {
			serviceStepBBClient.removeDocuments(new BasicDBObject());

			DBObject update = BasicDBObjectBuilder
					.start("status", StepStatusCode.FAILED.name())
					.push("statusData")
						.add("source", "service agent")
						.add("reason", "died")
						.pop()
					.get();
			productStepBBClient.updateDocuments(new BasicDBObject(),
					new BasicDBObject("$set", update));
		} catch (InvalidDBNamespaceException | GeneralMongoException e) {
			e.printStackTrace();
		}
	}

	public void handleHardwareAgentTimeout() {

	}

	public void handleEquipletAgentTimeout() {

	}

	public void handleLogisticsAgentTimeout() {

	}

	/**
	 * @param obj
	 * @param prefix
	 * @param total_prefix
	 * @param result
	 */
	public void printDBObjectPretty(DBObject obj, String prefix,
			String total_prefix, StringBuilder result) {
		Object value;
		for (String key : obj.keySet()) {
			value = obj.get(key);
			if (value instanceof DBObject) {
				result.append(total_prefix + key + ":\n");
				printDBObjectPretty((DBObject) value, prefix, prefix
						+ total_prefix, result);
			} else if (value == null) {
				result.append(total_prefix + key + ": " + value + "\n");
			} else {
				result.append(total_prefix + key + ": " + value + " ("
						+ value.getClass().getSimpleName() + ")\n");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * rexos.libraries.blackboard_client.BlackboardSubscriber#onMessage(rexos
	 * .libraries.blackboard_client.MongoOperation,
	 * rexos.libraries.blackboard_client.OplogEntry)
	 */
	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		try {
			switch (entry.getNamespace().split("\\.")[1]) {
			case "ProductStepsBlackBoard":
				ProductStepMessage productionStep = new ProductStepMessage(
						(BasicDBObject) productStepBBClient
								.findDocumentById(entry.getTargetObjectId()));
				switch (operation) {
				case UPDATE:
					StepStatusCode status = productionStep.getStatus();
					if (status == StepStatusCode.WAITING) {
						serviceStepBBClient.updateDocuments(new BasicDBObject(
								"productStepId", entry.getTargetObjectId()),
								new BasicDBObject("$set", new BasicDBObject(
										"status", status)));
					}
					break;
				case DELETE:
					serviceStepBBClient.removeDocuments(new BasicDBObject(
							"productStepId", entry.getTargetObjectId()));
					break;
				default:
					break;
				}
				break;
			case "ServiceStepsBlackBoard":
				BasicDBObject serviceStep = (BasicDBObject) serviceStepBBClient
						.findDocumentById(entry.getTargetObjectId());
				ObjectId productStepId = (ObjectId) serviceStep
						.get("productStepId");
				switch (operation) {
				case UPDATE:
					BasicDBObject update = new BasicDBObject("status",
							serviceStep.get("status"));
					update.put("statusData", serviceStep.get("statusData"));
					productStepBBClient.updateDocuments(new BasicDBObject(
							"_id", productStepId), new BasicDBObject("$set",
							update));
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		} catch (InvalidDBNamespaceException | GeneralMongoException e) {
			e.printStackTrace();
			doDelete();
		}
	}

	/**
	 * @return the services
	 */
	public HashMap<String, Integer> getServices() {
		return services;
	}

	/**
	 * @return the stepTypes
	 */
	public HashMap<Integer, String[]> getStepTypes() {
		return stepTypes;
	}

	/**
	 * @return the dbData
	 */
	public DbData getDbData() {
		return dbData;
	}

	/**
	 * @return the equipletAgentAID
	 */
	public AID getEquipletAgentAID() {
		return equipletAgentAID;
	}

	/**
	 * @return the logisticsAID
	 */
	public AID getLogisticsAID() {
		return logisticsAID;
	}

	/**
	 * @return the hardwareAgentAID
	 */
	public AID getHardwareAgentAID() {
		return hardwareAgentAID;
	}

	/**
	 * @return the productStepBBClient
	 */
	public BlackboardClient getProductStepBBClient() {
		return productStepBBClient;
	}

	/**
	 * @return the serviceStepBBClient
	 */
	public BlackboardClient getServiceStepBBClient() {
		return serviceStepBBClient;
	}
}
