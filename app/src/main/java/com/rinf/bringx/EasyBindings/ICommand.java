package com.rinf.bringx.EasyBindings;

public interface ICommand<Type> {
    void Execute(Type context);
}