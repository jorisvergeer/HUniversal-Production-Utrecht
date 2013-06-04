#include <iostream>

#include <boost/algorithm/string.hpp>

#include <ros/ros.h>

#include <libjson/libjson.h>

#include <rexos_most/MOSTDatabaseClient.h>

#include "mongoose.h"

static const char *ajax_reply_start_success = "HTTP/1.1 200 OK\r\n"
		"Cache: no-cache\r\n"
		"Content-Type: application/x-javascript\r\n"
		"\r\n";

static const char *ajax_reply_start_failed =
		"HTTP/1.1 500 Internal Server Error\r\n"
				"Cache: no-cache\r\n"
				"Content-Type: application/x-javascript\r\n"
				"\r\n";

void show_usage(std::string name) {
	std::cerr << "Usage: " << name << "<option(s)>\n" << "Options:\n"
			<< "\t--help\t\tShow this help message\n"
			<< "\t--root\t\tRoot of the http files folder\n";
}

void process_changeModuleModi(mg_connection* conn,
		mg_request_info* request_info) {
	//TODO fetch module id and modi

	if (request_info->query_string == NULL) {
		mg_printf(conn, "%s", ajax_reply_start_failed);
		return;
	}

	//Decode the http query into the moduleID and desiredModi
	std::string httpQuery = request_info->query_string;
	std::string moduleID, modi;
	std::vector<std::string> httpArgs;
	boost::split(httpArgs, httpQuery, boost::is_any_of("&"));
	for (std::string httpArg : httpArgs) {
		std::vector<std::string> httpArgContent;
		boost::split(httpArgContent, httpArg, boost::is_any_of("="));
		if (httpArgContent.size() != 2) {
			continue;
		}

		std::string name = httpArgContent[0];
		std::string value = httpArgContent[1];

		if (name == "id") {
			moduleID = value;
		} else if (name == "modi") {
			modi = value;
		}
	}

	if (moduleID == "" || modi == "") {
		mg_printf(conn, "%s", ajax_reply_start_failed);
		return;
	}

	MOSTDatabaseClient mdb;
	mdb.sendEquipletCommand("changeModuleModi",
			BSON("id" << moduleID << "modi" << modi));

	mg_printf(conn, "%s", ajax_reply_start_success);
}

void process_makeEquipletSafe(mg_connection* conn,
		mg_request_info* request_info) {
	//TODO fetch module id and modi

	MOSTDatabaseClient mdb;
	mdb.sendEquipletCommand("makeEquipletSafe");

	mg_printf(conn, "%s", ajax_reply_start_success);
}

void process_equipletInfo(mg_connection* conn, mg_request_info* request_info) {
	MOSTDatabaseClient mdb;

	JSONNode jsonObject;
	jsonObject.push_back(JSONNode("id", "TODO"));
	jsonObject.push_back(JSONNode("name", "TODO"));
	jsonObject.push_back(JSONNode("operational", mdb.getOperationalState()));
	jsonObject.push_back(JSONNode("safety", mdb.getSafetyState()));

	mg_printf(conn, "%s", ajax_reply_start_success);

	mg_printf(conn, "%s", jsonObject.write_formatted().c_str());

	//TODO;
}

void process_moduleInfo(mg_connection* conn, mg_request_info* request_info) {
	MOSTDatabaseClient mdb;
	std::vector<MOSTDatabaseClient::ModuleData> modules =
			mdb.getAllModuleData();
	JSONNode jsonModules(JSON_ARRAY);
	jsonModules.set_name("modules");
	for (MOSTDatabaseClient::ModuleData module : modules) {
		JSONNode jsonModule;
		jsonModule.push_back(JSONNode("id", module.id));
		jsonModule.push_back(JSONNode("modi", module.modi));
		jsonModule.push_back(JSONNode("state", module.state));
		jsonModule.push_back(JSONNode("name", "TODO")); //TODO
		jsonModule.push_back(JSONNode("type", "TODO")); //TODO
		jsonModules.push_back(jsonModule);
	}

	JSONNode jsonObject;
	jsonObject.push_back(jsonModules);

	mg_printf(conn, "%s", ajax_reply_start_success);

	mg_printf(conn, "%s", jsonObject.write_formatted().c_str());

	//TODO;
}

int begin_request_handler(struct mg_connection *conn) {
	mg_request_info * request_info = mg_get_request_info(conn);

	int processed = 1;
	if (strcmp(request_info->uri, "/remote/equipletInfo") == 0) {
		process_equipletInfo(conn, request_info);
	} else if (strcmp(request_info->uri, "/remote/moduleInfo") == 0) {
		process_moduleInfo(conn, request_info);
	} else if (strcmp(request_info->uri, "/remote/changeModuleModi") == 0) {
		process_changeModuleModi(conn, request_info);
	} else if (strcmp(request_info->uri, "/remote/makeEquipletSafe") == 0) {
		process_makeEquipletSafe(conn, request_info);
	} else {
		processed = 0;
	}

	return processed;
}

int main(int argc, char** argv) {
	mg_context* ctx;
	mg_callbacks callbacks;

	//Intialize default values
	std::string documentRoot = ".";

	//Read command line vars
	for (int i = 0; i < argc; i++) {
		std::string arg = argv[i];
		if (arg == "--help") {
			show_usage(argv[0]);
			return 0;
		} else if (arg == "--root") {
			if (i + 1 < argc) {
				documentRoot = argv[i++];
			} else {
				std::cerr << "--root required one argument" << std::endl;
				return -1;
			}
		}
	}

	//Initialize ROS
	ros::init(argc, argv, "equiplet_scada");

	/* Default options for the HTTP server */
	const char *options[] = { "document_root", documentRoot.c_str(),
			"listening_ports", "8081", "num_threads", "5", NULL };

	// Setup mongoose callbacks
	memset(&callbacks, 0, sizeof(callbacks));
	callbacks.begin_request = begin_request_handler;

	//Start mongoose
	ctx = mg_start(&callbacks, NULL, options);
	if (ctx == NULL) {
		std::cerr << "Could not start HTTP Server on port 8081" << std::endl;
		return -1;
	}

	//Start ROS
	ros::spin();

	mg_stop(ctx);

	return 0;
}
