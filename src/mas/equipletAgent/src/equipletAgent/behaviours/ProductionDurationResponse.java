package equipletAgent.behaviours;

import newDataClasses.ScheduleData;
import nl.hu.client.BlackboardClient;

import org.bson.types.ObjectId;
import behaviours.ReceiveBehaviour;

import com.mongodb.BasicDBObject;
import equipletAgent.EquipletAgent;
import equipletAgent.ProductStepMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The Class ProductionDurationResponse.
 */
public class ProductionDurationResponse extends ReceiveBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static MessageTemplate messageTemplate = MessageTemplate.MatchOntology("ProductionDurationResponse");
	private EquipletAgent equipletAgent;
	private BlackboardClient equipletBBClient;

	/**
	 * Instantiates a new production duration response.
	 *
	 * @param a the a
	 */
	public ProductionDurationResponse(Agent a, BlackboardClient equipletBBClient) {
		super(a, -1, messageTemplate);
		equipletAgent = (EquipletAgent) a;
		this.equipletBBClient = equipletBBClient;
	}

	@Override
	public void handle(ACLMessage message) {
		Object contentObject = null;
		String contentString = message.getContent();

		try {
			contentObject = message.getContentObject();
		} catch (UnreadableException e) {
			// System.out.println("Exception Caught, No Content Object Given");
		}
		System.out.format("%s received message from %s (%s:%s)%n", myAgent.getLocalName(), message.getSender().getLocalName(), message.getOntology(), contentObject == null ? contentString : contentObject);

		try {
			ObjectId id = equipletAgent.getRelatedObjectId(message.getConversationId());
			ProductStepMessage productStep = new ProductStepMessage((BasicDBObject)equipletBBClient.findDocumentById(id));

			ScheduleData schedule = productStep.getScheduleData();
			System.out.println(schedule.getDuration() + "");
			
			ACLMessage responseMessage = new ACLMessage(ACLMessage.INFORM);
			AID productAgent = productStep.getProductAgentId();
			responseMessage.addReceiver(productAgent);
			responseMessage.setOntology("ProductionDuration");
			responseMessage.setConversationId(message.getConversationId());
			responseMessage.setContentObject(schedule.getDuration());
			myAgent.send(responseMessage);
			
			System.out.format("sending message: %s%n", responseMessage.getOntology());
			

		} catch (Exception e) {
			e.printStackTrace();
			myAgent.doDelete();
		}
	}
}
