package com.s8.io.bohr.beryllium.demos;

import java.util.Set;

import com.s8.core.bohr.beryllium.branch.BeBranch;
import com.s8.core.bohr.beryllium.codebase.BeCodebase;
import com.s8.core.bohr.beryllium.exception.BeBuildException;
import com.s8.core.bohr.beryllium.exception.BeIOException;
import com.s8.io.bohr.beryllium.demos.examples.MyExtendedStorageEntry;
import com.s8.io.bohr.beryllium.demos.examples.MyStorageEntry;

public class PerformanceTest3 {

	public static void main(String[] args) throws BeIOException, BeBuildException {

		
		BeCodebase codebase = BeCodebase.from(MyStorageEntry.class);


		BeBranch branch = new BeBranch(codebase, "b");
		
		int n = 65536;
		MyStorageEntry entry;
		for(int i = 0; i<n; i++) {
			if(Math.random()<0.6) {
				entry = MyStorageEntry.generateRandom();	
			}
			else {
				entry = MyExtendedStorageEntry.generateRandom();	
			}
			branch.put(entry);
		}
		
		
		Set<String> keys = branch.getKeySet();
		
		
		class Wrapper { public double x = 0; }
		
		Wrapper wrapper = new Wrapper();
		long t = System.nanoTime();
		keys.forEach(k -> {
			try {
				wrapper.x += ((MyStorageEntry) branch.get(k)).lattitude;
			} catch (BeIOException e) {
				e.printStackTrace();
			}
		});
		long dt = System.nanoTime() - t;
		
		System.out.println(dt);
		System.out.println(wrapper.x);
		
	}

}
