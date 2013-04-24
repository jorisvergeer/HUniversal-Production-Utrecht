/**
 * @file ROSStateMachineServiceProvider.h
 * @brief ROS Services for the MOST State machine
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

#ifndef ROSSTATEMACHINESERVICEPROVIDER_H
#define ROSSTATEMACHINESERVICEPROVIDER_H

#include "ros/ros.h"

#include "rexos_most/MOSTStateMachine.h"

#include <rexos_most/ChangeState.h>
#include <rexos_most/ChangeModi.h>

namespace rexos_most {

class ROSStateMachineServiceProvider : public MOSTStateMachine::MOSTListener{
public:
	ROSStateMachineServiceProvider(MOSTStateMachine* most);

	virtual ~ROSStateMachineServiceProvider();

	virtual void onMOSTStateChanged();
	virtual void onMOSTModiChanged();

private:
	bool onChangeStateService(rexos_most::ChangeState::Request &req,
			rexos_most::ChangeState::Response &res);
	bool onChangeModiService(rexos_most::ChangeModi::Request &req,
			rexos_most::ChangeModi::Response &res);

	void notifyEquiplet();

	MOSTStateMachine* most;

	ros::NodeHandle nodeHandle;
	ros::ServiceServer changeStateService;
	ros::ServiceServer changeModiService;
	ros::ServiceClient moduleUpdateServiceClient;
};

}
#endif