package com.gregmcnew.android.pax;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class ShakeDetector implements SensorEventListener {
	
	private static final int NUM_SAMPLES = 5;
	
	public ShakeDetector() {
		mRecentDeltas = new float[NUM_SAMPLES];
		for (int i = 0; i < NUM_SAMPLES; i++) {
			mRecentDeltas[i] = 0;
		}
		mLastValues = new float[3];
	}
	
	public float getMagnitude() {
		return (mNumDeltas > 0) ? (mDeltaSum / mNumDeltas) : 0;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float delta = 0;
		int numValues = (mLastValues.length < event.values.length) ? mLastValues.length : event.values.length;
		for (int i = 0; i < numValues; i++) {
			float d = event.values[i] - mLastValues[i];
			mLastValues[i] = event.values[i];
			delta += (d < 0) ? -d : d;
		}
		
		mDeltaSum += delta;
		mDeltaSum -= mRecentDeltas[mNextIndex];
		mRecentDeltas[mNextIndex] = delta;
		mNextIndex = (mNextIndex + 1) % NUM_SAMPLES;
		
		if (mNumDeltas < NUM_SAMPLES) {
			mNumDeltas++;
		}
	}
	
	private int mNumDeltas;
	private int mNextIndex;
	private float mDeltaSum;
	private float mLastValues[];
	private float mRecentDeltas[];
}
