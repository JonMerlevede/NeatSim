package neatsim.experiments.sim.vehicles;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import neatsim.core.BlackBox;
import neatsim.experiments.sim.CoordinationModel;
import neatsim.experiments.sim.CoordinationUser;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Depot;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultVehicle;

import com.google.common.collect.Iterables;

/**
 * Vehicle that solves the PDP problem and decides where to drive to based on a
 * user-defined heuristic.
 * 
 * This class is not used nor well-documented because ultimately, I'll have to
 * use the heuristic vehicle that Rinde made for optimal comparability between
 * our results.
 * 
 * @author Jonathan Merlevede
 */
public class HeuristicVehicle extends DefaultVehicle implements CoordinationUser {
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
	protected void tickImpl(TimeLapse time) {
		boolean waiting = false;
		Iterable<Parcel> ip = getAvailableParcels(time.getTime());
		if (isDriving()) {
			driveTo(time);
		} else if (isDoingParcel()) {
			doParcel(time);
		} else if (ip.iterator().hasNext()) {
			Parcel p = bestParcel(time.getTime());
			coordinationModel.addTarget(p);
			doParcel(p,time);
		} else if (
				!dto.startPosition.equals(roadModel.getPosition(this))
				&& time.getTime() > dto.availabilityTimeWindow.end -
					((Point.distance(
							roadModel.getPosition(this),
							dto.startPosition
					) / getSpeed())	* 3600000)) {
			// Drive to depot
//			Depot closest = getClosestDepot();
			driveTo(dto.startPosition,time);
		} else {
			// Wait/do nothing
			waiting = true;
		}
		if (!waiting && time.hasTimeLeft())
			tickImpl(time);
	}
	
	
	protected Depot getClosestDepot() {
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
	
	protected Parcel myParcel;
	protected boolean pickingUp;
	protected boolean delivering;
	
	protected boolean isDoingParcel() {
		return myParcel != null;
	}
	
	protected void doParcel(TimeLapse time) {
		ParcelState ps = pdpModel.getParcelState(myParcel);
		switch (ps) {
		case DELIVERED :
			delivering = false;
			myParcel = null;
			break;
		case IN_CARGO :
			if (pickingUp) {
				myParcel = null;
				pickingUp = false;
				break;
			} else if (!myPosition().equals(myParcel.getDestination())) {
				driveTo(myParcel.getDestination(), time);
				break;
			}
		case DELIVERING :
			if (!delivering) {
				pdpModel.deliver(this, myParcel, time);
				delivering = true;
			}
			break;
		case ANNOUNCED :
		case AVAILABLE :
			if (!myPosition().equals(roadModel.getPosition(myParcel))) {
				driveTo(roadModel.getPosition(myParcel), time);
				break;
			}
		case PICKING_UP :
			if (!pickingUp) {
				pdpModel.pickup(this, myParcel, time);
				pickingUp = true;
			}
			break;
		}
	}
	
	protected void doParcel(Parcel pa, TimeLapse time) {
		myParcel = pa;
		doParcel(time);
	}
	
	
	protected Point myDrivingPoint;
	protected boolean isDriving() {
		return myDrivingPoint != null;
	}
	
	protected void driveTo(TimeLapse time) {
		if (myPosition().equals(myDrivingPoint))
			myDrivingPoint = null;
		else
			roadModel.moveTo(this, myDrivingPoint, time);
	}
	
	protected void driveTo(Point p, TimeLapse time) {
		myDrivingPoint = p;
		driveTo(time);
	}
	
	protected Point myPosition() {
		return roadModel.getPosition(this);
	}
	
	protected Parcel bestParcel(long time) {
		Iterable<Parcel> ip = getAvailableParcels(time);
		checkState(ip.iterator().hasNext());
		double he = Double.POSITIVE_INFINITY;
		Parcel pa = null;
		for (Parcel p : ip) {
			checkNotNull(p);
			double h = evaluateHeuristic(time, p);
			if (h < he) {
				pa = p;
				he = h;
			}
		}
		checkNotNull(pa);
		return pa;
	}
	
	public class AvailablePickupParcelsIterator implements Iterable<Parcel>, Iterator<Parcel> {
		protected final Iterator<Parcel> available;
		private boolean newNext;
		private Parcel next;
		
		public AvailablePickupParcelsIterator() {
			available = pdpModel.getAvailableParcels().iterator();
		}
		
		@Override
		public boolean hasNext() {
			if (newNext)
				return true;
			if (!available.hasNext())
				return false;
			next = available.next();
			if (coordinationModel.isTarget(next))
				return hasNext();
			else {
				newNext = true;
				return true;
			}
		}

		@Override
		public Parcel next() {
			if (hasNext()) {
				newNext = false;
				return next;
			} else
				throw new RuntimeException("Moo");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<Parcel> iterator() {
			return this;
		}
	}
	
	public class AvailableDropoffParcelsIterator implements Iterable<Parcel>, Iterator<Parcel> {
		protected final Iterator<Parcel> available;
		protected final long time;
		private boolean newNext;
		private Parcel next;
		
		public AvailableDropoffParcelsIterator(long time) {
			available = pdpModel.getContents(HeuristicVehicle.this).iterator();
			this.time = time;
		}
		
		@Override
		public boolean hasNext() {
			if (newNext)
				return true;
			if (!available.hasNext())
				return false;
			next = available.next();
			if (!pdpModel.getTimeWindowPolicy().canDeliver(
					next.getDeliveryTimeWindow(),
					time,
					next.getDeliveryDuration()))
				return hasNext();
			else {
				newNext = true;
				return true;
			}
		}

		@Override
		public Parcel next() {
			if (hasNext()) {
				newNext = false;
				return next;
			} else
				throw new RuntimeException("Moo");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<Parcel> iterator() {
			return this;
		}
	}
	
	public Iterable<Parcel> getAvailablePickUpParcels() {
		return new AvailablePickupParcelsIterator();
	}

	public Iterable<Parcel> getAvailableDropOffParcels(long time) {
		return new AvailableDropoffParcelsIterator(time);
	}
	
	public Iterable<Parcel> getAvailableParcels(long time) {
		return Iterables.concat(getAvailablePickUpParcels(),getAvailableDropOffParcels(time));
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

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////// HEURISTICS /////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	
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
	
//	private double newAdo(Parcel of, long time) {
//		Iterable<Parcel> parcels = getAvailableParcels(time);
//		if (!parcels.iterator().hasNext())
//			return 0d;
//		double d = 0d;
//		Point ofd = getParcelPoi(of);
//		for (final Parcel p : parcels) {
//			d += Point.distance(ofd, p.getDestination());
//		}
//		return d;
//	}
//	
//	private double newMido(Parcel of, long time) {
//		Iterable<Parcel> parcels = getAvailableParcels(time);
//		if (!parcels.iterator().hasNext())
//			return 0d;
//		double d = Double.POSITIVE_INFINITY;
//		Point ofd = getParcelPoi(of);
//		for (final Parcel p : parcels) {
//			Double d2 = Point.distance(ofd, p.getDestination());
//			if (d2 < d)
//				d = d2;
//		}
//		return d;
//	}
//	
//	private double newMado(Parcel of, long time) {
//		Iterable<Parcel> parcels = getAvailableParcels(time);
//		if (!parcels.iterator().hasNext())
//			return 0d;
//		double d = Double.NEGATIVE_INFINITY;
//		Point ofd = getParcelPoi(of);
//		for (final Parcel p : parcels) {
//			Double d2 = Point.distance(ofd, p.getDestination());
//			if (d2 > d)
//				d = d2;
//		}
//		return d;
//	}
	
	private double dist(Parcel of) {
		Point myLocation = roadModel.getPosition(this);
		Point dest = getParcelPoi(of);
		if (dest == null)
			return 0d;
		return Point.distance(myLocation, dest);
	}
	
	private double urge(Parcel of, long time) {
		if (pdpModel.containerContains(this, of))
			return of.getDeliveryTimeWindow().end - time;
		else
			return of.getPickupTimeWindow().end - time;
	}
	
	private double est(Parcel of, long time) {
		if (pdpModel.containerContains(this, of))
			return of.getDeliveryTimeWindow().begin - time;
		else
			return of.getPickupTimeWindow().begin - time;
	}
	
	private double ttl(long time) {
		return getDTO().availabilityTimeWindow.end - time;
	}
	
	private double evaluateHeuristic(long time, Parcel pa) {
		heuristic.reset();
		heuristic.setInput(0, ado(pa));
		heuristic.setInput(1, mido(pa));
		heuristic.setInput(2, mado(pa));
		heuristic.setInput(3, dist(pa));
		heuristic.setInput(4, urge(pa, time));
		heuristic.setInput(5, est(pa, time));
		heuristic.setInput(6, ttl(time));
		heuristic.setInput(7, pdpModel.getContents(this).size());
		heuristic.setInput(8, pdpModel.containerContains(this, pa) ? 1 : 0);
		heuristic.activate();
		return heuristic.getOutput(0);
	}
}