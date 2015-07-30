package com.example.dcwa.auxclasses;

import android.content.Context;
import android.widget.Toast;

public class ToastFactory {

	Toast toast;
	
	Context context;
	String message;
	int duration;
	
	public void Create(String message, int duration, Context context) {
		
		Cancel();
		
		this.message = message;
		this.duration = duration;
		this.context = context;
	}
	
	public void Show() {
		
		Cancel();
		
		toast = Toast.makeText(context, message, duration);
		toast.show();
	}
	
	public void Cancel() {
		if( toast != null ) {
			toast.cancel();
		}
	}
}
