package com.gregmcnew.android.pax;

import android.os.SystemClock;

public class FramerateCounter {
	
	public static float getFPS() {
		return sStarted ? sFps : 0;
	}
	
	public static float getTotalFPS() {
		return sStarted ? sTotalFps : 0;
	}
	
	public static long getRecentJitter() {
		return sStarted ? sRecentJitterMs : 0;
	}
	
	public static long getMaxJitter() {
		return sStarted ? sMaxJitterMs : 0;
	}
	
	public static void start() {
		for (int i = 0; i < NUM_FPS_SAMPLES; i++) {
			sRecentSamples[i] = 0;
		}
		
		sNextRecentSampleIndex = 0;
		sNumRecentSamples = 0;
		sRecentSampleTimeSum = 0;
		sFps = 0;
		
		sRecentJitterTime = 0;
		sRecentJitterMs = 0;
		sMaxJitterMs = 0;
		
		sTotalTime = 0;
		sNumTotalSamples = 0;
		sTotalFps = 0;
		
		sLastTime = SystemClock.uptimeMillis();
		
		sStarted = true;
	}
	
	// Returns the number of milliseconds since the last call to start() or
	// tick(). (Returns 0 if start() hasn't been called.)
	public static long tick() {
		
		if (Constants.SELF_BENCHMARK) {
			return Pax.UPDATE_INTERVAL_MS;
		}
		
		long dt = 0;
		
		if (sStarted) {

			long time = SystemClock.uptimeMillis();
			
			dt = time - sLastTime;
			
			// Recent jitter will be measured as the largest delta to occur
			// within the last second. (That's not exactly what this code does,
			// since all jitter values 1 second following a peak are ignored,
			// even if they were higher than the jitter value which follows the
			// 1-second window. This is close enough, though.)
			if (dt > sRecentJitterMs || sRecentJitterTime + 1000 < time) {
				sRecentJitterMs = dt;
				sRecentJitterTime = time;
			}
			
			if (dt > sMaxJitterMs && sNumTotalSamples > 0) {
				sMaxJitterMs = dt;
			}
			
			sLastTime = time;
			
			sRecentSampleTimeSum += dt - sRecentSamples[sNextRecentSampleIndex];
			sRecentSamples[sNextRecentSampleIndex] = dt;
			
			sTotalTime += dt;
			sNumTotalSamples++;
			
			sTotalFps = (float) (1000 * sNumTotalSamples) / sTotalTime;
			
			sNextRecentSampleIndex = (sNextRecentSampleIndex + 1) % NUM_FPS_SAMPLES;
			if (sNextRecentSampleIndex > sNumRecentSamples) {
				sNumRecentSamples = sNextRecentSampleIndex;
			}
			
			if (sRecentSampleTimeSum > 0) {
				sFps = (float) (1000 * sNumRecentSamples) / sRecentSampleTimeSum;
			}
		}
		
		return dt;
	}
	
	
	// Constants
	
	private static final int NUM_FPS_SAMPLES = 100;
	
	
	// Static variables
	
	private static final long[] sRecentSamples = new long[NUM_FPS_SAMPLES];
	private static float sFps;
	private static int sNumRecentSamples;
	private static int sNextRecentSampleIndex;
	private static long sRecentSampleTimeSum;
	private static long sLastTime;
	
	private static long sRecentJitterTime;
	private static long sRecentJitterMs;
	private static long sMaxJitterMs;
	
	private static long sTotalTime;
	private static int sNumTotalSamples;
	private static float sTotalFps;
	
	private static boolean sStarted = false;
}
