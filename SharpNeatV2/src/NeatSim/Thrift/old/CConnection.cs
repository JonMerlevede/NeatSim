/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.IO;
using Thrift;
using Thrift.Collections;
using System.Runtime.Serialization;
using Thrift.Protocol;
using Thrift.Transport;

namespace NeatSim.Thrift
{

  #if !SILVERLIGHT
  [Serializable]
  #endif
  public partial class CConnection : TBase
  {
    private int _fromNeuronId;
    private int _toNeuronId;
    private double _weight;

    public int FromNeuronId
    {
      get
      {
        return _fromNeuronId;
      }
      set
      {
        __isset.fromNeuronId = true;
        this._fromNeuronId = value;
      }
    }

    public int ToNeuronId
    {
      get
      {
        return _toNeuronId;
      }
      set
      {
        __isset.toNeuronId = true;
        this._toNeuronId = value;
      }
    }

    public double Weight
    {
      get
      {
        return _weight;
      }
      set
      {
        __isset.weight = true;
        this._weight = value;
      }
    }


    public Isset __isset;
    #if !SILVERLIGHT
    [Serializable]
    #endif
    public struct Isset {
      public bool fromNeuronId;
      public bool toNeuronId;
      public bool weight;
    }

    public CConnection() {
    }

    public void Read (TProtocol iprot)
    {
      TField field;
      iprot.ReadStructBegin();
      while (true)
      {
        field = iprot.ReadFieldBegin();
        if (field.Type == TType.Stop) { 
          break;
        }
        switch (field.ID)
        {
          case 10:
            if (field.Type == TType.I32) {
              FromNeuronId = iprot.ReadI32();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 20:
            if (field.Type == TType.I32) {
              ToNeuronId = iprot.ReadI32();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 30:
            if (field.Type == TType.Double) {
              Weight = iprot.ReadDouble();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          default: 
            TProtocolUtil.Skip(iprot, field.Type);
            break;
        }
        iprot.ReadFieldEnd();
      }
      iprot.ReadStructEnd();
    }

    public void Write(TProtocol oprot) {
      TStruct struc = new TStruct("CConnection");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (__isset.fromNeuronId) {
        field.Name = "fromNeuronId";
        field.Type = TType.I32;
        field.ID = 10;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32(FromNeuronId);
        oprot.WriteFieldEnd();
      }
      if (__isset.toNeuronId) {
        field.Name = "toNeuronId";
        field.Type = TType.I32;
        field.ID = 20;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32(ToNeuronId);
        oprot.WriteFieldEnd();
      }
      if (__isset.weight) {
        field.Name = "weight";
        field.Type = TType.Double;
        field.ID = 30;
        oprot.WriteFieldBegin(field);
        oprot.WriteDouble(Weight);
        oprot.WriteFieldEnd();
      }
      oprot.WriteFieldStop();
      oprot.WriteStructEnd();
    }

    public override string ToString() {
      StringBuilder sb = new StringBuilder("CConnection(");
      sb.Append("FromNeuronId: ");
      sb.Append(FromNeuronId);
      sb.Append(",ToNeuronId: ");
      sb.Append(ToNeuronId);
      sb.Append(",Weight: ");
      sb.Append(Weight);
      sb.Append(")");
      return sb.ToString();
    }

  }

}
