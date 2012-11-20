using System.Collections.Generic;
using System.Diagnostics;
using System.Xml;
using NeatSim.Core;
using SharpNeat.Core;
using SharpNeat.Decoders;
using SharpNeat.DistanceMetrics;
using SharpNeat.Domains;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.EvolutionAlgorithms.ComplexityRegulation;
using SharpNeat.Genomes.Neat;
using SharpNeat.Phenomes;
using SharpNeat.Phenomes.NeuralNets;
using SharpNeat.SpeciationStrategies;

namespace NeatSim.Experiments.Xor.DeadCode
{
    class RemoteBatchXorExperiment : IGuiNeatExperiment
    {

        public RemoteBatchXorExperiment()
        {
            Debug.WriteLine("Created new RemoteXorExperiment");
        }

        public string Name
        {
            get { return "Neat Simulation Experiment"; }
        }
        public string Description
        {
            get { return "Maybe, at some point in time, there will be an explanation here."; }
        }
        public int InputCount
        {
            get { return 2; }
        }
        public int OutputCount
        {
            get { return 1; }
        }
        public int DefaultPopulationSize
        {
            get { return 150; }
        }
        public NeatEvolutionAlgorithmParameters NeatEvolutionAlgorithmParameters { get; private set; }
        public NeatGenomeParameters NeatGenomeParameters { get; private set; }

        private NetworkActivationScheme _activationScheme;
        private FastCyclicNeatGenomeDecoder _decoder;

        public IGenomeDecoder<NeatGenome, IBlackBox> CreateGenomeDecoder()
        {
            return _decoder;
        }

        public IGenomeFactory<NeatGenome> CreateGenomeFactory()
        {
            return new NeatGenomeFactory(InputCount, OutputCount, NeatGenomeParameters);
        }

        public NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm()
        {
            return CreateEvolutionAlgorithm(DefaultPopulationSize);
        }

        public NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(int populationSize)
        {
            // Create a genome factory with our neat genome parameters object and the appropriate number of input and output neuron genes.
            IGenomeFactory<NeatGenome> genomeFactory = CreateGenomeFactory();
            // Create an initial population of randomly generated genomes.
            List<NeatGenome> genomeList = genomeFactory.CreateGenomeList(populationSize, 0);
            // Create evolution algorithm.
            return CreateEvolutionAlgorithm(genomeFactory, genomeList);
        }

        public NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(IGenomeFactory<NeatGenome> genomeFactory, List<NeatGenome> genomeList)
        {
            // Create distance metric. Mismatched genes have a fixed distance of 10; for matched genes the distance is their weigth difference.
            IDistanceMetric distanceMetric = new ManhattanDistanceMetric(1.0, 0.0, 10.0);
            ISpeciationStrategy<NeatGenome> speciationStrategy =
                new KMeansClusteringStrategy<NeatGenome>(distanceMetric);

            // Create complexity regulation strategy.
            IComplexityRegulationStrategy complexityRegulationStrategy =
                ExperimentUtils.CreateComplexityRegulationStrategy("absolute", 10);

            // Create the evolution algorithm.
            var ea = new NeatEvolutionAlgorithm<NeatGenome>(
                    NeatEvolutionAlgorithmParameters,
                    speciationStrategy,
                    complexityRegulationStrategy);

            // Create IBlackBox evaluator.
            // var evaluator = new RemoteXorEvaluator();
            var evaluator = new RemoteBatchXorEvaluator();
            //LocalXorEvaluator evaluator = new LocalXorEvaluator();

            // Create genome decoder.
            IGenomeDecoder<NeatGenome, FastCyclicNetwork> genomeDecoder = _decoder;

            // Create a genome list evaluator. This packages up the genome decoder with the genome evaluator.
            IGenomeListEvaluator<NeatGenome> innerEvaluator = new BatchGenomeListEvaluator<NeatGenome, FastCyclicNetwork>(genomeDecoder, evaluator);

            // Wrap the list evaluator in a 'selective' evaulator that will only evaluate new genomes. That is, we skip re-evaluating any genomes
            // that were in the population in previous generations (elite genomes). This is determined by examining each genome's evaluation info object.
            IGenomeListEvaluator<NeatGenome> selectiveEvaluator = new SelectiveGenomeListEvaluator<NeatGenome>(
                                                                                    innerEvaluator,
                                                                                    SelectiveGenomeListEvaluator<NeatGenome>.CreatePredicate_OnceOnly());
            // Initialize the evolution algorithm.
            ea.Initialize(selectiveEvaluator, genomeFactory, genomeList);

            // Finished. Return the evolution algorithm
            return ea;
        }

        public void SavePopulation(XmlWriter xw, IList<NeatGenome> genomeList)
        {
            // Writing node IDs is not necessary for NEAT.
            NeatGenomeXmlIO.WriteComplete(xw, genomeList, false);
        }

        public List<NeatGenome> LoadPopulation(XmlReader xr)
        {
            var genomeFactory = (NeatGenomeFactory)CreateGenomeFactory();
            return NeatGenomeXmlIO.ReadCompleteGenomeList(xr, false, genomeFactory);
        }

        public void Initialize(string name, XmlElement xmlConfig)
        {
            NeatEvolutionAlgorithmParameters = new NeatEvolutionAlgorithmParameters();
            NeatEvolutionAlgorithmParameters.SpecieCount = 10;
            // The NeatGenomeParameters object is passed to the NeatGenomeFactory.
            // The NeatGenomeFactory creates a NeatGenome (which is an INetworkDefinition).
            // The NeatGenome is constructed using the NeatGenomeParameters.
            // For example, the NeatGenomeParameters define what activation function to use.
            NeatGenomeParameters = new NeatGenomeParameters();
            // Create fast cyclic activation scheme with 3 evaluations for convergence
            _activationScheme = NetworkActivationScheme.CreateCyclicFixedTimestepsScheme(3,true);
            _decoder = new FastCyclicNeatGenomeDecoder(_activationScheme);
        }

        public AbstractGenomeView CreateGenomeView()
        {
            return new NeatGenomeView();
        }

        public AbstractDomainView CreateDomainView()
        {
            return null;
        }
    }
}
