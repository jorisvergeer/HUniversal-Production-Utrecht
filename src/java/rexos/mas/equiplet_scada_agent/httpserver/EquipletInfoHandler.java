package rexos.mas.equiplet_scada_agent.httpserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.google.gson.JsonObject;

import rexos.mas.equiplet_scada_agent.EquipletScada;

public class EquipletInfoHandler extends
		org.eclipse.jetty.server.handler.AbstractHandler {

	private EquipletScada scada;

	public EquipletInfoHandler(EquipletScada scada) {
		this.scada = scada;
	}

	@Override
	public void handle(String arg0, Request arg1, HttpServletRequest arg2,
			HttpServletResponse arg3) throws IOException, ServletException {
		if (arg0.equals("/remote/equipletInfo")) {
			
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", scada.getEquipletName());
			jsonObject.addProperty("id", scada.getEquipletId());
			jsonObject.addProperty("safety", scada.getEquipletSafetyState());
			jsonObject.addProperty("operational", scada.getEquipletOperationalState());
			arg3.getWriter().write(jsonObject.toString());
			
			arg1.setHandled(true);
		} else {
			arg1.setHandled(false);
		}
	}

}
