package com.s8.core.bohr.beryllium.fields.objects;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;

import com.s8.api.annotations.S8Field;
import com.s8.api.bytes.ByteInflow;
import com.s8.api.bytes.ByteOutflow;
import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.exceptions.S8IOException;
import com.s8.api.flow.table.objects.RowS8Object;
import com.s8.api.serial.S8SerialPrototype;
import com.s8.api.serial.S8Serializable;
import com.s8.core.bohr.atom.protocol.BOHR_Properties;
import com.s8.core.bohr.atom.protocol.BOHR_Types;
import com.s8.core.bohr.atom.serial.S8SerialUtilities;
import com.s8.core.bohr.beryllium.exception.BeBuildException;
import com.s8.core.bohr.beryllium.exception.BeIOException;
import com.s8.core.bohr.beryllium.fields.BeField;
import com.s8.core.bohr.beryllium.fields.BeFieldBuilder;
import com.s8.core.bohr.beryllium.fields.BeFieldComposer;
import com.s8.core.bohr.beryllium.fields.BeFieldDelta;
import com.s8.core.bohr.beryllium.fields.BeFieldParser;
import com.s8.core.bohr.beryllium.fields.BeFieldProperties;
import com.s8.core.bohr.beryllium.fields.BeFieldPrototype;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class S8SerializableBeField<T extends S8Serializable> extends BeField {



	public final static BeFieldPrototype PROTOTYPE = new BeFieldPrototype() {


		@Override
		public BeFieldProperties captureField(Field field) throws BeBuildException {
			Class<?> fieldType = field.getType();
			if(S8Serializable.class.isAssignableFrom(fieldType)){
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					BeFieldProperties properties = new BeFieldProperties(this, fieldType, BeFieldProperties.FIELD);
					properties.setFieldAnnotation(annotation);
					return properties;	
				}
				else { return null; }
			}
			else { return null; }
		}




		@Override
		public BeFieldBuilder createFieldBuilder(BeFieldProperties properties, Field handler) {
			return new Builder(properties, handler);
		}
	};




	private static class Builder extends BeFieldBuilder {

		public Builder(BeFieldProperties properties, Field handler) {
			super(properties, handler);
		}

		@Override
		public BeFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public BeField build(int ordinal) throws BeBuildException {
			return new S8SerializableBeField<>(ordinal, properties, field);
		}
	}



	private S8SerialPrototype<T> deserializer;



	/**
	 * 
	 * @param properties
	 * @param handler
	 * @throws NdBuildException 
	 */
	public S8SerializableBeField(int ordinal, BeFieldProperties properties, Field handler) throws BeBuildException {
		super(ordinal, properties, handler);
		Class<?> baseType = properties.baseType;
		try {
			deserializer = S8SerialUtilities.getPrototype(baseType);
		} 
		catch (S8IOException e) {
			e.printStackTrace();
			throw new BeBuildException("Failed to build the S8Serizalizable GphField due to  "+e.getMessage() , baseType);
		}
	}





	@Override
	public void computeFootprint(RowS8Object object, MemoryFootprint weight) 
			throws IllegalArgumentException, IllegalAccessException {
		S8Serializable value = (S8Serializable) field.get(object);
		if(value!=null) {
			weight.reportInstance();
			weight.reportBytes(value.computeFootprint());	
		}
	}

	@Override
	public void deepClone(RowS8Object origin, RowS8Object clone) 
			throws IllegalArgumentException, IllegalAccessException {
		S8Serializable value = (S8Serializable) field.get(origin);
		field.set(clone, value.deepClone());
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (ByteSerializableFieldHandler)");
	}


	@Override
	public boolean hasDiff(RowS8Object base, RowS8Object update) throws IllegalArgumentException, IllegalAccessException  {
		
		@SuppressWarnings("unchecked")
		T left = (T) field.get(base);
		
		@SuppressWarnings("unchecked")
		T right = (T) field.get(update);

		if(left != null && right !=null) {
			return deserializer.hasDelta(left, right);
		}
		else if((left != null && right == null) || (left == null && right != null)) {
			return true;
		}
		else {
			return false;
		}
	}


	@Override
	public BeFieldDelta produceDiff(RowS8Object object) throws IllegalArgumentException, IllegalAccessException  {
		return new S8SerializableBeFieldDelta<>(S8SerializableBeField.this, (S8Serializable) field.get(object));
	}


	@Override
	protected void printValue(RowS8Object object, Writer writer) throws IOException, IllegalArgumentException, IllegalAccessException {
		Object value = field.get(object);
		if(value!=null) {
			writer.write("(");
			writer.write(value.getClass().getCanonicalName());
			writer.write("): ");
			writer.write(value.toString());	
		}
		else {
			writer.write("null");
		}
	}

	@Override
	public String printType() {
		return "S8Object";
	}



	@Override
	public boolean isValueResolved(RowS8Object object) {
		return true; // always resolved at resolve step in shell
	}




	/* <IO-inflow-section> */

	@Override
	public BeFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		if(code != BOHR_Types.SERIAL) {
			throw new IOException("only SERIAL accepted");
		}

		// in fine, create parser
		return new Parser();
	}


	private class Parser extends BeFieldParser {

		@Override
		public void parseValue(RowS8Object object, ByteInflow inflow) 
				throws IllegalArgumentException, IllegalAccessException, IOException {
			field.set(object, deserialize(inflow));
		}


		@Override
		public S8SerializableBeField<T> getField() {
			return S8SerializableBeField.this;
		}

		@Override
		public BeFieldDelta deserializeDelta(ByteInflow inflow) throws IOException {
			return new S8SerializableBeFieldDelta<>(S8SerializableBeField.this, deserialize(inflow));
		}

		private S8Serializable deserialize(ByteInflow inflow) throws IOException {
			int props = inflow.getUInt8();
			if(isNonNull(props)) {
				return deserializer.deserialize(inflow);
			}
			else {
				return null;
			}
		}

	}

	/* </IO-inflow-section> */



	/* <IO-outflow-section> */

	@Override
	public BeFieldComposer createComposer(int code) throws BeIOException {
		switch(flow) {

		case DEFAULT_FLOW_TAG: case "serial" : return new Outflow(code);

		default : throw new BeIOException("Impossible to match IO type for flow: "+flow);
		}
	}


	private class Outflow extends BeFieldComposer {

		public Outflow(int code) { super(code); }

		@Override
		public BeField getField() {
			return S8SerializableBeField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.SERIAL);
		}

		@Override
		public void composeValue(RowS8Object object, ByteOutflow outflow) 
				throws IOException, IllegalArgumentException, IllegalAccessException {
			S8Serializable value = (S8Serializable) field.get(object);
			if(value != null) {
				outflow.putUInt8(BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT);
				value.serialize(outflow);
			}
			else {
				outflow.putUInt8(0x00);
			}
		}

		@Override
		public void publishValue(BeFieldDelta delta, ByteOutflow outflow) throws IOException {
			@SuppressWarnings("unchecked")
			S8Serializable value = ((S8SerializableBeFieldDelta<T>) delta).value;
			if(value != null) {
				outflow.putUInt8(BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT);
				value.serialize(outflow);
			}
			else {
				outflow.putUInt8(0x00);
			}
		}
	}
	/* </IO-outflow-section> */


}
