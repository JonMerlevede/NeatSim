package neatsim.experiments.sim.vehicles.junk;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Set;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Depot;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.VehicleDTO;

import com.google.common.collect.Iterables;

/**
 * A gendreau vehicle provides a base for easy implementation of vehicles
 * satisfying the Gendreau problem requirements.
 * 
 * Users of this class have to implement the {@link #action(TimeLapse)} method.
 * This method describes the behaviour of the Gendreau
 * vehicle. Instead of being time-based like {@link Vehicle#tick(TimeLapse)},
 * it is event-based so that it should be impossible to divert from a target
 * once committed to it. 
 * 
 * Implementations of this class should use pickUp() and dropOff() to respectively 
 * pick up and drop off packages.
 * 
 * @author Jonathan
 *
 */
public abstract class GendreauVehicle extends DefaultVehicle {
	protected boolean loading = false;
	protected Point targetPoint;
	protected Parcel targetParcel;
	
	private boolean pickingUp = false;
	private boolean droppingOff = false;
	
//	private final Collection<Parcel> availablePickUpParcels;
//	private final Collection<Parcel> availableDropOffParcels;
	
	
	public GendreauVehicle(VehicleDTO pDto) {
		super(pDto);
		checkNotNull(pDto);
//		availablePickUpParcels = new ArrayList<>();
//		availableDropOffParcels = new ArrayList<>();
//		pois = new HashMap<>();
	}
	
	@Override
	protected void tickImpl(TimeLapse time) {
		checkNotNull(time);
		checkState(targetParcel != null
				|| (targetParcel == null && targetPoint == null));
		if (pickingUp && !pdpModel.containerContains(this, targetParcel))
			return;
		if (droppingOff && pdpModel.getParcelState(targetParcel) != ParcelState.DELIVERED)
			return;
		if (pickingUp || droppingOff) {
			pickingUp = false;
			droppingOff = false;
			targetParcel = null;
			targetPoint = null;
		}
		
		final Point myPosPoint = roadModel.getPosition(this);
		if (targetPoint != null) {
			if (!targetPoint.equals(myPosPoint)) {
				roadModel.moveTo(this, targetPoint, time);
				return;
			}
		}
//		updatePois(time);
		action(time);
	}
	
	public boolean canDropOff(Parcel parcel, long time) {
		return parcel != null
				&& pdpModel.containerContains(this, parcel)
				&& pdpModel.getParcelState(parcel) == ParcelState.IN_CARGO
				&& pdpModel.getTimeWindowPolicy().canDeliver(
						parcel.getDeliveryTimeWindow(),
						time,
						parcel.getDeliveryDuration());
	}
	
	public boolean canPickup(Parcel parcel, long time) {
		return parcel != null
				&& pdpModel.getParcelState(parcel) == ParcelState.AVAILABLE
				&& roadModel.containsObject(parcel)
				&& pdpModel.getTimeWindowPolicy().canPickup(
						parcel.getPickupTimeWindow(),
						time,
						parcel.getPickupDuration());
	}
	
	/**
	 * Precondition: canPickup()
	 * @param parcel
	 * @param time
	 */
	public void pickUp(Parcel parcel, TimeLapse time) {
		checkArgument(canPickup(parcel, time.getTime()), "Pre: canPickup(parcel,time.getTime()) == true");
		pickingUp = true;
		targetParcel = parcel;
		targetPoint = getParcelPoi(parcel);
		pdpModel.pickup(this, parcel, time);
	}
	
	/**
	 * Precondition: canDeliver()
	 * @param parcel
	 * @param time
	 */
	public void dropOff(Parcel parcel, TimeLapse time) {
		checkArgument(canDropOff(parcel, time.getTime()), "Pre: canDeliver(parcel,getTime()) == true");
		droppingOff = true;
		targetParcel = parcel;
		targetPoint = getParcelPoi(parcel);
		pdpModel.deliver(this, parcel, time);
	}
	
	/**
	 * let point be getPoisMap().get(parcel)
	 * @param parcel
	 * @param time
	 */
	public void moveTo(Parcel parcel, TimeLapse time) {
		checkArgument(parcel != null);
		checkArgument(time != null);
		Point point = getParcelPoi(parcel);
		checkState(targetParcel == null || parcel == targetParcel,
				"Pre: can't change targets after committing.");
		checkState(targetPoint == null || point == targetPoint,
				"Pre: can't change targets after committing.");
		checkState(!loading,
				"Pre: can't move while loading.");
		targetPoint = point;
		targetParcel = parcel;
		roadModel.moveTo(this, point, time);
	}
	
	public void moveTo(Depot depot, TimeLapse time) {
		checkArgument(depot != null);
		checkArgument(time != null);
		Point point = roadModel.getPosition(depot);
		roadModel.moveTo(this, point, time);
	}
	
	public abstract void action(TimeLapse time);

	public Collection<Parcel> getAvailablePickUpParcels() {
		return pdpModel.getAvailableParcels();
		//return Collections.unmodifiableCollection(availablePickUpParcels);
	}

	public Collection<Parcel> getAvailableDropOffParcels() {
		return pdpModel.getContents(this);
	}
	
	public Iterable<Parcel> getAvailableParcels() {
		return Iterables.concat(getAvailablePickUpParcels(),getAvailableDropOffParcels());
//		return Collections.unmodifiableCollection(pois.keySet());
	}
	
	public int getAvailableParcelsSize() {
		return getAvailablePickUpParcels().size() + getAvailableDropOffParcels().size();
	}

	public Depot getClosestDepot() {
		Set<Depot> depots = roadModel.getObjectsOfType(Depot.class);
		Point myPosition = roadModel.getPosition(this);
		Depot closestDepot = null;
		double distance = Double.MAX_VALUE;
		for (Depot depot : depots) {
			double d = Point.distance(myPosition, roadModel.getPosition(depot));
			if (d < distance)
				distance = d; closestDepot = depot;
		}
		assert closestDepot != null;
		return closestDepot;
	}
	
	public Point getParcelPoi(Parcel parcel) {
		switch (pdpModel.getParcelState(parcel)) {
		case ANNOUNCED:
		case AVAILABLE:
		case PICKING_UP:
			return roadModel.getPosition(parcel);
		case IN_CARGO:
		case DELIVERING:
			return parcel.getDestination();
		case DELIVERED:
			return null;
		}
		throw new IllegalArgumentException("Uncovered case. This is only here because of compiler stupidity.");
	}
	
//	public Map<Parcel, Point> getPoisMap() {
//		return Collections.unmodifiableMap(pois);
//	}
}
