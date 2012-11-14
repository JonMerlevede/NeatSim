using Thrift.Protocol;
using Thrift.Transport;

namespace NeatSim
{
    class ProtocolManager
    {
        private static readonly TSocket PSocket;
        private static readonly TTransport PTransport;
        private static readonly TProtocol PProtocol;

        public static TSocket Socket { get { return PSocket; } }
        public static TTransport Transport { get { return PTransport; } }
        public static TProtocol Protocol { get { return PProtocol; } }

        static ProtocolManager()
        {
            PSocket = new TSocket("localhost", 7911);
            //_transport = new TFramedTransport(PSocket);
            PTransport = new TBufferedTransport(PSocket);
            PProtocol = new TBinaryProtocol(PTransport);
        }
    }
}
