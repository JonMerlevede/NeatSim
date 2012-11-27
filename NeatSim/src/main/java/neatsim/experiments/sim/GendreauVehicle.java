package neatsim.experiments.sim;

import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.VehicleDTO;

public abstract class GendreauVehicle extends DefaultVehicle implements CoordinationUser {

	protected CoordinationModel coordinationModel;
	protected boolean loading = false;
	protected Point target;
	private TimeLapse time;
	
	public GendreauVehicle(VehicleDTO pDto) {
		super(pDto);
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
	public void initCoordinationUser(CoordinationModel coordinationModel) {
		this.coordinationModel = coordinationModel;
	}
	
	@Override
	protected void tickImpl(TimeLapse time) {
		this.time = time;
		if (loading)
			return;
		final Point myPosPoint = roadModel.getPosition(this);
		if (target != null) {
			if (target.equals(myPosPoint))
				roadModel.moveTo(this, myPosPoint, time);
			else
				target = null;
		}
	}
	
	protected boolean canDeliver(Parcel parcel, TimeLapse time) {
		return pdpModel.getTimeWindowPolicy().canDeliver(
				parcel.getDeliveryTimeWindow(),
				time.getTime(),
				parcel.getDeliveryDuration());
	}
	protected boolean canPickup(Parcel parcel, TimeLapse time) {
		return pdpModel.getTimeWindowPolicy().canPickup(
				parcel.getPickupTimeWindow(),
				time.getTime(),
				parcel.getPickupDuration());
	}
	
	// Precondition: canPickup()
	public void pickUp(Parcel parcel) {
		assert canPickup(parcel, time) : "Precondition not satistfied: canPickup(parcel,time) == true";
		pdpModel.pickup(this, parcel, time);
		loading = true;
	}
	
	// Precondition: canDeliver()
	public void deliver(Parcel parcel) {
		assert canDeliver(parcel, time) : "Precondition not satisfied: canDeliver(parcel,time) == true";
		pdpModel.deliver(this, parcel, time);
		loading = true;
	}
	
	public void moveTo(Point point) {
		target = point;
		roadModel.moveTo(this, point, time);
	}
	
	public abstract void action();
}
