package edu.hitsz.factory;
import edu.hitsz.supply.AbstractSupply;
import edu.hitsz.supply.BloodSupply;
import edu.hitsz.supply.BombSupply;
import edu.hitsz.supply.FirePlusSupply;
import edu.hitsz.supply.FireSupply;
import edu.hitsz.supply.FreezeSupply;

import java.util.concurrent.ThreadLocalRandom;

public class SupplyFactory {

    private static final int SUPPLY_SPEED_Y = 6;

    public AbstractSupply createSupply(SupplyType type, int locationX, int locationY) {
        switch (type) {
            case BLOOD:
                return new BloodSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
            case FIRE:
                return new FireSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
            case FIRE_PLUS:
                return new FirePlusSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
            case BOMB:
                return new BombSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
            case FREEZE:
                return new FreezeSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
            default:
                throw new IllegalArgumentException("Unsupported supply type: " + type);
        }
    }

    public AbstractSupply createRandomSupply(SupplyType[] supplyTypes, int locationX, int locationY) {
        if (supplyTypes == null || supplyTypes.length == 0) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(supplyTypes.length);
        SupplyType supplyType = supplyTypes[index];
        return createSupply(supplyType, locationX, locationY);
    }
}