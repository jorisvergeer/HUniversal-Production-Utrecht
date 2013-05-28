package rexos.mas.equiplet_scada_agent.httpserver;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import rexos.mas.equiplet_scada_agent.EquipletScada;
import rexos.mas.equiplet_scada_agent.ModuleInfo;

public class ModuleInfoHandler extends
		org.eclipse.jetty.server.handler.AbstractHandler {

	private EquipletScada scada;

	public ModuleInfoHandler(EquipletScada scada) {
		this.scada = scada;
	}

	@Override
	public void handle(String arg0, Request arg1, HttpServletRequest arg2,
			HttpServletResponse arg3) throws IOException, ServletException {
		if (arg0.equals("/remote/moduleInfo")) {

			List<ModuleInfo> moduleInfos = scada.getModuleInfos();

			JsonArray jsonArray = new JsonArray();
			for (ModuleInfo modueInfo : moduleInfos) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", modueInfo.id);
				jsonObject.addProperty("modi", modueInfo.modi);
				jsonObject.addProperty("state", modueInfo.state);
				jsonArray.add(jsonObject);
			}
			JsonObject result = new JsonObject();
			result.add("modules", jsonArray);
			arg3.getWriter().write(result.toString());

			arg1.setHandled(true);
		} else {
			arg1.setHandled(false);
		}
	}

}
