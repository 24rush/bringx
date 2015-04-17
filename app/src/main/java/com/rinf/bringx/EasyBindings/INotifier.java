package com.rinf.bringx.EasyBindings;

public interface INotifier<Type extends Object> {
	public void OnValueChanged(Type value);
}

