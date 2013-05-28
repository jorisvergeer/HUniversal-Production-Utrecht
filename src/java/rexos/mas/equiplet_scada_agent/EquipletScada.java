/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rexos.mas.equiplet_scada_agent;

import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.MongoOperation;
import rexos.libraries.blackboard_client.OplogEntry;
import rexos.libraries.knowledgedb_client.KeyNotFoundException;
import rexos.libraries.knowledgedb_client.KnowledgeDBClient;
import rexos.libraries.knowledgedb_client.KnowledgeException;
import rexos.libraries.knowledgedb_client.Row;
import rexos.mas.equiplet_scada_agent.MOSTDBClient.MostDbClientException;
import rexos.mas.equiplet_scada_agent.behaviours.UpdateModulesBehaviour;
import rexos.mas.equiplet_scada_agent.httpserver.ScadaServer;
import rexos.mas.equiplet_scada_agent.interfaces.UpdateModulesListener;

/**
 * 
 * @author joris
 */
public class EquipletScada extends Agent implements UpdateModulesListener, BlackboardSubscriber {
	static final long serialVersionUID = 1L;
	int currentEquipletId = -1;
	private String equipletName;

	AID equipletAgentAID;
	AID serviceAgentAID;
	AID hardwareAgentAID;

	MOSTDBClient mostDbClient;
	KnowledgeDBClient kDbClient;

	ScadaServer server;
	private List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
	private EquipletInfo equipletInfo = new EquipletInfo();

	public String getEquipletName() {
		return equipletName;
	}

	public int getEquipletId() {
		return currentEquipletId;
	}

	public String getEquipletSafetyState() {
		return "TODO";
	}

	public String getEquipletOperationalState() {
		return "TODO";
	}

	@Override
	protected void setup() {
		try {
			mostDbClient = new MOSTDBClient(this);
			mostDbClient.subscribe(this);
			kDbClient = KnowledgeDBClient.getClient();
		} catch (KnowledgeException | MostDbClientException e) {
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

		equipletName = (String) args[0];
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
			currentEquipletId = (int) results[0].get("id");
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
				int mod_id = (int) result.get("id");
				int mod_type = (int) result.get("type");
				String mod_name = (String) result.get("name");
			}
		} catch (KnowledgeException | KeyNotFoundException e) {
			e.printStackTrace();
			doDelete();
			return;
		}

		addBehaviour(new UpdateModulesBehaviour(this, this));

		server = new ScadaServer(this);
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
		updateModules();
	}
	
	private void updateModules(){
		try {
			setModuleInfos(mostDbClient.getModules());
		} catch (MostDbClientException e) {
			e.printStackTrace();
		}
	}
	
	private void updateEquiplet(){
		try {
			setEquipletInfo(mostDbClient.getEquiplet());
		} catch (MostDbClientException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		updateModules();
		updateEquiplet();
	}

	public List<ModuleInfo> getModuleInfos() {
		return moduleInfos;
	}

	public void setModuleInfos(List<ModuleInfo> moduleInfos) {
		this.moduleInfos = moduleInfos;
	}

	public EquipletInfo getEquipletInfo() {
		return equipletInfo;
	}

	public void setEquipletInfo(EquipletInfo equipletInfo) {
		this.equipletInfo = equipletInfo;
	}
}
