using System;
using System.IO;
using System.Net.Sockets;
using System.Threading;
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
            PSocket = new TSocket("localhost", 7913);
            //_transport = new TFramedTransport(PSocket);
            PTransport = new TBufferedTransport(PSocket);
            //PTransport = new SafeTransport(PSocket);
            //PTransport = new SafeTransport("localhost", 7913);
            PProtocol = new TBinaryProtocol(PTransport);
            //PProtocol = new SafeBinaryProtocol(PTransport,PSocket,PProtocol);
            
            PClient = new CFitnessEvaluatorService.Client(PProtocol);
        }

        private ProtocolManager () {}

        /// <summary>
        /// If not already open, creates a connection to the client.
        /// This should always be called before accessing the client.
        /// </summary>
        public static void Open()
        {
            //Socket.Open();    This seems to be uneccessary
            try
            {
                if (!Transport.IsOpen)
                {
                    Console.WriteLine("Opening socket to localhost:7913");
                    Transport.Open();
                    Console.WriteLine("Socket opened.");
                }
            }
            catch (SocketException e)
            {
                Console.WriteLine("Socket exception: " + e.StackTrace);
                Console.WriteLine("Retrying in 2s...");
                Thread.Sleep(2000);
                Console.WriteLine("Retrying");
                Open();
            }
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
    

    class SafeTransport : TTransport
    {
        private TTransport _transport;
        public SafeTransport( String host, int port)
        {
            _transport = new TBufferedTransport(new TSocket(host,port));
        }

        private void SafeAction(Action action)
        {
            SafeFunc(new Func<Object>(delegate { action(); return null; }));
        }

        private T SafeFunc<T>(Func<T> func)
        {
            try
            {
                return func();
            }
            catch (SocketException e)
            {
                Console.WriteLine("JONATHAN: problem with transport/socket, retrying in 2s");
                Thread.Sleep(2000);
                return SafeFunc(func);
            }
            catch (IOException e)
            {
                Console.WriteLine("JONATHAN: problem with transport/socket, retrying");
                Thread.Sleep(2000);
                return SafeFunc(func);
            }
        }

        public override void Open()
        {
            SafeAction(_transport.Open);
        }

        public override void Close()
        {
            SafeAction(_transport.Close);
        }

        public override int Read(byte[] buf, int off, int len)
        {
            return SafeFunc(() => _transport.Read(buf, off, len));
        }

        public override void Write(byte[] buf, int off, int len)
        {
            SafeAction(() => _transport.Write(buf, off, len));
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                SafeAction(() => _transport.Dispose());
            }
        }

        public override bool IsOpen
        {
            get { return SafeFunc(() => _transport.IsOpen); }
        }
    }

    class SafeBinaryProtocol : TProtocol
    {
        private TBinaryProtocol _binaryProtocol;
        private TSocket _socket;
        private TProtocol _protocol;

        public SafeBinaryProtocol(TTransport trans, TSocket socket, TProtocol protocol)
            : base(trans)
        {
            _binaryProtocol = new TBinaryProtocol(trans);
        }

        private void SafeAction(Action action)
        {
            SafeFunc(new Func<Object>(delegate { action(); return null; }));
            //try
            //{
            //    action();
            //}
            //catch (IOException e)
            //{
            //    Console.WriteLine("JONATHAN: problem with connection, retrying in 2s");
            //    Thread.Sleep(2000);
            //    safeAction(action);
            //}
        }
        private T SafeFunc<T>(Func<T> func)
        {
            try
            {
                return func();
            }
            catch (IOException e)
            {
                Console.WriteLine("JONATHAN: problem with protocol, retrying in 2s");
                Thread.Sleep(2000);
                return SafeFunc(func);
            }
        }

        public override void WriteMessageBegin(TMessage message)
        {
            SafeAction(() => _binaryProtocol.WriteMessageBegin(message));
        }

        public override void WriteMessageEnd()
        {
            SafeAction(_binaryProtocol.WriteMessageEnd);
        }

        public override void WriteStructBegin(TStruct struc)
        {
            SafeAction(() => _binaryProtocol.WriteStructBegin(struc));
        }

        public override void WriteStructEnd()
        {
            SafeAction(_binaryProtocol.WriteStructEnd);
        }

        public override void WriteFieldBegin(TField field)
        {
            SafeAction(() => _binaryProtocol.WriteFieldBegin(field));
        }

        public override void WriteFieldEnd()
        {
            SafeAction(_binaryProtocol.WriteFieldEnd);
        }

        public override void WriteFieldStop()
        {
            SafeAction(_binaryProtocol.WriteFieldStop);
        }

        public override void WriteMapBegin(TMap map)
        {
            SafeAction(() => _binaryProtocol.WriteMapBegin(map));
        }

        public override void WriteMapEnd()
        {
            SafeAction(_binaryProtocol.WriteMapEnd);
        }

        public override void WriteListBegin(TList list)
        {
            SafeAction(() => _binaryProtocol.WriteListBegin(list));
        }

        public override void WriteListEnd()
        {
            SafeAction(_binaryProtocol.WriteListEnd);
        }

        public override void WriteSetBegin(TSet set)
        {
            SafeAction(() => _binaryProtocol.WriteSetBegin(set));
        }

        public override void WriteSetEnd()
        {
            SafeAction(_binaryProtocol.WriteSetEnd);
        }

        public override void WriteBool(bool b)
        {
            SafeAction(() => _binaryProtocol.WriteBool(b));
        }

        public override void WriteByte(byte b)
        {
            SafeAction(() => _binaryProtocol.WriteByte(b));
        }

        public override void WriteI16(short i16)
        {
            SafeAction(() => _binaryProtocol.WriteI16(i16));
        }

        public override void WriteI32(int i32)
        {
            SafeAction(() => _binaryProtocol.WriteI32(i32));
        }

        public override void WriteI64(long i64)
        {
            SafeAction(() => _binaryProtocol.WriteI64(i64));
        }

        public override void WriteDouble(double d)
        {
            SafeAction(() => _binaryProtocol.WriteDouble(d));
        }

        public override void WriteBinary(byte[] b)
        {
            SafeAction(() => _binaryProtocol.WriteBinary(b));
        }

        public override TMessage ReadMessageBegin()
        {
            return SafeFunc(_binaryProtocol.ReadMessageBegin);
        }

        public override void ReadMessageEnd()
        {
            SafeAction(_binaryProtocol.ReadMessageEnd);
        }

        public override TStruct ReadStructBegin()
        {
            return SafeFunc(_binaryProtocol.ReadStructBegin);
        }

        public override void ReadStructEnd()
        {
            SafeAction(_binaryProtocol.ReadStructEnd);
        }

        public override TField ReadFieldBegin()
        {
            return SafeFunc(_binaryProtocol.ReadFieldBegin);
        }

        public override void ReadFieldEnd()
        {
            SafeAction(_binaryProtocol.ReadFieldEnd);
        }

        public override TMap ReadMapBegin()
        {
            return SafeFunc(_binaryProtocol.ReadMapBegin);
        }

        public override void ReadMapEnd()
        {
            SafeAction(_binaryProtocol.ReadMapEnd);
        }

        public override TList ReadListBegin()
        {
            return SafeFunc(_binaryProtocol.ReadListBegin);
        }

        public override void ReadListEnd()
        {
            SafeAction(_binaryProtocol.ReadListEnd);
        }

        public override TSet ReadSetBegin()
        {
            return SafeFunc(_binaryProtocol.ReadSetBegin);
        }

        public override void ReadSetEnd()
        {
            SafeAction(_binaryProtocol.ReadSetEnd);
        }

        public override bool ReadBool()
        {
            return SafeFunc(_binaryProtocol.ReadBool);
        }

        public override byte ReadByte()
        {
            return SafeFunc(_binaryProtocol.ReadByte);
        }

        public override short ReadI16()
        {
            return SafeFunc(_binaryProtocol.ReadI16);
        }

        public override int ReadI32()
        {
            return SafeFunc(_binaryProtocol.ReadI32);
        }

        public override long ReadI64()
        {
            return SafeFunc(_binaryProtocol.ReadI64);
        }

        public override double ReadDouble()
        {
            return SafeFunc(_binaryProtocol.ReadDouble);
        }

        public override byte[] ReadBinary()
        {
            return SafeFunc(_binaryProtocol.ReadBinary);
        }
    }
}
