/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package neatsim.comm.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CConnection implements org.apache.thrift.TBase<CConnection, CConnection._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CConnection");

  private static final org.apache.thrift.protocol.TField FROM_NEURON_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("fromNeuronId", org.apache.thrift.protocol.TType.I32, (short)10);
  private static final org.apache.thrift.protocol.TField TO_NEURON_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("toNeuronId", org.apache.thrift.protocol.TType.I32, (short)20);
  private static final org.apache.thrift.protocol.TField WEIGHT_FIELD_DESC = new org.apache.thrift.protocol.TField("weight", org.apache.thrift.protocol.TType.DOUBLE, (short)30);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CConnectionStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CConnectionTupleSchemeFactory());
  }

  public int fromNeuronId; // required
  public int toNeuronId; // required
  public double weight; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    FROM_NEURON_ID((short)10, "fromNeuronId"),
    TO_NEURON_ID((short)20, "toNeuronId"),
    WEIGHT((short)30, "weight");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 10: // FROM_NEURON_ID
          return FROM_NEURON_ID;
        case 20: // TO_NEURON_ID
          return TO_NEURON_ID;
        case 30: // WEIGHT
          return WEIGHT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __FROMNEURONID_ISSET_ID = 0;
  private static final int __TONEURONID_ISSET_ID = 1;
  private static final int __WEIGHT_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FROM_NEURON_ID, new org.apache.thrift.meta_data.FieldMetaData("fromNeuronId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.TO_NEURON_ID, new org.apache.thrift.meta_data.FieldMetaData("toNeuronId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.WEIGHT, new org.apache.thrift.meta_data.FieldMetaData("weight", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CConnection.class, metaDataMap);
  }

  public CConnection() {
  }

  public CConnection(
    int fromNeuronId,
    int toNeuronId,
    double weight)
  {
    this();
    this.fromNeuronId = fromNeuronId;
    setFromNeuronIdIsSet(true);
    this.toNeuronId = toNeuronId;
    setToNeuronIdIsSet(true);
    this.weight = weight;
    setWeightIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CConnection(CConnection other) {
    __isset_bitfield = other.__isset_bitfield;
    this.fromNeuronId = other.fromNeuronId;
    this.toNeuronId = other.toNeuronId;
    this.weight = other.weight;
  }

  public CConnection deepCopy() {
    return new CConnection(this);
  }

  @Override
  public void clear() {
    setFromNeuronIdIsSet(false);
    this.fromNeuronId = 0;
    setToNeuronIdIsSet(false);
    this.toNeuronId = 0;
    setWeightIsSet(false);
    this.weight = 0.0;
  }

  public int getFromNeuronId() {
    return this.fromNeuronId;
  }

  public CConnection setFromNeuronId(int fromNeuronId) {
    this.fromNeuronId = fromNeuronId;
    setFromNeuronIdIsSet(true);
    return this;
  }

  public void unsetFromNeuronId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FROMNEURONID_ISSET_ID);
  }

  /** Returns true if field fromNeuronId is set (has been assigned a value) and false otherwise */
  public boolean isSetFromNeuronId() {
    return EncodingUtils.testBit(__isset_bitfield, __FROMNEURONID_ISSET_ID);
  }

  public void setFromNeuronIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FROMNEURONID_ISSET_ID, value);
  }

  public int getToNeuronId() {
    return this.toNeuronId;
  }

  public CConnection setToNeuronId(int toNeuronId) {
    this.toNeuronId = toNeuronId;
    setToNeuronIdIsSet(true);
    return this;
  }

  public void unsetToNeuronId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TONEURONID_ISSET_ID);
  }

  /** Returns true if field toNeuronId is set (has been assigned a value) and false otherwise */
  public boolean isSetToNeuronId() {
    return EncodingUtils.testBit(__isset_bitfield, __TONEURONID_ISSET_ID);
  }

  public void setToNeuronIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TONEURONID_ISSET_ID, value);
  }

  public double getWeight() {
    return this.weight;
  }

  public CConnection setWeight(double weight) {
    this.weight = weight;
    setWeightIsSet(true);
    return this;
  }

  public void unsetWeight() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __WEIGHT_ISSET_ID);
  }

  /** Returns true if field weight is set (has been assigned a value) and false otherwise */
  public boolean isSetWeight() {
    return EncodingUtils.testBit(__isset_bitfield, __WEIGHT_ISSET_ID);
  }

  public void setWeightIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __WEIGHT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case FROM_NEURON_ID:
      if (value == null) {
        unsetFromNeuronId();
      } else {
        setFromNeuronId((Integer)value);
      }
      break;

    case TO_NEURON_ID:
      if (value == null) {
        unsetToNeuronId();
      } else {
        setToNeuronId((Integer)value);
      }
      break;

    case WEIGHT:
      if (value == null) {
        unsetWeight();
      } else {
        setWeight((Double)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case FROM_NEURON_ID:
      return Integer.valueOf(getFromNeuronId());

    case TO_NEURON_ID:
      return Integer.valueOf(getToNeuronId());

    case WEIGHT:
      return Double.valueOf(getWeight());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case FROM_NEURON_ID:
      return isSetFromNeuronId();
    case TO_NEURON_ID:
      return isSetToNeuronId();
    case WEIGHT:
      return isSetWeight();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CConnection)
      return this.equals((CConnection)that);
    return false;
  }

  public boolean equals(CConnection that) {
    if (that == null)
      return false;

    boolean this_present_fromNeuronId = true;
    boolean that_present_fromNeuronId = true;
    if (this_present_fromNeuronId || that_present_fromNeuronId) {
      if (!(this_present_fromNeuronId && that_present_fromNeuronId))
        return false;
      if (this.fromNeuronId != that.fromNeuronId)
        return false;
    }

    boolean this_present_toNeuronId = true;
    boolean that_present_toNeuronId = true;
    if (this_present_toNeuronId || that_present_toNeuronId) {
      if (!(this_present_toNeuronId && that_present_toNeuronId))
        return false;
      if (this.toNeuronId != that.toNeuronId)
        return false;
    }

    boolean this_present_weight = true;
    boolean that_present_weight = true;
    if (this_present_weight || that_present_weight) {
      if (!(this_present_weight && that_present_weight))
        return false;
      if (this.weight != that.weight)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(CConnection other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    CConnection typedOther = (CConnection)other;

    lastComparison = Boolean.valueOf(isSetFromNeuronId()).compareTo(typedOther.isSetFromNeuronId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFromNeuronId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fromNeuronId, typedOther.fromNeuronId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetToNeuronId()).compareTo(typedOther.isSetToNeuronId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetToNeuronId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.toNeuronId, typedOther.toNeuronId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetWeight()).compareTo(typedOther.isSetWeight());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWeight()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.weight, typedOther.weight);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CConnection(");
    boolean first = true;

    sb.append("fromNeuronId:");
    sb.append(this.fromNeuronId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("toNeuronId:");
    sb.append(this.toNeuronId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("weight:");
    sb.append(this.weight);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'fromNeuronId' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'toNeuronId' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'weight' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class CConnectionStandardSchemeFactory implements SchemeFactory {
    public CConnectionStandardScheme getScheme() {
      return new CConnectionStandardScheme();
    }
  }

  private static class CConnectionStandardScheme extends StandardScheme<CConnection> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CConnection struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 10: // FROM_NEURON_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.fromNeuronId = iprot.readI32();
              struct.setFromNeuronIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 20: // TO_NEURON_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.toNeuronId = iprot.readI32();
              struct.setToNeuronIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 30: // WEIGHT
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.weight = iprot.readDouble();
              struct.setWeightIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetFromNeuronId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'fromNeuronId' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetToNeuronId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'toNeuronId' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetWeight()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'weight' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, CConnection struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(FROM_NEURON_ID_FIELD_DESC);
      oprot.writeI32(struct.fromNeuronId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(TO_NEURON_ID_FIELD_DESC);
      oprot.writeI32(struct.toNeuronId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(WEIGHT_FIELD_DESC);
      oprot.writeDouble(struct.weight);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CConnectionTupleSchemeFactory implements SchemeFactory {
    public CConnectionTupleScheme getScheme() {
      return new CConnectionTupleScheme();
    }
  }

  private static class CConnectionTupleScheme extends TupleScheme<CConnection> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CConnection struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.fromNeuronId);
      oprot.writeI32(struct.toNeuronId);
      oprot.writeDouble(struct.weight);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CConnection struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.fromNeuronId = iprot.readI32();
      struct.setFromNeuronIdIsSet(true);
      struct.toNeuronId = iprot.readI32();
      struct.setToNeuronIdIsSet(true);
      struct.weight = iprot.readDouble();
      struct.setWeightIsSet(true);
    }
  }

}

