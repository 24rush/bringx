package com.rinf.bringx.EasyBindings;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.rinf.bringx.utils.Log;
import com.rinf.bringx.EasyBindings.Bindings.Mode;

class OneWayBinding <ControlType, ValueType> {

	protected INotifier<ValueType> _notifier;
	protected Observable<ValueType> _observable;
	protected Bindings _bindings = new Bindings();

	public void Bind(Observable<ValueType> observable, Bindings.Mode flag, INotifier<ValueType> notifier) {
		_notifier = notifier;
		Bind(null, observable, flag);
	}
	
	public void Bind(ControlType control, Observable<ValueType> value, Bindings.Mode flag) {
		if (_notifier == null)
			return;
					
		_observable = value;
		
		_observable.addObserver(_notifier);
		_notifier.OnValueChanged(value.get());		
	}

	public void Destroy() {
		_observable.removeObserver(_notifier);
	};
}

class ViewProperties {	
	public static Enabled Enabled() { return new Enabled(); }
	public static ImageURI ImageURI() { return new ImageURI(); }
	public static ImageBitmap ImageBitmap() { return new ImageBitmap(); }
	public static Checked Checked() { return new Checked(); }
	public static Text Text() { return new Text(); }
	public static Visible Visible() { return new Visible(); }
	public static VisibleString VisibleString() { return new VisibleString(); }
}

class Changed <ControlType, ValueType> extends OneWayBinding<ControlType, ValueType> {
	protected Observable<ValueType> _observable;	

	@Override
	public void Bind(Observable<ValueType> observable, Mode flag, INotifier<ValueType> notifier) {
		_notifier = notifier;
		super.Bind(null, observable, flag);
	}		
}

class Enabled extends OneWayBinding<View, Boolean> {
	@Override
	public void Bind(final View control, Observable<Boolean> value, final Mode flag) {
		// Set the notifier and let the base class do the rest
		_notifier = new INotifier<Boolean>()
		{			
			@Override
			public void OnValueChanged(Boolean value) {			
				if (flag == Mode.Invert) value = !value;				
				control.setEnabled(value);
				control.setClickable(value);
			}
		};

		super.Bind(control, value, flag);			
	}
}

class ImageURI extends OneWayBinding<ImageView, String> {
	@Override
	public void Bind(final ImageView control, Observable<String> value, Mode flag) {
		_notifier = new INotifier<String>() {			
			@Override
			public void OnValueChanged(String value) {
				if (value != null && !value.isEmpty())
					control.setImageURI(Uri.parse(value));
			}
		};

		super.Bind(control, value, flag);
	}	
}

class ImageBitmap extends OneWayBinding<ImageView, Bitmap> {
	@Override
	public void Bind(final ImageView control, Observable<Bitmap> value, Mode flag) {		
		_notifier = new INotifier<Bitmap>() {			
			@Override
			public void OnValueChanged(Bitmap value) {
				Log.d("Bitmap" + value);
				control.setImageBitmap(value);	
			}
		};

		super.Bind(control, value, flag);
	}	
}

class Visible extends OneWayBinding<View, Boolean> {
	@Override
	public void Bind(final View control, Observable<Boolean> value, final Mode flag) {		
		_notifier = new INotifier<Boolean>() 
		{			
			@Override
			public void OnValueChanged(Boolean value) {
				if (flag == Mode.Invert) value = !value;

				control.setVisibility(value == true ? View.VISIBLE : View.GONE);
			}
		};

		super.Bind(control, value, flag);
	}	
}

class VisibleString extends OneWayBinding<View, String> {
	@Override
	public void Bind(final View control, Observable<String> value, final Mode flag) {		
		_notifier = new INotifier<String>() 
		{			
			@Override
			public void OnValueChanged(String value) {
				Boolean c = false;
				Converters.Convert(value, c);

				if (flag == Mode.Invert) c = !c;
				control.setVisibility(c == true ? View.VISIBLE : View.GONE);			
			}
		};

		super.Bind(control, value, flag);
	}
}

class Checked extends OneWayBinding<CheckBox, Boolean> {
	private OnCheckedChangeListener _checkedListener;
	private CheckBox _ctrlCheckBox;

	@Override
	public void Bind(final CheckBox control, final Observable<Boolean> value, final Mode flag) {
		_notifier = new INotifier<Boolean>() {		
			@Override
			public void OnValueChanged(Boolean value) {
				if (flag == Mode.Invert) value = !value;
				control.setChecked(value);				
			}
		};

		super.Bind(control, value, flag);

		if (flag == Mode.TwoWay) {
			if (_checkedListener == null) {
				_ctrlCheckBox = control;
				_checkedListener = new OnCheckedChangeListener() {					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {							
						value.set(isChecked);
					}
				};

				control.setOnCheckedChangeListener(_checkedListener);
			}									
		}
	}
	
	@Override
	public void Destroy() {
		super.Destroy();
		
		if (_checkedListener == null)
			return;
		
		_ctrlCheckBox.setOnCheckedChangeListener(null);
		
		_ctrlCheckBox = null;
		_checkedListener = null;
	}
}

class Text extends OneWayBinding<View, String> {
	private TextWatcher _textWatcher;
	private View _control;
	
	@Override
	public void Bind(final View control, final Observable<String> value, Mode flag) {
		_notifier = new INotifier<String>() {
			@Override
			public void OnValueChanged(String value) {				
				TextView txtCtrl = (TextView) control;
				if (txtCtrl.getText().toString().equals(value) == false) 
					txtCtrl.setText(value);	
			}
		};

		super.Bind(control, value, flag);

		if (flag == Mode.TwoWay && _textWatcher == null) {
			_control = control;
			_textWatcher = new TextWatcher() {				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {					
					value.set(s.toString());
				}
			};										

			((TextView)control).addTextChangedListener(_textWatcher);
		}
	}
	
	@Override
	public void Destroy() {
		super.Destroy();
		
		if (_textWatcher == null)
			return;
		
		((TextView)_control).removeTextChangedListener(_textWatcher);
		
		_textWatcher = null;
		_control = null;
	}
}