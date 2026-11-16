package edu.hitsz.observer;

public class FreezePropSubject extends PropSubject {

    @Override
    public int notifyObservers() {
        for (PropObserver observer : observers) {
            observer.onFreeze();
        }
        observers.clear();
        System.out.println("冰冻道具触发");
        return 0;
    }
}
