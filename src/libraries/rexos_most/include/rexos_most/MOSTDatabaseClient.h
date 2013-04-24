#pragma once

#include <mongo/client/dbclient.h>

#include <auto_ptr.h>

#include <rexos_most/MOSTModi.h>
#include <rexos_most/MOSTState.h>

#define MONGODB_HOST "145.89.191.131"

class MOSTDatabaseClient {
public:
	struct ModuleData {
		int id;
		int state;
		int modi;

		ModuleData(int id, rexos_most::MOSTState state, rexos_most::MOSTModi modi) :
				id(id), state(state), modi(modi) {
		}

		ModuleData() :
				id(-1), state(-1), modi(-1) {
		}

		static ModuleData fromBSON(const mongo::BSONObj& obj) {
			ModuleData result;
			result.id = obj.getIntField("id");
			result.state = obj.getIntField("state");
			result.modi =  obj.getIntField("modi");
			return result;
		}

		mongo::BSONObj toBSON() const {
			return BSON("id" << id << "state" << state << "modi" << modi);
		}
	};

	MOSTDatabaseClient() {
		connection.connect(MONGODB_HOST);
	}

	void setSafetyState(rexos_most::MOSTState state){
		connection.update("most.equiplet", mongo::Query(), BSON("$set" << BSON("safety" << state)));
	}

	ModuleData getModuleData(int moduleID) {
		std::auto_ptr<mongo::DBClientCursor> cursor = connection.query("most.modules", QUERY("id" << moduleID));
		if(cursor->more()) {
			mongo::BSONObj p = cursor->next();
			return ModuleData::fromBSON(p);
		}
		return ModuleData();
	}

	void setModuleData(const ModuleData& data) {
		connection.update("most.modules", BSON("id" << data.id), data.toBSON(), true);
	}

	void clearModuleData() {
		connection.remove("most.modules", mongo::Query());
	}

	std::vector<ModuleData> getAllModuleData() {
		std::vector<ModuleData> result;
		std::auto_ptr<mongo::DBClientCursor> cursor = connection.query("most.modules");
		while(cursor->more()){
			result.push_back(ModuleData::fromBSON(cursor->next()));
		}
		return result;
	}

private:
	mongo::DBClientConnection connection;
};
