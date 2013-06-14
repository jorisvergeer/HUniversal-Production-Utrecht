/*
 * ModuleRegistry.cpp
 *
 *  Created on: Jun 14, 2013
 *      Author: joris
 */

#include "equiplet_node/ModuleRegistry.h"

#include <equiplet_node/EquipletNode.h>

namespace equiplet_node {

ModuleRegistry::ModuleRegistry(std::string nodeName, int equipletId)
:newRegistrationsAllowed(false),
 equipletId(equipletId)
{
	registerModuleServiceServer = rosNodeHandle.advertiseService(
			nodeName + "/register_module",
			&ModuleRegistry::onRegisterServiceModuleCallback,
			this);
}

ModuleRegistry::~ModuleRegistry() {
	for(auto it = registeredModules.begin(); it != registeredModules.end(); it++) {
		ModuleProxy* proxy = *it;
		delete proxy;
	}
}

void ModuleRegistry::setNewRegistrationsAllowed(bool allowed){
	newRegistrationsAllowed = allowed;
}

bool ModuleRegistry::onRegisterServiceModuleCallback(RegisterModule::Request &req, RegisterModule::Response &res) {
	if(!newRegistrationsAllowed){
		return false;
	}

	ModuleProxy* proxy = new ModuleProxy(
			EquipletNode::nameFromId(equipletId),
			req.name,
			equipletId,
			req.id);
	registeredModules.push_back(proxy);

	return true;
}

} /* namespace equiplet_node */
