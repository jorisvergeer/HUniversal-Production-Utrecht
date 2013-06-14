/*
 * ModuleProxy.cpp
 *
 *  Created on: Jun 14, 2013
 *      Author: joris
 */

#include "equiplet_node/ModuleProxy.h"

namespace equiplet_node {

ModuleProxy::ModuleProxy(std::string equipletNodeName, std::string moduleName, int equipletId, int moduleId)
:ModuleProxy(equipletNodeName, moduleName + "_" + std::to_string(equipletId) + "_" + std::to_string(moduleId))
{
}

ModuleProxy::ModuleProxy(std::string equipletNodeName, std::string moduleNodeName):
		changeStateActionClient(nodeHandle, moduleNodeName + "/change_state"),
		changeModeActionClient(nodeHandle, moduleNodeName + "/change_mode"),
		currentMode(rexos_statemachine::Mode::MODE_NORMAL),
		currentState(rexos_statemachine::State::STATE_SAFE)
{
	stateUpdateServiceServer = nodeHandle.advertiseService(
			equipletNodeName + "/" + moduleNodeName + "/state_update",
			&ModuleProxy::onStateChangeServiceCallback, this);

	modeUpdateServiceServer = nodeHandle.advertiseService(
			equipletNodeName + "/" + moduleNodeName + "/mode_update",
			&ModuleProxy::onModeChangeServiceCallback, this);
}

ModuleProxy::~ModuleProxy() {
	// TODO Auto-generated destructor stub
}

void ModuleProxy::changeState(rexos_statemachine::State state) {
	rexos_statemachine::ChangeStateGoal goal;
	goal.desiredState = state;
	changeStateActionClient.sendGoal(goal);
}

void ModuleProxy::changeMode(rexos_statemachine::Mode mode) {
	rexos_statemachine::ChangeModeGoal goal;
	goal.desiredMode = mode;
	changeModeActionClient.sendGoal(goal);
}

bool ModuleProxy::onStateChangeServiceCallback(StateUpdateRequest &req, StateUpdateResponse &res){
	currentState = static_cast<rexos_statemachine::State>(req.state);
	return true;
}

bool ModuleProxy::onModeChangeServiceCallback(ModeUpdateRequest &req, ModeUpdateResponse &res){
	currentMode = static_cast<rexos_statemachine::Mode>(req.mode);
	return true;
}

} /* namespace equiplet_node */
