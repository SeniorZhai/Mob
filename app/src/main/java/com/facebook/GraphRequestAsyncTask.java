package com.facebook;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

public class GraphRequestAsyncTask extends AsyncTask<Void, Void, List<GraphResponse>> {
    private static final String TAG = GraphRequestAsyncTask.class.getCanonicalName();
    private static Method executeOnExecutorMethod;
    private final HttpURLConnection connection;
    private Exception exception;
    private final GraphRequestBatch requests;

    static {
        for (Method method : AsyncTask.class.getMethods()) {
            if ("executeOnExecutor".equals(method.getName())) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 2 && parameters[0] == Executor.class && parameters[1].isArray()) {
                    executeOnExecutorMethod = method;
                    return;
                }
            }
        }
    }

    public GraphRequestAsyncTask(GraphRequest... requests) {
        this(null, new GraphRequestBatch(requests));
    }

    public GraphRequestAsyncTask(Collection<GraphRequest> requests) {
        this(null, new GraphRequestBatch((Collection) requests));
    }

    public GraphRequestAsyncTask(GraphRequestBatch requests) {
        this(null, requests);
    }

    public GraphRequestAsyncTask(HttpURLConnection connection, GraphRequest... requests) {
        this(connection, new GraphRequestBatch(requests));
    }

    public GraphRequestAsyncTask(HttpURLConnection connection, Collection<GraphRequest> requests) {
        this(connection, new GraphRequestBatch((Collection) requests));
    }

    public GraphRequestAsyncTask(HttpURLConnection connection, GraphRequestBatch requests) {
        this.requests = requests;
        this.connection = connection;
    }

    protected final Exception getException() {
        return this.exception;
    }

    protected final GraphRequestBatch getRequests() {
        return this.requests;
    }

    public String toString() {
        return "{RequestAsyncTask: " + " connection: " + this.connection + ", requests: " + this.requests + "}";
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (this.requests.getCallbackHandler() == null) {
            this.requests.setCallbackHandler(new Handler());
        }
    }

    protected void onPostExecute(List<GraphResponse> result) {
        super.onPostExecute(result);
        if (this.exception != null) {
            Log.d(TAG, String.format("onPostExecute: exception encountered during request: %s", new Object[]{this.exception.getMessage()}));
        }
    }

    protected List<GraphResponse> doInBackground(Void... params) {
        try {
            if (this.connection == null) {
                return this.requests.executeAndWait();
            }
            return GraphRequest.executeConnectionAndWait(this.connection, this.requests);
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    GraphRequestAsyncTask executeOnSettingsExecutor() {
        if (executeOnExecutorMethod != null) {
            try {
                executeOnExecutorMethod.invoke(this, new Object[]{FacebookSdk.getExecutor(), null});
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e2) {
            }
        } else {
            execute(new Void[0]);
        }
        return this;
    }
}
