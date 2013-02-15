package neatsim.experiments.sim;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.HashSet;

import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class CoordinationModel implements Model<CoordinationUser>, ModelReceiver, TickListener{
	private RoadModel roadModel;
	private final HashSet<CoordinationUser> users;
	private final HashSet<Parcel> usedPoints;
	private final HashMap<Point,CoordinationUser> closestUsers;
	
	public CoordinationModel() {
		users = new HashSet<>();
		usedPoints = new HashSet<>();
		closestUsers = new HashMap<>();
	}
	
	@Override
	public boolean register(CoordinationUser element) {
		checkNotNull(element);
		element.initCoordinationUser(this);
		return users.add(element);
	}

	@Override
	public boolean unregister(CoordinationUser element) {
		checkNotNull(element);
		return users.remove(element);
	}
	
	public CoordinationUser getUserClosestTo(final Point point) {
		checkNotNull(point);
		checkState(users.size() > 0);
		if (closestUsers.containsKey(point))
			return closestUsers.get(point);
		double smallestDistance = Double.MAX_VALUE;
		CoordinationUser closestUser = null;
		for (final CoordinationUser newUser : users) {
			final Point newUserLocation = roadModel.getPosition(newUser);
			final double d = Point.distance(newUserLocation, point);
			if (d < smallestDistance) {
				closestUser = newUser;
				smallestDistance = d;
			}
		}
		closestUsers.put(point, closestUser);
		if (closestUser == null)
			throw new RuntimeException("Meuh");
		return closestUser;
	}
	
	public void addTarget(Parcel parcel) {
		checkNotNull(parcel);
		usedPoints.add(parcel);
	}
	
	public void removeTarget(Parcel parcel) {
		checkNotNull(parcel);
		usedPoints.remove(parcel);
	}
	
	public boolean isTarget(Parcel point) {
		checkNotNull(point);
		return usedPoints.contains(point);
	}
	
	@Override
	public Class<CoordinationUser> getSupportedType() {
		return CoordinationUser.class;
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		roadModel = mp.getModel(RoadModel.class);
	}

	@Override
	public void tick(TimeLapse timeLapse) {

	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		closestUsers.clear();
	}
}
