package rexos.mas.equiplet_scada_agent.httpserver;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import rexos.mas.equiplet_scada_agent.EquipletScada;

public class ScadaServer extends Server {
	EquipletScada scada;

	public ScadaServer(EquipletScada scada) {
		super(8081);

		this.scada = scada;

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { "index.html" });

		resource_handler.setResourceBase("./src/www/mas/scada_web/");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler,
				new EquipletInfoHandler(scada), new ModuleInfoHandler(scada),
				new MakeEquipletSafeHandler(scada),
				new ChangeModuleModiHandler(scada), new DefaultHandler() });

		setHandler(handlers);
	}
}
