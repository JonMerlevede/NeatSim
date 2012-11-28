package neatsim.experiments.sim.vehicles;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Checksum;

import org.apache.commons.logging.impl.AvalonLogger;

import neatsim.experiments.sim.CoordinationModel;
import neatsim.experiments.sim.CoordinationUser;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.VehicleDTO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public abstract class GendreauVehicle extends DefaultVehicle implements CoordinationUser {

	protected CoordinationModel coordinationModel;
	protected boolean loading = false;
	protected Point targetPoint;
	protected Parcel targetParcel;
	
	private boolean pickingUp = false;
	private boolean droppingOff = false;
	
	private final Collection<Parcel> availablePickUpParcels;
	private final Collection<Parcel> availableDropOffParcels;
	private final Map<Parcel, Point> pois;
	
	
	public GendreauVehicle(VehicleDTO pDto) {
		super(pDto);
		checkNotNull(pDto);
		availablePickUpParcels = new ArrayList<>();
		availableDropOffParcels = new ArrayList<>();
		pois = new HashMap<>();
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		checkNotNull(pRoadModel);
		checkNotNull(pPdpModel);
		super.initRoadPDP(pRoadModel, pPdpModel);
		// ...
	}
	
	@Override
	public void initCoordinationUser(CoordinationModel coordinationModel) {
		checkNotNull(coordinationModel);
		this.coordinationModel = coordinationModel;
	}
	
	@Override
	protected void tickImpl(TimeLapse time) {
		checkNotNull(time);
		if (pickingUp && !pdpModel.containerContains(this, targetParcel))
			return;
		if (droppingOff && pdpModel.getParcelState(targetParcel) != ParcelState.DELIVERED)
			return;
		if (pickingUp || droppingOff) {
			pickingUp = false;
			droppingOff = false;
			coordinationModel.removeTarget(targetParcel);
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
		updatePois(time);
		action(time);
	}
	
	protected boolean canDropOff(Parcel parcel, TimeLapse time) {
		return parcel != null
				&& time != null
				&& pdpModel.containerContains(this, parcel)
				&& pois.containsKey(parcel)
				&& pdpModel.getTimeWindowPolicy().canDeliver(
						parcel.getDeliveryTimeWindow(),
						time.getTime(),
						parcel.getDeliveryDuration());
	}
	
	protected boolean canPickup(Parcel parcel, TimeLapse time) {
		return parcel != null
				&& time != null
				&& pois.containsKey(parcel)
				&& roadModel.containsObject(parcel)
				&& pdpModel.getTimeWindowPolicy().canPickup(
						parcel.getPickupTimeWindow(),
						time.getTime(),
						parcel.getPickupDuration());
	}
	
	protected boolean canMoveTo(Point point, TimeLapse time) {
		return point != null
				&& time != null;
	}
	
	/**
	 * Precondition: canPickup()
	 * @param parcel
	 * @param time
	 */
	public void pickUp(Parcel parcel, TimeLapse time) {
		checkArgument(canPickup(parcel, time), "Pre: canPickup(parcel,time) == true");
		pickingUp = true;
		targetParcel = parcel;
		targetPoint = getPoisMap().get(parcel);
		pdpModel.pickup(this, parcel, time);
	}
	
	/**
	 * Precondition: canDeliver()
	 * @param parcel
	 * @param time
	 */
	public void dropOff(Parcel parcel, TimeLapse time) {
		checkArgument(canDropOff(parcel, time), "Pre: canDeliver(parcel,time) == true");
		droppingOff = true;
		targetParcel = parcel;
		targetPoint = getPoisMap().get(parcel);
		pdpModel.deliver(this, parcel, time);
	}
	
	/**
	 * let point be getPoisMap().get(parcel)
	 * @param parcel
	 * @param time
	 */
	public void moveTo(Parcel parcel, TimeLapse time) {
		checkArgument(getPoisMap().containsKey(parcel), "Pre: getPoinsMap().containsKey(parcel)");
		Point point = getPoisMap().get(parcel);
		checkArgument(canMoveTo(point, time), "Pre: canMoveTo(point,time) == true");
		checkState(targetParcel == null, "Pre: can't change targets after committing.");
		checkState(targetPoint == null, "Pre: can't change committing.");
		checkState(!loading, "Pre: can't move while loading.");
		targetPoint = point;
		targetParcel = parcel;
		coordinationModel.addTarget(parcel);
		roadModel.moveTo(this, point, time);
	}
	
	private void updatePois(TimeLapse time) {
		checkNotNull(time);
		pois.clear();
		availablePickUpParcels.clear();
		availableDropOffParcels.clear();
		// Update pickup parcels
		final Collection<Parcel> parcels = pdpModel.getAvailableParcels();
		for (Parcel parcel : parcels) {
			if (roadModel.containsObject(parcel))
				addPickUpParcel(parcel);
		}
		// Update dropoff parcels
		for (Parcel parcel : pdpModel.getContents(this))
			addDropOffParcel(parcel);
	}
	
	private void addPickUpParcel(Parcel parcel) {
		checkNotNull(parcel);
		checkNotNull(roadModel.getPosition(parcel));
		availablePickUpParcels.add(parcel);
		pois.put(parcel, roadModel.getPosition(parcel));
	}
	
	private void addDropOffParcel(Parcel parcel) {
		checkNotNull(parcel);
		checkNotNull(parcel.getDestination());
		availableDropOffParcels.add(parcel);
		pois.put(parcel, parcel.getDestination());
	}
	

	
	public abstract void action(TimeLapse time);

	public Collection<Parcel> getAvailablePickUpParcels() {
		return Collections.unmodifiableCollection(availablePickUpParcels);
	}

	public Collection<Parcel> getAvailableDropOffParcels() {
		return Collections.unmodifiableCollection(availableDropOffParcels);
	}
	
	public Collection<Parcel> getAvailableParcels() {
		return Collections.unmodifiableCollection(pois.keySet());
	}

	public Map<Parcel, Point> getPoisMap() {
		return Collections.unmodifiableMap(pois);
	}
}
