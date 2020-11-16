package com.example.project.CoinFlipModel;

import java.util.ArrayList;
import java.util.List;

/**
 * CoinFlipQueue: (Singleton)
 *  A class to keep track of the children's coin flip order
 *  Children in the back are last the get the flip
 *  New children are added to the back.
 *  Can remove children that are no longer there.
 */

public class CoinFlipQueue {

    private static CoinFlipQueue instance;
    private List<Integer> childIdQueue = new ArrayList<>();

    public static CoinFlipQueue getInstance(){
        if (instance == null){
            instance = new CoinFlipQueue();
        }
        return instance;
    }

    public void addId(int id){
        childIdQueue.add(id);
    }

    public void removeId(int id){
        childIdQueue.remove(id);
    }

    public void putToBack(int id){
        childIdQueue.remove(id);
        childIdQueue.add(id);
    }

    public void setQueue(List<Integer> savedQueue){
        childIdQueue = savedQueue;
    }

    public void removeMissingIds(List<Integer> childIdList){

        List<Integer> newChildIdQueue = new ArrayList<>();

        for(int id : childIdQueue){
            if(childIdList.contains(id)){
                newChildIdQueue.add(id);
            }
        }

        childIdQueue = newChildIdQueue;
    }

    public void addMissingNewIds(List<Integer> childIdList){
        for(int id : childIdList){
            if(!childIdQueue.contains(id)){
                childIdQueue.add(id);
            }
        }
    }



}
