package com.s8.core.bohr.beryllium.fields.primitives;

import com.s8.api.bytes.MemoryFootprint;
import com.s8.api.flow.table.objects.RowS8Object;
import com.s8.core.bohr.beryllium.fields.BeField;
import com.s8.core.bohr.beryllium.fields.BeFieldDelta;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class ShortBeFieldDelta extends BeFieldDelta {


	public final ShortBeField field;

	public final short value;

	
	/**
	 * 
	 * @param field
	 * @param value
	 */
	public ShortBeFieldDelta(ShortBeField field, short value) {
		super();
		this.field = field;
		this.value = value;
	}

	
	@Override
	public BeField getField() { 
		return field; 
	}

	
	@Override
	public void consume(RowS8Object object) throws IllegalArgumentException, IllegalAccessException {
		field.field.setShort(object, value);
	}
	

	@Override
	public void computeFootprint(MemoryFootprint weight) {
		weight.reportBytes(2);
	}

}
