package com.rinf.bringx.EasyBindings;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

//
// Validates a list of Observables and sets its own IsValid Observable accordingly 
//

public class Validator {
	class ValidationCache {
		public boolean IsValid;
		public Observable<?> Observable;
	}
	
	private List<ValidationCache> m_ValidationCache = new ArrayList<ValidationCache>(); 
			
	public Observable<Boolean> IsValid = new Observable<Boolean>(false);
	
	public Validator(Observable<?>...observables) {
		for (final Observable<?> observable : observables) {
			final ValidationCache cacheData = new ValidationCache();
			m_ValidationCache.add(cacheData);
			
			observable.addTypelessObserver(new INotifier<Object>() {				
				public void OnValueChanged(Object value) {
					cacheData.IsValid = observable.IsValid();
				
					CheckValidation();
				}
			});
		}
	}
	
	private void CheckValidation() {
		boolean finalResult = true;
		
		for (ValidationCache observableCache : m_ValidationCache) {
			finalResult &= observableCache.IsValid;
		}
	
		IsValid.set(finalResult);
	}
}

interface IValidator<Type> {
	public boolean IsValid(Type value);
}

class Validators {
	public static RequiredString RequiredString = new RequiredString();
	public static RequiredObject RequiredObject = new RequiredObject();
	public static RequiredBitmap RequiredBitmap = new RequiredBitmap();	
}

class RequiredString implements IValidator<String> {
	@Override
	public boolean IsValid(String value) {
		return value != null && !value.equals("");
	}
}

class RequiredObject implements IValidator<Object> {
	@Override
	public boolean IsValid(Object value) {
		return value != null;
	}
}

class RequiredBitmap implements IValidator<Bitmap> {
	@Override
	public boolean IsValid(Bitmap value) {
		return value != null;
	}
}