package com.google.android.exoplayer.extractor.mp3;

import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;

final class BufferingInput {
    private final ParsableByteArray buffer;
    private final int capacity;
    private int markPosition;
    private int readPosition;
    private int writePosition;

    public BufferingInput(int capacity) {
        this.capacity = capacity;
        this.buffer = new ParsableByteArray(capacity * 2);
    }

    public void reset() {
        this.readPosition = 0;
        this.writePosition = 0;
        this.markPosition = 0;
    }

    public void mark() {
        if (this.readPosition > this.capacity) {
            System.arraycopy(this.buffer.data, this.readPosition, this.buffer.data, 0, this.writePosition - this.readPosition);
            this.writePosition -= this.readPosition;
            this.readPosition = 0;
        }
        this.markPosition = this.readPosition;
    }

    public void returnToMark() {
        this.readPosition = this.markPosition;
    }

    public int getAvailableByteCount() {
        return this.writePosition - this.readPosition;
    }

    public ParsableByteArray getParsableByteArray(ExtractorInput extractorInput, int length) throws IOException, InterruptedException {
        if (ensureLoaded(extractorInput, length)) {
            ParsableByteArray parsableByteArray = new ParsableByteArray(this.buffer.data, this.writePosition);
            parsableByteArray.setPosition(this.readPosition);
            this.readPosition += length;
            return parsableByteArray;
        }
        throw new EOFException();
    }

    public int drainToOutput(TrackOutput trackOutput, int length) {
        if (length == 0) {
            return 0;
        }
        this.buffer.setPosition(this.readPosition);
        int bytesToDrain = Math.min(this.writePosition - this.readPosition, length);
        trackOutput.sampleData(this.buffer, bytesToDrain);
        this.readPosition += bytesToDrain;
        return bytesToDrain;
    }

    public void skip(ExtractorInput extractorInput, int length) throws IOException, InterruptedException {
        if (!readInternal(extractorInput, null, 0, length)) {
            throw new EOFException();
        }
    }

    public void read(ExtractorInput extractorInput, byte[] target, int offset, int length) throws IOException, InterruptedException {
        if (!readInternal(extractorInput, target, offset, length)) {
            throw new EOFException();
        }
    }

    public boolean readAllowingEndOfInput(ExtractorInput extractorInput, byte[] target, int offset, int length) throws IOException, InterruptedException {
        return readInternal(extractorInput, target, offset, length);
    }

    private boolean readInternal(ExtractorInput extractorInput, byte[] target, int offset, int length) throws InterruptedException, IOException {
        if (!ensureLoaded(extractorInput, length)) {
            return false;
        }
        if (target != null) {
            System.arraycopy(this.buffer.data, this.readPosition, target, offset, length);
        }
        this.readPosition += length;
        return true;
    }

    private boolean ensureLoaded(ExtractorInput extractorInput, int length) throws InterruptedException, IOException {
        if ((this.readPosition + length) - this.markPosition > this.capacity) {
            throw new BufferOverflowException();
        }
        int bytesToLoad = length - (this.writePosition - this.readPosition);
        if (bytesToLoad <= 0) {
            return true;
        }
        if (!extractorInput.readFully(this.buffer.data, this.writePosition, bytesToLoad, true)) {
            return false;
        }
        this.writePosition += bytesToLoad;
        return true;
    }
}
