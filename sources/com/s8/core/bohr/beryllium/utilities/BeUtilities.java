package com.s8.core.bohr.beryllium.utilities;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import com.s8.api.flow.table.objects.RowS8Object;
import com.s8.core.bohr.beryllium.branch.BeBranch;
import com.s8.core.bohr.beryllium.codebase.BeCodebase;
import com.s8.core.bohr.beryllium.types.BeType;

public class BeUtilities {

	
	
	/**
	 * 
	 * @param branch1
	 * @param branch2
	 * @param writer
	 * @throws IOException
	 */
	public static void deepCompare(BeBranch branch1, BeBranch branch2, Writer writer) throws IOException {

		BeCodebase codebase = branch1.codebase;
		
		Set<String> keys = branch1.getKeySet();
		
		writer.append("<deep-compare>");
		
		for(String id : keys) {
			
			RowS8Object object = branch1.get(id);
			RowS8Object objectCopy = branch2.get(id);
			BeType type = codebase.getType(object);
			
			type.deepCompare(object, objectCopy, writer);
		}
		writer.append("</deep-compare>");
		
	}
}
