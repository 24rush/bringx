package com.rinf.bringx.EasyBindings;

public interface IContextNotifier<Type> {
    public void OnValueChanged(Type value, Object context);
}

class ContextualListener<Type> {
    public Object m_Context;
    public IContextNotifier<Type> m_Observer;

    public ContextualListener(IContextNotifier<Type> observer, Object context) {
        m_Context = context;
        m_Observer = observer;
    }
}