package com.firebase.client.realtime.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class StringListReader extends Reader {
    private int charPos;
    private boolean closed;
    private boolean frozen;
    private int markedCharPos;
    private int markedStringListPos;
    private int stringListPos;
    private List<String> strings;

    public StringListReader() {
        this.strings = null;
        this.closed = false;
        this.markedCharPos = this.charPos;
        this.markedStringListPos = this.stringListPos;
        this.frozen = false;
        this.strings = new ArrayList();
    }

    public void addString(String string) {
        if (this.frozen) {
            throw new IllegalStateException("Trying to add string after reading");
        } else if (string.length() > 0) {
            this.strings.add(string);
        }
    }

    public void freeze() {
        if (this.frozen) {
            throw new IllegalStateException("Trying to freeze frozen StringListReader");
        }
        this.frozen = true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String string : this.strings) {
            builder.append(string);
        }
        return builder.toString();
    }

    public void reset() throws IOException {
        this.charPos = this.markedCharPos;
        this.stringListPos = this.markedStringListPos;
    }

    private String currentString() {
        return this.stringListPos < this.strings.size() ? (String) this.strings.get(this.stringListPos) : null;
    }

    private int currentStringRemainingChars() {
        String current = currentString();
        return current == null ? 0 : current.length() - this.charPos;
    }

    private void checkState() throws IOException {
        if (this.closed) {
            throw new IOException("Stream already closed");
        } else if (!this.frozen) {
            throw new IOException("Reader needs to be frozen before read operations can be called");
        }
    }

    private long advance(long numChars) {
        long advanced = 0;
        while (this.stringListPos < this.strings.size() && advanced < numChars) {
            int remainingStringChars = currentStringRemainingChars();
            long remainingChars = numChars - advanced;
            if (remainingChars < ((long) remainingStringChars)) {
                this.charPos = (int) (((long) this.charPos) + remainingChars);
                advanced += remainingChars;
            } else {
                advanced += (long) remainingStringChars;
                this.charPos = 0;
                this.stringListPos++;
            }
        }
        return advanced;
    }

    public int read(CharBuffer target) throws IOException {
        checkState();
        int remaining = target.remaining();
        int total = 0;
        String current = currentString();
        while (remaining > 0 && current != null) {
            int strLength = Math.min(current.length() - this.charPos, remaining);
            target.put((String) this.strings.get(this.stringListPos), this.charPos, this.charPos + strLength);
            remaining -= strLength;
            total += strLength;
            advance((long) strLength);
            current = currentString();
        }
        return (total > 0 || current != null) ? total : -1;
    }

    public int read() throws IOException {
        checkState();
        String current = currentString();
        if (current == null) {
            return -1;
        }
        int c = current.charAt(this.charPos);
        advance(1);
        return c;
    }

    public long skip(long n) throws IOException {
        checkState();
        return advance(n);
    }

    public boolean ready() throws IOException {
        checkState();
        return true;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) throws IOException {
        checkState();
        this.markedCharPos = this.charPos;
        this.markedStringListPos = this.stringListPos;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        checkState();
        int charsCopied = 0;
        String current = currentString();
        while (current != null && charsCopied < len) {
            int copyLength = Math.min(currentStringRemainingChars(), len - charsCopied);
            current.getChars(this.charPos, this.charPos + copyLength, cbuf, off + charsCopied);
            charsCopied += copyLength;
            advance((long) copyLength);
            current = currentString();
        }
        return (charsCopied > 0 || current != null) ? charsCopied : -1;
    }

    public void close() throws IOException {
        checkState();
        this.closed = true;
    }
}
