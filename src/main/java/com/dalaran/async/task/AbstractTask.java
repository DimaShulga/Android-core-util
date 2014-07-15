/*
 * Copyright 2014 Dima Shulga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dalaran.async.task;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;


import com.dalaran.async.task.listener.OnTaskCancelledListener;
import com.dalaran.async.task.listener.OnTaskCompletedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public abstract class AbstractTask<Params, Result> extends AsyncTask<Params, String, Result> {

    protected List<AbstractTask> childrenTasks;
    protected Context context;
    private AtomicReference<Result> result = new AtomicReference<Result>();
    private AtomicReference<Params> params = new AtomicReference<Params>();
    protected AtomicReference<Throwable> error = new AtomicReference<Throwable>();
    private OnTaskCancelledListener onTaskCancelledListener;
    private OnTaskCompletedListener onTaskCompletedListener;
    private ProgressBar progressBar;


    protected AbstractTask() {
        childrenTasks = new ArrayList<AbstractTask>();
    }

    protected AbstractTask(OnTaskCancelledListener onTaskCancelledListener, OnTaskCompletedListener onTaskCompletedListener, ProgressBar progressBar) {
        this.onTaskCancelledListener = onTaskCancelledListener;
        this.onTaskCompletedListener = onTaskCompletedListener;
        this.progressBar = progressBar;
        childrenTasks = new ArrayList<AbstractTask>();
    }

    protected AbstractTask(OnTaskCompletedListener onTaskCompletedListener, ProgressBar progressBar) {
        this.onTaskCompletedListener = onTaskCompletedListener;
        this.progressBar = progressBar;
        childrenTasks = new ArrayList<AbstractTask>();
    }

    protected void clearChildrenTasks() {
        childrenTasks.clear();
    }

    public void addChildrenTask(AbstractTask task) {
        if (childrenTasks != null)
            childrenTasks.add(task);
    }

    public void removeChildrenTask(AbstractTask task) {
        if (task != null && childrenTasks != null && childrenTasks.size() != 0) {
            int position = childrenTasks.indexOf(task);
            childrenTasks.remove(position);
        }
    }

    public void executeChildren(Result res) {
        for (AbstractTask childrenTask : childrenTasks) {
            childrenTask.setParam(res);
            childrenTask.start();
        }
    }

    public void start() {
        Log.d(this.getClass().getSimpleName(), "started");
        execute();
    }

    public void stop() {
        cancel(true);
        if (onTaskCancelledListener != null)
            onTaskCancelledListener.onTaskCancelled(this);
    }


    @Override
    protected final Result doInBackground(Params... params) {
        publishProgress("");
        Result result;
        try {
            result = inBackground();
            this.result.set(result);
        } catch (Throwable e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
        executeChildren(result);

        Log.d(this.getClass().getSimpleName(), "completed" + (getError() != null ? " with error" : " successful"));
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (onTaskCompletedListener != null)
            onTaskCompletedListener.onTaskCompleted(this);

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    public abstract Result inBackground() throws Exception;

    public Params getParam() {
        return params.get();
    }

    public AbstractTask<Params, Result> setParam(Params param) {
        if (param != null) {
            try {
                this.params.set(param);
            } catch (Exception ignored) {
                //classCastException
            }
        }
        return this;
    }

    public OnTaskCancelledListener getOnTaskCancelledListener() {
        return onTaskCancelledListener;
    }

    public void setOnTaskCancelledListener(OnTaskCancelledListener onTaskCancelledListener) {
        this.onTaskCancelledListener = onTaskCancelledListener;
    }

    public OnTaskCompletedListener getOnTaskCompletedListener() {
        return onTaskCompletedListener;
    }

    public AbstractTask<Params, Result> setOnTaskCompletedListener(OnTaskCompletedListener onTaskCompletedListener) {
        this.onTaskCompletedListener = onTaskCompletedListener;
        return this;
    }

    public Result getResult() {
        return result.get();
    }

    public Params getParams() {
        return params.get();
    }

    public void setParams(AtomicReference<Params> params) {
        this.params = params;
    }

    public Context getContext() {
        return context;
    }

    public AbstractTask<Params, Result> setContext(Context context) {
        this.context = context;
        return this;
    }

    public Throwable getError() {
        return error.get();
    }

    public void setError(Throwable error) {
        this.error.set(error);
    }

    public boolean hasError() {
        return error.get() != null;
    }

}
