package com.harry.bytecode.cglib;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

/**
 * @author zhouhong
 * @version 1.0
 * @title: ChangeListenerAbstract
 * @description: TODO
 * @date 2019/11/25 11:13
 */
public class ChangeListenerAbstract {

    public void addPropertyChangeListener(PropertyChangeListener listener) {}

    public void removePropertyChangeListener(PropertyChangeListener listener) {}

    public void addVetoableChangeListener(VetoableChangeListener listener) {}

    public void removeVetoableChangeListener(VetoableChangeListener listener) {}
}
