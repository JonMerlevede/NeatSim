package neatsim.experiments.sim.vehicles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;

public class AverageVehicle extends GendreauVehicle {
	public AverageVehicle(AddVehicleEvent event) {
		super(event.vehicleDTO);
	}
	
	@Override
	public void action(final TimeLapse time) {
		final Point myPosPoint = roadModel.getPosition(this);
		// Package destinations are not in the road model... Because of this we need to
		// differentiate between the calculation of the distance to a pick-up or a drop-off
		// point. We use this HashMap as a cleaner solution.
		Parcel currentParcel = getClosestParcel(time);
		
		if (currentParcel != null) {
			// If we're at the POI, pick it up
			if (myPosPoint.equals(getPoisMap().get(currentParcel))) {
				if (canDropOff(currentParcel, time))
					dropOff(currentParcel, time);
				if (canPickup(currentParcel, time))
					pickUp(currentParcel, time);
			// If we're not at the POI, move towards the parcel
			} else
				moveTo(currentParcel, time);
			// If our timelapse is not depleted yet but we might still have more stuff to do, do more stuff
			if (time.hasTimeLeft())
				tickImpl(time);
		}	
	}
	

	private boolean isPickedBySomeoneElse(Parcel pa) {
		return coordinationModel.isTarget(pa) && pa != targetParcel;
	}
	
	private Parcel getClosestParcel(TimeLapse time) {
		Parcel currentParcel = null;
		double dist = Double.POSITIVE_INFINITY;
		final Point myPosPoint = roadModel.getPosition(this);
		for (Parcel pa : getAvailableDropOffParcels()) {
			if (!canDropOff(pa, time))
				continue;
			
			final Point po = getPoisMap().get(pa);
			final double di = Point.distance(myPosPoint, po);
			if (di < dist) {
				currentParcel = pa;
				dist = di;
			}
		}
		for (Parcel pa : getAvailablePickUpParcels()) {
			
			final Point po = getPoisMap().get(pa);
			if (isPickedBySomeoneElse(pa)
					|| !canPickup(pa, time)
					|| coordinationModel.getUserClosestTo(po) != this)
				continue;
			
			final double di = Point.distance(myPosPoint, po);
			if (di < dist) {
				currentParcel = pa;
				dist = di;
			}
		}
		return currentParcel;
	}
}