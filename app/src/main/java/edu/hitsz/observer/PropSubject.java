package edu.hitsz.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class PropSubject {

    protected final List<PropObserver> observers = new ArrayList<>();

    public void addObserver(PropObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(PropObserver observer) {
        observers.remove(observer);
    }

    public abstract int notifyObservers();
}
