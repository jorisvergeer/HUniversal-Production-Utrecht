/**
 * @file MOSTStateMachine.h
 * @brief Statemachine for MOST in module
 * @date Created: 2013-17-03
 *
 * @author Gerben Boot & Joris Vergeer
 *
 * @section LICENSE
 * License: newBSD
 * Copyright © 2013, HU University of Applied Sciences Utrecht.
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

#ifndef MOSTSTATEMACHINE_H
#define MOSTSTATEMACHINE_H

#include <map>
#include <vector>

#include "rexos_most/MOSTState.h"
#include "rexos_most/MOSTModi.h"
#include "rexos_most/MOSTTransitions.h"

#include <rexos_most/ChangeState.h>
#include <rexos_most/ChangeModi.h>

namespace rexos_most {

class MOSTStateMachine: public MOSTTransitions {

	/**
	 * @var typedef int (StateMachine::*stateFunctionPtr)()
	 * Function pointer definition for a state transition function
	 **/
	typedef bool (MOSTStateMachine::*stateFunctionPtr)();
public:
	MOSTStateMachine(int moduleID);

	virtual ~MOSTStateMachine();

	/**
	 * Get the state of the statemachine
	 * @return the currentState of the machine
	 **/
	MOSTState getCurrentState();

	MOSTModi getCurrentModi();

	int getModuleID();

	bool changeState(MOSTState newState);

	bool changeModi(MOSTModi newModi);

	bool statePossibleInModi(MOSTState state, MOSTModi modi);

private:
	bool onChangeStateService(rexos_most::ChangeState::Request &req, rexos_most::ChangeState::Response &res);
	bool onChangeModiService(rexos_most::ChangeModi::Request &req, rexos_most::ChangeModi::Response &res);

	/**
	 * @var MOSTState currentState
	 * The current state of the the state machine
	 **/
	MOSTState currentState;

	/**
	 * @var MOSTModi currentModi
	 * The current modi of the the state machine
	 **/
	MOSTModi currentModi;

	/**
	 * @var map<MOSTModi,MOSTState[]> ModiPossibleStates
	 * Possible states of all modus
	 **/
	std::map<MOSTModi, std::vector<MOSTState> > modiPossibleStates;

	/**
	 * @var std::map<std::pair<MOSTState,MOSTState>, std::pair<stateFunctionPtr,stateFunctionPtr>> transitionMap;
	 * key is a pair from src to destination
	 * value is a pair with:
	 * the key: functionpointer of the transition
	 * the value: functionpointer of the transition while abort
	 **/
	struct transitionMapEntryValue {
		stateFunctionPtr transitionFunctionPointer;
		MOSTState transitionState;

		stateFunctionPtr abortTransitionFunctionPointer;
		MOSTState abortTransitionState;
	};

	typedef std::pair<MOSTState, MOSTState> MOSTStatePair;
	typedef std::pair<MOSTStatePair, transitionMapEntryValue> transitionMapEntry;
	typedef std::map<MOSTStatePair, transitionMapEntryValue> transitionMapType;
	transitionMapType transitionMap;

	/**
	 * @var int moduleID
	 * The identifier for the module the state machine belongs to
	 **/
	int moduleID;
};

}
#endif
