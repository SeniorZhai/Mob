package com.mobcrush.mobcrush;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuItem;
import android.widget.ImageView;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import java.io.File;

public class EditProfileFragment extends Fragment implements OnMenuItemClickListener {
    protected static final int PICK_IMAGE = 1;
    protected static final int TAKE_IMAGE = 2;
    protected ImageView mProfileLogo;
    protected File mUserPhotoFile;
    protected int mUserPhotoOrientation = 0;

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile_photo:
                CharSequence[] charSequenceArr;
                try {
                    this.mUserPhotoFile = UIUtils.createImageFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.mUserPhotoFile = null;
                }
                boolean diableTakingPhoto = false;
                if (this.mUserPhotoFile == null || new Intent("android.media.action.IMAGE_CAPTURE").resolveActivity(getActivity().getPackageManager()) == null) {
                    diableTakingPhoto = true;
                }
                Builder title = new Builder(getActivity()).setTitle(MainApplication.getRString(R.string.select_profile_photo, new Object[0]));
                if (diableTakingPhoto) {
                    charSequenceArr = new CharSequence[PICK_IMAGE];
                    charSequenceArr[0] = MainApplication.getRString(R.string.your_photos, new Object[0]);
                } else {
                    charSequenceArr = new CharSequence[TAKE_IMAGE];
                    charSequenceArr[0] = MainApplication.getRString(R.string.your_photos, new Object[0]);
                    charSequenceArr[PICK_IMAGE] = MainApplication.getRString(R.string.take_photo, new Object[0]);
                }
                title.setItems(charSequenceArr, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        switch (which) {
                            case ResponseParser.ResponseActionDiscard /*0*/:
                                i = new Intent("android.intent.action.GET_CONTENT");
                                i.setType("image/*");
                                i.putExtra("android.intent.extra.LOCAL_ONLY", true);
                                EditProfileFragment.this.startActivityForResult(i, EditProfileFragment.PICK_IMAGE);
                                return;
                            case EditProfileFragment.PICK_IMAGE /*1*/:
                                i = new Intent("android.media.action.IMAGE_CAPTURE");
                                i.putExtra("output", Uri.fromFile(EditProfileFragment.this.mUserPhotoFile));
                                EditProfileFragment.this.startActivityForResult(i, EditProfileFragment.TAKE_IMAGE);
                                return;
                            default:
                                return;
                        }
                    }
                }).create().show();
                return true;
            case R.id.action_edit_profile_info:
                startActivity(ProfileAboutActivity.getIntent(getActivity(), PreferenceUtility.getUser(), true));
                return true;
            default:
                return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int r11, int r12, android.content.Intent r13) {
        /*
        r10 = this;
        super.onActivityResult(r11, r12, r13);
        r6 = -1;
        if (r12 == r6) goto L_0x0018;
    L_0x0006:
        r6 = r10.mUserPhotoFile;
        if (r6 == 0) goto L_0x0017;
    L_0x000a:
        r6 = r10.mUserPhotoFile;
        r6 = r6.exists();
        if (r6 == 0) goto L_0x0017;
    L_0x0012:
        r6 = r10.mUserPhotoFile;
        r6.delete();
    L_0x0017:
        return;
    L_0x0018:
        switch(r11) {
            case 1: goto L_0x001c;
            case 2: goto L_0x00a0;
            case 6709: goto L_0x00ea;
            default: goto L_0x001b;
        };
    L_0x001b:
        goto L_0x0017;
    L_0x001c:
        if (r13 == 0) goto L_0x0017;
    L_0x001e:
        r6 = com.mobcrush.mobcrush.common.UIUtils.createImageFile();	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r10.mUserPhotoFile = r6;	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r3 = new android.media.ExifInterface;	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
        r6 = r10.mUserPhotoFile;	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
        r6 = r6.getAbsolutePath();	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
        r3.<init>(r6);	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
        r6 = "Orientation";
        r7 = 0;
        r6 = r3.getAttributeInt(r6, r7);	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
        r10.mUserPhotoOrientation = r6;	 Catch:{ Exception -> 0x0080, OutOfMemoryError -> 0x0085 }
    L_0x0038:
        r6 = new com.soundcloud.android.crop.CropCompat;	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r7 = r13.getData();	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r6.<init>(r7);	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r7 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r8 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r6 = r6.withMaxSize(r7, r8);	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r7 = r10.mUserPhotoFile;	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r7 = android.net.Uri.fromFile(r7);	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r6 = r6.output(r7);	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r6 = r6.asSquare();	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r7 = r10.getActivity();	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        r6.start(r7, r10);	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        goto L_0x0017;
    L_0x005f:
        r2 = move-exception;
        r2.printStackTrace();
        com.crashlytics.android.Crashlytics.logException(r2);
        r6 = 0;
        r10.mUserPhotoFile = r6;
        r6 = r10.getActivity();
        r7 = 2131165500; // 0x7f07013c float:1.7945219E38 double:1.052935659E-314;
        r8 = 0;
        r8 = new java.lang.Object[r8];
        r7 = com.mobcrush.mobcrush.MainApplication.getRString(r7, r8);
        r8 = 1;
        r6 = android.widget.Toast.makeText(r6, r7, r8);
        r6.show();
        goto L_0x0017;
    L_0x0080:
        r2 = move-exception;
        r6 = 0;
        r10.mUserPhotoOrientation = r6;	 Catch:{ Exception -> 0x005f, OutOfMemoryError -> 0x0085 }
        goto L_0x0038;
    L_0x0085:
        r2 = move-exception;
        r2.printStackTrace();
        com.crashlytics.android.Crashlytics.logException(r2);
        r6 = 0;
        r10.mUserPhotoFile = r6;
        r6 = r10.getActivity();
        r7 = 2131165582; // 0x7f07018e float:1.7945385E38 double:1.0529356997E-314;
        r8 = 1;
        r6 = android.widget.Toast.makeText(r6, r7, r8);
        r6.show();
        goto L_0x0017;
    L_0x00a0:
        r6 = r10.mUserPhotoFile;
        if (r6 == 0) goto L_0x0017;
    L_0x00a4:
        java.lang.System.gc();
        r3 = new android.media.ExifInterface;	 Catch:{ Exception -> 0x00e5 }
        r6 = r10.mUserPhotoFile;	 Catch:{ Exception -> 0x00e5 }
        r6 = r6.getAbsolutePath();	 Catch:{ Exception -> 0x00e5 }
        r3.<init>(r6);	 Catch:{ Exception -> 0x00e5 }
        r6 = "Orientation";
        r7 = 0;
        r6 = r3.getAttributeInt(r6, r7);	 Catch:{ Exception -> 0x00e5 }
        r10.mUserPhotoOrientation = r6;	 Catch:{ Exception -> 0x00e5 }
    L_0x00bb:
        r6 = new com.soundcloud.android.crop.CropCompat;
        r7 = r10.mUserPhotoFile;
        r7 = android.net.Uri.fromFile(r7);
        r6.<init>(r7);
        r7 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r8 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r6 = r6.withMaxSize(r7, r8);
        r7 = r10.mUserPhotoFile;
        r7 = android.net.Uri.fromFile(r7);
        r6 = r6.output(r7);
        r6 = r6.asSquare();
        r7 = r10.getActivity();
        r6.start(r7, r10);
        goto L_0x0017;
    L_0x00e5:
        r2 = move-exception;
        r6 = 0;
        r10.mUserPhotoOrientation = r6;
        goto L_0x00bb;
    L_0x00ea:
        r6 = r10.mUserPhotoFile;	 Catch:{ OutOfMemoryError -> 0x016e }
        if (r6 == 0) goto L_0x01a3;
    L_0x00ee:
        java.lang.System.gc();	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r10.mUserPhotoFile;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r6.getAbsolutePath();	 Catch:{ OutOfMemoryError -> 0x016e }
        r5 = com.mobcrush.mobcrush.common.UIUtils.getImageSize(r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r5.first;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = (java.lang.Integer) r6;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r6.intValue();	 Catch:{ OutOfMemoryError -> 0x016e }
        r7 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        if (r6 > r7) goto L_0x0113;
    L_0x0107:
        r6 = r5.second;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = (java.lang.Integer) r6;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r6.intValue();	 Catch:{ OutOfMemoryError -> 0x016e }
        r7 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        if (r6 <= r7) goto L_0x018c;
    L_0x0113:
        r6 = r10.mUserPhotoFile;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r6.getAbsolutePath();	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = android.graphics.BitmapFactory.decodeFile(r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        r7 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r8 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r9 = 0;
        r0 = android.graphics.Bitmap.createScaledBitmap(r6, r7, r8, r9);	 Catch:{ OutOfMemoryError -> 0x016e }
    L_0x0126:
        r6 = r10.mUserPhotoOrientation;	 Catch:{ OutOfMemoryError -> 0x016e }
        if (r6 == 0) goto L_0x0133;
    L_0x012a:
        r6 = r10.mUserPhotoOrientation;	 Catch:{ OutOfMemoryError -> 0x016e }
        r0 = com.mobcrush.mobcrush.common.UIUtils.rotateBitmap(r0, r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = 0;
        r10.mUserPhotoOrientation = r6;	 Catch:{ OutOfMemoryError -> 0x016e }
    L_0x0133:
        r4 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x0197 }
        r6 = r10.mUserPhotoFile;	 Catch:{ Exception -> 0x0197 }
        r4.<init>(r6);	 Catch:{ Exception -> 0x0197 }
        r6 = android.graphics.Bitmap.CompressFormat.JPEG;	 Catch:{ Exception -> 0x0197 }
        r7 = 100;
        r0.compress(r6, r7, r4);	 Catch:{ Exception -> 0x0197 }
        r0.recycle();	 Catch:{ Exception -> 0x0197 }
        r1 = new android.app.ProgressDialog;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r10.getActivity();	 Catch:{ OutOfMemoryError -> 0x016e }
        r1.<init>(r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = 2131165697; // 0x7f070201 float:1.7945618E38 double:1.0529357565E-314;
        r7 = 0;
        r7 = new java.lang.Object[r7];	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = com.mobcrush.mobcrush.MainApplication.getRString(r6, r7);	 Catch:{ OutOfMemoryError -> 0x016e }
        r1.setMessage(r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        r1.show();	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r10.mUserPhotoFile;	 Catch:{ OutOfMemoryError -> 0x016e }
        r7 = new com.mobcrush.mobcrush.EditProfileFragment$2;	 Catch:{ OutOfMemoryError -> 0x016e }
        r7.<init>(r1);	 Catch:{ OutOfMemoryError -> 0x016e }
        r8 = new com.mobcrush.mobcrush.EditProfileFragment$3;	 Catch:{ OutOfMemoryError -> 0x016e }
        r8.<init>(r1);	 Catch:{ OutOfMemoryError -> 0x016e }
        com.mobcrush.mobcrush.network.Network.uploadPhoto(r6, r7, r8);	 Catch:{ OutOfMemoryError -> 0x016e }
        goto L_0x0017;
    L_0x016e:
        r2 = move-exception;
        r2.printStackTrace();
        com.crashlytics.android.Crashlytics.logException(r2);
        r6 = 0;
        r10.mUserPhotoFile = r6;
        r6 = com.mobcrush.mobcrush.MainApplication.getContext();	 Catch:{ Throwable -> 0x0189 }
        r7 = 2131165582; // 0x7f07018e float:1.7945385E38 double:1.0529356997E-314;
        r8 = 1;
        r6 = android.widget.Toast.makeText(r6, r7, r8);	 Catch:{ Throwable -> 0x0189 }
        r6.show();	 Catch:{ Throwable -> 0x0189 }
        goto L_0x0017;
    L_0x0189:
        r6 = move-exception;
        goto L_0x0017;
    L_0x018c:
        r6 = r10.mUserPhotoFile;	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = r6.getAbsolutePath();	 Catch:{ OutOfMemoryError -> 0x016e }
        r0 = android.graphics.BitmapFactory.decodeFile(r6);	 Catch:{ OutOfMemoryError -> 0x016e }
        goto L_0x0126;
    L_0x0197:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ OutOfMemoryError -> 0x016e }
        com.crashlytics.android.Crashlytics.logException(r2);	 Catch:{ OutOfMemoryError -> 0x016e }
        r6 = 0;
        r10.mUserPhotoFile = r6;	 Catch:{ OutOfMemoryError -> 0x016e }
        goto L_0x0017;
    L_0x01a3:
        r6 = com.mobcrush.mobcrush.MainApplication.getContext();	 Catch:{ Throwable -> 0x01bb }
        r7 = 2131165500; // 0x7f07013c float:1.7945219E38 double:1.052935659E-314;
        r8 = 0;
        r8 = new java.lang.Object[r8];	 Catch:{ Throwable -> 0x01bb }
        r7 = com.mobcrush.mobcrush.MainApplication.getRString(r7, r8);	 Catch:{ Throwable -> 0x01bb }
        r8 = 1;
        r6 = android.widget.Toast.makeText(r6, r7, r8);	 Catch:{ Throwable -> 0x01bb }
        r6.show();	 Catch:{ Throwable -> 0x01bb }
        goto L_0x0017;
    L_0x01bb:
        r6 = move-exception;
        goto L_0x0017;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mobcrush.mobcrush.EditProfileFragment.onActivityResult(int, int, android.content.Intent):void");
    }
}
