package rexos.mas.equiplet_scada_agent.model;

import java.util.List;

public class Equiplet {
	private int id;
	private EMostState safety;
	private EMostState operational;
	private List<Module> modules;
}