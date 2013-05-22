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

#include <iostream>
#include "rexos_est_cpp_client/StateBlackboardCppClient.h"

/**
 * Constructor for the BlackboardCppClient
 *
 * @param hostname of the mongodb server
 * @param db The name of the database
 * @param ESTStateMachine The address of the ESTStateMachineCppClient
 **/
StateBlackboardCppClient::StateBlackboardCppClient(const std::string &hostname, const std::string db, ESTStateMachineCppClient *ESTStateMachine) :
		database(db),ESTStateMachine(ESTStateMachine)
{
	try{
		connection.connect(hostname);
		std::cout << "connected to database" << std::endl;
	} catch(const mongo::DBException &e){
		std::cout << "caught " << e.what() << std::endl;
	}
}

/**
 * Constructor for the BlackboardCppClient
 *
 * @param hostname of the mongodb server
 * @param port the port number for the mongodb server
 * @param db The name of the database
 * @param ESTStateMachine The address of the ESTStateMachineCppClient
 **/
StateBlackboardCppClient::StateBlackboardCppClient(const std::string &hostname, int port, const std::string db, ESTStateMachineCppClient *ESTStateMachine):
		database(db),ESTStateMachine(ESTStateMachine)
{
		try{
		connection.connect(mongo::HostAndPort(hostname, port));
		std::cout << "connected to database" << std::endl;
	} catch(const mongo::DBException &e){
		std::cout << "caught " << e.what() << std::endl;
	}
}

/**
 * Destructor for the StateBlackboardCppClient
 **/
StateBlackboardCppClient::~StateBlackboardCppClient(){
	readMessageThread->interrupt();
	delete readMessageThread;
}

void StateBlackboardCppClient::setDesiredEST(int desiredState){
	std::string name = database;
	name.append(".");
	name.append(this->EST);
	connection.update(name, mongo::Query(), BSON("$set" << BSON("desiredState" << desiredState)));
}
void StateBlackboardCppClient::updateEST(int id, int state, int modi){
	std::string name = database;
	name.append(".");
	name.append(this->EST);
	connection.update(name, BSON("id" << id), BSON("id" << id << "state" << state << "modi" << modi), true);
}

void StateBlackboardCppClient::updateMOST(int id, int state, int modi){
	std::string name = database;
	name.append(".");
	name.append(this->MOST);
	connection.update(name, BSON("id" << id), BSON("id" << id << "state" << state << "modi" << modi), true);
}

void StateBlackboardCppClient::updateSafetyState(int state){
	std::string name = database;
	name.append(".");
	name.append(this->STATE);
	connection.update(name, mongo::Query(), BSON("$set" << BSON("safety" << state)));
}

void StateBlackboardCppClient::updateOperationalState(int state){
	std::string name = database;
	name.append(".");
	name.append(this->STATE);
	connection.update(name, mongo::Query(), BSON("$set" << BSON("operational" << state)));
}

