package rexos.mas.equiplet_scada_agent;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MOSTDBClient {

	private Mongo mongo;
	private DB db;
	private DBCollection equiplet;
	private DBCollection modules;

	public MOSTDBClient() {
		try {
			mongo = new Mongo("145.89.131.191");
			db = mongo.getDB("most");
			equiplet = db.getCollection("equiplet");
			modules = db.getCollection("modules");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public List<ModuleInfo> getModules() {
		DBCursor cursor = modules.find();

		ArrayList<ModuleInfo> result = new ArrayList<ModuleInfo>();

		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			ModuleInfo module = new ModuleInfo();
			module.id = (int) obj.get("id");
			module.state = (int) obj.get("state");
			module.modi = (int) obj.get("modi");
			result.add(module);
		}

		return result;
	}

	public EquipletInfo getEquiplet() {
		DBCursor cursor = modules.find();

		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			EquipletInfo equiplet = new EquipletInfo();
			equiplet.safety = (int) obj.get("safety");
			return equiplet;
		}

		return null;
	}
}
