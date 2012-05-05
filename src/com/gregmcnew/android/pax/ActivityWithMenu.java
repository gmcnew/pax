package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Implements menu behavior common to IntroActivity and Pax.
 */
public class ActivityWithMenu extends Activity {

	private static Map<String, AI.Difficulty> sAIDifficulties = null;
	private static Map<String, Float> sGameSpeeds = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
		
		/*
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		*/
        
        return true;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        applyPreferences();
    }
    
    private void applyPreferences() {
		
		Resources res = getResources();
		if (sGameSpeeds == null) {
			sGameSpeeds = new HashMap<String, Float>();
			sGameSpeeds.put(res.getString(R.string.game_speed_normal), Constants.GAME_SPEED_NORMAL);
			sGameSpeeds.put(res.getString(R.string.game_speed_fast),   Constants.GAME_SPEED_FAST);
			sGameSpeeds.put(res.getString(R.string.game_speed_insane), Constants.GAME_SPEED_INSANE);
		}
		
		if (sAIDifficulties == null) {
			sAIDifficulties = new HashMap<String, AI.Difficulty>();
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_braindead), AI.Difficulty.BRAINDEAD);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_easy),      AI.Difficulty.EASY);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_medium),    AI.Difficulty.MEDIUM);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_hard),      AI.Difficulty.HARD);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_very_hard), AI.Difficulty.VERY_HARD);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_insane),    AI.Difficulty.INSANE);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_cheater),   AI.Difficulty.CHEATER);
		}
		
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
    	Constants.sAIDifficulty = sAIDifficulties.get(settings.getString(getString(R.string.ai_difficulty), null));
    	if (Constants.sAIDifficulty == null) {
    		Constants.sAIDifficulty = AI.Difficulty.EASY;
    	}
    	
    	String gameSpeedString = settings.getString(getString(R.string.game_speed), null);
    	Float gameSpeed = sGameSpeeds.get(gameSpeedString);
    	Constants.sGameSpeed = (gameSpeed == null) ? Constants.GAME_SPEED_NORMAL : gameSpeed;
    	Constants.sShowShips = settings.getBoolean(getString(R.string.show_ships), true);
    	Constants.sShowParticles = settings.getBoolean(getString(R.string.show_particles), true);
    	Constants.sShowFPS = settings.getBoolean(getString(R.string.show_fps), false);
    	Constants.sShowHealthForAllShipTypes = settings.getBoolean(getString(R.string.show_all_health), false);
    	Constants.sShowCollisionBoxes = settings.getBoolean(getString(R.string.show_collision_boxes), false);
    	Constants.sFadeOutIntro = settings.getBoolean(getString(R.string.fade_out_intro), true);
    	Constants.sSound = settings.getBoolean(getString(R.string.sound), false);
    	Constants.sBenchmarkMode = settings.getBoolean(getString(R.string.benchmark_mode), false);
    	Constants.sVertexBufferObjects = settings.getBoolean(getString(R.string.vertex_buffer_objects), true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tutorial:
            	// TODO: Add a tutorial.
            	Toast.makeText(this, "Sorry, buddy, you're on your own!", Toast.LENGTH_LONG).show();
            	break;
            case R.id.settings:
                Intent prefsIntent = new Intent(this, PrefsActivity.class);
                startActivity(prefsIntent);
                break;
        }
        return true;
    }
}
