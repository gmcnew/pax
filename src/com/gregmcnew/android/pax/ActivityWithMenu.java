package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Implements menu behavior common to IntroActivity and Pax.
 */
public class ActivityWithMenu extends Activity {
	
	public static Game.Speed sGameSpeed;
	public static AI.Difficulty sAIDifficulty;
	public static boolean sBlackBackground;
	public static boolean sShowFPS;

	private static Map<String, AI.Difficulty> sAIDifficulties = null;
	private static Map<String, Game.Speed> sGameSpeeds = null;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
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
			sGameSpeeds = new HashMap<String, Game.Speed>();
			sGameSpeeds.put(res.getString(R.string.game_speed_normal), Game.Speed.NORMAL);
			sGameSpeeds.put(res.getString(R.string.game_speed_fast),   Game.Speed.FAST);
			sGameSpeeds.put(res.getString(R.string.game_speed_insane), Game.Speed.INSANE);
		}
		
		if (sAIDifficulties == null) {
			sAIDifficulties = new HashMap<String, AI.Difficulty>();
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_easy),   AI.Difficulty.EASY);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_medium), AI.Difficulty.MEDIUM);
			sAIDifficulties.put(res.getString(R.string.ai_difficulty_hard),   AI.Difficulty.HARD);
		}
		
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

    	sAIDifficulty = sAIDifficulties.get(settings.getString("ai_difficulty_preference", null));
    	sGameSpeed = sGameSpeeds.get(settings.getString("game_speed_preference", null));
    	sBlackBackground = settings.getBoolean("black_background", false);
    	sShowFPS = settings.getBoolean("show_fps", false);
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
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
        }
        return true;
    }
}
