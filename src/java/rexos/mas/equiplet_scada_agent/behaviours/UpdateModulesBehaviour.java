package rexos.mas.equiplet_scada_agent.behaviours;

import rexos.mas.equiplet_scada_agent.interfaces.UpdateModulesListener;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

public class UpdateModulesBehaviour extends MsgReceiver {
	private static final long serialVersionUID = 1L;
	
	private UpdateModulesListener listener;
	
	private class UpdateModulesTicker extends TickerBehaviour{
		private static final long serialVersionUID = 1L;

		public UpdateModulesTicker(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			listener.onModuleUpdateRequested(null);			
		}
		
	}
	
	private UpdateModulesTicker updateModulesTicker;
	
	public UpdateModulesBehaviour(Agent agent, UpdateModulesListener listener) {
		super(agent, MessageTemplate.MatchOntology("update-modules"), INFINITE, null, null);
		this.listener = listener;
		this.updateModulesTicker = new UpdateModulesTicker(agent, 10000);
	}
	
	@Override
	protected void handleMessage(ACLMessage msg) {
		if(msg != null){
			updateModulesTicker.reset();
			listener.onModuleUpdateRequested(msg.getSender());
		}
	}
	
	@Override
	public int onEnd() {
		myAgent.removeBehaviour(updateModulesTicker);
		return super.onEnd();
	}
}
