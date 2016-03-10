package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;

public class VersionUtil {
    public static final String PACKAGE_VERSION_CLASS_NAME = "PackageVersion";
    @Deprecated
    public static final String VERSION_FILE = "VERSION.txt";
    private static final Pattern VERSION_SEPARATOR = Pattern.compile("[-_./;:]");
    private final Version _version;

    protected VersionUtil() {
        Version version = null;
        try {
            version = versionFor(getClass());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load Version information for bundle (via " + getClass().getName() + ").");
        }
        if (version == null) {
            version = Version.unknownVersion();
        }
        this._version = version;
    }

    public Version version() {
        return this._version;
    }

    public static Version versionFor(Class<?> cls) {
        Version packageVersionFor = packageVersionFor(cls);
        if (packageVersionFor != null) {
            return packageVersionFor;
        }
        InputStream resourceAsStream = cls.getResourceAsStream(VERSION_FILE);
        if (resourceAsStream == null) {
            return Version.unknownVersion();
        }
        Reader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(resourceAsStream, HTTP.UTF_8);
            packageVersionFor = doReadVersion(inputStreamReader);
            try {
                inputStreamReader.close();
            } catch (IOException e) {
            }
            try {
                resourceAsStream.close();
                return packageVersionFor;
            } catch (Throwable e2) {
                throw new RuntimeException(e2);
            }
        } catch (UnsupportedEncodingException e3) {
            try {
                packageVersionFor = Version.unknownVersion();
                return packageVersionFor;
            } finally {
                try {
                    resourceAsStream.close();
                } catch (Throwable e22) {
                    throw new RuntimeException(e22);
                }
            }
        } catch (Throwable th) {
            try {
                inputStreamReader.close();
            } catch (IOException e4) {
            }
        }
    }

    public static Version packageVersionFor(Class<?> cls) {
        try {
            Class cls2 = Class.forName(new StringBuilder(cls.getPackage().getName()).append(".").append(PACKAGE_VERSION_CLASS_NAME).toString(), true, cls.getClassLoader());
            if (cls2 == null) {
                return null;
            }
            try {
                Object newInstance = cls2.newInstance();
                if (newInstance instanceof Versioned) {
                    return ((Versioned) newInstance).version();
                }
                throw new IllegalArgumentException("Bad version class " + cls2.getName() + ": does not implement " + Versioned.class.getName());
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e2) {
                throw new IllegalArgumentException("Failed to instantiate " + cls2.getName() + " to find version information, problem: " + e2.getMessage(), e2);
            }
        } catch (Exception e3) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static com.fasterxml.jackson.core.Version doReadVersion(java.io.Reader r6) {
        /*
        r1 = 0;
        r3 = new java.io.BufferedReader;
        r3.<init>(r6);
        r2 = r3.readLine();	 Catch:{ IOException -> 0x0032, all -> 0x0041 }
        if (r2 == 0) goto L_0x004d;
    L_0x000c:
        r0 = r3.readLine();	 Catch:{ IOException -> 0x0048, all -> 0x0041 }
        if (r0 == 0) goto L_0x0016;
    L_0x0012:
        r1 = r3.readLine();	 Catch:{ IOException -> 0x004b, all -> 0x0041 }
    L_0x0016:
        r3.close();	 Catch:{ IOException -> 0x002d }
        r5 = r1;
        r1 = r0;
        r0 = r5;
    L_0x001c:
        if (r1 == 0) goto L_0x0022;
    L_0x001e:
        r1 = r1.trim();
    L_0x0022:
        if (r0 == 0) goto L_0x0028;
    L_0x0024:
        r0 = r0.trim();
    L_0x0028:
        r0 = parseVersion(r2, r1, r0);
        return r0;
    L_0x002d:
        r3 = move-exception;
        r5 = r1;
        r1 = r0;
        r0 = r5;
        goto L_0x001c;
    L_0x0032:
        r0 = move-exception;
        r0 = r1;
        r2 = r1;
    L_0x0035:
        r3.close();	 Catch:{ IOException -> 0x003c }
        r5 = r1;
        r1 = r0;
        r0 = r5;
        goto L_0x001c;
    L_0x003c:
        r3 = move-exception;
        r5 = r1;
        r1 = r0;
        r0 = r5;
        goto L_0x001c;
    L_0x0041:
        r0 = move-exception;
        r3.close();	 Catch:{ IOException -> 0x0046 }
    L_0x0045:
        throw r0;
    L_0x0046:
        r1 = move-exception;
        goto L_0x0045;
    L_0x0048:
        r0 = move-exception;
        r0 = r1;
        goto L_0x0035;
    L_0x004b:
        r4 = move-exception;
        goto L_0x0035;
    L_0x004d:
        r0 = r1;
        goto L_0x0016;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.util.VersionUtil.doReadVersion(java.io.Reader):com.fasterxml.jackson.core.Version");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.fasterxml.jackson.core.Version mavenVersionFor(java.lang.ClassLoader r5, java.lang.String r6, java.lang.String r7) {
        /*
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "META-INF/maven/";
        r0 = r0.append(r1);
        r1 = "\\.";
        r2 = "/";
        r1 = r6.replaceAll(r1, r2);
        r0 = r0.append(r1);
        r1 = "/";
        r0 = r0.append(r1);
        r0 = r0.append(r7);
        r1 = "/pom.properties";
        r0 = r0.append(r1);
        r0 = r0.toString();
        r1 = r5.getResourceAsStream(r0);
        if (r1 == 0) goto L_0x0058;
    L_0x0031:
        r0 = new java.util.Properties;	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r0.<init>();	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r0.load(r1);	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r2 = "version";
        r2 = r0.getProperty(r2);	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r3 = "artifactId";
        r3 = r0.getProperty(r3);	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r4 = "groupId";
        r0 = r0.getProperty(r4);	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r0 = parseVersion(r2, r0, r3);	 Catch:{ IOException -> 0x0054, all -> 0x005d }
        r1.close();	 Catch:{ IOException -> 0x0062 }
    L_0x0053:
        return r0;
    L_0x0054:
        r0 = move-exception;
        r1.close();	 Catch:{ IOException -> 0x0064 }
    L_0x0058:
        r0 = com.fasterxml.jackson.core.Version.unknownVersion();
        goto L_0x0053;
    L_0x005d:
        r0 = move-exception;
        r1.close();	 Catch:{ IOException -> 0x0066 }
    L_0x0061:
        throw r0;
    L_0x0062:
        r1 = move-exception;
        goto L_0x0053;
    L_0x0064:
        r0 = move-exception;
        goto L_0x0058;
    L_0x0066:
        r1 = move-exception;
        goto L_0x0061;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.util.VersionUtil.mavenVersionFor(java.lang.ClassLoader, java.lang.String, java.lang.String):com.fasterxml.jackson.core.Version");
    }

    @Deprecated
    public static Version parseVersion(String str) {
        return parseVersion(str, null, null);
    }

    public static Version parseVersion(String str, String str2, String str3) {
        String str4 = null;
        if (str == null) {
            return null;
        }
        CharSequence trim = str.trim();
        if (trim.length() == 0) {
            return null;
        }
        int parseVersionPart;
        int parseVersionPart2;
        String[] split = VERSION_SEPARATOR.split(trim);
        int parseVersionPart3 = parseVersionPart(split[0]);
        if (split.length > 1) {
            parseVersionPart = parseVersionPart(split[1]);
        } else {
            parseVersionPart = 0;
        }
        if (split.length > 2) {
            parseVersionPart2 = parseVersionPart(split[2]);
        } else {
            parseVersionPart2 = 0;
        }
        if (split.length > 3) {
            str4 = split[3];
        }
        return new Version(parseVersionPart3, parseVersionPart, parseVersionPart2, str4, str2, str3);
    }

    protected static int parseVersionPart(String str) {
        int i = 0;
        String str2 = str.toString();
        int length = str2.length();
        int i2 = 0;
        while (i < length) {
            char charAt = str2.charAt(i);
            if (charAt > '9' || charAt < '0') {
                break;
            }
            i2 = (i2 * 10) + (charAt - 48);
            i++;
        }
        return i2;
    }

    public static final void throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}
