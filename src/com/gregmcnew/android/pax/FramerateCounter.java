package com.gregmcnew.android.pax;

public class FramerateCounter {
	
	public static int getFPS() {
		return sFPS;
	}
	
	// Returns the number of milliseconds since the previous tick (or 0 on the
	// first tick).
	public static long tick() {

		long time = System.currentTimeMillis();
		
		long dt = (sLastTime == -1) ? 0 : time - sLastTime;
		
		sLastTime = time;
		
		sFpsTotalTime += dt - sFpsSamples[sFpsNextSample];
		sFpsSamples[sFpsNextSample] = dt;
		
		sFpsNextSample = (sFpsNextSample + 1) % NUM_FPS_SAMPLES;
		if (sFpsNextSample > sFpsNumSamples) {
			sFpsNumSamples = sFpsNextSample;
		}
		
		if (sFpsTotalTime > 0) {
			sFPS = (int) (1000 * sFpsNumSamples / sFpsTotalTime);
		}
		
		return dt;
	}
	
	
	// Constants
	
	private static final int NUM_FPS_SAMPLES = 100;
	
	
	// Static variables
	
	private static int sFPS = 0;
	private static long[] sFpsSamples;
	private static int sFpsNextSample = 0;
	private static int sFpsNumSamples = 0;
	private static long sFpsTotalTime = 0;
	private static long sLastTime = -1;
	
	static {
		sFpsSamples = new long[NUM_FPS_SAMPLES];
		for (int i = 0; i < NUM_FPS_SAMPLES; i++) {
			sFpsSamples[i] = 0;
		}
	}
}
