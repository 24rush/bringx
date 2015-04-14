package com.rinf.bringx.EasyBindings;

import com.rinf.bringx.utils.Log;

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;

public class Controls {
	
	private Activity m_Activity;
	
	private SparseArray<View> m_Controls = new SparseArray<View>();
	
	public Controls(Activity _parent) {
		m_Activity = _parent;		
		m_Controls.clear();
		
		if (_parent == null)
			return;							
	}
	
	@SuppressWarnings("unchecked")
	public <Type extends View> Type get(int _ctrlId) {
		View ctrl = m_Controls.get(_ctrlId); 
		
		if (ctrl == null) {
			ctrl = m_Activity.findViewById(_ctrlId);
			
			if (ctrl == null) {
				Log.e("Control ID " + _ctrlId + " does not exist.");
			}					
		}
		
		return (Type) ctrl;
	}		
}
