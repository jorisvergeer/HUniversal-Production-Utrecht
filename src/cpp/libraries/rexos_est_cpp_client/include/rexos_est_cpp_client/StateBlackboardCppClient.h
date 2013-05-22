/**
 * @file StateBlackboardCppClient.cpp
 * @brief The cpp client for the blackboard
 * @date Created: 2013-05-17
 *
 * @author Gerben Boot
 *
 * @section LICENSE
 * License: newBSD
 *
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

#ifndef STATE_BLACKBOARD_CPP_CLIENT_H_
#define STATE_BLACKBOARD_CPP_CLIENT_H_

#include <string>
#include <boost/thread.hpp>
#include <boost/bind.hpp>
#include <algorithm>
#include <iostream>

#pragma GCC system_header
#include <mongo/client/dbclient.h>
#include "rexos_est_cpp_client/ESTStateMachineCppClient.h"

/**
 * This class represents the C++ client for the blackboard system
 **/
class StateBlackboardCppClient{
public:
	StateBlackboardCppClient(const std::string &hostname, const std::string db, ESTStateMachineCppClient *ESTStateMachine);
	StateBlackboardCppClient(const std::string &hostname, int port, const std::string db, ESTStateMachineCppClient *ESTStateMachine);
	virtual ~StateBlackboardCppClient();
	void setDesiredEST(int desiredState);
	void updateEST(int id, int state, int modi);
	void updateMOST(int id, int state, int modi);
	void updateSafetyState(int state);
	void updateOperationalState(int state);
	
private:
	void run();
	/**
	 * @var mongo::DBClientConnection connection
	 * The connection to the mongodb database
	 **/
	mongo::DBClientConnection connection;

	/**
	 * @var std::string database
	 * The name of the database
	 **/
	std::string database;

	/**
	 * @var boost::thread *readMessageThread
	 * Pointer to the thread
	 **/
	boost::thread *readMessageThread;

	/**
	 * @var BlackboardSubscriber *callback
	 * Pointer to the statemachine which is called is when transition must be executed
	 **/
	ESTStateMachineCppClient *ESTStateMachine;

	const std::string EST = "EST";

	const std::string MOST = "MOST";

	const std::string STATE = "STATE";
};

#endif
