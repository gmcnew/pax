package com.gregmcnew.android.pax;

import android.os.SystemClock;

public class FramerateCounter {
	
	public static int getFPS() {
		return sFPS;
	}
	
	public static void start() {
		sFpsSamples = new long[NUM_FPS_SAMPLES];
		for (int i = 0; i < NUM_FPS_SAMPLES; i++) {
			sFpsSamples[i] = 0;
		}
		
		sLastTime = SystemClock.uptimeMillis();
	}
	
	// Returns the number of milliseconds since the previous tick (or 0 on the
	// first tick).
	public static long tick() {
		
		if (sLastTime == -1) {
			return 0;
		}

		long time = SystemClock.uptimeMillis();
		
		long dt = time - sLastTime;
		
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
}
