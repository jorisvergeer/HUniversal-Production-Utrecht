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
import rexos.mas.equiplet_scada_agent.MOSTDBClient.MostDbClientException;
import rexos.mas.equiplet_scada_agent.ModuleInfo;

public class ChangeModuleModiHandler extends
		org.eclipse.jetty.server.handler.AbstractHandler {

	private EquipletScada scada;

	public ChangeModuleModiHandler(EquipletScada scada) {
		this.scada = scada;
	}

	@Override
	public void handle(String arg0, Request arg1, HttpServletRequest arg2,
			HttpServletResponse arg3) throws IOException, ServletException {
		if (arg0.equals("/remote/changeModuleModi")) {

			try {
				scada.getMOSTDBClient().callEquipletCommand("changeModuleModi", arg2.getParameter("id"), arg2.getParameter("modi"));
			} catch (MostDbClientException e) {
				e.printStackTrace();
			}

			arg1.setHandled(true);
		} else {
			arg1.setHandled(false);
		}
	}

}
