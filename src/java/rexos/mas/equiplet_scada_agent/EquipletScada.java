/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rexos.mas.equiplet_scada_agent;

import java.util.List;

import javax.swing.SwingUtilities;

import rexos.libraries.knowledgedb_client.KeyNotFoundException;
import rexos.libraries.knowledgedb_client.KnowledgeDBClient;
import rexos.libraries.knowledgedb_client.KnowledgeException;
import rexos.libraries.knowledgedb_client.Row;
import rexos.mas.equiplet_scada_agent.behaviours.UpdateModulesBehaviour;
import rexos.mas.equiplet_scada_agent.httpserver.ScadaServer;
import rexos.mas.equiplet_scada_agent.interfaces.UpdateModulesListener;
import rexos.mas.equiplet_scada_agent.window.ScadaWindow;
import jade.core.AID;
import jade.core.Agent;

/**
 * 
 * @author joris
 */
public class EquipletScada extends Agent implements UpdateModulesListener {
	static final long serialVersionUID = 1L;
	int currentEquipletId = -1;

	AID equipletAgentAID;
	AID serviceAgentAID;
	AID hardwareAgentAID;

	MOSTDBClient mostDbClient;
	KnowledgeDBClient kDbClient;
	
	ScadaServer server;

	@Override
	protected void setup() {
		mostDbClient = new MOSTDBClient();
		try {
			kDbClient = KnowledgeDBClient.getClient();
		} catch (KnowledgeException e) {
			e.printStackTrace();
			doDelete();
			return;
		}

		Object[] args = getArguments();
		if (args.length != 1) {
			System.out.println("Not the right amount of arguments are given");
			doDelete();
			return;
		}

		String equipletName = (String) args[0];
		equipletAgentAID = new AID(equipletName, AID.ISLOCALNAME);
		serviceAgentAID = new AID(equipletName + "-serviceAgent",
				AID.ISLOCALNAME);
		hardwareAgentAID = new AID(equipletName + "-hardwareAgent",
				AID.ISLOCALNAME);

		try {
			String sql_get_quiplet_id = "SELECT id FROM equiplets WHERE jade_address = '"
					+ equipletName + "'";
			Row[] results = kDbClient.executeSelectQuery(sql_get_quiplet_id);
			if (results.length != 1) {
				System.out
						.println("Could not find the right equiplet in the knowledge database");
				doDelete();
				return;
			}
			currentEquipletId = results[0].getInt("id");
		} catch (KnowledgeException | KeyNotFoundException e) {
			e.printStackTrace();
			doDelete();
			return;
		}

		try {
			String modules_per_equiplet = "SELECT modules.id AS id, "
					+ "modules.module_type AS type, "
					+ "module_types.name AS name "
					+ "FROM modules, module_types "
					+ "WHERE modules.module_type = module_types.id "
					+ "AND modules.location = " + currentEquipletId;

			Row[] results = kDbClient.executeSelectQuery(modules_per_equiplet);
			for (Row result : results) {
				int mod_id = result.getInt("id");
				int mod_type = result.getInt("type");
				String mod_name = result.getString("name");
			}
		} catch (KnowledgeException | KeyNotFoundException e) {
			e.printStackTrace();
			doDelete();
			return;
		}

		addBehaviour(new UpdateModulesBehaviour(this, this));
		
		server = new ScadaServer();
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void takeDown() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onModuleUpdateRequested(AID sender) {
		List<ModuleInfo> modules = mostDbClient.getModules();
	}
}
