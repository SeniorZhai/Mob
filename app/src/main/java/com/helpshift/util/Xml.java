package com.helpshift.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import com.google.android.exoplayer.util.MimeTypes;

public class Xml {
    public static int getLogoResourceValue(Context context) {
        int resId = getAttributeResourceValue(context, "AndroidManifest.xml", MimeTypes.BASE_TYPE_APPLICATION, "logo");
        if (resId == 0) {
            return context.getApplicationInfo().icon;
        }
        return resId;
    }

    private static int getAttributeResourceValue(Context context, String xmlFileName, String element, String attribute) {
        int logo = 0;
        try {
            XmlResourceParser xml = context.createPackageContext(context.getApplicationInfo().packageName, 0).getAssets().openXmlResourceParser(xmlFileName);
            for (int eventType = xml.getEventType(); eventType != 1; eventType = xml.nextToken()) {
                if (eventType == 2 && element.equals(xml.getName())) {
                    for (int i = xml.getAttributeCount() - 1; i >= 0; i--) {
                        if (attribute.equals(xml.getAttributeName(i))) {
                            logo = xml.getAttributeResourceValue(i, 0);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logo;
    }
}
