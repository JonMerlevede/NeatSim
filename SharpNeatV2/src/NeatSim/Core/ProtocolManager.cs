using System.Net.Sockets;
using NeatSim.Thrift;
using Thrift.Protocol;
using Thrift.Transport;

namespace NeatSim.Core
{
    class ProtocolManager
    {
        private static readonly TSocket PSocket;
        private static readonly TTransport PTransport;
        private static readonly TProtocol PProtocol;
        private static readonly CFitnessEvaluatorService.Client PClient;

        private static TSocket Socket { get { return PSocket; } }
        private static TTransport Transport { get { return PTransport; } }
        private static TProtocol Protocol { get { return PProtocol; } }
        public static CFitnessEvaluatorService.Client Client { get { return PClient; } }

        static ProtocolManager()
        {
            PSocket = new TSocket("localhost", 7911);
            //_transport = new TFramedTransport(PSocket);
            PTransport = new TBufferedTransport(PSocket);
            PProtocol = new TBinaryProtocol(PTransport);
            PClient = new CFitnessEvaluatorService.Client(PProtocol);
        }

        /// <summary>
        /// If not already open, creates a connection to the client.
        /// This should always be called before accessing the client.
        /// </summary>
        public static void Open()
        {
            //Socket.Open();    This seems to be uneccessary
            if (!Transport.IsOpen)
                Transport.Open();
        }
        
        /// <summary>
        /// Closes the connection to the PClient.
        /// Doing this for every single evaluation can be very bad for performance.
        /// </summary>
        public static void Close()
        {
            Socket.Close();
            Transport.Close();
        }
    }
}
