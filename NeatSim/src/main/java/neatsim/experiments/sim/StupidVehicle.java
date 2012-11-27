package neatsim.experiments.sim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.AbstractRoadModel.RoadEventType;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultVehicle;

public class StupidVehicle extends DefaultVehicle {
	private boolean loading = false;
	private Point currentTarget = null;
	
	public StupidVehicle(AddVehicleEvent event) {
		super(event.vehicleDTO);
	}
	
	private boolean canDeliver(Parcel parcel, TimeLapse time) {
//		return true;
		return pdpModel.getTimeWindowPolicy().canDeliver(
				parcel.getDeliveryTimeWindow(),
				time.getTime(),
				parcel.getDeliveryDuration());
	}
	private boolean canPickup(Parcel parcel, TimeLapse time) {
		return pdpModel.getTimeWindowPolicy().canPickup(
				parcel.getPickupTimeWindow(),
				time.getTime(),
				parcel.getPickupDuration());
	}
	
	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		super.initRoadPDP(pRoadModel, pPdpModel);
		pdpModel.getEventAPI().addListener(new Listener() {
			@Override
			public void handleEvent(Event e) {
				loading = false;
			}
		}, PDPModelEventType.END_DELIVERY, PDPModelEventType.END_PICKUP);
	}
	
	@Override
	protected void tickImpl(TimeLapse time) {
		// If still busy loading/unloading, do nothing. There is a listener setting this to false.
		if (loading)
			return;
		final Point myPosPoint = roadModel.getPosition(this);
		// It is not possible to change destination while on route.
		if (currentTarget != null) {
			if (currentTarget.equals(myPosPoint))
				roadModel.moveTo(this, myPosPoint, time);
			else
				currentTarget = null;
		}
		
		final Collection<Parcel> parcels = pdpModel.getAvailableParcels();
		// Package destinations are not in the road model... Because of this we need to
		// differentiate between the calculation of the distance to a pick-up or a drop-off
		// point. We use this HashMap as a cleaner solution.
		final HashMap<Parcel, Point> parcelPOIs = new HashMap<>();
		updatePickupPoints(parcels, parcelPOIs);
		updateDropoffPoints(parcels, parcelPOIs, time);
		Parcel currentParcel = getClosestParcel(parcels, parcelPOIs, myPosPoint);
		
		if (currentParcel != null) {
			// If we're at the POI, pick it up
			if (myPosPoint.equals(parcelPOIs.get(currentParcel))) {
				boolean delivering = pdpModel.containerContains(this, currentParcel);
				if (delivering && canDeliver(currentParcel, time)) {
					pdpModel.deliver(this, currentParcel, time);
					loading = true;
				}
				if (!delivering && canPickup(currentParcel, time)) {
					pdpModel.pickup(this, currentParcel, time);
					loading = true;
				}	
			// If we're not at the POI, move towards the parcel
			} else {
				roadModel.moveTo(this, parcelPOIs.get(currentParcel), time);
				currentTarget = parcelPOIs.get(currentParcel);
			}
		}	
		// If our timelapse is not depleted yet but we still have stuff to do, do more stuff
		if (parcels.size() != 0 && time.hasTimeLeft())
			tickImpl(time);
	}
	
	private void updatePickupPoints(Collection<Parcel> parcels, HashMap<Parcel, Point> distances) {
		Iterator<Parcel> iParcels = parcels.iterator();
		while (iParcels.hasNext()) {
			Parcel parcel = iParcels.next();
			if (roadModel.containsObject(parcel))
				distances.put(parcel, roadModel.getPosition(parcel));
			else // I do not know why this can occur!
				iParcels.remove();
		}
	}
	
	private void updateDropoffPoints(Collection<Parcel> parcels, HashMap<Parcel, Point> distances, TimeLapse time) {
		for (Parcel parcel : pdpModel.getContents(this)) {
//			if (roadModel.containsObject(parcel) && canDeliver(parcel, time))
			if (canDeliver(parcel, time)) {
				parcels.add(parcel);
				distances.put(parcel, parcel.getDestination());
			}
		}
	}
	
	private Parcel getClosestParcel(Collection<Parcel> parcels, HashMap<Parcel, Point> distances, Point myPosPoint) {
		Parcel currentParcel = null;
		if (!parcels.isEmpty() && currentParcel == null) {
			double dist = Double.POSITIVE_INFINITY;
			for (final Parcel p : parcels) {
				if (distances.containsKey(p)) {
					final double d = Point.distance(myPosPoint, distances.get(p));
					if (d < dist) {
						dist = d;
						currentParcel = p;
					}
				}
			}
		}
		return currentParcel;
	}
}