using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using SharpNeat.Core;
using SharpNeat.Decoders;
using SharpNeat.Decoders.Neat;
using SharpNeat.DistanceMetrics;
using SharpNeat.Domains;
using SharpNeat.EvolutionAlgorithms;
using SharpNeat.EvolutionAlgorithms.ComplexityRegulation;
using SharpNeat.Genomes.Neat;
using SharpNeat.Phenomes;
using SharpNeat.SpeciationStrategies;

namespace NeatSim.Core
{
    abstract class AbstractNeatExperiment : IGuiNeatExperiment
    {
        // These still need to be implemented by the user
        #region Abstract
        public abstract int InputCount { get; }
        public abstract int OutputCount { get; }
        public abstract NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(
            IGenomeFactory<NeatGenome> genomeFactory,
            List<NeatGenome> genomeList);
        #endregion

        // It might be interesting to override these properties if you don't want to use the values provided in the XML file.
        #region Useful to override
        public virtual string Name { get { return _name; } }
        public virtual string Description { get { return _description; } }
        public virtual int DefaultPopulationSize { get { return _populationSize; } }
        #endregion


        protected ISpeciationStrategy<NeatGenome> DefaultSpeciationStrategy;
        protected NeatEvolutionAlgorithm<NeatGenome> DefaultNeatEvolutionAlgorithm;
        protected NetworkActivationScheme DefaultNetworkActivationScheme { get { return _activationScheme; } }

        // Create complexity regulation strategy.
        protected IComplexityRegulationStrategy DefaultComplexityRegulationStrategy;

        // The following are nice default definitions for methods that make up the bulk of the IGuiNeatExperiment interface
        #region  Private members
        private String _name;
        private int _populationSize;
        private int _specieCount;
        private  NetworkActivationScheme _activationScheme;
        private String _complexityRegulationStr;
        private int? _complexityThreshold;
        private string _description;
        private ParallelOptions _parallelOptions;
        private NeatEvolutionAlgorithmParameters _eaParams;
        private NeatGenomeParameters _neatGenomeParams;
        #endregion

        #region Public properties
        public virtual NeatEvolutionAlgorithmParameters NeatEvolutionAlgorithmParameters { get { return _eaParams; } }
        public virtual NeatGenomeParameters NeatGenomeParameters { get { return _neatGenomeParams; } }
        #endregion

        #region IGuiNeatExperiment defaults
        public virtual  void Initialize(string name, XmlElement xmlConfig)
        {
            _name = name;
            _populationSize = XmlUtils.GetValueAsInt(xmlConfig, "PopulationSize");
            _specieCount = XmlUtils.GetValueAsInt(xmlConfig, "SpecieCount");
            _activationScheme = ExperimentUtils.CreateActivationScheme(xmlConfig, "Activation");
            _complexityRegulationStr = XmlUtils.TryGetValueAsString(xmlConfig,
                "DefaultComplexityRegulationStrategy");
            _complexityThreshold = XmlUtils.TryGetValueAsInt(xmlConfig, "ComplexityThreshold");
            _description = XmlUtils.TryGetValueAsString(xmlConfig, "Description");
            _parallelOptions = ExperimentUtils.ReadParallelOptions(xmlConfig);

            _eaParams = new NeatEvolutionAlgorithmParameters();
            _eaParams.SpecieCount = _specieCount;
            _neatGenomeParams = new NeatGenomeParameters();
            _neatGenomeParams.FeedforwardOnly = _activationScheme.AcyclicNetwork;

            DefaultComplexityRegulationStrategy = ExperimentUtils.CreateComplexityRegulationStrategy(
                _complexityRegulationStr,
                _complexityThreshold);
            DefaultSpeciationStrategy = new KMeansClusteringStrategy<NeatGenome>(new ManhattanDistanceMetric(1.0, 0.0, 10.0));
            DefaultNeatEvolutionAlgorithm = new NeatEvolutionAlgorithm<NeatGenome>(
                    NeatEvolutionAlgorithmParameters,
                    DefaultSpeciationStrategy,
                    DefaultComplexityRegulationStrategy);
        }

        public virtual List<NeatGenome> LoadPopulation(XmlReader xr)
        {
            var genomeFactory = (NeatGenomeFactory)CreateGenomeFactory();
            return NeatGenomeXmlIO.ReadCompleteGenomeList(xr, false, genomeFactory);
        }
        public virtual void SavePopulation(XmlWriter xw, IList<NeatGenome> genomeList)
        {
            // Writing node IDs is not necessary for NEAT.
            NeatGenomeXmlIO.WriteComplete(xw, genomeList, false);
        }
        public virtual IGenomeDecoder<NeatGenome, IBlackBox> CreateGenomeDecoder()
        {
            return new NeatGenomeDecoder(_activationScheme);
        }
        public virtual IGenomeFactory<NeatGenome> CreateGenomeFactory()
        {
            return new NeatGenomeFactory(InputCount, OutputCount, NeatGenomeParameters);
        }
        public virtual NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm()
        {
            return CreateEvolutionAlgorithm(DefaultPopulationSize);
        }
        public virtual NeatEvolutionAlgorithm<NeatGenome> CreateEvolutionAlgorithm(int populationSize)
        {
            // Create a genome factory with our neat genome parameters object and the appropriate number of input and output neuron genes.
            IGenomeFactory<NeatGenome> genomeFactory = CreateGenomeFactory();
            // Create an initial population of randomly generated genomes.
            List<NeatGenome> genomeList = genomeFactory.CreateGenomeList(populationSize, 0);
            // Create evolution algorithm.
            return CreateEvolutionAlgorithm(genomeFactory, genomeList);
        }
        public virtual AbstractGenomeView CreateGenomeView()
        {
            return new NeatGenomeView();
        }
        public virtual AbstractDomainView CreateDomainView()
        {
            return null;
        }
        #endregion
    }
}
