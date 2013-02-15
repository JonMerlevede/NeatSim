package neatsim.experiments.sim.vehicles.junk;

import static com.google.common.base.Preconditions.checkNotNull;
import neatsim.experiments.sim.CoordinationModel;
import neatsim.experiments.sim.CoordinationUser;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;

public class PresentationVehicle extends GendreauVehicle implements CoordinationUser {
	protected CoordinationModel coordinationModel;
	
	public PresentationVehicle(AddVehicleEvent event) {
		super(event.vehicleDTO);
	}
	
	@Override
	public void initCoordinationUser(CoordinationModel coordinationModel) {
		checkNotNull(coordinationModel);
		this.coordinationModel = coordinationModel;
	}
	
	@Override
	public void action(final TimeLapse time) {
		if (targetParcel != null)
			this.coordinationModel.removeTarget(targetParcel);
		
		final Point myPosPoint = roadModel.getPosition(this);
		// Package destinations are not in the road model... Because of this we need to
		// differentiate between the calculation of the distance to a pick-up or a drop-off
		// point. We use this HashMap as a cleaner solution.
		Parcel currentParcel = getClosestParcel(time);
		
		if (currentParcel != null) {
			// If we're at the POI, pick it up
			if (myPosPoint.equals(getParcelPoi(currentParcel))) {
				if (canDropOff(currentParcel, time.getTime()))
					dropOff(currentParcel, time);
				if (canPickup(currentParcel, time.getTime()))
					pickUp(currentParcel, time);
			// If we're not at the POI, move towards the parcel
			} else {
				moveTo(currentParcel, time);
				this.coordinationModel.addTarget(targetParcel);
			}
			// If our timelapse is not depleted yet but we might still have more stuff to do, do more stuff
			if (time.hasTimeLeft())
				tickImpl(time);
		} else if (time.hasTimeLeft()
				&& time.getTime() > dto.availabilityTimeWindow.end -
					((Point.distance(
							roadModel.getPosition(this),
							dto.startPosition
					) / getSpeed())	* 3600000)) {
			// there is nothing left to do and the end of the day is
			// approaching so we are going back to base
			roadModel.moveTo(this, dto.startPosition, time);
		}
	}
	

//	private boolean isPickedBySomeoneElse(Parcel pa) {
//		return coordinationModel.isTarget(pa) && pa != targetParcel;
//	}
	
	private Parcel getClosestParcel(TimeLapse time) {
		Parcel currentParcel = null;
		double dist = Double.POSITIVE_INFINITY;
		final Point myPosPoint = roadModel.getPosition(this);
		for (Parcel pa : getAvailableDropOffParcels()) {
			if (!canDropOff(pa, time.getTime()))
				continue;
			
			final Point po = getParcelPoi(pa);
			final double di = Point.distance(myPosPoint, po);
			if (di < dist) {
				currentParcel = pa;
				dist = di;
			}
		}
		for (Parcel pa : getAvailablePickUpParcels()) {
			final Point po = getParcelPoi(pa);
			if (!canPickup(pa, time.getTime()) 
					//||isPickedBySomeoneElse(pa)
					)
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