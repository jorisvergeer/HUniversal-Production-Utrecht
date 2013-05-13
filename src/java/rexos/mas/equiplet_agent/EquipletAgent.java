/**
 * @file EquipletAgent.java
 * @brief Provides an equiplet agent that communicates with product agents and with its own service agent.
 * @date Created: 2013-04-02
 *
 * @author Hessel Meulenbeld
 * @author Thierry Gerritse
 * @author Wouter Veen
 *
 * @section LICENSE
 * License: newBSD
 *
 * Copyright � 2013, HU University of Applied Sciences Utrecht.
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

package rexos.mas.equiplet_agent;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bson.types.ObjectId;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.FieldUpdateSubscription.MongoUpdateLogOperation;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.MongoOperation;
import rexos.libraries.blackboard_client.OplogEntry;
import rexos.mas.data.DbData;
import rexos.mas.data.ScheduleData;
import rexos.mas.equiplet_agent.behaviours.*;

import com.mongodb.BasicDBObject;

/**
 * EquipletAgent that communicates with product agents and with its own service agent.
 **/
public class EquipletAgent extends Agent implements BlackboardSubscriber {
	/**
	 * @var long serialVersionUID
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @var AID serviceAgent
	 * AID of the serviceAgent connected to this EquipletAgent.
	 */
	private AID serviceAgent;

	/**
	 * @var String collectiveDbIp
	 * IP of the collective database.
	 */
	private String collectiveDbIp = "145.89.191.131";
//	private String collectiveDbIp = "localhost";
	
	/**
	 * @var int collectiveDbPort
	 * Port number of the collective database.
	 */
	private int collectiveDbPort = 27017;
	
	/**
	 * @var String collectiveDbName
	 * Name of the collective database.
	 */
	private String collectiveDbName = "CollectiveDb";
	
	/**
	 * @var String equipletDirectoryName
	 * Name of the collection containing the equipletDirectory.
	 */
	private String equipletDirectoryName = "EquipletDirectory";
	
	/**
	 * @var String timeDataName
	 * Name of the collection containing the timeData.
	 */
	private String timeDataName = "TimeData";

	/**
	 * @var String equipletDbIp
	 * IP of the equiplet database.
	 */
	private String equipletDbIp = "localhost";
	
	/**
	 * @var int equipletDbPort
	 * Port number of the equiplet database.
	 */
	private int equipletDbPort = 27017;
	
	/**
	 * @var String equipletDbName
	 * Name of the equiplet database.
	 */
	private String equipletDbName = "";
	
	/**
	 * @var String productStepsName
	 * Name of the collection containing the productSteps.
	 */
	private String productStepsName = "ProductStepsBlackBoard";

	/**
	 * @var BlackboardClient collectiveBBClient
	 * Object for communication with the collective blackboard.
	 */
	private BlackboardClient collectiveBBClient;
	
	/**
	 * @var BlackboardClient equipletBBClient
	 * Object for communication with the equiplet blackboard.
	 */
	private BlackboardClient equipletBBClient;

	/**
	 * @var ArrayList<Integer> capabilities
	 * List with all the capabilities of this equiplet.
	 */
	private ArrayList<Integer> capabilities;

	/**
	 * @var HashMap<String, ObjectId> communicationTable
	 * Table with the combinations conversationID and ObjectId.
	 */
	private HashMap<String, ObjectId> communicationTable;

	/**
	 * @var Timer timeToNextUsedTimeSlot
	 * Timer used to trigger when the next used time slot is ready to start.
	 */
	private NextProductStepTimer timer;
	
	/**
	 * @var ObjectId nextProductStep
	 * The next product step.
	 */
	private ObjectId nextProductStep;

	/**
	 * @var int firstTimeSlot
	 * The first time slot of the grid.
	 */
	private int firstTimeSlot;
	
	/**
	 * @var int timeSlotLength
	 * The length of a time slot.
	 */
	private int timeSlotLength;

	/**
	 * Setup function for the equipletAgent.
	 * Configures the IP and database name of the equiplet.
	 * Gets its capabilities from the arguments.
	 * Creates its service agent. 
	 * Makes connections with the BlackboardCLients and subscribes on changes on the status field.
	 * Puts its capabilities on the equipletDirectory blackboard.
	 * Gets the time data from the blackboard.
	 * Initializes the Timer objects.
	 * Starts its behaviours.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setup() {
		System.out.println("I spawned as a equiplet agent.");
		
		//gets his IP and sets the equiplet blackboard IP.
		try {
			InetAddress IP = InetAddress.getLocalHost();
			equipletDbIp = IP.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		equipletDbName = getAID().getLocalName();
		communicationTable = new HashMap<String, ObjectId>();
		try{
			// TODO: Not Hardcoded capabilities/get capabilities from the service agent.
			Object[] args = getArguments();
			AID logisticsAgent = null;
			if (args != null && args.length > 0) {
				capabilities = (ArrayList<Integer>) args[0];
				logisticsAgent = (AID) args[1];
				System.out.format("%s %s%n", capabilities, equipletDbName);
			}
			
			DbData dbData = new DbData(equipletDbIp, equipletDbPort, equipletDbName);
			
			//creates his hardware agent.
			Object[] arguments = new Object[] { dbData, this };
			getContainerController().createNewAgent(getLocalName() + "-hardwareAgent", "rexos.mas.hardware_agent.HardwareAgent", arguments).start();
			AID hardwareAgent = new AID(getLocalName() + "-hardwareAgent", AID.ISLOCALNAME);
			
			//creates his service agent.
			arguments = new Object[] { dbData, getAID(), hardwareAgent, logisticsAgent };
			getContainerController().createNewAgent(getLocalName() + "-serviceAgent", "rexos.mas.service_agent.ServiceAgent", arguments).start();
			serviceAgent = new AID(getLocalName() + "-serviceAgent", AID.ISLOCALNAME);
			
			//makes connection with the collective blackboard.
			collectiveBBClient = new BlackboardClient(collectiveDbIp, collectiveDbPort);
			collectiveBBClient.setDatabase(collectiveDbName);
			collectiveBBClient.setCollection(equipletDirectoryName);

			//makes connection with the equiplet blackboard.
			equipletBBClient = new BlackboardClient(equipletDbIp, equipletDbPort);
			equipletBBClient.setDatabase(equipletDbName);
			equipletBBClient.setCollection(productStepsName);
			
			//subscribes on changes of the status field on the equiplet blackboard.
			FieldUpdateSubscription statusSubscription = new FieldUpdateSubscription("status", this);
			statusSubscription.addOperation(MongoUpdateLogOperation.SET);
			equipletBBClient.subscribe(statusSubscription);
			
			//inserts himself on the collective blackboard equiplet directory.
			EquipletDirectoryMessage entry = new EquipletDirectoryMessage(getAID(), capabilities, dbData);
			collectiveBBClient.insertDocument(entry.toBasicDBObject());
			
			//gets the timedata for synchronizing from the collective blackboard.
			collectiveBBClient.setCollection(timeDataName);
			BasicDBObject timeData = (BasicDBObject)collectiveBBClient.findDocuments(new BasicDBObject()).get(0);
			firstTimeSlot = timeData.getInt("firstTimeSlot");
			timeSlotLength = timeData.getInt("timeSlotLength");
			collectiveBBClient.setCollection(equipletDirectoryName);
		} catch(Exception e) {
			e.printStackTrace();
			doDelete();
		}
		
		//initiates the timer to the next product step.
		timer = new NextProductStepTimer(firstTimeSlot, timeSlotLength);
		timer.setNextUsedTimeSlot(-1);

		//starts the behaviour for receiving messages with the Ontology CanPerformStep.
		addBehaviour(new CanPerformStep(this, equipletBBClient));

		//starts the behaviour for receiving messages with the Ontology GetProductionDuration.
		addBehaviour(new GetProductionDuration(this));

		//starts the behaviour for receiving messages with the Ontology ScheduleStep.
		addBehaviour(new ScheduleStep(this));
		
		//starts the behaviour for receiving messages with the Ontology StartStep.
		addBehaviour(new StartStep(this, equipletBBClient));
		
		//starts the behaviour for receiving message when the Service Agent Dies.
		addBehaviour(new ServiceAgentDied(this));
	}

	/**
	 * Takedown function for the equipletAgent.
	 * Removes itself from the equipletDirectory.
	 * Informs the productAgents who have planned a productStep on its blackboard of its dead.
	 * Removes its database.
	 */
	@Override
	public void takeDown() {
		try {
			//Removes himself from the collective blackboard equiplet directory.
			collectiveBBClient.removeDocuments(new BasicDBObject("AID", getAID().getName()));

			//Messages all his product agents that he is going to die.
			Object[] productAgents = equipletBBClient.findDistinctValues("productAgentId", new BasicDBObject());
			for(Object productAgent : productAgents){
				ACLMessage responseMessage = new ACLMessage(ACLMessage.FAILURE);
				responseMessage.addReceiver(new AID(productAgent.toString(), AID.ISGUID));
				responseMessage.setOntology("EquipletAgentDied");
				responseMessage.setContent("I'm dying");
				send(responseMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// The equiplet is already going down, so it has to do nothing here.
		}
		try {
			//Clears his own blackboard and removes his subscription on that blackboard.
			equipletBBClient.removeDocuments(new BasicDBObject());
			FieldUpdateSubscription statusSubscription = new FieldUpdateSubscription("status", this);
			statusSubscription.addOperation(MongoUpdateLogOperation.SET);
			equipletBBClient.unsubscribe(statusSubscription);
		} catch (InvalidDBNamespaceException | GeneralMongoException e) {
			e.printStackTrace();
		}
		
		ACLMessage deadMessage = new ACLMessage(ACLMessage.FAILURE);
		deadMessage.addReceiver(serviceAgent);
		deadMessage.setOntology("EquipletAgentDied");
		send(deadMessage);
	}

	/**
	 * onMessage function for the equipletAgent.
	 * Listens to updates of the blackboard clients and handles them.
	 */
	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		switch (entry.getNamespace().split("\\.")[1]) {
		case "ProductStepsBlackBoard":
			try {
				//Get the productstep.
				ObjectId id = entry.getTargetObjectId();
				ProductStepMessage productStep = new ProductStepMessage((BasicDBObject)equipletBBClient.findDocumentById(id));

				//Gets the conversationId if it doesn't exist throws an error.
				String conversationId = getConversationId(id);
				if (conversationId == null) {
					throw new Exception();
				}

				//Create the responseMessage
				ACLMessage responseMessage = new ACLMessage(ACLMessage.INFORM);
				responseMessage.addReceiver(productStep.getProductAgentId());
				responseMessage.setConversationId(conversationId);

				System.out.println("status update: " + productStep.getStatus().toString());
				switch (productStep.getStatus()) {
				//Depending on the changed status fills in  the responseMessage and sends it to the product agent.
				case PLANNED:
					try {
						//If the start time of the newly planned productStep is 
						//earlier as the next used time slot make it the next used timeslot.
						ScheduleData scheduleData = productStep.getScheduleData();
						
						if (scheduleData.getStartTime() < timer.getNextUsedTimeSlot()) {
							timer.setNextUsedTimeSlot(scheduleData.getStartTime());
						}

//						System.out.println("%s Sending ProductionDuration tot %s%n", getAID(), );
						responseMessage.setOntology("Planned");
						responseMessage.setContentObject(scheduleData.getStartTime());
						send(responseMessage);
					} catch (IOException e) {
						responseMessage.setPerformative(ACLMessage.FAILURE);
						responseMessage.setContent("An error occured in the planning/please reschedule");
						send(responseMessage);
						e.printStackTrace();
					}
					break;
				case IN_PROGRESS:
					responseMessage.setOntology("StatusUpdate");
					responseMessage.setContent("INPROGRESS");
					send(responseMessage);
					break;
				case FAILED:
					responseMessage.setOntology("StatusUpdate");
					responseMessage.setContent("FAILED");
					responseMessage.setContentObject(productStep.getStatusData());
					send(responseMessage);
					break;
				case SUSPENDED_OR_WARNING:
					responseMessage.setOntology("StatusUpdate");
					responseMessage.setContent("SUSPENDED_OR_WARNING");
					responseMessage.setContentObject(productStep.getStatusData());
					send(responseMessage);
					break;
				case DONE:
					responseMessage.setOntology("StatusUpdate");
					responseMessage.setContent("DONE");
					send(responseMessage);
					break;
				default:
					break;
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Getter for the EquipletBBClient
	 * @return the equipletBBClient.
	 */
	public BlackboardClient getEquipletBBClient() {
		return equipletBBClient;
	}

	/**
	 * Function for adding a new relation between conversationId and objectId
	 * @param conversationId the conversationId in the new relation.
	 * @param objectId the objectId in the new relation.
	 */
	public void addCommunicationRelation(String conversationId, ObjectId objectId) {
		communicationTable.put(conversationId, objectId);
	}

	/**
	 * Getter for getting the objectId by a conversationId.
	 * @param conversationId the conversationId of which the related objectId is needed.
	 * @return ObjectId for the given conversationId.
	 */
	public ObjectId getRelatedObjectId(String conversationId) {
		return communicationTable.get(conversationId);
	}
	
	/**
	 * Getter for getting the related conversationId for the given ObjectId.
	 * @param productStepEntry the ObjectId for which the related conversationId is needed.
	 * @return the related conversationId or null if the relation does not exist.
	 */
	public String getConversationId(ObjectId productStepEntry) {
		String conversationId = null;
		if(communicationTable.containsValue(productStepEntry)){
			for (Entry<String, ObjectId> tableEntry : communicationTable.entrySet()) {
				if (tableEntry.getValue().equals(productStepEntry)) {
					conversationId = tableEntry.getKey();
					break;
				}
			}
		}
		return conversationId; 		
	}
	
	/**
	 * Getter for the service agent from this equiplet agent.
	 * @return the serviceAgent.
	 */
	public AID getServiceAgent() {
		return serviceAgent;
	}
	
	/**
	 * Getter for the timer that handles the next product step. 
	 * @return the timer.
	 */
	public NextProductStepTimer getTimer(){
		return timer;
	}

	/**
	 * Getter for the next product step.
	 * @return the nextProductStep
	 */
	public ObjectId getNextProductStep() {
		return nextProductStep;
	}
}
