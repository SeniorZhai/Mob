package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.google.android.gms.analytics.ecommerce.Promotion;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class zzdg extends zzdd {
    private static final String ID = zzad.UNIVERSAL_ANALYTICS.toString();
    private static final String zzaOC = zzae.ACCOUNT.toString();
    private static final String zzaOD = zzae.ANALYTICS_PASS_THROUGH.toString();
    private static final String zzaOE = zzae.ENABLE_ECOMMERCE.toString();
    private static final String zzaOF = zzae.ECOMMERCE_USE_DATA_LAYER.toString();
    private static final String zzaOG = zzae.ECOMMERCE_MACRO_DATA.toString();
    private static final String zzaOH = zzae.ANALYTICS_FIELDS.toString();
    private static final String zzaOI = zzae.TRACK_TRANSACTION.toString();
    private static final String zzaOJ = zzae.TRANSACTION_DATALAYER_MAP.toString();
    private static final String zzaOK = zzae.TRANSACTION_ITEM_DATALAYER_MAP.toString();
    private static final List<String> zzaOL = Arrays.asList(new String[]{ProductAction.ACTION_DETAIL, ProductAction.ACTION_CHECKOUT, ProductAction.ACTION_CHECKOUT_OPTION, Promotion.ACTION_CLICK, ProductAction.ACTION_ADD, ProductAction.ACTION_REMOVE, ProductAction.ACTION_PURCHASE, ProductAction.ACTION_REFUND});
    private static final Pattern zzaOM = Pattern.compile("dimension(\\d+)");
    private static final Pattern zzaON = Pattern.compile("metric(\\d+)");
    private static Map<String, String> zzaOO;
    private static Map<String, String> zzaOP;
    private final DataLayer zzaKz;
    private final Set<String> zzaOQ;
    private final zzdc zzaOR;

    public zzdg(Context context, DataLayer dataLayer) {
        this(context, dataLayer, new zzdc(context));
    }

    zzdg(Context context, DataLayer dataLayer, zzdc com_google_android_gms_tagmanager_zzdc) {
        super(ID, new String[0]);
        this.zzaKz = dataLayer;
        this.zzaOR = com_google_android_gms_tagmanager_zzdc;
        this.zzaOQ = new HashSet();
        this.zzaOQ.add(BuildConfig.FLAVOR);
        this.zzaOQ.add(HSConsts.STATUS_NEW);
        this.zzaOQ.add("false");
    }

    private Double zzM(Object obj) {
        if (obj instanceof String) {
            try {
                return Double.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert the object to Double: " + e.getMessage());
            }
        } else if (obj instanceof Integer) {
            return Double.valueOf(((Integer) obj).doubleValue());
        } else {
            if (obj instanceof Double) {
                return (Double) obj;
            }
            throw new RuntimeException("Cannot convert the object to Double: " + obj.toString());
        }
    }

    private Integer zzN(Object obj) {
        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert the object to Integer: " + e.getMessage());
            }
        } else if (obj instanceof Double) {
            return Integer.valueOf(((Double) obj).intValue());
        } else {
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
            throw new RuntimeException("Cannot convert the object to Integer: " + obj.toString());
        }
    }

    private Promotion zzO(Map<String, String> map) {
        Promotion promotion = new Promotion();
        String str = (String) map.get(DBLikedChannelsHelper.KEY_ID);
        if (str != null) {
            promotion.setId(String.valueOf(str));
        }
        str = (String) map.get(ProfilesDBHelper.COLUMN_NAME);
        if (str != null) {
            promotion.setName(String.valueOf(str));
        }
        str = (String) map.get("creative");
        if (str != null) {
            promotion.setCreative(String.valueOf(str));
        }
        str = (String) map.get("position");
        if (str != null) {
            promotion.setPosition(String.valueOf(str));
        }
        return promotion;
    }

    private Product zzP(Map<String, Object> map) {
        Product product = new Product();
        Object obj = map.get(DBLikedChannelsHelper.KEY_ID);
        if (obj != null) {
            product.setId(String.valueOf(obj));
        }
        obj = map.get(ProfilesDBHelper.COLUMN_NAME);
        if (obj != null) {
            product.setName(String.valueOf(obj));
        }
        obj = map.get("brand");
        if (obj != null) {
            product.setBrand(String.valueOf(obj));
        }
        obj = map.get("category");
        if (obj != null) {
            product.setCategory(String.valueOf(obj));
        }
        obj = map.get("variant");
        if (obj != null) {
            product.setVariant(String.valueOf(obj));
        }
        obj = map.get("coupon");
        if (obj != null) {
            product.setCouponCode(String.valueOf(obj));
        }
        obj = map.get("position");
        if (obj != null) {
            product.setPosition(zzN(obj).intValue());
        }
        obj = map.get("price");
        if (obj != null) {
            product.setPrice(zzM(obj).doubleValue());
        }
        obj = map.get("quantity");
        if (obj != null) {
            product.setQuantity(zzN(obj).intValue());
        }
        for (String str : map.keySet()) {
            Matcher matcher = zzaOM.matcher(str);
            if (matcher.matches()) {
                try {
                    product.setCustomDimension(Integer.parseInt(matcher.group(1)), String.valueOf(map.get(str)));
                } catch (NumberFormatException e) {
                    zzbg.zzaC("illegal number in custom dimension value: " + str);
                }
            } else {
                matcher = zzaON.matcher(str);
                if (matcher.matches()) {
                    try {
                        product.setCustomMetric(Integer.parseInt(matcher.group(1)), zzN(map.get(str)).intValue());
                    } catch (NumberFormatException e2) {
                        zzbg.zzaC("illegal number in custom metric value: " + str);
                    }
                }
            }
        }
        return product;
    }

    private Map<String, String> zzQ(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaOJ);
        if (com_google_android_gms_internal_zzag_zza != null) {
            return zzc(com_google_android_gms_internal_zzag_zza);
        }
        if (zzaOO == null) {
            Map hashMap = new HashMap();
            hashMap.put("transactionId", "&ti");
            hashMap.put("transactionAffiliation", "&ta");
            hashMap.put("transactionTax", "&tt");
            hashMap.put("transactionShipping", "&ts");
            hashMap.put("transactionTotal", "&tr");
            hashMap.put("transactionCurrency", "&cu");
            zzaOO = hashMap;
        }
        return zzaOO;
    }

    private Map<String, String> zzR(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaOK);
        if (com_google_android_gms_internal_zzag_zza != null) {
            return zzc(com_google_android_gms_internal_zzag_zza);
        }
        if (zzaOP == null) {
            Map hashMap = new HashMap();
            hashMap.put(ProfilesDBHelper.COLUMN_NAME, "&in");
            hashMap.put("sku", "&ic");
            hashMap.put("category", "&iv");
            hashMap.put("price", "&ip");
            hashMap.put("quantity", "&iq");
            hashMap.put("currency", "&cu");
            zzaOP = hashMap;
        }
        return zzaOP;
    }

    private void zza(Tracker tracker, Map<String, zza> map) {
        String zzeO = zzeO("transactionId");
        if (zzeO == null) {
            zzbg.zzaz("Cannot find transactionId in data layer.");
            return;
        }
        List<Map> linkedList = new LinkedList();
        try {
            Map zzm = zzm((zza) map.get(zzaOH));
            zzm.put("&t", "transaction");
            for (Entry entry : zzQ(map).entrySet()) {
                zzd(zzm, (String) entry.getValue(), zzeO((String) entry.getKey()));
            }
            linkedList.add(zzm);
            List<Map> zzeP = zzeP("transactionProducts");
            if (zzeP != null) {
                for (Map map2 : zzeP) {
                    if (map2.get(ProfilesDBHelper.COLUMN_NAME) == null) {
                        zzbg.zzaz("Unable to send transaction item hit due to missing 'name' field.");
                        return;
                    }
                    Map zzm2 = zzm((zza) map.get(zzaOH));
                    zzm2.put("&t", "item");
                    zzm2.put("&ti", zzeO);
                    for (Entry entry2 : zzR(map).entrySet()) {
                        zzd(zzm2, (String) entry2.getValue(), (String) map2.get(entry2.getKey()));
                    }
                    linkedList.add(zzm2);
                }
            }
            for (Map map22 : linkedList) {
                tracker.send(map22);
            }
        } catch (Throwable e) {
            zzbg.zzb("Unable to send transaction", e);
        }
    }

    private void zzb(Tracker tracker, Map<String, zza> map) {
        Object obj;
        Map map2;
        ScreenViewBuilder screenViewBuilder = new ScreenViewBuilder();
        Map zzm = zzm((zza) map.get(zzaOH));
        screenViewBuilder.setAll(zzm);
        if (zzi(map, zzaOF)) {
            obj = this.zzaKz.get("ecommerce");
            map2 = obj instanceof Map ? (Map) obj : null;
        } else {
            obj = zzdf.zzl((zza) map.get(zzaOG));
            map2 = obj instanceof Map ? (Map) obj : null;
        }
        if (map2 != null) {
            Map map3;
            List<Map> list;
            String str = (String) zzm.get("&cu");
            if (str == null) {
                str = (String) map2.get("currencyCode");
            }
            if (str != null) {
                screenViewBuilder.set("&cu", str);
            }
            obj = map2.get("impressions");
            if (obj instanceof List) {
                for (Map map4 : (List) obj) {
                    try {
                        screenViewBuilder.addImpression(zzP(map4), (String) map4.get("list"));
                    } catch (RuntimeException e) {
                        zzbg.zzaz("Failed to extract a product from DataLayer. " + e.getMessage());
                    }
                }
            }
            List list2 = map2.containsKey("promoClick") ? (List) ((Map) map2.get("promoClick")).get("promotions") : map2.containsKey("promoView") ? (List) ((Map) map2.get("promoView")).get("promotions") : null;
            if (r0 != null) {
                for (Map map42 : r0) {
                    try {
                        screenViewBuilder.addPromotion(zzO(map42));
                    } catch (RuntimeException e2) {
                        zzbg.zzaz("Failed to extract a promotion from DataLayer. " + e2.getMessage());
                    }
                }
                if (map2.containsKey("promoClick")) {
                    screenViewBuilder.set("&promoa", Promotion.ACTION_CLICK);
                    obj = null;
                    if (obj != null) {
                        for (String str2 : zzaOL) {
                            if (map2.containsKey(str2)) {
                                map3 = (Map) map2.get(str2);
                                list = (List) map3.get("products");
                                if (list != null) {
                                    for (Map map22 : list) {
                                        try {
                                            screenViewBuilder.addProduct(zzP(map22));
                                        } catch (RuntimeException e3) {
                                            zzbg.zzaz("Failed to extract a product from DataLayer. " + e3.getMessage());
                                        }
                                    }
                                }
                                try {
                                    screenViewBuilder.setProductAction(map3.containsKey("actionField") ? zze(str2, (Map) map3.get("actionField")) : new ProductAction(str2));
                                } catch (RuntimeException e22) {
                                    zzbg.zzaz("Failed to extract a product action from DataLayer. " + e22.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    screenViewBuilder.set("&promoa", Promotion.ACTION_VIEW);
                }
            }
            int i = 1;
            if (obj != null) {
                for (String str22 : zzaOL) {
                    if (map22.containsKey(str22)) {
                        map3 = (Map) map22.get(str22);
                        list = (List) map3.get("products");
                        if (list != null) {
                            while (r4.hasNext()) {
                                screenViewBuilder.addProduct(zzP(map22));
                            }
                        }
                        if (map3.containsKey("actionField")) {
                        }
                        screenViewBuilder.setProductAction(map3.containsKey("actionField") ? zze(str22, (Map) map3.get("actionField")) : new ProductAction(str22));
                    }
                }
            }
        }
        tracker.send(screenViewBuilder.build());
    }

    private Map<String, String> zzc(zza com_google_android_gms_internal_zzag_zza) {
        Object zzl = zzdf.zzl(com_google_android_gms_internal_zzag_zza);
        if (!(zzl instanceof Map)) {
            return null;
        }
        Map map = (Map) zzl;
        Map<String, String> linkedHashMap = new LinkedHashMap();
        for (Entry entry : map.entrySet()) {
            linkedHashMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return linkedHashMap;
    }

    private void zzd(Map<String, String> map, String str, String str2) {
        if (str2 != null) {
            map.put(str, str2);
        }
    }

    private ProductAction zze(String str, Map<String, Object> map) {
        ProductAction productAction = new ProductAction(str);
        Object obj = map.get(DBLikedChannelsHelper.KEY_ID);
        if (obj != null) {
            productAction.setTransactionId(String.valueOf(obj));
        }
        obj = map.get("affiliation");
        if (obj != null) {
            productAction.setTransactionAffiliation(String.valueOf(obj));
        }
        obj = map.get("coupon");
        if (obj != null) {
            productAction.setTransactionCouponCode(String.valueOf(obj));
        }
        obj = map.get("list");
        if (obj != null) {
            productAction.setProductActionList(String.valueOf(obj));
        }
        obj = map.get("option");
        if (obj != null) {
            productAction.setCheckoutOptions(String.valueOf(obj));
        }
        obj = map.get("revenue");
        if (obj != null) {
            productAction.setTransactionRevenue(zzM(obj).doubleValue());
        }
        obj = map.get("tax");
        if (obj != null) {
            productAction.setTransactionTax(zzM(obj).doubleValue());
        }
        obj = map.get("shipping");
        if (obj != null) {
            productAction.setTransactionShipping(zzM(obj).doubleValue());
        }
        obj = map.get("step");
        if (obj != null) {
            productAction.setCheckoutStep(zzN(obj).intValue());
        }
        return productAction;
    }

    private String zzeO(String str) {
        Object obj = this.zzaKz.get(str);
        return obj == null ? null : obj.toString();
    }

    private List<Map<String, String>> zzeP(String str) {
        Object obj = this.zzaKz.get(str);
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            for (Object obj2 : (List) obj) {
                if (!(obj2 instanceof Map)) {
                    throw new IllegalArgumentException("Each element of transactionProducts should be of type Map.");
                }
            }
            return (List) obj;
        }
        throw new IllegalArgumentException("transactionProducts should be of type List.");
    }

    private boolean zzi(Map<String, zza> map, String str) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(str);
        return com_google_android_gms_internal_zzag_zza == null ? false : zzdf.zzk(com_google_android_gms_internal_zzag_zza).booleanValue();
    }

    private Map<String, String> zzm(zza com_google_android_gms_internal_zzag_zza) {
        if (com_google_android_gms_internal_zzag_zza == null) {
            return new HashMap();
        }
        Map<String, String> zzc = zzc(com_google_android_gms_internal_zzag_zza);
        if (zzc == null) {
            return new HashMap();
        }
        String str = (String) zzc.get("&aip");
        if (str != null && this.zzaOQ.contains(str.toLowerCase())) {
            zzc.remove("&aip");
        }
        return zzc;
    }

    public /* bridge */ /* synthetic */ zza zzE(Map map) {
        return super.zzE(map);
    }

    public void zzG(Map<String, zza> map) {
        Tracker zzeG = this.zzaOR.zzeG("_GTM_DEFAULT_TRACKER_");
        zzeG.enableAdvertisingIdCollection(zzi(map, "collect_adid"));
        if (zzi(map, zzaOE)) {
            zzb(zzeG, map);
        } else if (zzi(map, zzaOD)) {
            zzeG.send(zzm((zza) map.get(zzaOH)));
        } else if (zzi(map, zzaOI)) {
            zza(zzeG, map);
        } else {
            zzbg.zzaC("Ignoring unknown tag.");
        }
    }

    public /* bridge */ /* synthetic */ String zzyM() {
        return super.zzyM();
    }

    public /* bridge */ /* synthetic */ Set zzyN() {
        return super.zzyN();
    }

    public /* bridge */ /* synthetic */ boolean zzyh() {
        return super.zzyh();
    }
}
