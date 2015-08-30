package com.remoty.common;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Bogdan on 8/29/2015.
 * <p/>
 * The purpose of this class is to create an abstraction between the design and the functionality.
 * This way the backend is not dependent on the design of the views. The backend will provide the
 * functionality of the views while this class will take care of the design.
 */
public class ViewFactory {

	public enum ButtonType {
		BUTTON_SERVER_INFO
	}

	public static Button getButton(ButtonType type, Context context) {

		switch (type) {
			case BUTTON_SERVER_INFO:
				return createButtonServerInfo(context);
			default:
				return null;
		}
	}

	/**
	 * Basic server info button design. The content of this method will be replaced when the design
	 * for this button will be ready.
	 */
	private static Button createButtonServerInfo(Context context) {

		Button button = new Button(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		button.setLayoutParams(params);

		return button;
	}

}
