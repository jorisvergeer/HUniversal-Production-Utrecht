/**
 * @file PeriodicMsgSenderAgent.java
 * @brief Sends periodic msgs to test the product agent's parallel behaviour
 *        Sends an 'Reschedule' msg to force the product agent to reschedule.
 *        Sends an 'EquipletMalfunction' msg to fake the malfunctioning of an
 *        equiplet
 * 
 * @date Created: 17 Apr 2013
 * 
 * @author Alexander Streng
 * 
 * @section LICENSE License: newBSD
 * 
 *          Copyright � 2012, HU University of Applied Sciences Utrecht. All
 *          rights reserved.
 * 
 *          Redistribution and use in source and binary forms, with or without
 *          modification, are permitted provided that the following conditions
 *          are met: - Redistributions of source code must retain the above
 *          copyright notice, this list of conditions and the following
 *          disclaimer. - Redistributions in binary form must reproduce the
 *          above copyright notice, this list of conditions and the following
 *          disclaimer in the documentation and/or other materials provided with
 *          the distribution. - Neither the name of the HU University of Applied
 *          Sciences Utrecht nor the names of its contributors may be used to
 *          endorse or promote products derived from this software without
 *          specific prior written permission.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *          "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *          LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *          FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE HU
 *          UNIVERSITY OF APPLIED SCIENCES UTRECHT BE LIABLE FOR ANY DIRECT,
 *          INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *          (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *          SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *          HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *          STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *          ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *          OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 **/

package rexos.mas.testingAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class PeriodicMsgSenderAgent extends Agent{
	private static final long serialVersionUID = 1L;
	private boolean debug = true;
	// CID variables
	private static int _convIDCnt = 0;
	private String _convIDBase;

	@Override
	@SuppressWarnings("serial")
	protected void setup(){
		try{
			addBehaviour(new WakerBehaviour(this, getRandomInt(30000)){
				@Override
				protected void onWake(){
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					message.addReceiver(new AID("pa1", AID.ISLOCALNAME));
					message.setConversationId(generateCID());
					if (getRandomBoolean()){
						if (debug)
							System.out.println("Sending a reschedule message.");
						message.setOntology("Reschedule");
					} else{
						if (debug)
							System.out.println("Sending a move message.");
						message.setOntology("MoveToEQ");
					}
					myAgent.send(message);
				}
			});
		} catch(Exception e){
			System.out.println("PeriodicMsgSenderAgent exited with: " + e);
			doDelete();
		}
	}

	public static boolean getRandomBoolean(){
		Random randomBool = new Random();
		return randomBool.nextBoolean();
	}

	public static int getRandomInt(int random){
		Random randomInt = new Random();
		return randomInt.nextInt(random);
	}

	/*
	 * Generates an unique conversation id based on the agents localname, the
	 * objects hashcode and the current time.
	 */
	public String generateCID(){
		if (_convIDBase == null){
			_convIDBase = getLocalName() + hashCode()
					+ System.currentTimeMillis() % 10000 + "_";
		}
		return _convIDBase + (_convIDCnt++);
	}
}