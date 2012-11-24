package neatsim.experiments.sim;

import java.io.IOException;
import java.util.Collection;

import rinde.sim.core.Simulator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauProblem {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 5;	
	private final DynamicPDPTWProblem problem;
	
	public GendreauProblem() throws IOException {
		problem = createProblem();
	}
	
	private final DynamicPDPTWProblem createProblem() throws IOException {
		Gendreau06Scenario scenario = Gendreau06Parser.parse(SCENARIO_NAME, NUMBER_OF_VEHICLES);
		System.out.println(scenario.getPossibleEventTypes().toString());
		
		long randomSeed = 823745;
		DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, randomSeed);
		problem.enableUI();
		problem.addCreator(AddVehicleEvent.class, new MyVehicleEventCreator());
		problem.addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return false;
			}
		});
		return problem;
	}
	
	public void start() {
		problem.simulate();
	}
	
	private class MyAwesomeVehicle extends DefaultVehicle {
//		private final AddVehicleEvent event;
		protected Parcel curr;
	
		
		public MyAwesomeVehicle(AddVehicleEvent event) {
			super(event.vehicleDTO);
			//this.event = event;
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

					if (roadModel.equalPosition(this, curr)) {
						pdpModel.pickup(this, curr, time);
					}
				} else {
					curr = null;
				}
			} else {
				roadModel.moveTo(this, curr.getDestination(), time);
				if (roadModel.getPosition(this).equals(curr.getDestination())) {
					pdpModel.deliver(this, curr, time);
				}
			}
		}
	}
	
	private class MyVehicleEventCreator implements DynamicPDPTWProblem.Creator<AddVehicleEvent> {
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
			return sim.register(new MyAwesomeVehicle(event));
		}
	}
}
