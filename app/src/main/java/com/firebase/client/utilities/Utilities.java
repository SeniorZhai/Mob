package com.firebase.client.utilities;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.firebase.client.FirebaseException;
import com.firebase.client.core.Path;
import com.firebase.client.core.RepoInfo;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.protocol.HTTP;

public class Utilities {
    public static ParsedUrl parseUrl(String url) throws FirebaseException {
        String original = url;
        try {
            int schemeOffset = original.indexOf("//");
            if (schemeOffset == -1) {
                throw new URISyntaxException(original, "Invalid scheme specified");
            }
            int pathOffset = original.substring(schemeOffset + 2).indexOf("/");
            if (pathOffset != -1) {
                pathOffset += schemeOffset + 2;
                String[] pathSegments = original.substring(pathOffset).split("/");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < pathSegments.length; i++) {
                    if (!pathSegments[i].equals(BuildConfig.FLAVOR)) {
                        builder.append("/");
                        builder.append(URLEncoder.encode(pathSegments[i], HTTP.UTF_8));
                    }
                }
                original = original.substring(0, pathOffset) + builder.toString();
            }
            URI uri = new URI(original);
            String pathString = uri.getPath().replace("+", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            Validation.validateRootPathString(pathString);
            Path path = new Path(pathString);
            String scheme = uri.getScheme();
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.host = uri.getHost().toLowerCase();
            int port = uri.getPort();
            if (port != -1) {
                repoInfo.secure = scheme.equals("https");
                repoInfo.host += ":" + port;
            } else {
                repoInfo.secure = true;
            }
            repoInfo.namespace = repoInfo.host.split("\\.")[0].toLowerCase();
            repoInfo.internalHost = repoInfo.host;
            ParsedUrl parsedUrl = new ParsedUrl();
            parsedUrl.path = path;
            parsedUrl.repoInfo = repoInfo;
            return parsedUrl;
        } catch (URISyntaxException e) {
            throw new FirebaseException("Invalid Firebase url specified", e);
        } catch (UnsupportedEncodingException e2) {
            throw new FirebaseException("Failed to URLEncode the path", e2);
        }
    }

    public static String[] splitIntoFrames(String src, int maxFrameSize) {
        if (src.length() <= maxFrameSize) {
            return new String[]{src};
        }
        ArrayList<String> segs = new ArrayList();
        int i = 0;
        while (i < src.length()) {
            segs.add(src.substring(i, Math.min(i + maxFrameSize, src.length())));
            i += maxFrameSize;
        }
        return (String[]) segs.toArray(new String[segs.size()]);
    }

    public static String sha1HexDigest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(CommonUtils.SHA1_INSTANCE);
            md.update(input.getBytes(HTTP.UTF_8));
            return Base64.encodeBytes(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing SHA-1 MessageDigest provider.", e);
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException("UTF-8 encoding is required for Firebase to run!");
        }
    }

    public static String doubleToHashString(double value) {
        StringBuilder sb = new StringBuilder(16);
        ByteBuffer.wrap(new byte[8]).putDouble(value);
        for (int i = 0; i < 8; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }

    public static Integer tryParseInt(String num) {
        if (num.length() > 11 || num.length() == 0) {
            return null;
        }
        int i = 0;
        boolean negative = false;
        if (num.charAt(0) == '-') {
            if (num.length() == 1) {
                return null;
            }
            negative = true;
            i = 1;
        }
        long number = 0;
        for (i = 
/*
Method generation error in method: com.firebase.client.utilities.Utilities.tryParseInt(java.lang.String):java.lang.Integer
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r1_2 'i' int) = (r1_0 'i' int), (r1_1 'i' int) binds: {(r1_1 'i' int)=B:8:0x0022, (r1_0 'i' int)=B:5:0x0019} in method: com.firebase.client.utilities.Utilities.tryParseInt(java.lang.String):java.lang.Integer
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:230)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:183)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:280)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:167)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:534)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:518)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:224)
	... 19 more

*/

        public static int compareInts(int i, int j) {
            if (i < j) {
                return -1;
            }
            if (i == j) {
                return 0;
            }
            return 1;
        }

        public static int compareLongs(long i, long j) {
            if (i < j) {
                return -1;
            }
            if (i == j) {
                return 0;
            }
            return 1;
        }

        public static <C> C castOrNull(Object o, Class<C> clazz) {
            return clazz.isAssignableFrom(o.getClass()) ? o : null;
        }

        public static <C> C getOrNull(Object o, String key, Class<C> clazz) {
            if (o == null) {
                return null;
            }
            Object result = ((Map) castOrNull(o, Map.class)).get(key);
            if (result != null) {
                return castOrNull(result, clazz);
            }
            return null;
        }

        public static Long longFromObject(Object o) {
            if (o instanceof Integer) {
                return Long.valueOf((long) ((Integer) o).intValue());
            }
            if (o instanceof Long) {
                return (Long) o;
            }
            return null;
        }

        public static void hardAssert(boolean condition) {
            hardAssert(condition, BuildConfig.FLAVOR);
        }

        public static void hardAssert(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError("hardAssert failed: " + message);
            }
        }
    }
