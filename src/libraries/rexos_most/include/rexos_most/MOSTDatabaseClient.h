#pragma once

#include <mongo/client/dbclient.h>

#include <auto_ptr.h>

#define MONGODB_HOST "145.89.191.131"

class MOSTDatabaseClient {
public:
	struct ModuleData {
		int id;
		int state;

		ModuleData(int id, rexos_most::MOSTState state) :
				id(id), state(state) {
		}

		ModuleData() :
				id(-1), state(0) {
		}

		static ModuleData fromBSON(mongo::BSONObj& obj) {
			ModuleData result;
			result.id = obj.getIntField("id");
			result.state = obj.getIntField("state");
			return result;
		}

		mongo::BSONObj toBSON() const {
			return BSON("id" << id << "state" << state);
		}
	};

	MOSTDatabaseClient() {
		connection.connect(MONGODB_HOST);
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

private:
	mongo::DBClientConnection connection;
};
