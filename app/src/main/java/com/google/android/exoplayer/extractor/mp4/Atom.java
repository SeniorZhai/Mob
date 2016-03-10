package com.google.android.exoplayer.extractor.mp4;

import android.support.v4.view.ViewCompat;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Atom {
    public static final int FULL_HEADER_SIZE = 12;
    public static final int HEADER_SIZE = 8;
    public static final int LONG_HEADER_SIZE = 16;
    public static final int LONG_SIZE_PREFIX = 1;
    public static final int TYPE_TTML = Util.getIntegerCodeForString("TTML");
    public static final int TYPE_ac_3 = Util.getIntegerCodeForString("ac-3");
    public static final int TYPE_avc1 = Util.getIntegerCodeForString("avc1");
    public static final int TYPE_avc3 = Util.getIntegerCodeForString("avc3");
    public static final int TYPE_avcC = Util.getIntegerCodeForString("avcC");
    public static final int TYPE_co64 = Util.getIntegerCodeForString("co64");
    public static final int TYPE_ctts = Util.getIntegerCodeForString("ctts");
    public static final int TYPE_dac3 = Util.getIntegerCodeForString("dac3");
    public static final int TYPE_dec3 = Util.getIntegerCodeForString("dec3");
    public static final int TYPE_ec_3 = Util.getIntegerCodeForString("ec-3");
    public static final int TYPE_enca = Util.getIntegerCodeForString("enca");
    public static final int TYPE_encv = Util.getIntegerCodeForString("encv");
    public static final int TYPE_esds = Util.getIntegerCodeForString("esds");
    public static final int TYPE_frma = Util.getIntegerCodeForString("frma");
    public static final int TYPE_ftyp = Util.getIntegerCodeForString("ftyp");
    public static final int TYPE_hdlr = Util.getIntegerCodeForString("hdlr");
    public static final int TYPE_hev1 = Util.getIntegerCodeForString("hev1");
    public static final int TYPE_hvc1 = Util.getIntegerCodeForString("hvc1");
    public static final int TYPE_hvcC = Util.getIntegerCodeForString("hvcC");
    public static final int TYPE_mdat = Util.getIntegerCodeForString("mdat");
    public static final int TYPE_mdhd = Util.getIntegerCodeForString("mdhd");
    public static final int TYPE_mdia = Util.getIntegerCodeForString("mdia");
    public static final int TYPE_minf = Util.getIntegerCodeForString("minf");
    public static final int TYPE_moof = Util.getIntegerCodeForString("moof");
    public static final int TYPE_moov = Util.getIntegerCodeForString("moov");
    public static final int TYPE_mp4a = Util.getIntegerCodeForString("mp4a");
    public static final int TYPE_mp4v = Util.getIntegerCodeForString("mp4v");
    public static final int TYPE_mvex = Util.getIntegerCodeForString("mvex");
    public static final int TYPE_mvhd = Util.getIntegerCodeForString("mvhd");
    public static final int TYPE_pasp = Util.getIntegerCodeForString("pasp");
    public static final int TYPE_pssh = Util.getIntegerCodeForString("pssh");
    public static final int TYPE_saiz = Util.getIntegerCodeForString("saiz");
    public static final int TYPE_schi = Util.getIntegerCodeForString("schi");
    public static final int TYPE_schm = Util.getIntegerCodeForString("schm");
    public static final int TYPE_senc = Util.getIntegerCodeForString("senc");
    public static final int TYPE_sidx = Util.getIntegerCodeForString("sidx");
    public static final int TYPE_sinf = Util.getIntegerCodeForString("sinf");
    public static final int TYPE_smhd = Util.getIntegerCodeForString("smhd");
    public static final int TYPE_stbl = Util.getIntegerCodeForString("stbl");
    public static final int TYPE_stco = Util.getIntegerCodeForString("stco");
    public static final int TYPE_stsc = Util.getIntegerCodeForString("stsc");
    public static final int TYPE_stsd = Util.getIntegerCodeForString("stsd");
    public static final int TYPE_stss = Util.getIntegerCodeForString("stss");
    public static final int TYPE_stsz = Util.getIntegerCodeForString("stsz");
    public static final int TYPE_stts = Util.getIntegerCodeForString("stts");
    public static final int TYPE_tenc = Util.getIntegerCodeForString("tenc");
    public static final int TYPE_tfdt = Util.getIntegerCodeForString("tfdt");
    public static final int TYPE_tfhd = Util.getIntegerCodeForString("tfhd");
    public static final int TYPE_tkhd = Util.getIntegerCodeForString("tkhd");
    public static final int TYPE_traf = Util.getIntegerCodeForString("traf");
    public static final int TYPE_trak = Util.getIntegerCodeForString("trak");
    public static final int TYPE_trex = Util.getIntegerCodeForString("trex");
    public static final int TYPE_trun = Util.getIntegerCodeForString("trun");
    public static final int TYPE_tx3g = Util.getIntegerCodeForString("tx3g");
    public static final int TYPE_uuid = Util.getIntegerCodeForString("uuid");
    public static final int TYPE_vmhd = Util.getIntegerCodeForString("vmhd");
    public final int type;

    public static final class ContainerAtom extends Atom {
        public final List<ContainerAtom> containerChildren = new ArrayList();
        public final long endByteOffset;
        public final List<LeafAtom> leafChildren = new ArrayList();

        public ContainerAtom(int type, long endByteOffset) {
            super(type);
            this.endByteOffset = endByteOffset;
        }

        public void add(LeafAtom atom) {
            this.leafChildren.add(atom);
        }

        public void add(ContainerAtom atom) {
            this.containerChildren.add(atom);
        }

        public LeafAtom getLeafAtomOfType(int type) {
            int childrenSize = this.leafChildren.size();
            for (int i = 0; i < childrenSize; i += Atom.LONG_SIZE_PREFIX) {
                LeafAtom atom = (LeafAtom) this.leafChildren.get(i);
                if (atom.type == type) {
                    return atom;
                }
            }
            return null;
        }

        public ContainerAtom getContainerAtomOfType(int type) {
            int childrenSize = this.containerChildren.size();
            for (int i = 0; i < childrenSize; i += Atom.LONG_SIZE_PREFIX) {
                ContainerAtom atom = (ContainerAtom) this.containerChildren.get(i);
                if (atom.type == type) {
                    return atom;
                }
            }
            return null;
        }

        public String toString() {
            return Atom.getAtomTypeString(this.type) + " leaves: " + Arrays.toString(this.leafChildren.toArray(new LeafAtom[0])) + " containers: " + Arrays.toString(this.containerChildren.toArray(new ContainerAtom[0]));
        }
    }

    public static final class LeafAtom extends Atom {
        public final ParsableByteArray data;

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public LeafAtom(int type, ParsableByteArray data) {
            super(type);
            this.data = data;
        }
    }

    Atom(int type) {
        this.type = type;
    }

    public String toString() {
        return getAtomTypeString(this.type);
    }

    public static int parseFullAtomVersion(int fullAtomInt) {
        return (fullAtomInt >> 24) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
    }

    public static int parseFullAtomFlags(int fullAtomInt) {
        return ViewCompat.MEASURED_SIZE_MASK & fullAtomInt;
    }

    public static String getAtomTypeString(int type) {
        return BuildConfig.FLAVOR + ((char) (type >> 24)) + ((char) ((type >> LONG_HEADER_SIZE) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) + ((char) ((type >> HEADER_SIZE) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) + ((char) (type & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
    }
}
