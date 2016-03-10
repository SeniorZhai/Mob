package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.app.Service;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.AutoCompleteTextView.Validator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.mobcrush.mobcrush.ChatFragment;
import com.mobcrush.mobcrush.ChatMessagesAdapter;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.broadcast.BroadcastHelper.BroadcastStatusCallback;
import com.mobcrush.mobcrush.broadcast.BroadcastLayout.SoftKeyboardVisibilityChangeListener;
import com.mobcrush.mobcrush.common.AnimationUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.helper.ModerationHelper.MutedCallback;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(21)
public class BroadcastMenu implements OnClickListener, TextWatcher {
    private static final int ANIMATION_FRAME_RATE = 30;
    private static final int MAX_CHARACTERS = 70;
    private static final int MOVE_THRESHOLD = 20;
    private static final String TAG = BroadcastService.class.getName();
    private Drawable DEFAULT_AVATAR;
    private Timer animationTimer = new Timer();
    private Button broadcastConfig;
    private final BroadcastHelper broadcastHelper;
    private BroadcastLayout broadcastMenu;
    private OnTouchListener broadcastMenuOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            Point currentTouch = new Point();
            currentTouch.x = (int) event.getRawX();
            currentTouch.y = (int) event.getRawY();
            switch (event.getAction()) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    BroadcastMenu.this.isMoving = false;
                    BroadcastMenu.this.initialTouch = new Point(currentTouch);
                    break;
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    if (BroadcastMenu.this.isMoving) {
                        BroadcastMenu.this.isMoving = false;
                        return true;
                    }
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    if (!(BroadcastMenu.this.isMoving || BroadcastMenu.this.isExpanded || (Math.abs(currentTouch.x - BroadcastMenu.this.initialTouch.x) <= BroadcastMenu.MOVE_THRESHOLD && Math.abs(currentTouch.y - BroadcastMenu.this.initialTouch.y) <= BroadcastMenu.MOVE_THRESHOLD))) {
                        BroadcastMenu.this.isMoving = true;
                    }
                    if (BroadcastMenu.this.isMoving) {
                        move(BroadcastMenu.this.broadcastMenu, currentTouch);
                        return true;
                    }
                    break;
            }
            return false;
        }

        private void move(View view, Point point) {
            int x = point.x - (view.getWidth() / 2);
            int y = point.y - (view.getHeight() / 2);
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            BroadcastMenu.this.windowManager.getDefaultDisplay().getSize(BroadcastMenu.this.screenSize);
            BroadcastMenu.this.currentLocation.x = Math.min(BroadcastMenu.this.screenSize.x - view.getWidth(), Math.max(x, 0));
            BroadcastMenu.this.currentLocation.y = Math.min((BroadcastMenu.this.screenSize.y - view.getHeight()) - BroadcastMenu.this.statusBarHeight, Math.max(y, 0));
            params.x = BroadcastMenu.this.currentLocation.x;
            params.y = BroadcastMenu.this.currentLocation.y;
            BroadcastMenu.this.windowManager.updateViewLayout(view, params);
        }
    };
    private LinearLayout broadcastSettings;
    private View broadcastingNow;
    private AutoFitTextureView cameraPreview;
    private TextView characterCount;
    private final ChatMessagesAdapter chatAdapter = new ChatMessagesAdapter(null, null) {
        private final MutedCallback mutedCallback = new MutedCallback() {
            public void userMuted(final ChatMessage message) {
                BroadcastMenu.this.handler.post(new Runnable() {
                    public void run() {
                        message.triggeredMute = true;
                        BroadcastMenu.this.chatAdapter.notifyItemChanged(AnonymousClass26.this.mDataset.indexOf(message));
                    }
                });
            }
        };

        public void onClick(View view) {
            Object tag = view.getTag();
            if (tag != null && (tag instanceof ChatMessage)) {
                try {
                    final ChatMessage message = (ChatMessage) tag;
                    final User user = message.getUser();
                    if (!TextUtils.equals(PreferenceUtility.getUser()._id, message.userId)) {
                        final View mainMenu = BroadcastMenu.this.inflater.inflate(R.layout.popup_chat_menu, null);
                        onDismissRemoveView(mainMenu);
                        ((Button) mainMenu.findViewById(R.id.action_ignore)).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (AnonymousClass26.this.mDataset.indexOf(message) == -1) {
                                    Exception e = new Exception("Can't find message " + message);
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                BroadcastMenu.this.windowManager.removeView(mainMenu);
                                AnonymousClass26.this.showIgnorePopup(user);
                            }
                        });
                        ((Button) mainMenu.findViewById(R.id.action_appoint)).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (AnonymousClass26.this.mDataset.indexOf(message) == -1) {
                                    Exception e = new Exception("Can't find message " + message);
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                BroadcastMenu.this.windowManager.removeView(mainMenu);
                                ModerationHelper.appointAsModerator(MainApplication.mFirebase, user, BroadcastMenu.this.broadcastHelper.getChatRoomId());
                                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.moderator_appointed, new Object[]{user.username}), 1).show();
                            }
                        });
                        ((Button) mainMenu.findViewById(R.id.action_mute)).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (AnonymousClass26.this.mDataset.indexOf(message) == -1) {
                                    Exception e = new Exception("Can't find message " + message);
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                BroadcastMenu.this.windowManager.removeView(mainMenu);
                                AnonymousClass26.this.showMutePopup(user, message);
                            }
                        });
                        Button banButton = (Button) mainMenu.findViewById(R.id.action_ban);
                        if (!ModerationHelper.canBanUser(RoleType.broadcaster, message.role)) {
                            banButton.setVisibility(8);
                        }
                        banButton.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (AnonymousClass26.this.mDataset.indexOf(message) == -1) {
                                    Exception e = new Exception("Can't find message " + message);
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                BroadcastMenu.this.windowManager.removeView(mainMenu);
                                AnonymousClass26.this.showBanPopup(user);
                            }
                        });
                        BroadcastMenu.this.windowManager.addView(mainMenu, BroadcastMenu.this.getLayoutParams(true));
                    }
                } catch (Exception e) {
                    Log.e(BroadcastMenu.TAG, e.getMessage());
                    Crashlytics.logException(new Exception("error while parsing ChatMessage " + tag, e));
                }
            }
        }

        private void showIgnorePopup(final User user) {
            final View ignoreDialog = BroadcastMenu.this.inflater.inflate(R.layout.popup_ignore_dialog, null);
            onDismissRemoveView(ignoreDialog);
            ((Button) ignoreDialog.findViewById(R.id.positive_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ModerationHelper.ignoreUser(user, BroadcastMenu.this.broadcastHelper.getChatRoomId());
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.user_ignored, new Object[]{user.username}), 1).show();
                    BroadcastMenu.this.windowManager.removeView(ignoreDialog);
                }
            });
            ((Button) ignoreDialog.findViewById(R.id.negative_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BroadcastMenu.this.windowManager.removeView(ignoreDialog);
                }
            });
            ((TextView) ignoreDialog.findViewById(R.id.text)).setText(Html.fromHtml(BroadcastMenu.this.service.getString(R.string.IgnoreUser_S__, new Object[]{user.username})));
            BroadcastMenu.this.windowManager.addView(ignoreDialog, BroadcastMenu.this.getLayoutParams(true));
        }

        private void showMutePopup(final User user, final ChatMessage message) {
            final View muteDialog = BroadcastMenu.this.inflater.inflate(R.layout.popup_mute_dialog, null);
            onDismissRemoveView(muteDialog);
            ((Button) muteDialog.findViewById(R.id.mute_10)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ModerationHelper.muteUser(message, user, 600000.0d, BroadcastMenu.this.broadcastHelper.getChatRoomId(), AnonymousClass26.this.mutedCallback);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.user_muted, new Object[]{user.username}), 1).show();
                    BroadcastMenu.this.windowManager.removeView(muteDialog);
                }
            });
            ((Button) muteDialog.findViewById(R.id.mute_day)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ModerationHelper.muteUser(message, user, 8.64E7d, BroadcastMenu.this.broadcastHelper.getChatRoomId(), AnonymousClass26.this.mutedCallback);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.user_muted, new Object[]{user.username}), 1).show();
                    BroadcastMenu.this.windowManager.removeView(muteDialog);
                }
            });
            ((Button) muteDialog.findViewById(R.id.mute_indefinitely)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ModerationHelper.muteUser(message, user, 6.3072E10d, BroadcastMenu.this.broadcastHelper.getChatRoomId(), AnonymousClass26.this.mutedCallback);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.user_muted, new Object[]{user.username}), 1).show();
                    BroadcastMenu.this.windowManager.removeView(muteDialog);
                }
            });
            ((TextView) muteDialog.findViewById(R.id.text)).setText(Html.fromHtml(BroadcastMenu.this.service.getString(R.string.MuteAllMessagesFrom_S_, new Object[]{user.username})));
            BroadcastMenu.this.windowManager.addView(muteDialog, BroadcastMenu.this.getLayoutParams(true));
        }

        private void showBanPopup(final User user) {
            final View banDialog = BroadcastMenu.this.inflater.inflate(R.layout.popup_ban_dialog, null);
            onDismissRemoveView(banDialog);
            ((Button) banDialog.findViewById(R.id.positive_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ModerationHelper.ignoreUser(user, BroadcastMenu.this.broadcastHelper.getChatRoomId());
                    BroadcastMenu.this.windowManager.removeView(banDialog);
                    ModerationHelper.banUser(null, MainApplication.mFirebase, user, RoleType.broadcaster.toString(), BroadcastMenu.this.broadcastHelper.getChatRoomId());
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), BroadcastMenu.this.service.getString(R.string.user_banned, new Object[]{user.username}), 1).show();
                }
            });
            ((Button) banDialog.findViewById(R.id.negative_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BroadcastMenu.this.windowManager.removeView(banDialog);
                }
            });
            ((TextView) banDialog.findViewById(R.id.text)).setText(Html.fromHtml(BroadcastMenu.this.service.getString(R.string.ban_confirm_S_, new Object[]{user.username})));
            BroadcastMenu.this.windowManager.addView(banDialog, BroadcastMenu.this.getLayoutParams(true));
        }

        private void onDismissRemoveView(final View view) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BroadcastMenu.this.windowManager.removeView(view);
                }
            });
            view.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode != 4) {
                        return false;
                    }
                    BroadcastMenu.this.windowManager.removeView(view);
                    return true;
                }
            });
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BroadcastMenu.this.windowManager.removeView(view);
                }
            });
        }
    };
    private View chatCloseBtn;
    private final ChatFragment chatFragment = new ChatFragment() {
        public String getChatChannelId() {
            return BroadcastMenu.this.broadcastHelper.getChatRoomId();
        }

        public boolean isAddedOrIsComponent() {
            return true;
        }

        protected boolean isCurrentUserOwner() {
            return true;
        }

        protected boolean isLiveVideo() {
            return true;
        }
    };
    private View chatHeadMessage;
    private ImageView chatHeadMessageIcon;
    private TextView chatHeadMessageText;
    private TextView chatHeadMessageUsername;
    private LinearLayout chatLayout;
    private View chatSendBtn;
    private FrameLayout chatWindow;
    private Point currentLocation;
    private AutoCompleteTextView gameSelection;
    private Handler handler = new Handler();
    private ImageView hideSettings;
    private LayoutInflater inflater;
    private Point initialTouch;
    private boolean isChatHUDEnabled;
    private boolean isExpanded;
    private boolean isMoving;
    private boolean isSettingsEnabled;
    private boolean keyboardIsVisible;
    private ImageView logo;
    private DisplayImageOptions mDio;
    private AnimationTimerTask mTimerTask;
    private int minHeight;
    private int minWidth;
    private HashMap<String, String> nameToIdMap;
    private LinearLayout optionsMenu;
    private ProgressBar progressBar;
    private int rotation;
    private Point screenSize = new Point();
    private final Service service;
    private ImageView showSettings;
    private List<View> spaces;
    private int statusBarHeight;
    private ImageView stopBroadcast;
    private EditText titleSelection;
    private ImageView toggleCamera;
    private ImageView toggleCameraSwap;
    private ImageView toggleChatHUD;
    private ImageView toggleMic;
    private ImageView togglePrivacy;
    private WindowManager windowManager;

    private class AnimationTimerTask extends TimerTask {
        private Point destination;
        private boolean expand;
        private View view;

        public AnimationTimerTask(Point destination, View view, boolean expand) {
            this.destination = destination;
            this.view = view;
            this.expand = expand;
        }

        public void run() {
            BroadcastMenu.this.handler.removeCallbacks(null);
            BroadcastMenu.this.handler.post(new Runnable() {
                public void run() {
                    boolean cancel = false;
                    LayoutParams oldParams = (LayoutParams) AnimationTimerTask.this.view.getLayoutParams();
                    LayoutParams newParams = BroadcastMenu.this.getLayoutParams(AnimationTimerTask.this.expand);
                    if (Math.abs(oldParams.x - AnimationTimerTask.this.destination.x) >= 2 || Math.abs(oldParams.y - AnimationTimerTask.this.destination.y) >= 2) {
                        newParams.x = (((oldParams.x - AnimationTimerTask.this.destination.x) * 2) / 3) + AnimationTimerTask.this.destination.x;
                        newParams.y = (((oldParams.y - AnimationTimerTask.this.destination.y) * 2) / 3) + AnimationTimerTask.this.destination.y;
                    } else {
                        newParams.x = AnimationTimerTask.this.destination.x;
                        newParams.y = AnimationTimerTask.this.destination.y;
                        cancel = true;
                    }
                    BroadcastMenu.this.windowManager.updateViewLayout(AnimationTimerTask.this.view, newParams);
                    if (cancel) {
                        if (AnimationTimerTask.this.expand) {
                            BroadcastMenu.this.showMenuItems();
                        }
                        AnimationTimerTask.this.cancel();
                    }
                }
            });
        }
    }

    public BroadcastMenu(Service service, BroadcastHelper broadcastHelper) {
        this.service = service;
        this.broadcastHelper = broadcastHelper;
    }

    public void init() {
        this.windowManager = (WindowManager) this.service.getSystemService("window");
        this.windowManager.getDefaultDisplay().getSize(this.screenSize);
        this.inflater = (LayoutInflater) this.service.getSystemService("layout_inflater");
        this.broadcastMenu = (BroadcastLayout) this.inflater.inflate(R.layout.broadcast_menu, null);
        this.broadcastMenu.setFocusableInTouchMode(true);
        this.broadcastMenu.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 4 || event.getAction() != 1 || !BroadcastMenu.this.isExpanded) {
                    return false;
                }
                if (BroadcastMenu.this.isSettingsEnabled && BroadcastMenu.this.broadcastHelper.isBroadcasting()) {
                    BroadcastMenu.this.hideSettings();
                    return true;
                }
                BroadcastMenu.this.expandMenu(false);
                return true;
            }
        });
        this.broadcastMenu.setOnSoftKeyboardVisibilityChangeListener(new SoftKeyboardVisibilityChangeListener() {
            public void onSoftKeyboardShow() {
            }

            public void onSoftKeyboardHide() {
                BroadcastMenu.this.broadcastMenu.requestFocus();
            }
        });
        this.optionsMenu = (LinearLayout) this.broadcastMenu.findViewById(R.id.options_menu);
        this.chatWindow = (FrameLayout) this.broadcastMenu.findViewById(R.id.chat_window);
        this.logo = (ImageView) this.broadcastMenu.findViewById(R.id.logo);
        deselectIcon(this.logo);
        this.logo.setOnTouchListener(this.broadcastMenuOnTouchListener);
        this.logo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.expandMenu(!BroadcastMenu.this.isExpanded);
            }
        });
        initSpaces(this.broadcastMenu);
        this.progressBar = (ProgressBar) this.broadcastMenu.findViewById(R.id.progress_bar);
        this.progressBar.setVisibility(8);
        this.stopBroadcast = (ImageView) this.broadcastMenu.findViewById(R.id.stop_broadcast);
        this.stopBroadcast.setVisibility(8);
        this.stopBroadcast.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.showBroadcastEnd();
            }
        });
        this.cameraPreview = (AutoFitTextureView) this.broadcastMenu.findViewById(R.id.camera_preview);
        this.cameraPreview.setVisibility(8);
        this.toggleCamera = (ImageView) this.broadcastMenu.findViewById(R.id.toggle_camera);
        deselectIcon(this.toggleCamera);
        this.toggleCamera.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BroadcastMenu.this.broadcastHelper.isCameraEnabled()) {
                    if (BroadcastMenu.this.broadcastHelper.isCameraSwapped()) {
                        BroadcastMenu.this.deselectIcon(BroadcastMenu.this.toggleCameraSwap);
                    }
                    BroadcastMenu.this.broadcastHelper.stopCamera();
                    BroadcastMenu.this.cameraPreview.setVisibility(8);
                    BroadcastMenu.this.deselectIcon(BroadcastMenu.this.toggleCamera);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.camera_off, 0).show();
                    return;
                }
                BroadcastMenu.this.startCamera();
                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.camera_on, 0).show();
            }
        });
        this.toggleCameraSwap = (ImageView) this.broadcastMenu.findViewById(R.id.toggle_camera_swap);
        deselectIcon(this.toggleCameraSwap);
        this.toggleCameraSwap.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BroadcastMenu.this.broadcastHelper.isCameraSwapped()) {
                    BroadcastMenu.this.broadcastHelper.setCameraSwapped(false);
                    BroadcastMenu.this.deselectIcon(BroadcastMenu.this.toggleCameraSwap);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.cam_swap_off, 0).show();
                    return;
                }
                if (!BroadcastMenu.this.broadcastHelper.isCameraEnabled()) {
                    BroadcastMenu.this.startCamera();
                }
                BroadcastMenu.this.broadcastHelper.setCameraSwapped(true);
                BroadcastMenu.this.selectIcon(BroadcastMenu.this.toggleCameraSwap);
                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.cam_swap_on, 0).show();
            }
        });
        this.toggleMic = (ImageView) this.broadcastMenu.findViewById(R.id.toggle_mic);
        selectIcon(this.toggleMic);
        this.toggleMic.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BroadcastMenu.this.broadcastHelper.isMicEnabled()) {
                    BroadcastMenu.this.broadcastHelper.setMicEnabled(false);
                    BroadcastMenu.this.deselectIcon(BroadcastMenu.this.toggleMic);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.mic_off, 0).show();
                    return;
                }
                BroadcastMenu.this.broadcastHelper.setMicEnabled(true);
                BroadcastMenu.this.selectIcon(BroadcastMenu.this.toggleMic);
                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.mic_on, 0).show();
            }
        });
        this.togglePrivacy = (ImageView) this.broadcastMenu.findViewById(R.id.toggle_privacy);
        deselectIcon(this.togglePrivacy);
        this.togglePrivacy.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BroadcastMenu.this.broadcastHelper.isPrivacyEnabled()) {
                    BroadcastMenu.this.broadcastHelper.setPrivacyMode(false);
                    BroadcastMenu.this.deselectIcon(BroadcastMenu.this.togglePrivacy);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.privacy_off, 0).show();
                    return;
                }
                BroadcastMenu.this.broadcastHelper.setPrivacyMode(true);
                BroadcastMenu.this.selectIcon(BroadcastMenu.this.togglePrivacy);
                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.privacy_on, 0).show();
            }
        });
        this.toggleChatHUD = (ImageView) this.broadcastMenu.findViewById(R.id.toggle_chat);
        this.isChatHUDEnabled = false;
        deselectIcon(this.toggleChatHUD);
        this.toggleChatHUD.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.isChatHUDEnabled = !BroadcastMenu.this.isChatHUDEnabled;
                if (BroadcastMenu.this.isChatHUDEnabled) {
                    BroadcastMenu.this.selectIcon(BroadcastMenu.this.toggleChatHUD);
                    Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.hud_chat_on, 0).show();
                    return;
                }
                BroadcastMenu.this.deselectIcon(BroadcastMenu.this.toggleChatHUD);
                Toast.makeText(BroadcastMenu.this.service.getApplicationContext(), R.string.hud_chat_off, 0).show();
            }
        });
        this.showSettings = (ImageView) this.broadcastMenu.findViewById(R.id.show_settings);
        this.isSettingsEnabled = true;
        deselectIcon(this.showSettings);
        this.showSettings.setVisibility(8);
        this.showSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.showSettings();
            }
        });
        this.hideSettings = (ImageView) this.broadcastMenu.findViewById(R.id.hide_settings);
        this.hideSettings.setVisibility(4);
        this.hideSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.hideSettings();
            }
        });
        this.broadcastingNow = this.broadcastMenu.findViewById(R.id.broadcasting_now);
        this.broadcastingNow.setVisibility(8);
        this.broadcastMenu.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int[] onScreenLocation = new int[2];
                LayoutParams params = (LayoutParams) BroadcastMenu.this.broadcastMenu.getLayoutParams();
                BroadcastMenu.this.broadcastMenu.getLocationOnScreen(onScreenLocation);
                if (onScreenLocation[1] != params.y) {
                    BroadcastMenu.this.statusBarHeight = onScreenLocation[1] - params.y;
                } else {
                    BroadcastMenu.this.statusBarHeight = 0;
                }
            }
        });
        this.broadcastSettings = (LinearLayout) this.broadcastMenu.findViewById(R.id.configure_broadcast);
        this.broadcastConfig = (Button) this.broadcastSettings.findViewById(R.id.broadcast_setup_button);
        this.broadcastConfig.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BroadcastMenu.this.broadcastMenu.requestFocus();
                UIUtils.hideVirtualKeyboard(BroadcastMenu.this.service, v);
                if (BroadcastMenu.this.broadcastHelper.isBroadcasting()) {
                    String title = BroadcastMenu.this.titleSelection.getText().toString();
                    String game = null;
                    if (BroadcastMenu.this.nameToIdMap != null) {
                        game = (String) BroadcastMenu.this.nameToIdMap.get(BroadcastMenu.this.gameSelection.getText().toString());
                    }
                    BroadcastMenu.this.broadcastHelper.updateBroadcast(title, game);
                    return;
                }
                BroadcastMenu.this.showBroadcastStart();
            }
        });
        this.gameSelection = (AutoCompleteTextView) this.broadcastMenu.findViewById(R.id.game_selection);
        Network.getGames(null, new Listener<Game[]>() {
            public void onResponse(Game[] response) {
                final String[] gameTitles = new String[response.length];
                BroadcastMenu.this.nameToIdMap = new HashMap();
                for (int i = 0; i < gameTitles.length; i++) {
                    try {
                        gameTitles[i] = response[i].name;
                        BroadcastMenu.this.nameToIdMap.put(gameTitles[i], response[i]._id);
                    } catch (Exception e) {
                        gameTitles[i] = "\u00af\\_(\u30c4)_/\u00af";
                        BroadcastMenu.this.nameToIdMap.put("\u00af\\_(\u30c4)_/\u00af", "ugh");
                    }
                }
                BroadcastMenu.this.gameSelection.setAdapter(new ArrayAdapter(BroadcastMenu.this.service.getApplicationContext(), 17367050, gameTitles));
                BroadcastMenu.this.gameSelection.setValidator(new Validator() {
                    public boolean isValid(CharSequence text) {
                        return Arrays.asList(gameTitles).contains(text.toString());
                    }

                    public CharSequence fixText(CharSequence invalidText) {
                        return "Other";
                    }
                });
                BroadcastMenu.this.gameSelection.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            ((AutoCompleteTextView) v).performValidation();
                        }
                    }
                });
            }
        }, null);
        this.characterCount = (TextView) this.broadcastMenu.findViewById(R.id.character_count);
        this.characterCount.setText("0 / 70");
        this.titleSelection = (EditText) this.broadcastMenu.findViewById(R.id.broadcast_title_selection);
        this.titleSelection.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BroadcastMenu.this.characterCount.setText(s.length() + " / " + BroadcastMenu.MAX_CHARACTERS);
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.chatLayout = (LinearLayout) this.broadcastMenu.findViewById(R.id.chat_layout);
        this.chatLayout.setVisibility(8);
        this.chatHeadMessage = this.broadcastMenu.findViewById(R.id.chat_head_message);
        this.chatHeadMessage.setVisibility(8);
        this.chatHeadMessage.setBackgroundResource(R.color.broadcast_menu_bg);
        this.chatHeadMessageIcon = (ImageView) this.chatHeadMessage.findViewById(R.id.message_icon);
        this.chatHeadMessageUsername = (TextView) this.chatHeadMessage.findViewById(R.id.user_name_text);
        this.chatHeadMessageText = (TextView) this.chatHeadMessage.findViewById(R.id.message_text);
        this.DEFAULT_AVATAR = ResourcesCompat.getDrawable(this.service.getResources(), R.drawable.default_profile_pic, this.service.getTheme());
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(this.service.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri(this.DEFAULT_AVATAR).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
        this.chatFragment.mMessagesAdapter = this.chatAdapter;
        this.chatFragment.mRecyclerView = (RecyclerView) this.broadcastMenu.findViewById(R.id.recycler_view);
        this.chatFragment.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.service.getApplication(), 1, false));
        this.chatFragment.mRecyclerView.setAdapter(this.chatFragment.mMessagesAdapter);
        this.chatFragment.mEditLayout = this.broadcastMenu.findViewById(R.id.edit_layout);
        this.chatFragment.mEditLayout.animate().alpha(AdaptiveEvaluator.DEFAULT_BANDWIDTH_FRACTION);
        this.chatSendBtn = this.broadcastMenu.findViewById(R.id.send_btn);
        this.chatSendBtn.setOnClickListener(this);
        this.chatCloseBtn = this.broadcastMenu.findViewById(R.id.close_btn);
        this.chatCloseBtn.setOnClickListener(this);
        this.chatFragment.mEditText = (EditText) this.broadcastMenu.findViewById(R.id.edit);
        this.chatFragment.mEditText.setEnabled(true);
        this.chatFragment.mEditText.setOnClickListener(this);
        this.chatFragment.mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    BroadcastMenu.this.stopBroadcast.setVisibility(8);
                    BroadcastMenu.this.showSettings.setVisibility(8);
                    BroadcastMenu.this.optionsMenu.setVisibility(8);
                    BroadcastMenu.this.onKeyboardVisibilityChanged(true);
                    return;
                }
                BroadcastMenu.this.handler.postDelayed(new Runnable() {
                    public void run() {
                        BroadcastMenu.this.onKeyboardVisibilityChanged(false);
                        BroadcastMenu.this.stopBroadcast.setVisibility(0);
                        BroadcastMenu.this.showSettings.setVisibility(0);
                        BroadcastMenu.this.optionsMenu.setVisibility(0);
                    }
                }, 200);
            }
        });
        this.chatFragment.mEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    BroadcastMenu.this.broadcastMenu.requestFocus();
                }
                return false;
            }
        });
        this.chatFragment.mEditText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 4) {
                    return false;
                }
                BroadcastMenu.this.sendChatMessage();
                return true;
            }
        });
        this.chatFragment.mEditText.addTextChangedListener(this);
        this.broadcastMenu.findViewById(R.id.send_btn).setVisibility(8);
        this.broadcastMenu.findViewById(R.id.chat_btn).setVisibility(8);
        this.broadcastMenu.findViewById(R.id.chat_options_btn).setVisibility(8);
        this.broadcastMenu.findViewById(R.id.close_btn).setVisibility(8);
        this.rotation = this.windowManager.getDefaultDisplay().getRotation();
        this.currentLocation = new Point(0, 0);
        this.isExpanded = false;
        hideMenuItems();
        updateLayoutOrientation();
        TransitionManager.beginDelayedTransition(this.broadcastMenu, new Fade(1));
        this.windowManager.addView(this.broadcastMenu, getLayoutParams(this.isExpanded));
        this.handler.postDelayed(new Runnable() {
            public void run() {
                BroadcastMenu.this.expandMenu(true);
            }
        }, 200);
    }

    public void sendChatMessage() {
        String s = this.chatFragment.mEditText.getText().toString();
        if (!TextUtils.isEmpty(s)) {
            this.chatFragment.mEditText.setText(BuildConfig.FLAVOR);
            this.chatFragment.sendMessage(s, false);
        }
    }

    public void destroy() {
        this.handler.removeCallbacks(null);
        if (this.windowManager != null) {
            this.windowManager.removeView(this.broadcastMenu);
        }
    }

    public void updateOrientation() {
        int newRotation = this.windowManager.getDefaultDisplay().getRotation();
        if (this.minWidth == 0 || this.minHeight == 0) {
            this.minWidth = this.broadcastMenu.getWidth();
            this.minHeight = this.broadcastMenu.getHeight();
        }
        switch (newRotation) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                if (this.rotation != 1) {
                    if (this.rotation == 3) {
                        rotateLeft(this.broadcastMenu);
                        break;
                    }
                }
                rotateRight(this.broadcastMenu);
                break;
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (this.rotation != 0) {
                    if (this.rotation == 2) {
                        rotateRight(this.broadcastMenu);
                        break;
                    }
                }
                rotateLeft(this.broadcastMenu);
                break;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this.rotation != 1) {
                    if (this.rotation == 3) {
                        rotateRight(this.broadcastMenu);
                        break;
                    }
                }
                rotateLeft(this.broadcastMenu);
                break;
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                if (this.rotation != 0) {
                    if (this.rotation == 2) {
                        rotateLeft(this.broadcastMenu);
                        break;
                    }
                }
                rotateRight(this.broadcastMenu);
                break;
                break;
        }
        this.rotation = newRotation;
    }

    private void configChatHUD() {
        if (this.chatFragment != null && this.isChatHUDEnabled) {
            if (this.isExpanded) {
                this.chatFragment.setOnNewMessageCallback(null);
            } else {
                this.chatFragment.setOnNewMessageCallback(new Callback() {
                    public boolean handleMessage(Message msg) {
                        try {
                            ChatMessage message = msg.obj;
                            Log.i(BroadcastMenu.TAG, "onChatMessage: " + message);
                            if (message != null) {
                                BroadcastMenu.this.chatHeadMessageUsername.setText(message.username);
                                BroadcastMenu.this.chatHeadMessageText.setText(message.message);
                                BroadcastMenu.this.chatHeadMessage.setVisibility(0);
                                BroadcastMenu.this.chatHeadMessage.postDelayed(new Runnable() {
                                    public void run() {
                                        UIUtils.fadeOut(BroadcastMenu.this.chatHeadMessage);
                                    }
                                }, Constants.NOTIFICATION_BANNER_TIMEOUT);
                                ImageLoader.getInstance().displayImage(message.getProfileLogoSmall(), BroadcastMenu.this.chatHeadMessageIcon, BroadcastMenu.this.mDio);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void updateLayoutOrientation() {
        int i;
        int i2 = 0;
        int i3 = -2;
        int orientation = this.service.getResources().getConfiguration().orientation;
        BroadcastLayout broadcastLayout = this.broadcastMenu;
        if (orientation == 2 || !this.isExpanded) {
            i = 0;
        } else {
            i = 1;
        }
        broadcastLayout.setOrientation(i);
        LinearLayout linearLayout = this.optionsMenu;
        if (orientation != 1) {
            i2 = 1;
        }
        linearLayout.setOrientation(i2);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.optionsMenu.getLayoutParams();
        if (orientation == 2) {
            i = -2;
        } else {
            i = -1;
        }
        params.width = i;
        if (orientation != 1) {
            i3 = -1;
        }
        params.height = i3;
        this.optionsMenu.setLayoutParams(params);
    }

    private void rotateRight(LinearLayout layout) {
        int prevX = this.currentLocation.x;
        int prevY = this.currentLocation.y;
        this.windowManager.getDefaultDisplay().getSize(this.screenSize);
        this.currentLocation.x = (this.screenSize.x - prevY) - this.minWidth;
        this.currentLocation.y = prevX;
        LayoutParams params = (LayoutParams) layout.getLayoutParams();
        if (this.isExpanded) {
            params.x = 0;
            params.y = 0;
        } else {
            params.x = this.currentLocation.x;
            params.y = Math.min((this.screenSize.y - layout.getHeight()) - this.statusBarHeight, this.currentLocation.y);
        }
        this.cameraPreview.rotateAspectRatio();
        updateLayoutOrientation();
        this.windowManager.updateViewLayout(layout, params);
    }

    private void rotateLeft(LinearLayout layout) {
        int prevX = this.currentLocation.x;
        int prevY = this.currentLocation.y;
        this.windowManager.getDefaultDisplay().getSize(this.screenSize);
        this.currentLocation.x = prevY;
        this.currentLocation.y = (this.screenSize.y - prevX) - this.minHeight;
        LayoutParams params = (LayoutParams) layout.getLayoutParams();
        if (this.isExpanded) {
            params.x = 0;
            params.y = 0;
        } else {
            params.x = this.currentLocation.x;
            params.y = Math.min((this.screenSize.y - layout.getHeight()) - this.statusBarHeight, this.currentLocation.y);
        }
        this.cameraPreview.rotateAspectRatio();
        updateLayoutOrientation();
        this.windowManager.updateViewLayout(layout, params);
    }

    private LayoutParams getLayoutParams(boolean fullScreen) {
        LayoutParams params;
        if (fullScreen) {
            params = new LayoutParams(-1, -1, 2002, 262656, -3);
        } else {
            params = new LayoutParams(-2, -2, 2002, 262664, -3);
        }
        params.softInputMode = 16;
        params.gravity = 51;
        params.x = 0;
        params.y = 0;
        return params;
    }

    public void expandMenu(boolean expanded) {
        if (this.isExpanded != expanded) {
            this.isExpanded = expanded;
            if (this.isExpanded) {
                openMenu(this.broadcastMenu);
                this.broadcastMenu.requestFocus();
            } else {
                closeMenu(this.broadcastMenu);
            }
            configChatHUD();
        }
    }

    private void startCamera() {
        this.broadcastHelper.startCamera(this.cameraPreview);
        this.cameraPreview.setVisibility(0);
        selectIcon(this.toggleCamera);
    }

    private void hideSettings() {
        this.isSettingsEnabled = false;
        this.broadcastSettings.setVisibility(8);
        this.chatLayout.setVisibility(0);
        this.stopBroadcast.setVisibility(0);
        this.showSettings.setVisibility(0);
    }

    public void showSettings() {
        if (!this.isSettingsEnabled) {
            this.isSettingsEnabled = true;
            this.stopBroadcast.setVisibility(8);
            this.showSettings.setVisibility(8);
            this.chatLayout.setVisibility(8);
            this.broadcastSettings.setVisibility(0);
        }
    }

    private void openMenu(View view) {
        if (this.minWidth == 0 || this.minHeight == 0) {
            this.minWidth = this.broadcastMenu.getWidth();
            this.minHeight = this.broadcastMenu.getHeight();
        }
        int size = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_icon_size);
        this.logo.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        int paddingH = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_icon_padding_h);
        int paddingV = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_icon_padding_v);
        this.logo.setPadding(paddingH, paddingV, paddingH, paddingV);
        this.logo.setBackground(null);
        if (!this.broadcastHelper.isBroadcasting()) {
            this.logo.animate().alpha(0.35f);
        }
        this.broadcastMenu.setBackgroundResource(R.color.broadcast_menu_bg);
        this.optionsMenu.setBackgroundResource(17170444);
        this.mTimerTask = new AnimationTimerTask(new Point(0, 0), view, true);
        this.animationTimer.cancel();
        this.animationTimer = new Timer();
        this.animationTimer.schedule(this.mTimerTask, 0, 30);
    }

    private void closeMenu(View view) {
        hideMenuItems();
        this.mTimerTask = new AnimationTimerTask(new Point(this.currentLocation.x, Math.min((this.screenSize.y - this.minHeight) - this.statusBarHeight, this.currentLocation.y)), view, false);
        this.animationTimer.cancel();
        this.animationTimer = new Timer();
        this.animationTimer.schedule(this.mTimerTask, 0, 30);
    }

    private void showMenuItems() {
        int i = 0;
        if (this.broadcastHelper.isBroadcasting()) {
            this.stopBroadcast.setVisibility(0);
            this.showSettings.setVisibility(0);
        }
        for (View space : this.spaces) {
            space.setVisibility(0);
        }
        this.toggleCameraSwap.setVisibility(0);
        this.toggleMic.setVisibility(0);
        this.toggleCamera.setVisibility(0);
        this.togglePrivacy.setVisibility(0);
        this.toggleChatHUD.setVisibility(0);
        this.chatWindow.setVisibility(0);
        int orientation = this.service.getResources().getConfiguration().orientation;
        BroadcastLayout broadcastLayout = this.broadcastMenu;
        if (orientation != 2) {
            i = 1;
        }
        broadcastLayout.setOrientation(i);
    }

    private void hideMenuItems() {
        if (this.broadcastHelper.isBroadcasting()) {
            hideSettings();
        }
        this.chatWindow.setVisibility(8);
        this.toggleCameraSwap.setVisibility(8);
        this.toggleMic.setVisibility(8);
        this.stopBroadcast.setVisibility(8);
        this.toggleCamera.setVisibility(8);
        this.togglePrivacy.setVisibility(8);
        this.toggleChatHUD.setVisibility(8);
        this.broadcastMenu.setBackground(null);
        this.optionsMenu.setBackground(null);
        this.logo.setBackground(this.service.getDrawable(R.drawable.broadcast_circle));
        int size = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_small_icon_size);
        this.logo.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        int paddingH = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_small_icon_padding_h);
        int paddingW = this.service.getResources().getDimensionPixelSize(R.dimen.broadcast_small_icon_padding_v);
        this.logo.setPadding(paddingH, paddingW, paddingH, paddingW);
        if (!this.broadcastHelper.isBroadcasting()) {
            this.logo.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        }
        for (View space : this.spaces) {
            space.setVisibility(8);
        }
        this.broadcastMenu.setOrientation(0);
    }

    private void deselectIcon(ImageView view) {
        view.setColorFilter(-1, Mode.SRC_ATOP);
        view.animate().alpha(0.35f);
    }

    private void selectIcon(ImageView view) {
        view.setColorFilter(null);
        view.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:
                InputMethodManager imm = (InputMethodManager) this.service.getSystemService("input_method");
                if (imm != null) {
                    imm.showSoftInput(v, 1);
                }
                this.chatFragment.mEditText.setCursorVisible(true);
                return;
            case R.id.send_btn:
                sendChatMessage();
                return;
            case R.id.close_btn:
                UIUtils.hideVirtualKeyboard(this.service, this.chatFragment.mEditText);
                onKeyboardVisibilityChanged(false);
                return;
            default:
                return;
        }
    }

    public void showBroadcastStart() {
        if (!this.broadcastHelper.isBroadcasting()) {
            String title = this.titleSelection.getText().toString();
            String game = null;
            if (this.nameToIdMap != null) {
                game = (String) this.nameToIdMap.get(this.gameSelection.getText().toString());
            }
            this.progressBar.setVisibility(0);
            this.broadcastConfig.setEnabled(false);
            this.broadcastHelper.startBroadcast(new Callback() {
                public boolean handleMessage(Message msg) {
                    BroadcastMenu.this.progressBar.setVisibility(8);
                    BroadcastMenu.this.chatLayout.setVisibility(0);
                    BroadcastMenu.this.chatFragment.configChat();
                    return true;
                }
            }, new BroadcastStatusCallback() {
                public void onBroadcastStarted() {
                    BroadcastMenu.this.onBroadcastStarted();
                }

                public void onBroadcastEnded() {
                    BroadcastMenu.this.progressBar.setVisibility(8);
                    BroadcastMenu.this.showBroadcastEnd();
                }
            }, title, game);
        }
    }

    private void onBroadcastStarted() {
        this.broadcastSettings.setVisibility(8);
        this.hideSettings.setVisibility(0);
        this.isSettingsEnabled = false;
        this.showSettings.setVisibility(0);
        deselectIcon(this.showSettings);
        this.stopBroadcast.setVisibility(0);
        selectIcon(this.logo);
        this.broadcastConfig.setEnabled(true);
        this.broadcastConfig.setText(R.string.update);
        this.handler.postDelayed(new Runnable() {
            public void run() {
                BroadcastMenu.this.broadcastingNow.setVisibility(0);
                BroadcastMenu.this.handler.postDelayed(new Runnable() {
                    public void run() {
                        BroadcastMenu.this.broadcastingNow.setVisibility(8);
                    }
                }, 3000);
            }
        }, 200);
    }

    public void showBroadcastEnd() {
        if (this.broadcastHelper.isBroadcasting()) {
            this.broadcastHelper.endBroadcast();
            this.chatFragment.releaseChat();
            this.chatLayout.setVisibility(8);
            this.stopBroadcast.setVisibility(8);
            deselectIcon(this.logo);
            deselectIcon(this.togglePrivacy);
            this.isSettingsEnabled = true;
            this.showSettings.setVisibility(8);
            this.broadcastSettings.setVisibility(0);
            this.hideSettings.setVisibility(4);
            this.broadcastConfig.setText(R.string.start_your_broadcast);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        configChatButtons();
    }

    private void initSpaces(View view) {
        this.spaces = new LinkedList();
        this.spaces.add(view.findViewById(R.id.space1));
        this.spaces.add(view.findViewById(R.id.space2));
        this.spaces.add(view.findViewById(R.id.space3));
        this.spaces.add(view.findViewById(R.id.space4));
        this.spaces.add(view.findViewById(R.id.space5));
    }

    private void onKeyboardVisibilityChanged(boolean isKeyboardShown) {
        this.keyboardIsVisible = isKeyboardShown;
        this.chatFragment.mEditText.setCursorVisible(isKeyboardShown);
        if (isKeyboardShown) {
            this.chatFragment.mEditLayout.getHandler().removeCallbacksAndMessages(null);
            this.chatFragment.mEditLayout.getHandler().postDelayed(new Runnable() {
                public void run() {
                    AnimationUtils.changeColor(BroadcastMenu.this.chatFragment.mEditLayout, Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_inactive_background)).intValue(), Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_active_background)).intValue(), true);
                    AnimationUtils.changeColor(BroadcastMenu.this.chatFragment.mEditText, Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_inactive_color)).intValue(), Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_active_background)).intValue(), true);
                }
            }, 150);
            this.chatFragment.scrollToLatestMessage(true);
        } else {
            this.chatFragment.mEditText.setText(BuildConfig.FLAVOR);
            this.broadcastMenu.requestFocus();
            hideSettings();
            this.chatFragment.mEditLayout.getHandler().removeCallbacksAndMessages(null);
            this.chatFragment.mEditLayout.getHandler().postDelayed(new Runnable() {
                public void run() {
                    AnimationUtils.changeColor(BroadcastMenu.this.chatFragment.mEditLayout, Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_active_background)).intValue(), Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_inactive_background)).intValue(), true);
                    AnimationUtils.changeColor(BroadcastMenu.this.chatFragment.mEditText, Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_active_background)).intValue(), Integer.valueOf(MainApplication.getContext().getResources().getColor(R.color.chat_edit_inactive_color)).intValue(), true);
                }
            }, 150);
        }
        configChatButtons();
    }

    private void configChatButtons() {
        boolean messageIsEmpty;
        int i;
        int i2 = 0;
        if (this.chatFragment.mEditText.getText().length() == 0) {
            messageIsEmpty = true;
        } else {
            messageIsEmpty = false;
        }
        View view = this.chatSendBtn;
        if (messageIsEmpty || !this.keyboardIsVisible) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        View view2 = this.chatCloseBtn;
        if (!(messageIsEmpty && this.keyboardIsVisible)) {
            i2 = 8;
        }
        view2.setVisibility(i2);
    }
}
