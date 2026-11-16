package edu.hitsz.observer;

public class BombPropSubject extends PropSubject {

    @Override
    public int notifyObservers() {
        int gainedScore = 0;
        for (PropObserver observer : observers) {
            gainedScore += observer.onBomb();
        }
        observers.clear();
        System.out.println("炸弹道具触发，获得分数：" + gainedScore);
        return gainedScore;
    }
}
