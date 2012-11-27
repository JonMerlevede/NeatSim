package neatsim.experiments.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import rinde.sim.core.model.Model;

public class CoordinationModel implements Model<CoordinationUser>{
	private Collection<CoordinationUser> users;	
	
	public CoordinationModel() {
		users = new HashSet<>();
	}
	
	@Override
	public boolean register(CoordinationUser element) {
		return users.add(element);
	}

	@Override
	public boolean unregister(CoordinationUser element) {
		return users.remove(element);
	}

	@Override
	public Class<CoordinationUser> getSupportedType() {
		return CoordinationUser.class;
	}

}
