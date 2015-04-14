package com.rinf.bringx.EasyBindings;
import java.util.*;

public class ObservableCollection<Type> extends ArrayList<Type> {
	private static final long serialVersionUID = 2971425900090512797L;
	
	private List<INotifier<ObservableCollection<Type>>> m_Listeners = new ArrayList<INotifier<ObservableCollection<Type>>>();
	
	public void addObserver(INotifier<ObservableCollection<Type>> observer) {
		m_Listeners.add(observer);
	}
	
	private void FireNotification() {
		for (INotifier<ObservableCollection<Type>> iterable_element : m_Listeners) {
			iterable_element.OnValueChanged(this);
		}
		
		Count.set(this.size());
	}
	
	public Observable<Integer> Count = new Observable<Integer>();
	
	@Override
	public boolean add(Type object) {		
		super.add(object);		
		FireNotification();
		
		return true;
	}
	
	@Override
	public void clear() {
		super.clear();
		FireNotification();
	}
	
	@Override
	public Type remove(int location) {
		Type rem = super.remove(location);
		FireNotification();
		
		return rem;
	}

	@Override
	public boolean remove(Object object) {
		Boolean rem = super.remove(object);
		FireNotification();
		return rem;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		Boolean rem = super.removeAll(arg0);
		FireNotification();
		return rem;
	}
}
