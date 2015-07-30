package com.example.dcwa.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.desktopcontrolwithandroid.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	public static String helpContent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		if( helpContent == null ) {
			ReadHelpFile();
		}
		
		TextView helpView = (TextView) findViewById(R.id.help_text_view);
		helpView.setText(helpContent);
	}
	
	private void ReadHelpFile() {
		
		helpContent = "";
		String line;
		
        InputStream is = null;
		try {
			is = getAssets().open(getString(R.string.help_file));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader in =  new BufferedReader(new InputStreamReader(is));
		
		try {
			while ((line = in.readLine()) != null) {
				helpContent += line + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
