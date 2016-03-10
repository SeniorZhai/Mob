package com.firebase.client.authentication;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.firebase.client.AuthData;
import com.firebase.client.CredentialStore;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.AuthListener;
import com.firebase.client.Firebase.AuthResultHandler;
import com.firebase.client.Firebase.AuthStateListener;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.Firebase.ResultHandler;
import com.firebase.client.Firebase.ValueResultHandler;
import com.firebase.client.FirebaseError;
import com.firebase.client.authentication.util.JsonWebToken;
import com.firebase.client.core.AuthExpirationBehavior;
import com.firebase.client.core.Context;
import com.firebase.client.core.Path;
import com.firebase.client.core.PersistentConnection;
import com.firebase.client.core.Repo;
import com.firebase.client.core.RepoInfo;
import com.firebase.client.utilities.HttpUtilities;
import com.firebase.client.utilities.HttpUtilities.HttpRequestType;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.Utilities;
import com.firebase.client.utilities.encoding.JsonHelpers;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.broadcast.BroadcastService;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class AuthenticationManager {
    static final /* synthetic */ boolean $assertionsDisabled = (!AuthenticationManager.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final String AUTH_DATA_KEY = "authData";
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final String CUSTOM_PROVIDER = "custom";
    private static final String ERROR_KEY = "error";
    private static final String LOG_TAG = "AuthenticationManager";
    private static final String TOKEN_KEY = "token";
    private static final String USER_DATA_KEY = "userData";
    private AuthData authData = null;
    private final PersistentConnection connection;
    private final Context context;
    private AuthAttempt currentAuthAttempt;
    private final Set<AuthStateListener> listenerSet;
    private final LogWrapper logger;
    private final Repo repo;
    private final RepoInfo repoInfo;
    private final CredentialStore store;

    private class AuthAttempt {
        private boolean fired = AuthenticationManager.$assertionsDisabled;
        private final AuthResultHandler handler;
        private final AuthListener legacyListener;

        AuthAttempt(AuthResultHandler handler) {
            this.handler = handler;
            this.legacyListener = null;
        }

        AuthAttempt(AuthListener legacyListener) {
            this.legacyListener = legacyListener;
            this.handler = null;
        }

        public void fireError(final FirebaseError error) {
            if (!(this.fired && this.legacyListener == null)) {
                AuthenticationManager.this.fireEvent(new Runnable() {
                    public void run() {
                        if (AuthAttempt.this.legacyListener != null) {
                            AuthAttempt.this.legacyListener.onAuthError(error);
                        } else if (AuthAttempt.this.handler != null) {
                            AuthAttempt.this.handler.onAuthenticationError(error);
                        }
                    }
                });
            }
            this.fired = true;
        }

        public void fireSuccess(final AuthData authData) {
            if (!(this.fired && this.legacyListener == null)) {
                AuthenticationManager.this.fireEvent(new Runnable() {
                    public void run() {
                        if (AuthAttempt.this.legacyListener != null) {
                            AuthAttempt.this.legacyListener.onAuthSuccess(authData);
                        } else if (AuthAttempt.this.handler != null) {
                            AuthAttempt.this.handler.onAuthenticated(authData);
                        }
                    }
                });
            }
            this.fired = true;
        }

        public void fireRevoked(final FirebaseError error) {
            if (this.legacyListener != null) {
                AuthenticationManager.this.fireEvent(new Runnable() {
                    public void run() {
                        AuthAttempt.this.legacyListener.onAuthRevoked(error);
                    }
                });
            }
            this.fired = true;
        }
    }

    public AuthenticationManager(Context context, Repo repo, RepoInfo repoInfo, PersistentConnection connection) {
        this.context = context;
        this.repo = repo;
        this.repoInfo = repoInfo;
        this.connection = connection;
        this.store = context.getCredentialStore();
        this.logger = context.getLogger(LOG_TAG);
        this.listenerSet = new HashSet();
    }

    private void fireEvent(Runnable r) {
        this.context.getEventTarget().postEvent(r);
    }

    private void fireOnSuccess(final ValueResultHandler handler, final Object result) {
        if (handler != null) {
            fireEvent(new Runnable() {
                public void run() {
                    handler.onSuccess(result);
                }
            });
        }
    }

    private void fireOnError(final ValueResultHandler handler, final FirebaseError error) {
        if (handler != null) {
            fireEvent(new Runnable() {
                public void run() {
                    handler.onError(error);
                }
            });
        }
    }

    private ValueResultHandler ignoreResultValueHandler(final ResultHandler handler) {
        return new ValueResultHandler() {
            public void onSuccess(Object result) {
                handler.onSuccess();
            }

            public void onError(FirebaseError error) {
                handler.onError(error);
            }
        };
    }

    private void preemptAnyExistingAttempts() {
        if (this.currentAuthAttempt != null) {
            this.currentAuthAttempt.fireError(new FirebaseError(-5, "Due to another authentication attempt, this authentication attempt was aborted before it could complete."));
            this.currentAuthAttempt = null;
        }
    }

    private FirebaseError decodeErrorResponse(Object errorResponse) {
        String code = (String) Utilities.getOrNull(errorResponse, BroadcastService.EXTRA_CODE, String.class);
        String message = (String) Utilities.getOrNull(errorResponse, SettingsJsonConstants.PROMPT_MESSAGE_KEY, String.class);
        String details = (String) Utilities.getOrNull(errorResponse, "details", String.class);
        if (code != null) {
            return FirebaseError.fromStatus(code, message, details);
        }
        String errorMessage;
        if (message == null) {
            errorMessage = "Error while authenticating.";
        } else {
            errorMessage = message;
        }
        return new FirebaseError(FirebaseError.UNKNOWN_ERROR, errorMessage, details);
    }

    private boolean attemptHasBeenPreempted(AuthAttempt attempt) {
        return attempt != this.currentAuthAttempt ? true : $assertionsDisabled;
    }

    private AuthAttempt newAuthAttempt(AuthResultHandler handler) {
        preemptAnyExistingAttempts();
        this.currentAuthAttempt = new AuthAttempt(handler);
        return this.currentAuthAttempt;
    }

    private AuthAttempt newAuthAttempt(AuthListener listener) {
        preemptAnyExistingAttempts();
        this.currentAuthAttempt = new AuthAttempt(listener);
        return this.currentAuthAttempt;
    }

    private void fireAuthErrorIfNotPreempted(final FirebaseError error, final AuthAttempt attempt) {
        if (!attemptHasBeenPreempted(attempt)) {
            if (attempt != null) {
                fireEvent(new Runnable() {
                    public void run() {
                        attempt.fireError(error);
                    }
                });
            }
            this.currentAuthAttempt = null;
        }
    }

    private void checkServerSettings() {
        if (this.repoInfo.isDemoHost()) {
            this.logger.warn("Firebase authentication is supported on production Firebases only (*.firebaseio.com). To secure your Firebase, create a production Firebase at https://www.firebase.com.");
        } else if (this.repoInfo.isCustomHost() && !this.context.isCustomAuthenticationServerSet()) {
            throw new IllegalStateException("For a custom firebase host you must first set your authentication server before using authentication features!");
        }
    }

    private String getFirebaseCredentialIdentifier() {
        return this.repoInfo.host;
    }

    private void scheduleNow(Runnable r) {
        this.context.getRunLoop().scheduleNow(r);
    }

    private AuthData parseAuthData(String token, Map<String, Object> rawAuthData, Map<String, Object> userData) {
        long expires;
        Map<String, Object> authData = (Map) Utilities.getOrNull(rawAuthData, "auth", Map.class);
        if (authData == null) {
            this.logger.warn("Received invalid auth data: " + rawAuthData);
        }
        Object expiresObj = rawAuthData.get("expires");
        if (expiresObj == null) {
            expires = 0;
        } else if (expiresObj instanceof Integer) {
            expires = (long) ((Integer) expiresObj).intValue();
        } else if (expiresObj instanceof Long) {
            expires = ((Long) expiresObj).longValue();
        } else if (expiresObj instanceof Double) {
            expires = ((Double) expiresObj).longValue();
        } else {
            expires = 0;
        }
        String uid = (String) Utilities.getOrNull(authData, "uid", String.class);
        if (uid == null) {
            uid = (String) Utilities.getOrNull(userData, "uid", String.class);
        }
        String provider = (String) Utilities.getOrNull(authData, "provider", String.class);
        if (provider == null) {
            provider = (String) Utilities.getOrNull(userData, "provider", String.class);
        }
        if (provider == null) {
            provider = CUSTOM_PROVIDER;
        }
        if (uid == null || uid.isEmpty()) {
            this.logger.warn("Received invalid auth data: " + authData);
        }
        Map<String, Object> providerData = (Map) Utilities.getOrNull(userData, provider, Map.class);
        if (providerData == null) {
            providerData = new HashMap();
        }
        return new AuthData(token, expires, uid, provider, authData, providerData);
    }

    private void handleBadAuthStatus(FirebaseError error, AuthAttempt attempt, boolean revoked) {
        if ((error.getCode() == -6 ? true : $assertionsDisabled) && this.context.getAuthExpirationBehavior() == AuthExpirationBehavior.PAUSE_WRITES_UNTIL_REAUTH) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Pausing writes due to expired token.");
            }
            this.connection.pauseWrites();
        } else if (!this.connection.writesPaused()) {
            clearSession();
        } else if (!$assertionsDisabled && this.context.getAuthExpirationBehavior() != AuthExpirationBehavior.PAUSE_WRITES_UNTIL_REAUTH) {
            throw new AssertionError();
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Invalid auth while writes are paused; keeping existing session.");
        }
        updateAuthState(null);
        if (attempt == null) {
            return;
        }
        if (revoked) {
            attempt.fireRevoked(error);
        } else {
            attempt.fireError(error);
        }
    }

    private void handleAuthSuccess(String credential, Map<String, Object> authDataMap, Map<String, Object> optionalUserData, boolean isNewSession, AuthAttempt attempt) {
        JsonWebToken token;
        try {
            token = JsonWebToken.decode(credential);
        } catch (IOException e) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Failed to parse JWT, probably a Firebase secret.");
            }
            token = null;
        }
        if (!(!isNewSession || token == null || saveSession(credential, authDataMap, optionalUserData))) {
            this.logger.warn("Failed to store credentials! Authentication will not be persistent!");
        }
        AuthData authData = parseAuthData(credential, authDataMap, optionalUserData);
        updateAuthState(authData);
        if (attempt != null) {
            attempt.fireSuccess(authData);
        }
        if (this.connection.writesPaused()) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Unpausing writes after successful login.");
            }
            this.connection.unpauseWrites();
        }
    }

    public void resumeSession() {
        try {
            String credentialData = this.store.loadCredential(getFirebaseCredentialIdentifier(), this.context.getSessionPersistenceKey());
            if (credentialData != null) {
                Map<String, Object> credentials = (Map) JsonHelpers.getMapper().readValue(credentialData, new TypeReference<Map<String, Object>>() {
                });
                final String tokenString = (String) Utilities.getOrNull(credentials, TOKEN_KEY, String.class);
                final Map<String, Object> authDataObj = (Map) Utilities.getOrNull(credentials, AUTH_DATA_KEY, Map.class);
                final Map<String, Object> userData = (Map) Utilities.getOrNull(credentials, USER_DATA_KEY, Map.class);
                if (authDataObj != null) {
                    updateAuthState(parseAuthData(tokenString, authDataObj, userData));
                    this.context.getRunLoop().scheduleNow(new Runnable() {
                        public void run() {
                            AuthenticationManager.this.connection.auth(tokenString, new AuthListener() {
                                public void onAuthError(FirebaseError error) {
                                    AuthenticationManager.this.handleBadAuthStatus(error, null, AuthenticationManager.$assertionsDisabled);
                                }

                                public void onAuthSuccess(Object authData) {
                                    AuthenticationManager.this.handleAuthSuccess(tokenString, authDataObj, userData, AuthenticationManager.$assertionsDisabled, null);
                                }

                                public void onAuthRevoked(FirebaseError error) {
                                    AuthenticationManager.this.handleBadAuthStatus(error, null, true);
                                }
                            });
                        }
                    });
                }
            }
        } catch (IOException e) {
            this.logger.warn("Failed resuming authentication session!", e);
            clearSession();
        }
    }

    private boolean saveSession(String token, Map<String, Object> authData, Map<String, Object> userData) {
        String firebaseId = getFirebaseCredentialIdentifier();
        String sessionId = this.context.getSessionPersistenceKey();
        this.store.clearCredential(firebaseId, sessionId);
        Map<String, Object> sessionMap = new HashMap();
        sessionMap.put(TOKEN_KEY, token);
        sessionMap.put(AUTH_DATA_KEY, authData);
        sessionMap.put(USER_DATA_KEY, userData);
        try {
            if (this.logger.logsDebug()) {
                this.logger.debug("Storing credentials for Firebase \"" + firebaseId + "\" and session \"" + sessionId + "\".");
            }
            return this.store.storeCredential(firebaseId, sessionId, JsonHelpers.getMapper().writeValueAsString(sessionMap));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean clearSession() {
        String firebaseId = getFirebaseCredentialIdentifier();
        String sessionId = this.context.getSessionPersistenceKey();
        if (this.logger.logsDebug()) {
            this.logger.debug("Clearing credentials for Firebase \"" + firebaseId + "\" and session \"" + sessionId + "\".");
        }
        return this.store.clearCredential(firebaseId, sessionId);
    }

    private void updateAuthState(final AuthData authData) {
        boolean changed = true;
        if (this.authData == null) {
            if (authData == null) {
                changed = $assertionsDisabled;
            }
        } else if (this.authData.equals(authData)) {
            changed = $assertionsDisabled;
        }
        this.authData = authData;
        if (changed) {
            for (final AuthStateListener listener : this.listenerSet) {
                fireEvent(new Runnable() {
                    public void run() {
                        listener.onAuthStateChanged(authData);
                    }
                });
            }
        }
    }

    private String buildUrlPath(String urlPath) {
        StringBuilder path = new StringBuilder();
        path.append("/v2/");
        path.append(this.repoInfo.namespace);
        if (!urlPath.startsWith("/")) {
            path.append("/");
        }
        path.append(urlPath);
        return path.toString();
    }

    private void makeRequest(String urlPath, HttpRequestType type, Map<String, String> urlParams, Map<String, String> requestParams, RequestHandler handler) {
        Map<String, String> actualUrlParams = new HashMap(urlParams);
        actualUrlParams.put(NotificationCompatApi21.CATEGORY_TRANSPORT, "json");
        actualUrlParams.put("v", this.context.getPlatformVersion());
        final HttpUriRequest request = HttpUtilities.requestWithType(this.context.getAuthenticationServer(), buildUrlPath(urlPath), type, actualUrlParams, requestParams);
        if (this.logger.logsDebug()) {
            URI uri = request.getURI();
            String scheme = uri.getScheme();
            String authority = uri.getAuthority();
            String path = uri.getPath();
            int numQueryParams = uri.getQuery().split("&").length;
            this.logger.debug(String.format("Sending request to %s://%s%s with %d query params", new Object[]{scheme, authority, path, Integer.valueOf(numQueryParams)}));
        }
        final RequestHandler requestHandler = handler;
        this.context.runBackgroundTask(new Runnable() {
            public void run() {
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, AuthenticationManager.CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParameters, AuthenticationManager.CONNECTION_TIMEOUT);
                DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                httpClient.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
                try {
                    final Map<String, Object> result = (Map) httpClient.execute(request, new JsonBasicResponseHandler());
                    if (result == null) {
                        throw new IOException("Authentication server did not respond with a valid response");
                    }
                    AuthenticationManager.this.scheduleNow(new Runnable() {
                        public void run() {
                            requestHandler.onResult(result);
                        }
                    });
                } catch (final IOException e) {
                    AuthenticationManager.this.scheduleNow(new Runnable() {
                        public void run() {
                            requestHandler.onError(e);
                        }
                    });
                }
            }
        });
    }

    private void makeAuthenticationRequest(String urlPath, Map<String, String> params, AuthResultHandler handler) {
        final AuthAttempt attempt = newAuthAttempt(handler);
        makeRequest(urlPath, HttpRequestType.GET, params, Collections.emptyMap(), new RequestHandler() {
            public void onResult(Map<String, Object> result) {
                Object errorResponse = result.get(AuthenticationManager.ERROR_KEY);
                String token = (String) Utilities.getOrNull(result, AuthenticationManager.TOKEN_KEY, String.class);
                if (errorResponse != null || token == null) {
                    AuthenticationManager.this.fireAuthErrorIfNotPreempted(AuthenticationManager.this.decodeErrorResponse(errorResponse), attempt);
                } else if (!AuthenticationManager.this.attemptHasBeenPreempted(attempt)) {
                    AuthenticationManager.this.authWithCredential(token, result, attempt);
                }
            }

            public void onError(IOException e) {
                AuthenticationManager.this.fireAuthErrorIfNotPreempted(new FirebaseError(-24, "There was an exception while connecting to the authentication server: " + e.getLocalizedMessage()), attempt);
            }
        });
    }

    private void makeOperationRequest(String urlPath, HttpRequestType type, Map<String, String> urlParams, Map<String, String> requestParams, ResultHandler handler, boolean logUserOut) {
        makeOperationRequestWithResult(urlPath, type, urlParams, requestParams, ignoreResultValueHandler(handler), logUserOut);
    }

    private void makeOperationRequestWithResult(String urlPath, HttpRequestType type, Map<String, String> urlParams, Map<String, String> requestParams, final ValueResultHandler<Map<String, Object>> handler, final boolean logUserOut) {
        makeRequest(urlPath, type, urlParams, requestParams, new RequestHandler() {
            public void onResult(final Map<String, Object> result) {
                Object errorResponse = result.get(AuthenticationManager.ERROR_KEY);
                if (errorResponse == null) {
                    if (logUserOut) {
                        String uid = (String) Utilities.getOrNull(result, "uid", String.class);
                        if (!(uid == null || AuthenticationManager.this.authData == null || !uid.equals(AuthenticationManager.this.authData.getUid()))) {
                            AuthenticationManager.this.unauth(null, AuthenticationManager.$assertionsDisabled);
                        }
                    }
                    AuthenticationManager.this.scheduleNow(new Runnable() {
                        public void run() {
                            AuthenticationManager.this.fireOnSuccess(handler, result);
                        }
                    });
                    return;
                }
                AuthenticationManager.this.fireOnError(handler, AuthenticationManager.this.decodeErrorResponse(errorResponse));
            }

            public void onError(IOException e) {
                AuthenticationManager.this.fireOnError(handler, new FirebaseError(-24, "There was an exception while performing the request: " + e.getLocalizedMessage()));
            }
        });
    }

    private void authWithCredential(final String credential, final Map<String, Object> optionalUserData, final AuthAttempt attempt) {
        if (attempt != this.currentAuthAttempt) {
            throw new IllegalStateException("Ooops. We messed up tracking which authentications are running!");
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("Authenticating with credential of length " + credential.length());
        }
        this.currentAuthAttempt = null;
        this.connection.auth(credential, new AuthListener() {
            public void onAuthSuccess(Object authData) {
                AuthenticationManager.this.handleAuthSuccess(credential, (Map) authData, optionalUserData, true, attempt);
            }

            public void onAuthRevoked(FirebaseError error) {
                AuthenticationManager.this.handleBadAuthStatus(error, attempt, true);
            }

            public void onAuthError(FirebaseError error) {
                AuthenticationManager.this.handleBadAuthStatus(error, attempt, AuthenticationManager.$assertionsDisabled);
            }
        });
    }

    public AuthData getAuth() {
        return this.authData;
    }

    public void unauth() {
        checkServerSettings();
        unauth(null);
    }

    public void unauth(CompletionListener listener) {
        unauth(listener, true);
    }

    public void unauth(final CompletionListener listener, boolean waitForCompletion) {
        checkServerSettings();
        final Semaphore semaphore = new Semaphore(0);
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.preemptAnyExistingAttempts();
                AuthenticationManager.this.updateAuthState(null);
                semaphore.release();
                AuthenticationManager.this.clearSession();
                AuthenticationManager.this.connection.unauth(new CompletionListener() {
                    public void onComplete(FirebaseError error, Firebase unusedRef) {
                        if (listener != null) {
                            listener.onComplete(error, new Firebase(AuthenticationManager.this.repo, new Path(BuildConfig.FLAVOR)));
                        }
                    }
                });
                if (AuthenticationManager.this.connection.writesPaused()) {
                    if (AuthenticationManager.this.logger.logsDebug()) {
                        AuthenticationManager.this.logger.debug("Unpausing writes after explicit unauth.");
                    }
                    AuthenticationManager.this.connection.unpauseWrites();
                }
            }
        });
        if (waitForCompletion) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addAuthStateListener(final AuthStateListener listener) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.listenerSet.add(listener);
                final AuthData authData = AuthenticationManager.this.authData;
                AuthenticationManager.this.fireEvent(new Runnable() {
                    public void run() {
                        listener.onAuthStateChanged(authData);
                    }
                });
            }
        });
    }

    public void removeAuthStateListener(final AuthStateListener listener) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.listenerSet.remove(listener);
            }
        });
    }

    public void authAnonymously(final AuthResultHandler handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.makeAuthenticationRequest(Constants.FIREBASE_AUTH_ANONYMOUS_PATH, new HashMap(), handler);
            }
        });
    }

    public void authWithPassword(final String email, final String password, final AuthResultHandler handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                Map<String, String> params = new HashMap();
                params.put(ProfilesDBHelper.COLUMN_EMAIL, email);
                params.put("password", password);
                AuthenticationManager.this.makeAuthenticationRequest(Constants.FIREBASE_AUTH_PASSWORD_PATH, params, handler);
            }
        });
    }

    public void authWithCustomToken(final String token, final AuthResultHandler handler) {
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.authWithCredential(token, null, AuthenticationManager.this.newAuthAttempt(handler));
            }
        });
    }

    public void authWithFirebaseToken(final String token, final AuthListener listener) {
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.authWithCredential(token, null, AuthenticationManager.this.newAuthAttempt(listener));
            }
        });
    }

    public void authWithOAuthToken(String provider, String token, AuthResultHandler handler) {
        if (token == null) {
            throw new IllegalArgumentException("Token must not be null!");
        }
        Map params = new HashMap();
        params.put(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, token);
        authWithOAuthToken(provider, params, handler);
    }

    public void authWithOAuthToken(final String provider, final Map<String, String> params, final AuthResultHandler handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                AuthenticationManager.this.makeAuthenticationRequest(String.format(Constants.FIREBASE_AUTH_PROVIDER_PATH_FORMAT, new Object[]{provider}), params, handler);
            }
        });
    }

    public void createUser(String email, String password, ResultHandler handler) {
        createUser(email, password, ignoreResultValueHandler(handler));
    }

    public void createUser(final String email, final String password, final ValueResultHandler<Map<String, Object>> handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                Map<String, String> requestParams = new HashMap();
                requestParams.put(ProfilesDBHelper.COLUMN_EMAIL, email);
                requestParams.put("password", password);
                AuthenticationManager.this.makeOperationRequestWithResult(Constants.FIREBASE_AUTH_CREATE_USER_PATH, HttpRequestType.POST, Collections.emptyMap(), requestParams, handler, AuthenticationManager.$assertionsDisabled);
            }
        });
    }

    public void removeUser(final String email, final String password, final ResultHandler handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                Map<String, String> urlParams = new HashMap();
                urlParams.put("password", password);
                AuthenticationManager.this.makeOperationRequest(String.format(Constants.FIREBASE_AUTH_REMOVE_USER_PATH_FORMAT, new Object[]{email}), HttpRequestType.DELETE, urlParams, Collections.emptyMap(), handler, true);
            }
        });
    }

    public void changePassword(String email, String oldPassword, String newPassword, ResultHandler handler) {
        checkServerSettings();
        final String str = oldPassword;
        final String str2 = newPassword;
        final String str3 = email;
        final ResultHandler resultHandler = handler;
        scheduleNow(new Runnable() {
            public void run() {
                Map<String, String> urlParams = new HashMap();
                urlParams.put("oldPassword", str);
                Map<String, String> requestParams = new HashMap();
                requestParams.put("password", str2);
                AuthenticationManager.this.makeOperationRequest(String.format(Constants.FIREBASE_AUTH_PASSWORD_PATH_FORMAT, new Object[]{str3}), HttpRequestType.PUT, urlParams, requestParams, resultHandler, AuthenticationManager.$assertionsDisabled);
            }
        });
    }

    public void changeEmail(String oldEmail, String password, String newEmail, ResultHandler handler) {
        checkServerSettings();
        final String str = password;
        final String str2 = newEmail;
        final String str3 = oldEmail;
        final ResultHandler resultHandler = handler;
        scheduleNow(new Runnable() {
            public void run() {
                Map<String, String> urlParams = new HashMap();
                urlParams.put("password", str);
                Map<String, String> requestParams = new HashMap();
                requestParams.put(ProfilesDBHelper.COLUMN_EMAIL, str2);
                AuthenticationManager.this.makeOperationRequest(String.format(Constants.FIREBASE_AUTH_EMAIL_PATH_FORMAT, new Object[]{str3}), HttpRequestType.PUT, urlParams, requestParams, resultHandler, AuthenticationManager.$assertionsDisabled);
            }
        });
    }

    public void resetPassword(final String email, final ResultHandler handler) {
        checkServerSettings();
        scheduleNow(new Runnable() {
            public void run() {
                String url = String.format(Constants.FIREBASE_AUTH_PASSWORD_PATH_FORMAT, new Object[]{email});
                Map<String, String> params = Collections.emptyMap();
                AuthenticationManager.this.makeOperationRequest(url, HttpRequestType.POST, params, params, handler, AuthenticationManager.$assertionsDisabled);
            }
        });
    }
}
