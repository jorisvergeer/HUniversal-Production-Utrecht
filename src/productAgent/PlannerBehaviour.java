package productAgent;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import newDataClasses.Product;
import newDataClasses.Production;
import newDataClasses.ProductionEquipletMapper;
import newDataClasses.ProductionStep;
import libraries.blackboardJavaClient.src.nl.hu.client.BlackboardClient;
import main.MainAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

@SuppressWarnings("serial")
public class PlannerBehaviour extends CyclicBehaviour {
	private ProductAgent _productAgent;

	public PlannerBehaviour() {
	}

	public void action() {
		_productAgent = (ProductAgent) myAgent;
		try {
			BlackboardClient bbc = new BlackboardClient("145.89.191.131",
					27017);
			bbc.setDatabase("CollectiveDb");
			bbc.setCollection("EquipletDirectory");
			
			Product product = this._productAgent.getProduct();
			Production production = product.getProduction();
			ProductionStep[] psa = production.getProductionSteps();
			
			ProductionEquipletMapper pem = production.getProductionEquipletMapping();
			
			for (ProductionStep ps : psa) {
				long PA_id = ps.getId();
				long PA_capability = ps.getCapability();
				
				DBObject equipletCapabilityQuery = QueryBuilder.start("capabilities").is(PA_capability).get();
				List<DBObject> testData = bbc.findDocuments(equipletCapabilityQuery);
				
				for(DBObject db : testData) {
					String aid = (String)db.get("AID").toString();
					pem.addEquipletToProductionStep(PA_id, new AID(aid, true));
				}
			}
			
			production.setProductionEquipletMapping(pem);
			product.setProduction(production);
			this._productAgent.setProduct(product);

		} catch (Exception e) {
			System.out.println("Exception");
		}
	}
}
