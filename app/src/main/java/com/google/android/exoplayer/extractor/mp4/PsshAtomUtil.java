package com.google.android.exoplayer.extractor.mp4;

import com.google.android.exoplayer.util.ParsableByteArray;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class PsshAtomUtil {
    private PsshAtomUtil() {
    }

    public static byte[] buildPsshAtom(UUID uuid, byte[] data) {
        int psshBoxLength = data.length + 32;
        ByteBuffer psshBox = ByteBuffer.allocate(psshBoxLength);
        psshBox.putInt(psshBoxLength);
        psshBox.putInt(Atom.TYPE_pssh);
        psshBox.putInt(0);
        psshBox.putLong(uuid.getMostSignificantBits());
        psshBox.putLong(uuid.getLeastSignificantBits());
        psshBox.putInt(data.length);
        psshBox.put(data);
        return psshBox.array();
    }

    public static UUID parseUuid(byte[] atom) {
        ParsableByteArray atomData = new ParsableByteArray(atom);
        if (!isPsshAtom(atomData, null)) {
            return null;
        }
        atomData.setPosition(12);
        return new UUID(atomData.readLong(), atomData.readLong());
    }

    public static byte[] parseSchemeSpecificData(byte[] atom, UUID uuid) {
        ParsableByteArray atomData = new ParsableByteArray(atom);
        if (!isPsshAtom(atomData, uuid)) {
            return null;
        }
        atomData.setPosition(28);
        int dataSize = atomData.readInt();
        byte[] data = new byte[dataSize];
        atomData.readBytes(data, 0, dataSize);
        return data;
    }

    private static boolean isPsshAtom(ParsableByteArray atomData, UUID uuid) {
        if (atomData.limit() < 32) {
            return false;
        }
        atomData.setPosition(0);
        if (atomData.readInt() != atomData.bytesLeft() + 4 || atomData.readInt() != Atom.TYPE_pssh) {
            return false;
        }
        atomData.setPosition(12);
        if (uuid == null) {
            atomData.skipBytes(16);
        } else if (atomData.readLong() != uuid.getMostSignificantBits()) {
            return false;
        } else {
            if (atomData.readLong() != uuid.getLeastSignificantBits()) {
                return false;
            }
        }
        if (atomData.readInt() == atomData.bytesLeft()) {
            return true;
        }
        return false;
    }
}
