package com.harry.bytecode.javadynamicproxy;

import com.harry.bytecode.model.Person;

import java.beans.*;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.harry.bytecode.model.Person.isNumeric;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * @author zhouhong
 * @version 1.0
 * @title: RunnerDemo
 * @description: TODO
 * @date 2019/11/25 10:32
 */
public class RunnerDemo {

    public static void main(String[] args) throws Exception {

        Person person = new Person();

        // Java 动态代理
        // 动态代理的对象它是 java.lang.reflect.Proxy 的子类（实例），
        // 换言之，动态代理的对象所在类是动态合成，它继承了 java.lang.reflect.Proxy 类，同时，实现了多期望的接口列表
        // 缺点：只能拦截接口，不能拦截整个类，势必对编程不太友好
        Nameable nameable = newInstance(person);

        nameable.name("harry");
        nameable.name("zhouhong");
        nameable.name("123456789");

    }

    static Nameable newInstance(Person person) {

        ClassLoader classLoader = RunnerDemo.class.getClassLoader();

        PublishingPropertyEventInvocationHandler handler = new PublishingPropertyEventInvocationHandler(person);

        // 添加 PropertyChangeListener
        handler.addPropertyChangeListener(event -> {
            // 属性变化通知事件
            System.out.printf("监听到属性[%s] 内容变化（事件来源：%s），老值：%s，新值：%s\n",
                    event.getPropertyName(),
                    event.getSource(),
                    event.getOldValue(),
                    event.getNewValue()
            );
        });

        // 添加 VetoableChangeListener
        handler.addVetoableChangeListener(event -> {
            String newValue = valueOf(event.getNewValue());
            if (isNumeric(newValue)) {
                throw new PropertyVetoException(
                        format("当前属性[%s]的新值[%s]不合法，不能为纯数字!", event.getPropertyName(), newValue), event);
            }
        });

        return (Nameable) newProxyInstance(classLoader, new Class[]{Nameable.class, Serializable.class}, handler);

    }
}

class PublishingPropertyEventInvocationHandler implements InvocationHandler {

    private final Person person;

    private final transient PropertyChangeSupport propertyChangeSupport;

    private final transient VetoableChangeSupport vetoableChangeSupport;

    PublishingPropertyEventInvocationHandler(Person person) {
        this.person = person;
        this.propertyChangeSupport = new PropertyChangeSupport(person);
        this.vetoableChangeSupport = new VetoableChangeSupport(person);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();

        if ("name".equals(methodName) &&                  // 方法名判断
                void.class.equals(method.getReturnType()) && // 方法返回类型判断
                args.length == 1 &&                          // 方法参数数量
                args[0] instanceof String                     // 方法参数类型的判断
        ) {
            // 当 name 属性变化时，发送通知
            // 勉强属性（Constrained properties）:当属性变化不合适时，阻断属性更新，并且通知异常来说明
            String propertyName = "name";

            String oldValue = this.person.getName(); // 读取老值
            String newValue = (String) args[0];     // 修改后值(newValue)
            // 勉强属性（Constrained properties）必须在更新前执行
            // 校验规则：当名称为纯数字时，阻断更新
            // 当 PropertyVetoException 异常发生时
            fireVetoableChange(propertyName, oldValue, newValue);
            // 调整 person.name 内容
            this.person.setName(newValue);
            // 发布属性已经变化事件 - PropertyChangeEvent
            // 强迫属性（Bound Properties）：当属性变化时，强制更新并且发送通知属性变化通知事件
            firePropertyChange(propertyName, oldValue, newValue);

        }

        return null;
    }

    private void firePropertyChange(String propertyName, String oldValue, String newValue) {
        // 广播事件
        // 得到所有 PropertyChangeEvent 监听器
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vetoableChangeSupport.getVetoableChangeListeners();
    }
}
