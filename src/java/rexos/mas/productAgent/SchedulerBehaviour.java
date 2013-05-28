/**
 * @file SchedulerBehaviour.java
 * @brief Behaviour in which the product agent schedules the productsteps.
 * @date Created: 23-04-2013
 * 
 * @author Ricky van Rijn
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

package rexos.mas.productAgent;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import rexos.mas.data.Product;
import rexos.mas.data.Production;
import rexos.mas.data.ProductionStep;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.log.Logger;

import com.mongodb.DBObject;

@SuppressWarnings("serial")
public class SchedulerBehaviour extends OneShotBehaviour{
	private ProductAgent _productAgent;
	private int timeslotsToSchedule = 0;
	private int debug = 1;

	@Override
	public void action(){
		// Shedule the PA with the equiplet agents in the current list.
		_productAgent = (ProductAgent) myAgent;
		_productAgent.getProduct().getProduction()
				.getProductionEquipletMapping();
		try{
			Product product = this._productAgent.getProduct();
			Production production = product.getProduction();
			ArrayList<ProductionStep> psa = production.getProductionSteps();
			if (debug != 0){
				// debug
				System.out.println("Number of equiplets: " + psa.size());
			}
			for(ProductionStep ps : psa){
				int PA_id = ps.getId();
				if (production.getProductionEquipletMapping()
						.getEquipletsForProductionStep(PA_id).keySet().size() > 0){
					this.timeslotsToSchedule = production
							.getProductionEquipletMapping()
							.getTimeSlotsForEquiplet(
									PA_id,
									(AID) production
											.getProductionEquipletMapping()
											.getEquipletsForProductionStep(
													PA_id).keySet().toArray()[0])
							.intValue();
				}
				if (debug != 0){
					// debug
					System.out.println("-------------------");
					System.out.println("step_id:"
							+ PA_id
							+ " number of eq available: "
							+ production.getProductionEquipletMapping()
									.getEquipletsForProductionStep(PA_id)
									.keySet().size());
					System.out.println("STEP_ID:" + ps.getId() + " requires "
							+ this.timeslotsToSchedule + " timeslots");
					System.out.println("-------------------");
				}
				Scheduler(production.getProductionEquipletMapping()
						.getEquipletsForProductionStep(PA_id).keySet(), ps);
			}
		} catch(Exception e){
			Logger.log(e);
		}
	}

	/**
	 * Scheduler function schedules the given production step
	 * 
	 * @param equipletList
	 * @param productionStep
	 * @throws Exception
	 */
	public void Scheduler(Set<AID> equipletList, ProductionStep productionstep)
			throws Exception{
		Schedule[] schedules;
		// load set into arraylist
		List<AID> equipletlist = new ArrayList<AID>(equipletList);
		// Make connection with blackboard
		BlackboardClient bbc = new BlackboardClient("145.89.191.131");
		bbc.setDatabase("ScheduleBlackBoard");
		if (debug != 0){
			// debug
			System.out.println("Scheduler started");
		}
		// authenticating mongodb
		// boolean auth = db.authenticate("root", char['g','e','e','n']);
		// end connecting
		// extract data of every equiplet their mongoDB to Object Array
		int scheduleCount = 0;
		FreeTimeSlot[] freetimeslot;
		for(int i = 0; i < equipletlist.size(); i++){
			// old name is eqa1
			bbc.setCollection(equipletlist.get(i).getLocalName().toString());
			List<DBObject> blackBoard = bbc.findDocuments(" ");
			scheduleCount += blackBoard.size();
			if (debug != 0){
				// debug
				System.out
						.println("----- Get list of the already scheduled data -------");
				System.out.println("" + equipletlist.get(i).getLocalName());
				System.out.println();
			}
		}
		if (debug != 0){
			// debug
			System.out.println("--------- ");
			System.out.println("ScheduleCount: " + scheduleCount);
			System.out.println();
		}
		// intialize object 'Schedule' and object 'FreeTimeSlot' arrays
		schedules = new Schedule[scheduleCount];
		freetimeslot = new FreeTimeSlot[scheduleCount];
		// get every scheduled timeslot of every equiplet
		for(int extract = 0; extract < equipletlist.size(); extract++){
			bbc.setCollection(equipletlist.get(extract).getLocalName()
					.toString());
			List<DBObject> blackBoard = bbc.findDocuments(" ");
			// List<DBObject> data =
			// db.getCollection(equipletlist.get(extract).getLocalName()).find().toArray();
			// nameOfCollection should be 'schedule'
			for(int i = 0; i < blackBoard.size(); i++){
				double b = (Double) blackBoard.get(i).get("startTime");
				int stati = (int) b;
				double c = (Double) blackBoard.get(i).get("duration");
				int dur = (int) c;
				// add scheduled timeslot to array of scheduled timeslots and
				// mention which equiplet
				schedules[i] = this.new Schedule(stati, dur, equipletlist.get(
						extract).getName());
			}
		}
		// initialize timeslot to start checking and temporarily value for
		// calculation
		int startTimeSlot = 0;
		int freetimeslotCounter = 0;
		// check within every schedule of the 'schedules' array for free
		// timeslots
		// and add them to the 'freetimeslot' array
		for(int run = 0; run < schedules.length; run++){
			if (schedules[run].getStartTime() > startTimeSlot){
				if (schedules.length > (run + 1)){
					if (schedules[run].getDeadline() < schedules[(run + 1)]
							.getStartTime()){
						int freeTimeSlot = schedules[(run + 1)].getStartTime()
								- schedules[run].getDeadline() - 1;
						int timeslotToSchedule = (schedules[run].getDeadline() + 1);
						if (debug != 0){
							// debug
							System.out.println("Free timeslot: " + freeTimeSlot
									+ " starting at timeslot: "
									+ timeslotToSchedule);
							freetimeslot[freetimeslotCounter] = this.new FreeTimeSlot(
									timeslotToSchedule, freeTimeSlot,
									schedules[run].getEquipletName());
							System.out.println(freetimeslotCounter
									+ " : "
									+ freetimeslot[freetimeslotCounter]
											.toString());
							System.out.println();
						}
						freetimeslotCounter++;
					}
				}
			}
		}
		// Startslot which need to be scheduled
		FreeTimeSlot freetimeslotEq = null;
		if (debug != 0){
			System.out.println("---- Number of timeslots to schedule -----");
			System.out.println("Timeslots to schedule: " + timeslotsToSchedule);
			System.out.println();
		}
		// calculate freetime slot and asign them to the above intialized values
		if (freetimeslot.length > 1){
			if (debug != 0){
				System.out.println("Free time slots:" + freetimeslot.length);
			}
			for(int chooseTimeSlot = 0; chooseTimeSlot < freetimeslot.length; chooseTimeSlot++){
				if (freetimeslot[chooseTimeSlot] != null){
					if (freetimeslot[chooseTimeSlot].getDuration() <= timeslotsToSchedule){
						freetimeslotEq = freetimeslot[chooseTimeSlot];
					}
				}
			}
		}
		// init AID
		AID equipletAID = null;
		// get the equiplet from the timeslot
		for(int i = 0; i < equipletlist.size(); i++){
			if (freetimeslotEq != null
					&& equipletlist.get(i).getName()
							.equals(freetimeslotEq.getEquipletName())){
				equipletAID = equipletlist.get(i);
			}
		}
		if (debug != 0){
			// debug
			System.out
					.println("------- Equiplet which gains free time slot --------");
			System.out.println("AID name:" + equipletAID + "");
			System.out.println();
		}
		// send the message to the equiplet to schedule the timeslot
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		if (freetimeslotEq != null && equipletAID != null){
			msg.setConversationId(_productAgent.generateCID());
			msg.setOntology("ScheduleStep");
			msg.setContent("" + freetimeslotEq.getStartTime());
			msg.addReceiver(equipletAID);
			myAgent.send(msg);
			/*
			 * SEND MESSAGES TO OTHER PLATFORMS computername is resolved by the
			 * hosts file in either Linux or Windows AID remoteAMS = new
			 * AID("agentname@"+containername, AID.ISGUID);
			 * remoteAMS.addAddresses
			 * ("http://"+computername+":"+portnumber+"/acc");
			 * msg.addReceiver(remoteAMS); myAgent.send(msg);
			 */
			if (debug != 0){
				// debug
				System.out.println("Send timeslot " + equipletAID.getName()
						+ " to EQ");
				System.out.println(equipletAID);
			}
		} else{
			if (debug != 0){
				// debug
				System.out.println("No timeslot asigned.");
			}
		}
		if (debug != 0){
			// debug
			System.out.println();
		}
	}

	private class FreeTimeSlot{
		private int startTime = -1;
		private int duration = -1;
		private String equipletName = "";

		public FreeTimeSlot(int start, int dura, String equiplet){
			this.startTime = start;
			this.duration = dura;
			this.equipletName = equiplet;
		}

		public String getEquipletName(){
			return this.equipletName;
		}

		public int getStartTime(){
			return this.startTime;
		}

		public int getDuration(){
			return this.duration;
		}

		@Override
		public String toString(){
			return "{Start TimeSlot: " + this.startTime + ", Duration: "
					+ this.duration + ", EquipletName: " + this.equipletName
					+ "}";
		}
	}

	private class Schedule{
		private int startTime = -1;
		private int duration = -1;
		private int deadline = -1;
		private String equipletName = "";

		public Schedule(int start, int dura, String equiplet){
			this.startTime = start;
			this.duration = dura;
			this.deadline = start + dura - 1;
			this.equipletName = equiplet;
		}

		public int getStartTime(){
			return this.startTime;
		}

		@SuppressWarnings("unused")
		public void setStartTime(int newStartTime){
			this.startTime = newStartTime;
		}

		public String getEquipletName(){
			return this.equipletName;
		}

		public int getDeadline(){
			return this.deadline;
		}

		@SuppressWarnings("unused")
		public void setDeadline(int newDeadline){
			this.deadline = newDeadline;
		}

		@Override
		public String toString(){
			return "{ startTime:" + startTime + ", duration:" + duration
					+ ", deadline:" + deadline + ", EquipletName:"
					+ equipletName + " }";
		}
	}
}
