/**
 * @file ROSStateMachineActionProvider.h
 * @brief ROSStateMachineActionProvider Implementation
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

#include "rexos_most/ROSStateMachineActionProvider.h"
#include <boost/bind.hpp>
#include <rexos_most/ModuleUpdate.h>

using namespace rexos_most;

/**
 * Create a stateMachine
 * @param moduleID the unique identifier for the module that implements the statemachine
 **/
ROSStateMachineActionProvider::ROSStateMachineActionProvider(
		MOSTStateMachine* most) :
		changeStateActionServer(nodeHandle, "change_state", boost::bind(&ROSStateMachineActionProvider::onChangeStateService, this, _1), false),
		changeModeActionServer(nodeHandle, "change_mode", boost::bind(&ROSStateMachineActionProvider::onChangeModeService, this, _1), false),
		most(most) {

	most->setMostListener(this);

	changeStateActionServer.start();
	changeModeActionServer.start();

	ROS_INFO("Wait for equiplet...");
	ROS_INFO("Wait for equiplet...");
	moduleUpdateServiceClient = nodeHandle.serviceClient<rexos_most::ModuleUpdate>("/most/equiplet/moduleUpdate");
	moduleUpdateServiceClient.waitForExistence();
	ROS_INFO("Check-in by equiplet");
	notifyEquiplet();
}

ROSStateMachineActionProvider::~ROSStateMachineActionProvider() {
}

void ROSStateMachineActionProvider::onChangeStateService(const rexos_most::ChangeStateGoalConstPtr& goal){
	ChangeStateResult res;

	switch (goal->desiredState) {
		case STATE_SAFE:
			res.executed = most->changeState(STATE_SAFE);
			break;
		case STATE_STANDBY:
			res.executed = most->changeState(STATE_STANDBY);
			break;
		case STATE_NORMAL:
			res.executed = most->changeState(STATE_NORMAL);
			break;
		default:
			changeStateActionServer.setAborted(res);
		}

	changeStateActionServer.setSucceeded(res);
}
void ROSStateMachineActionProvider::onChangeModeService(const rexos_most::ChangeModeGoalConstPtr& goal){
	ChangeModeResult res;
	switch (goal->desiredMode) {
		case MODI_NORMAL:
			res.executed = most->changeModi(MODI_NORMAL);
			break;
		case MODI_SERVICE:
			res.executed = most->changeModi(MODI_SERVICE);
			break;
		case MODI_ERROR:
			res.executed = most->changeModi(MODI_ERROR);
			break;
		case MODI_CRITICAL_ERROR:
			res.executed = most->changeModi(MODI_CRITICAL_ERROR);
			break;
		case MODI_E_STOP:
			res.executed = most->changeModi(MODI_E_STOP);
			break;
		default:
			changeModeActionServer.setAborted(res);
		}
	changeModeActionServer.setSucceeded(res);
}

void ROSStateMachineActionProvider::onMOSTStateChanged() {
	notifyEquiplet();
}
void ROSStateMachineActionProvider::onMOSTModiChanged() {
	notifyEquiplet();
}

void ROSStateMachineActionProvider::notifyEquiplet() {
	rexos_most::ModuleUpdate::Request req;
	req.info.id = most->getModuleID();
	req.info.state = most->getCurrentState();
	req.info.modi = most->getCurrentModi();
	rexos_most::ModuleUpdate::Response res;
	if(moduleUpdateServiceClient.call(req, res) == false){
		ros::shutdown();
	}
}
