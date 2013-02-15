package neatsim.experiments.sim.vehicles.junk;

import java.util.Collection;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultVehicle;

public class VeryStupidVehicle extends DefaultVehicle {
	protected Parcel curr;

	
	public VeryStupidVehicle(AddVehicleEvent event) {
		super(event.vehicleDTO);
	}
	
	@Override
	protected void tickImpl(TimeLapse time) {
		final Collection<Parcel> parcels = pdpModel.getAvailableParcels();

		if (pdpModel.getContents(this).isEmpty()) {
			if (!parcels.isEmpty() && curr == null) {
				double dist = Double.POSITIVE_INFINITY;
				for (final Parcel p : parcels) {
					final double d = Point.distance(roadModel.getPosition(this), roadModel.getPosition(p));
					if (d < dist) {
						dist = d;
						curr = p;
					}
				}
			}

			if (curr != null && roadModel.containsObject(curr)) {
				roadModel.moveTo(this, curr, time);

				if (roadModel.equalPosition(this, curr)
						&& pdpModel.getTimeWindowPolicy().canPickup(
								curr.getDeliveryTimeWindow(),
								time.getTime(),
								curr.getPickupDuration())) {
					pdpModel.pickup(this, curr, time);
				}
			} else {
				curr = null;
			}
		} else {
			roadModel.moveTo(this, curr.getDestination(), time);
			if (roadModel.getPosition(this).equals(curr.getDestination())
					&& pdpModel.getTimeWindowPolicy().canDeliver(
							curr.getPickupTimeWindow(),
							time.getTime(),
							curr.getDeliveryDuration())) {
				pdpModel.deliver(this, curr, time);
			}
		}
	}
}