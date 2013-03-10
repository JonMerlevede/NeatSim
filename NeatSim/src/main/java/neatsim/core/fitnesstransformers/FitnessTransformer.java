package neatsim.core.fitnesstransformers;

import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public interface FitnessTransformer {
	public void transform(final List<? extends CFitnessInfo> infos);
}
