package com.s8.core.bohr.beryllium.fields.objects;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.flow.table.objects.RowS8Object;
import com.s8.api.serial.S8Serializable;
import com.s8.core.bohr.beryllium.fields.BeField;
import com.s8.core.bohr.beryllium.fields.BeFieldDelta;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class S8SerializableBeFieldDelta<T extends S8Serializable> extends BeFieldDelta {
	
	
	public final S8SerializableBeField<T> field;
	
	public final S8Serializable value;

	
	
	public S8SerializableBeFieldDelta(S8SerializableBeField<T> field, S8Serializable value) {
		super();
		this.field = field;
		this.value = value;
	}

	@Override
	public void consume(RowS8Object object) throws IllegalArgumentException, IllegalAccessException {
		field.field.set(object, value);
	}
	
	
	@Override
	public BeField getField() { 
		return field;
	}
	
	
	@Override
	public void computeFootprint(MemoryFootprint weight) {
		if(value!=null) {
			weight.reportInstance();
			weight.reportBytes(value.computeFootprint());	
		}
	}

}
