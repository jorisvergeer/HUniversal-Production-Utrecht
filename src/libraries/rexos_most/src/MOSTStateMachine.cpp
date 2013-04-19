/**
 * @file MOSTStateMachine.h
 * @brief MOSTStatemachine Implementation
 * @date Created: 2013-17-04
 *
 * @author Gerben Boot & Joris Vergeer
 *
 * @section LICENSE
 * License: newBSD
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

#include "rexos_most/MOSTStateMachine.h"

using namespace rexos_most;

/**
 * Create a stateMachine
 * @param moduleID the unique identifier for the module that implements the statemachine
 **/
MOSTStateMachine::MOSTStateMachine(int module) :
		currentState(STATE_SAFE), currentModi(MODI_NORMAL) {
	transitionMap[MOSTStatePair(STATE_SAFE, STATE_STANDBY)]= {
		&MOSTStateMachine::transitionSetup, STATE_SETUP,
		&MOSTStateMachine::transitionShutdown, STATE_SHUTDOWN};
	transitionMap[MOSTStatePair(STATE_STANDBY, STATE_NORMAL)] = {
		&MOSTStateMachine::transitionStart, STATE_START,
		&MOSTStateMachine::transitionStop, STATE_STOP};
	transitionMap[MOSTStatePair(STATE_NORMAL, STATE_STANDBY)] =
	{	&MOSTStateMachine::transitionStop, STATE_STOP, NULL, STATE_NOSTATE};
	transitionMap[MOSTStatePair(STATE_STANDBY, STATE_SAFE)]= {
		&MOSTStateMachine::transitionShutdown, STATE_SHUTDOWN, NULL,
		STATE_NOSTATE};

	modiPossibleStates[MODI_NORMAL] = {STATE_NORMAL,STATE_STANDBY,STATE_SAFE};
	modiPossibleStates[MODI_ERROR] = {STATE_STANDBY,STATE_SAFE};
	modiPossibleStates[MODI_CRITICAL_ERROR] = {STATE_SAFE};
	modiPossibleStates[MODI_E_STOP] = {STATE_SAFE};

	std::stringstream ss;
	ss << "most/" << moduleID << "/change_state";
	std::string string = ss.str();
	changeStateService = nodeHandle.advertiseService(string, &MOSTStateMachine::onChangeStateService,this);
	changeModiService = nodeHandle.advertiseService(string, &MOSTStateMachine::onChangeModiService,this);
}

bool MOSTStateMachine::onChangeStateService(
		rexos_most::ChangeState::Request &req,
		rexos_most::ChangeState::Response &res) {
	switch (req.desiredState) {
	case STATE_SAFE:
		res.executed = changeState(STATE_SAFE);
		break;
	case STATE_STANDBY:
		res.executed = changeState(STATE_STANDBY);
		break;
	case STATE_NORMAL:
		res.executed = changeState(STATE_NORMAL);
		break;
	default:
		return false;
	}
	return true;
}

bool MOSTStateMachine::onChangeModiService(rexos_most::ChangeModi::Request &req,
		rexos_most::ChangeModi::Response &res) {

	switch (req.desiredModi) {
	case MODI_NORMAL:
		res.executed = changeModi(MODI_NORMAL);
		break;
	case MODI_SERVICE:
		res.executed = changeModi(MODI_NORMAL);
		break;
	case MODI_ERROR:
		res.executed = changeModi(MODI_NORMAL);
		break;
	case MODI_CRITICAL_ERROR:
		res.executed = changeModi(MODI_NORMAL);
		break;
	case MODI_E_STOP:
		res.executed = changeModi(MODI_NORMAL);
		break;
	default:
		return false;
	}
	return true;
}

/**
 * Callback for the requestStateChange topic
 * Will lookup the transition function and execute it
 * @param request Contains the params for the state change
 * @param response Will tell if the state transition was succesfull for the state change
 **/
bool MOSTStateMachine::changeState(MOSTState newState) {
	// decode msg and read variables
	//ROS_INFO("Request Statechange message received");
	if (!statePossibleInModi(newState, currentModi))
		return false;

	transitionMapType::iterator it = transitionMap.find(
			MOSTStatePair(currentState, newState));
	if (it == transitionMap.end()) {
		return false;
	}

	currentState = it->second.transitionState;
	if ((this->*it->second.transitionFunctionPointer)()) {
		currentState = it->first.second;
	} else {
		currentState = it->second.abortTransitionState;
		(this->*it->second.abortTransitionFunctionPointer)();
		currentState = it->first.first; //previousstate
	}

	return true;
}

MOSTState MOSTStateMachine::getCurrentState() {
	return currentState;
}

MOSTModi MOSTStateMachine::getCurrentModi() {
	return currentModi;
}

bool MOSTStateMachine::statePossibleInModi(MOSTState state, MOSTModi modi) {
	std::vector<MOSTState> mostStates = modiPossibleStates[modi];
	for (int i = 0; i < mostStates.size(); i++) {
		if (mostStates[i] == state)
			return true;
	}
	return false;
}

bool MOSTStateMachine::changeModi(MOSTModi newModi) {
	currentModi = newModi;
	while (!statePossibleInModi(currentState, currentModi)) {
		switch (currentState) {
		case STATE_NORMAL:
			changeState(STATE_STANDBY);
			break;
		case STATE_STANDBY:
			changeState(STATE_SAFE);
			break;
		}
	}
	return true;
}
