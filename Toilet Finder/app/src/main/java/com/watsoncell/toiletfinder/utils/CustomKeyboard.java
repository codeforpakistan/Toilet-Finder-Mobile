
package com.watsoncell.toiletfinder.utils;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class CustomKeyboard {

	/**
	 * A link to the KeyboardView that is used to render this CustomKeyboard.
	 */
	private KeyboardView mKeyboardView;
	/** A link to the activity that hosts the {@link #mKeyboardView}. */
	private Activity mHostActivity;

	private KeyboardView kv;
	private Keyboard keyboard;

	private boolean caps = false;

	/** The key (code) handler. */
	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

		// public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
		// public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
		// public final static int CodePrev = 55000;
		// public final static int CodeAllLeft = 55001;
		// public final static int CodeLeft = 55002;
		// public final static int CodeRight = 55003;
		// public final static int CodeAllRight = 55004;
		// public final static int CodeNext = 55005;
		 private final static int CodeClear = 55006;
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			try {
				// NOTE We can say '<Key android:codes="49,50" ... >' in the xml
				// file; all codes come in keyCodes, the first in this list in
				// primaryCode
				// Get the EditText and its Editable
				View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
				// if( focusCurrent==null ||
				// focusCurrent.getClass()!=EditText.class) return;
				if (focusCurrent == null)
					return;
				EditText edittext = (EditText) focusCurrent;
				Editable editable = edittext.getText();
				int start = edittext.getSelectionStart();

				switch (primaryCode) {
				case Keyboard.KEYCODE_DELETE:
					if (editable != null && start > 0)
						editable.delete(start - 1, start);
					break;
				case Keyboard.KEYCODE_SHIFT:
					caps = !caps;
					keyboard.setShifted(caps);
					kv.invalidateAllKeys();
					break;
				case CodeClear:
					edittext.setText("");
					break;
					
				case Keyboard.KEYCODE_DONE:
					// editable..sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					// KeyEvent.KEYCODE_ENTER));
					/*if (!edittext.getText().toString().equals("")) {
						edittext.setText("");
						hideCustomKeyboard();
					} else {
						showToast("No word found");
					}*/
					hideCustomKeyboard();
					break;
				default:
					char code = (char) primaryCode;
					if (Character.isLetter(code) && caps) {
						code = Character.toUpperCase(code);
					}
					// ic.commitText(String.valueOf(code),1);
					editable.insert(start, Character.toString((char) primaryCode));
				}
			} catch (Exception ex) {
			}
		}

		@Override
		public void onPress(int arg0) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeUp() {
		}
	};

	public CustomKeyboard(Activity host, KeyboardView keyboardView, int layoutid) {
		mHostActivity = host;
		mKeyboardView = keyboardView;
		mKeyboardView.bringToFront();
		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview
												// balloons
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
		// Hide the standard keyboard initially
		mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/** Returns whether the CustomKeyboard is visible. */
	public boolean isCustomKeyboardVisible() {
		return mKeyboardView.getVisibility() == View.VISIBLE;
	}

	/**
	 * Make the CustomKeyboard visible, and hide the system keyboard for view v.
	 */
	public void showCustomKeyboard(View v) {

		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);
		if (v != null)
			((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/** Make the CustomKeyboard invisible. */
	public void hideCustomKeyboard() {
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}

	public void registerEditText(final EditText edittext) {
		// Find the EditText 'resid'
		// final EditText edittext= (EditText)mHostActivity.findViewById(resid);
		// Make the custom keyboard appear
		edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
			// NOTE By setting the on focus listener, we can show the custom
			// keyboard when the edit box gets focus, but also hide it when the
			// edit box loses focus
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (hasFocus)
					showCustomKeyboard(v);
				else
					hideCustomKeyboard();
			}
		});
		edittext.setOnClickListener(new OnClickListener() {
			// NOTE By setting the on click listener, we can show the custom
			// keyboard again, by tapping on an edit box that already had focus
			// (but that had the keyboard hidden).
			@Override
			public void onClick(View v) {
				edittext.requestFocus();
				int start = edittext.length();
				if(start > 0){
					edittext.setSelection(start);
				}
				showCustomKeyboard(v);
			}
		});
		// Disable standard keyboard hard way
		// NOTE There is also an easy way:
		// 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a
		// cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
		edittext.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				EditText edittext = (EditText) v;
				int inType = edittext.getInputType(); // Backup the input type
				edittext.setInputType(InputType.TYPE_NULL); // Disable standard
															// keyboard
				edittext.onTouchEvent(event); // Call native handler
				edittext.setInputType(inType); // Restore input type
				return true; // Consume touch event
			}
		});
		// Disable spell check (hex strings look like words to Android)
		edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}

	public void showToast(String toast) {
		Toast.makeText(mHostActivity, toast, Toast.LENGTH_LONG).show();
	}

	/*
	 * private void playClick(int keyCode){ AudioManager am = (AudioManager)
	 * mHostActivity.getSystemService(AUDIO_SERVICE); switch(keyCode){ case 32:
	 * am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR); break; case
	 * Keyboard.KEYCODE_DONE: case 10:
	 * am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN); break; case
	 * Keyboard.KEYCODE_DELETE:
	 * am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE); break; default:
	 * am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD); } }
	 */

}

// NOTE How can we change the background color of some keys (like the
// shift/ctrl/alt)?
// NOTE What does android:keyEdgeFlags do/mean
