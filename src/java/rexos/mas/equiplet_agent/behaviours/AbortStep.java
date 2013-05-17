package rexos.mas.equiplet_agent.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

import org.bson.types.ObjectId;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.log.Logger;
import rexos.mas.behaviours.ReceiveBehaviour;
import rexos.mas.data.ProductionStep;
import rexos.mas.data.ScheduleData;
import rexos.mas.equiplet_agent.EquipletAgent;
import rexos.mas.equiplet_agent.ProductStepMessage;
import rexos.mas.equiplet_agent.StepStatusCode;

import com.mongodb.BasicDBObject;

public class AbortStep extends ReceiveBehaviour {
	private static final long serialVersionUID = 1L;

	/**
	 * @var MessageTemplate messageTemplate The messageTemplate this behaviour
	 *      listens to. This behaviour listens to the ontology: AbortStep.
	 */
	private static MessageTemplate messageTemplate = MessageTemplate
			.MatchOntology("AbortStep");

	/**
	 * @var EquipletAgent equipletAgent The equipletAgent related to this
	 *      behaviour.
	 */
	private EquipletAgent equipletAgent;
	private BlackboardClient equipletBBClient;

	/**
	 * Instantiates a new can perform step.
	 * 
	 * @param a The agent for this behaviour
	 */
	public AbortStep(Agent a, BlackboardClient equipletBBClient) {
		super(a, messageTemplate);
		equipletAgent = (EquipletAgent) a;
		this.equipletBBClient = equipletBBClient;
	}

	/**
	 * Function to handle the incoming messages for this behaviour. Handles the
	 * response to the AbortStep checks the step status, and aborts it if able to.
	 * 
	 * @param message The received message.
	 */
	@Override
	public void handle(ACLMessage message) {
		Logger.log("%s received message from %s%n", myAgent.getLocalName(), message
				.getSender().getLocalName(), message.getOntology());

		
	}
	
}
