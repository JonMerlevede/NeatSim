/* ***************************************************************************
 * This file is part of SharpNEAT - Evolution of Neural Networks.
 * 
 * Copyright 2004-2006, 2009-2010 Colin Green (sharpneat@gmail.com)
 *
 * SharpNEAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SharpNEAT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SharpNEAT.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using SharpNeat.Core;
using SharpNeat.Decoders;
using SharpNeat.Genomes.Neat;
using SharpNeat.Phenomes;
using SharpNeat.Phenomes.NeuralNets;

namespace NeatSim.Core
{
    /// <summary>
    /// Decodes NeatGenome's into fast cyclic neat networks.
    /// This class is based on SharpNeat's NeatGenomeDecoder class.
    /// 
    /// This class was introduced because we need to know the structure of the underlying IBlackBox.
    /// The NeatGenomeDecoder class gives us an IBlackBox with the underlying network structure being
    /// either a fast cyclic network, a slow cyclic network or an acyclic network. Since we need to transfer
    /// this network over to another program and have support there only for the Fast Cyclic Network,
    /// this is not useful to us.
    /// </summary>
    public class FastCyclicNeatGenomeDecoder : IGenomeDecoder<NeatGenome,FastCyclicNetwork>
    {
        readonly NetworkActivationScheme _activationScheme;
        delegate FastCyclicNetwork DecodeGenome(NeatGenome genome);
        readonly DecodeGenome _decodeMethod;

        #region Constructors

        /// <summary>
        /// Construct the decoder with the network activation scheme to use in decoded networks.
        /// </summary>
        public FastCyclicNeatGenomeDecoder(NetworkActivationScheme activationScheme)
        {
            _activationScheme = activationScheme;

            // Pre-determine which decode routine to use based on the activation scheme.
            _decodeMethod = GetDecodeMethod(activationScheme);
        }

        #endregion

        #region IGenomeDecoder Members

        /// <summary>
        /// Decodes a NeatGenome to a concrete network instance.
        /// </summary>
        public FastCyclicNetwork Decode(NeatGenome genome)
        {
            return _decodeMethod(genome);
        }

        #endregion

        #region Private Methods

        private DecodeGenome GetDecodeMethod(NetworkActivationScheme activationScheme)
        {
            if(activationScheme.AcyclicNetwork)
            {
                throw new Exception("The FastCyclicNeatGenomeDecoder only supports activation schemes specifying fast CYCLIC networks.");
            }

            if(activationScheme.FastFlag)
            {
                return DecodeToFastCyclicNetwork;
            }
            throw new Exception("The FastCyclicNeatGenomeDecoder only supports activation schemes specifying FAST cyclic networks.");
        }

        //private FastAcyclicNetwork DecodeToFastAcyclicNetwork(NeatGenome genome)
        //{
        //    return FastAcyclicNetworkFactory.CreateFastAcyclicNetwork(genome);
        //}

        //private CyclicNetwork DecodeToCyclicNetwork(NeatGenome genome)
        //{
        //    return CyclicNetworkFactory.CreateCyclicNetwork(genome, _activationScheme);
        //}

        private FastCyclicNetwork DecodeToFastCyclicNetwork(NeatGenome genome)
        {
            return FastCyclicNetworkFactory.CreateFastCyclicNetwork(genome, _activationScheme);
        }

        #endregion
    }
}
