package com.rinf.bringx.EasyBindings;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

public class Bindings {
    public enum Mode { None, Invert, TwoWay };
	public enum BindingType { TEXT, COMMAND, NONE };

	private List<OneWayBinding <?, ?>> m_Bindings = new ArrayList<OneWayBinding <?, ?>>();

	private void addBinding(OneWayBinding <?, ?> binding) {
		m_Bindings.add(binding);		
	}

	public void Destroy() {
		for (OneWayBinding <?, ?> binding : m_Bindings) {
			binding.Destroy();
		}
	}
	
	private <ControlType, Type> void bind(final Observable<Type> observable, final OneWayBinding<ControlType, Type> action, INotifier<Type> notifier, final Mode flag) {
		action.Bind(observable, flag, notifier);	

		addBinding(action);
	}
	
	private <ControlType, ValueType> void bind(final ControlType control, final Observable<ValueType> observable, final OneWayBinding<ControlType, ValueType> action, final Mode flag) {
		action.Bind(control, observable, flag);	

		addBinding(action);
	}

	// Enabled

	public void BindEnabled(final View control, Observable<Boolean> source) {
		bind(control, source, ViewProperties.Enabled(), Mode.None);						
	}

	// Checked

	public void BindChecked(final CheckBox control, final Observable<Boolean> source, Mode flags) {		
		bind(control, source, ViewProperties.Checked(), flags);			
	}

	// ImageURI

	public void BindImageURI(final ImageView control, Observable<String> source) {
		bind(control, source, ViewProperties.ImageURI(), Mode.None);
	}

	// ImageBitmap

	public void BindImageBitmap(final ImageView control, Observable<Bitmap> source) {
		bind(control, source, ViewProperties.ImageBitmap(), Mode.None);
	}

	//
	// Visibility
	//

	public void BindVisible(final View control, Observable<Boolean> source) {
		bind(control, source, ViewProperties.Visible(), Mode.None);
	}

	public void BindVisible(final View control, Observable<Boolean> source, final Mode flag) {
		bind(control, source, ViewProperties.Visible(), flag);
	}

	// 
	// Text
	//

	public void BindText(final View control, Observable<String> source) {
		bind(control, source, ViewProperties.Text(), Mode.None);
	}

	public void BindText(final View control, final Observable<String> source, final Mode flag) {
		bind(control, source, ViewProperties.Text(), flag);
	}

	//
	// Commands
	//

	public <Type> void BindCommand(final View source, final ICommand<Type> target, final Type context) {
		source.setTag(context);
		source.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				target.Execute(context);
			}
		});			
	}

	public <Type> void BindCommand(final CheckBox source, final ICommand<Type> target, final Type context) {
		source.setTag(context);
		source.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				target.Execute(context);				
			}
		});		
	}
	
	public <Type> void BindChanged(final Observable<Type> observable, final INotifier<Type> onChanged) {
		bind(observable, new Changed<View, Type>(), onChanged, Mode.None);
	}
}