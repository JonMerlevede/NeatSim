package neatsim.experiments.sim;

import rinde.sim.core.model.road.RoadUser;

public interface CoordinationUser extends RoadUser {
	void initCoordinationUser(CoordinationModel coordinationModel);
}
