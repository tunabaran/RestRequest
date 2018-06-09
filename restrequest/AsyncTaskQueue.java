package com.tunabaranurut.restrequest;

import android.util.Log;

import com.tunabaranurut.restrequest.http.AsyncRequestTask;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tunabaranurut on 9.06.2018.
 */

public class AsyncTaskQueue {

    private static String TAG = AsyncTaskQueue.class.getSimpleName();

    private static AsyncTaskQueue instance;

    private List<AsyncRequestTask> taskList;

    public AsyncTaskQueue() {
        this.taskList = new LinkedList<>();
    }

    public void add(AsyncRequestTask task){
        synchronized (taskList) {
            taskList.add(task);
        }
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: added task to queue. " + task.toString());
        }
    }

    public void remove(AsyncRequestTask task){
        synchronized (taskList) {
            taskList.remove(task);
        }
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: removed task from queue. " + task.toString());
        }
    }

    public void cancelAllTasks(){
        synchronized (taskList) {
            for (AsyncRequestTask task : taskList) {
                task.cancel(true);
            }
        }
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: cancelled all tasks. ");
        }
    }

    public static AsyncTaskQueue getInstance(){
        if(instance == null){
            instance = new AsyncTaskQueue();
        }
        return instance;
    }
}
