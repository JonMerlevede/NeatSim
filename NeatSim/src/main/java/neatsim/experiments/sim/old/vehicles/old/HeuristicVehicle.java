package neatsim.experiments.sim.vehicles.junk;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;

import com.google.common.base.Preconditions;

import neatsim.core.BlackBox;
import neatsim.experiments.sim.CoordinationModel;
import neatsim.experiments.sim.CoordinationUser;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;

public class HeuristicVehicle extends GendreauVehicle implements CoordinationUser {
	protected CoordinationModel coordinationModel;
	protected BlackBox heuristic;
	
	public HeuristicVehicle(AddVehicleEvent event, BlackBox heuristic) {
		super(event.vehicleDTO);
		this.heuristic = heuristic;
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
		
		if (targetPoint != null) {
			// We should be at the target
			checkState(myPosPoint.equals(targetPoint));
			System.out.println("moo");
			if (canDropOff(targetParcel, time.getTime()))
				dropOff(targetParcel, time);
			if (canPickup(targetParcel, time.getTime()))
				pickUp(targetParcel, time);
		} else {
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
			} else if (getAvailableParcelsSize() == 0) {
				moveTo(getClosestDepot(), time);
			}
		}
	}
	

	private boolean isPickedBySomeoneElse(Parcel pa) {
		return coordinationModel.isTarget(pa) && pa != targetParcel;
	}
	
	
	private double ado(Parcel of) {
		Collection<Parcel> contents = pdpModel.getContents(this);
		if (contents.isEmpty())
			return 0d;
		double d = 0d;
		Point ofd = getParcelPoi(of);
		for (final Parcel p : contents) {
			d += Point.distance(ofd, p.getDestination());
		}
		return d;
	}
	
	private double mido(Parcel of) {
		Collection<Parcel> contents = pdpModel.getContents(this);
		if (contents.isEmpty())
			return 0d;
		double d = Double.POSITIVE_INFINITY;
		Point ofd = getParcelPoi(of);
		for (final Parcel p : contents) {
			Double d2 = Point.distance(ofd, p.getDestination());
			if (d2 < d)
				d = d2;
		}
		return d;
	}
	
	private double mado(Parcel of) {
		Collection<Parcel> contents = pdpModel.getContents(this);
		double d = 0d;
		Point ofd = getParcelPoi(of);
		for (final Parcel p : contents) {
			Double d2 = Point.distance(ofd, p.getDestination());
			if (d2 > d)
				d = d2;
		}
		return d;
	}
	
	private double dist(Parcel of) {
		if (pdpModel.containerContains(this, of))
			return 0d;
		else {
			Point myLocation = roadModel.getPosition(this);
			Point packetLocation = roadModel.getPosition(of);
			return Point.distance(myLocation, packetLocation);
		}
	}
	
	private double urge(Parcel of, TimeLapse time) {
		if (pdpModel.containerContains(this, of))
			return of.getDeliveryTimeWindow().end - time.getTime();
		else
			return of.getPickupTimeWindow().end - time.getTime();
	}
	
	private double est(Parcel of, TimeLapse time) {
		if (pdpModel.containerContains(this, of))
			return of.getDeliveryTimeWindow().begin - time.getTime();
		else
			return of.getPickupTimeWindow().begin - time.getTime();
	}
	
	private double ttl(TimeLapse time) {
		return getDTO().availabilityTimeWindow.end - time.getTime();
	}
	
	private double evaluateHeuristic(TimeLapse time, Parcel pa) {
		heuristic.reset();
		heuristic.setInput(0, ado(pa));
		heuristic.setInput(1, mido(pa));
		heuristic.setInput(2, mado(pa));
		heuristic.setInput(3, dist(pa));
		heuristic.setInput(4, urge(pa, time));
		heuristic.setInput(5, est(pa, time));
		heuristic.setInput(6, ttl(time));
		return heuristic.getOutput(0);
	}
	
	private Parcel getClosestParcel(TimeLapse time) {
		Parcel currentParcel = null;
		double heur =  Double.POSITIVE_INFINITY;
		for (Parcel pa : getAvailableDropOffParcels()) {
			if (!canDropOff(pa, time.getTime()))
				continue;
			double h;
			try {
				h = evaluateHeuristic(time,pa);
			} catch (IllegalArgumentException e) {
				h = Double.POSITIVE_INFINITY;
			}
			if (h < heur) {
				currentParcel = pa;
				heur = h;
			}
		}
		for (Parcel pa : getAvailablePickUpParcels()) {
			if (isPickedBySomeoneElse(pa)
					|| !canPickup(pa, time.getTime()))
				continue;
			double h;
			try {
				h = evaluateHeuristic(time,pa);
			} catch (IllegalArgumentException e){
				h = Double.POSITIVE_INFINITY;
			}
			if (h < heur) {
				currentParcel = pa;
				heur = h;
			}
		}
		// Make sure simulation ends even in the case of BS heuristic
		if (currentParcel == null && this.getAvailableParcelsSize() > 0)
			currentParcel = this.getAvailableParcels().iterator().next();
		return currentParcel;
	}
}