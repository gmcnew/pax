package com.gregmcnew.android.pax;

import android.os.SystemClock;

public class FramerateCounter {
	
	public static int getFPS() {
		return sStarted ? sFps : 0;
	}
	
	public static void start() {
		for (int i = 0; i < NUM_FPS_SAMPLES; i++) {
			sFpsSamples[i] = 0;
		}
		
		sNextSampleIndex = 0;
		sFpsNumSamples = 0;
		sFpsTotalTime = 0;
		sFps = 0;
		
		sLastTime = SystemClock.uptimeMillis();
		
		sStarted = true;
	}
	
	// Returns the number of milliseconds since the previous tick (or 0 on the
	// first tick).
	public static long tick() {
		
		if (Pax.SELF_BENCHMARK) {
			return Pax.UPDATE_INTERVAL_MS;
		}
		
		long dt = 0;
		
		if (sStarted) {

			long time = SystemClock.uptimeMillis();
			
			dt = time - sLastTime;
			
			sLastTime = time;
			
			sFpsTotalTime += dt - sFpsSamples[sNextSampleIndex];
			sFpsSamples[sNextSampleIndex] = dt;
			
			sNextSampleIndex = (sNextSampleIndex + 1) % NUM_FPS_SAMPLES;
			if (sNextSampleIndex > sFpsNumSamples) {
				sFpsNumSamples = sNextSampleIndex;
			}
			
			if (sFpsTotalTime > 0) {
				sFps = (int) (1000 * sFpsNumSamples / sFpsTotalTime);
			}
		}
		
		return dt;
	}
	
	
	// Constants
	
	private static final int NUM_FPS_SAMPLES = 100;
	
	
	// Static variables
	
	private static final long[] sFpsSamples = new long[NUM_FPS_SAMPLES];
	private static int sFps;
	private static int sFpsNumSamples;
	private static int sNextSampleIndex;
	private static long sFpsTotalTime;
	private static long sLastTime;
	private static boolean sStarted = false;
}
