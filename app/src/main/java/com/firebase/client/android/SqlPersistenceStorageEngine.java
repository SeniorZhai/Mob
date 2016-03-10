package com.firebase.client.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.core.CompoundWrite;
import com.firebase.client.core.Path;
import com.firebase.client.core.UserWriteRecord;
import com.firebase.client.core.persistence.PersistenceStorageEngine;
import com.firebase.client.core.persistence.PruneForest;
import com.firebase.client.core.persistence.TrackedQuery;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.utilities.ImmutableTree.TreeVisitor;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.NodeSizeEstimator;
import com.firebase.client.utilities.Pair;
import com.firebase.client.utilities.Utilities;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.protocol.HTTP;

public class SqlPersistenceStorageEngine implements PersistenceStorageEngine {
    static final /* synthetic */ boolean $assertionsDisabled = (!SqlPersistenceStorageEngine.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int CHILDREN_NODE_SPLIT_SIZE_THRESHOLD = 16384;
    private static final String FIRST_PART_KEY = ".part-0000";
    private static final String LOGGER_COMPONENT = "Persistence";
    private static final String PART_KEY_FORMAT = ".part-%04d";
    private static final String PART_KEY_PREFIX = ".part-";
    private static final String PATH_COLUMN_NAME = "path";
    private static final String ROW_ID_COLUMN_NAME = "rowid";
    private static final int ROW_SPLIT_SIZE = 262144;
    private static final String SERVER_CACHE_TABLE = "serverCache";
    private static final String TRACKED_KEYS_ID_COLUMN_NAME = "id";
    private static final String TRACKED_KEYS_KEY_COLUMN_NAME = "key";
    private static final String TRACKED_KEYS_TABLE = "trackedKeys";
    private static final String TRACKED_QUERY_ACTIVE_COLUMN_NAME = "active";
    private static final String TRACKED_QUERY_COMPLETE_COLUMN_NAME = "complete";
    private static final String TRACKED_QUERY_ID_COLUMN_NAME = "id";
    private static final String TRACKED_QUERY_LAST_USE_COLUMN_NAME = "lastUse";
    private static final String TRACKED_QUERY_PARAMS_COLUMN_NAME = "queryParams";
    private static final String TRACKED_QUERY_PATH_COLUMN_NAME = "path";
    private static final String TRACKED_QUERY_TABLE = "trackedQueries";
    private static final String VALUE_COLUMN_NAME = "value";
    private static final String WRITES_TABLE = "writes";
    private static final String WRITE_ID_COLUMN_NAME = "id";
    private static final String WRITE_NODE_COLUMN_NAME = "node";
    private static final String WRITE_PART_COLUMN_NAME = "part";
    private static final String WRITE_TYPE_COLUMN_NAME = "type";
    private static final String WRITE_TYPE_MERGE = "m";
    private static final String WRITE_TYPE_OVERWRITE = "o";
    private static final String createServerCache = "CREATE TABLE serverCache (path TEXT PRIMARY KEY, value BLOB);";
    private static final String createTrackedKeys = "CREATE TABLE trackedKeys (id INTEGER, key TEXT);";
    private static final String createTrackedQueries = "CREATE TABLE trackedQueries (id INTEGER PRIMARY KEY, path TEXT, queryParams TEXT, lastUse INTEGER, complete INTEGER, active INTEGER);";
    private static final String createWrites = "CREATE TABLE writes (id INTEGER, path TEXT, type TEXT, part INTEGER, node BLOB, UNIQUE (id, part));";
    private final SQLiteDatabase database;
    private boolean insideTransaction;
    private final ObjectMapper jsonMapper;
    private final LogWrapper logger;
    private long transactionStart = 0;

    private static class PersistentCacheOpenHelper extends SQLiteOpenHelper {
        static final /* synthetic */ boolean $assertionsDisabled = (!SqlPersistenceStorageEngine.class.desiredAssertionStatus() ? true : $assertionsDisabled);
        private static final int DATABASE_VERSION = 2;

        public PersistentCacheOpenHelper(Context context, String cacheId) {
            super(context, cacheId, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SqlPersistenceStorageEngine.createServerCache);
            db.execSQL(SqlPersistenceStorageEngine.createWrites);
            db.execSQL(SqlPersistenceStorageEngine.createTrackedQueries);
            db.execSQL(SqlPersistenceStorageEngine.createTrackedKeys);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (!$assertionsDisabled && newVersion != DATABASE_VERSION) {
                throw new AssertionError("Why is onUpgrade() called with a different version?");
            } else if (oldVersion <= 1) {
                dropTable(db, SqlPersistenceStorageEngine.SERVER_CACHE_TABLE);
                db.execSQL(SqlPersistenceStorageEngine.createServerCache);
                dropTable(db, SqlPersistenceStorageEngine.TRACKED_QUERY_COMPLETE_COLUMN_NAME);
                db.execSQL(SqlPersistenceStorageEngine.createTrackedKeys);
                db.execSQL(SqlPersistenceStorageEngine.createTrackedQueries);
            } else {
                throw new AssertionError("We don't handle upgrading to " + newVersion);
            }
        }

        private void dropTable(SQLiteDatabase db, String table) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
    }

    public SqlPersistenceStorageEngine(Context context, com.firebase.client.core.Context firebaseContext, String cacheId) {
        try {
            this.database = new PersistentCacheOpenHelper(context, URLEncoder.encode(cacheId, "utf-8")).getWritableDatabase();
            this.jsonMapper = new ObjectMapper();
            this.logger = firebaseContext.getLogger(LOGGER_COMPONENT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveUserOverwrite(Path path, Node node, long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        Path path2 = path;
        long j = writeId;
        saveWrite(path2, j, WRITE_TYPE_OVERWRITE, serializeObject(node.getValue(true)));
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Persisted user overwrite in %dms", new Object[]{Long.valueOf(duration)}));
        }
    }

    public void saveUserMerge(Path path, CompoundWrite children, long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        Path path2 = path;
        long j = writeId;
        saveWrite(path2, j, WRITE_TYPE_MERGE, serializeObject(children.getValue(true)));
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Persisted user merge in %dms", new Object[]{Long.valueOf(duration)}));
        }
    }

    public void removeUserWrite(long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int count = this.database.delete(WRITES_TABLE, "id = ?", new String[]{String.valueOf(writeId)});
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Deleted %d write(s) with writeId %d in %dms", new Object[]{Integer.valueOf(count), Long.valueOf(writeId), Long.valueOf(duration)}));
        }
    }

    public List<UserWriteRecord> loadUserWrites() {
        String[] columns = new String[]{WRITE_ID_COLUMN_NAME, TRACKED_QUERY_PATH_COLUMN_NAME, WRITE_TYPE_COLUMN_NAME, WRITE_PART_COLUMN_NAME, WRITE_NODE_COLUMN_NAME};
        long start = System.currentTimeMillis();
        Cursor cursor = this.database.query(WRITES_TABLE, columns, null, null, null, null, "id, part");
        List<UserWriteRecord> writes = new ArrayList();
        while (cursor.moveToNext()) {
            byte[] serialized;
            UserWriteRecord record;
            long writeId = cursor.getLong(0);
            Path path = new Path(cursor.getString(1));
            String type = cursor.getString(2);
            if (cursor.isNull(3)) {
                serialized = cursor.getBlob(4);
            } else {
                try {
                    List<byte[]> parts = new ArrayList();
                    do {
                        parts.add(cursor.getBlob(4));
                        if (!cursor.moveToNext()) {
                            break;
                        }
                    } while (cursor.getLong(0) == writeId);
                    cursor.moveToPrevious();
                    serialized = joinBytes(parts);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load writes", e);
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            Object writeValue = this.jsonMapper.readValue(serialized, Object.class);
            if (WRITE_TYPE_OVERWRITE.equals(type)) {
                record = new UserWriteRecord(writeId, path, NodeUtilities.NodeFromJSON(writeValue), true);
            } else if (WRITE_TYPE_MERGE.equals(type)) {
                record = new UserWriteRecord(writeId, path, CompoundWrite.fromValue((Map) writeValue));
            } else {
                throw new IllegalStateException("Got invalid write type: " + type);
            }
            writes.add(record);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Loaded %d writes in %dms", new Object[]{Integer.valueOf(writes.size()), Long.valueOf(duration)}));
        }
        cursor.close();
        return writes;
    }

    private void saveWrite(Path path, long writeId, String type, byte[] serializedWrite) {
        verifyInsideTransaction();
        this.database.delete(WRITES_TABLE, "id = ?", new String[]{String.valueOf(writeId)});
        if (serializedWrite.length >= ROW_SPLIT_SIZE) {
            List<byte[]> parts = splitBytes(serializedWrite, ROW_SPLIT_SIZE);
            for (int i = 0; i < parts.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(WRITE_ID_COLUMN_NAME, Long.valueOf(writeId));
                values.put(TRACKED_QUERY_PATH_COLUMN_NAME, pathToKey(path));
                values.put(WRITE_TYPE_COLUMN_NAME, type);
                values.put(WRITE_PART_COLUMN_NAME, Integer.valueOf(i));
                values.put(WRITE_NODE_COLUMN_NAME, (byte[]) parts.get(i));
                this.database.insertWithOnConflict(WRITES_TABLE, null, values, 5);
            }
            return;
        }
        values = new ContentValues();
        values.put(WRITE_ID_COLUMN_NAME, Long.valueOf(writeId));
        values.put(TRACKED_QUERY_PATH_COLUMN_NAME, pathToKey(path));
        values.put(WRITE_TYPE_COLUMN_NAME, type);
        values.put(WRITE_PART_COLUMN_NAME, (Integer) null);
        values.put(WRITE_NODE_COLUMN_NAME, serializedWrite);
        this.database.insertWithOnConflict(WRITES_TABLE, null, values, 5);
    }

    public Node serverCache(Path path) {
        return loadNested(path);
    }

    public void overwriteServerCache(Path path, Node node) {
        verifyInsideTransaction();
        updateServerCache(path, node, $assertionsDisabled);
    }

    public void mergeIntoServerCache(Path path, Node node) {
        verifyInsideTransaction();
        updateServerCache(path, node, true);
    }

    private void updateServerCache(Path path, Node node, boolean merge) {
        int removedRows;
        int savedRows;
        long start = System.currentTimeMillis();
        if (merge) {
            removedRows = 0;
            savedRows = 0;
            for (NamedNode child : node) {
                removedRows += removeNested(SERVER_CACHE_TABLE, path.child(child.getName()));
                savedRows += saveNested(path.child(child.getName()), child.getNode());
            }
        } else {
            removedRows = removeNested(SERVER_CACHE_TABLE, path);
            savedRows = saveNested(path, node);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Persisted a total of %d rows and deleted %d rows for a set at %s in %dms", new Object[]{Integer.valueOf(savedRows), Integer.valueOf(removedRows), path.toString(), Long.valueOf(duration)}));
        }
    }

    public void mergeIntoServerCache(Path path, CompoundWrite children) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int savedRows = 0;
        int removedRows = 0;
        Iterator i$ = children.iterator();
        while (i$.hasNext()) {
            Entry<Path, Node> entry = (Entry) i$.next();
            removedRows += removeNested(SERVER_CACHE_TABLE, path.child((Path) entry.getKey()));
            savedRows += saveNested(path.child((Path) entry.getKey()), (Node) entry.getValue());
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Persisted a total of %d rows and deleted %d rows for a merge at %s in %dms", new Object[]{Integer.valueOf(savedRows), Integer.valueOf(removedRows), path.toString(), Long.valueOf(duration)}));
        }
    }

    public long serverCacheEstimatedSizeInBytes() {
        Cursor cursor = this.database.rawQuery(String.format("SELECT sum(length(%s) + length(%s)) FROM %s", new Object[]{VALUE_COLUMN_NAME, TRACKED_QUERY_PATH_COLUMN_NAME, SERVER_CACHE_TABLE}), null);
        try {
            if (cursor.moveToFirst()) {
                long j = cursor.getLong(0);
                return j;
            }
            throw new IllegalStateException("Couldn't read database result!");
        } finally {
            cursor.close();
        }
    }

    public void saveTrackedQuery(TrackedQuery trackedQuery) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(WRITE_ID_COLUMN_NAME, Long.valueOf(trackedQuery.id));
        values.put(TRACKED_QUERY_PATH_COLUMN_NAME, pathToKey(trackedQuery.querySpec.getPath()));
        values.put(TRACKED_QUERY_PARAMS_COLUMN_NAME, trackedQuery.querySpec.getParams().toJSON());
        values.put(TRACKED_QUERY_LAST_USE_COLUMN_NAME, Long.valueOf(trackedQuery.lastUse));
        values.put(TRACKED_QUERY_COMPLETE_COLUMN_NAME, Boolean.valueOf(trackedQuery.complete));
        values.put(TRACKED_QUERY_ACTIVE_COLUMN_NAME, Boolean.valueOf(trackedQuery.active));
        this.database.insertWithOnConflict(TRACKED_QUERY_TABLE, null, values, 5);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Saved new tracked query in %dms", new Object[]{Long.valueOf(duration)}));
        }
    }

    public void deleteTrackedQuery(long trackedQueryId) {
        verifyInsideTransaction();
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        this.database.delete(TRACKED_QUERY_TABLE, "id = ?", new String[]{trackedQueryIdStr});
        this.database.delete(TRACKED_KEYS_TABLE, "id = ?", new String[]{trackedQueryIdStr});
    }

    public List<TrackedQuery> loadTrackedQueries() {
        String[] columns = new String[]{WRITE_ID_COLUMN_NAME, TRACKED_QUERY_PATH_COLUMN_NAME, TRACKED_QUERY_PARAMS_COLUMN_NAME, TRACKED_QUERY_LAST_USE_COLUMN_NAME, TRACKED_QUERY_COMPLETE_COLUMN_NAME, TRACKED_QUERY_ACTIVE_COLUMN_NAME};
        long start = System.currentTimeMillis();
        Cursor cursor = this.database.query(TRACKED_QUERY_TABLE, columns, null, null, null, null, WRITE_ID_COLUMN_NAME);
        List<TrackedQuery> queries = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                queries.add(new TrackedQuery(cursor.getLong(0), QuerySpec.fromPathAndQueryObject(new Path(cursor.getString(1)), (Map) this.jsonMapper.readValue(cursor.getString(2), Object.class)), cursor.getLong(3), cursor.getInt(4) != 0 ? true : $assertionsDisabled, cursor.getInt(5) != 0 ? true : $assertionsDisabled));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Throwable th) {
                cursor.close();
            }
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Loaded %d tracked queries in %dms", new Object[]{Integer.valueOf(queries.size()), Long.valueOf(duration)}));
        }
        cursor.close();
        return queries;
    }

    public void resetPreviouslyActiveTrackedQueries(long lastUse) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(TRACKED_QUERY_ACTIVE_COLUMN_NAME, Boolean.valueOf($assertionsDisabled));
        values.put(TRACKED_QUERY_LAST_USE_COLUMN_NAME, Long.valueOf(lastUse));
        this.database.updateWithOnConflict(TRACKED_QUERY_TABLE, values, "active = 1", new String[0], 5);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Reset active tracked queries in %dms", new Object[]{Long.valueOf(duration)}));
        }
    }

    public void saveTrackedQueryKeys(long trackedQueryId, Set<ChildKey> keys) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        this.database.delete(TRACKED_KEYS_TABLE, "id = ?", new String[]{trackedQueryIdStr});
        for (ChildKey addedKey : keys) {
            ContentValues values = new ContentValues();
            values.put(WRITE_ID_COLUMN_NAME, Long.valueOf(trackedQueryId));
            values.put(TRACKED_KEYS_KEY_COLUMN_NAME, addedKey.asString());
            this.database.insertWithOnConflict(TRACKED_KEYS_TABLE, null, values, 5);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Set %d tracked query keys for tracked query %d in %dms", new Object[]{Integer.valueOf(keys.size()), Long.valueOf(trackedQueryId), Long.valueOf(duration)}));
        }
    }

    public void updateTrackedQueryKeys(long trackedQueryId, Set<ChildKey> added, Set<ChildKey> removed) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        String whereClause = "id = ? AND key = ?";
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        for (ChildKey removedKey : removed) {
            this.database.delete(TRACKED_KEYS_TABLE, whereClause, new String[]{trackedQueryIdStr, removedKey.asString()});
        }
        for (ChildKey addedKey : added) {
            ContentValues values = new ContentValues();
            values.put(WRITE_ID_COLUMN_NAME, Long.valueOf(trackedQueryId));
            values.put(TRACKED_KEYS_KEY_COLUMN_NAME, addedKey.asString());
            this.database.insertWithOnConflict(TRACKED_KEYS_TABLE, null, values, 5);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Updated tracked query keys (%d added, %d removed) for tracked query id %d in %dms", new Object[]{Integer.valueOf(added.size()), Integer.valueOf(removed.size()), Long.valueOf(trackedQueryId), Long.valueOf(duration)}));
        }
    }

    public Set<ChildKey> loadTrackedQueryKeys(long trackedQueryId) {
        return loadTrackedQueryKeys(Collections.singleton(Long.valueOf(trackedQueryId)));
    }

    public Set<ChildKey> loadTrackedQueryKeys(Set<Long> trackedQueryIds) {
        String[] columns = new String[]{TRACKED_KEYS_KEY_COLUMN_NAME};
        long start = System.currentTimeMillis();
        Cursor cursor = this.database.query(true, TRACKED_KEYS_TABLE, columns, "id IN (" + commaSeparatedList(trackedQueryIds) + ")", null, null, null, null, null);
        Set<ChildKey> keys = new HashSet();
        while (cursor.moveToNext()) {
            try {
                keys.add(ChildKey.fromString(cursor.getString(0)));
            } catch (Throwable th) {
                cursor.close();
            }
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Loaded %d tracked queries keys for tracked queries %s in %dms", new Object[]{Integer.valueOf(keys.size()), trackedQueryIds.toString(), Long.valueOf(duration)}));
        }
        cursor.close();
        return keys;
    }

    public void pruneCache(Path root, PruneForest pruneForest) {
        if (pruneForest.prunesAnything()) {
            verifyInsideTransaction();
            long start = System.currentTimeMillis();
            Cursor cursor = loadNestedQuery(root, new String[]{ROW_ID_COLUMN_NAME, TRACKED_QUERY_PATH_COLUMN_NAME});
            ImmutableTree<Long> rowIdsToPrune = new ImmutableTree(null);
            ImmutableTree<Long> rowIdsToKeep = new ImmutableTree(null);
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                Path path = new Path(cursor.getString(1));
                if (root.contains(path)) {
                    Path relativePath = Path.getRelative(root, path);
                    if (pruneForest.shouldPruneUnkeptDescendants(relativePath)) {
                        rowIdsToPrune = rowIdsToPrune.set(relativePath, Long.valueOf(rowId));
                    } else if (pruneForest.shouldKeep(relativePath)) {
                        rowIdsToKeep = rowIdsToKeep.set(relativePath, Long.valueOf(rowId));
                    } else {
                        this.logger.warn("We are pruning at " + root + " and have data at " + path + " that isn't marked for pruning or keeping. Ignoring.");
                    }
                } else {
                    this.logger.warn("We are pruning at " + root + " but we have data stored higher up at " + path + ". Ignoring.");
                }
            }
            int prunedCount = 0;
            int resavedCount = 0;
            if (!rowIdsToPrune.isEmpty()) {
                List<Pair<Path, Node>> rowsToResave = new ArrayList();
                pruneTreeRecursive(root, Path.getEmptyPath(), rowIdsToPrune, rowIdsToKeep, pruneForest, rowsToResave);
                Collection<Long> rowIdsToDelete = rowIdsToPrune.values();
                this.database.delete(SERVER_CACHE_TABLE, "rowid IN (" + commaSeparatedList(rowIdsToDelete) + ")", null);
                for (Pair<Path, Node> node : rowsToResave) {
                    saveNested(root.child((Path) node.getFirst()), (Node) node.getSecond());
                }
                prunedCount = rowIdsToDelete.size();
                resavedCount = rowsToResave.size();
            }
            long duration = System.currentTimeMillis() - start;
            if (this.logger.logsDebug()) {
                this.logger.debug(String.format("Pruned %d rows with %d nodes resaved in %dms", new Object[]{Integer.valueOf(prunedCount), Integer.valueOf(resavedCount), Long.valueOf(duration)}));
            }
        }
    }

    private void pruneTreeRecursive(Path pruneRoot, Path relativePath, ImmutableTree<Long> rowIdsToPrune, ImmutableTree<Long> rowIdsToKeep, PruneForest pruneForest, List<Pair<Path, Node>> rowsToResaveAccumulator) {
        if (rowIdsToPrune.getValue() != null) {
            final ImmutableTree<Long> immutableTree = rowIdsToKeep;
            if (((Integer) pruneForest.foldKeptNodes(Integer.valueOf(0), new TreeVisitor<Void, Integer>() {
                public Integer onNodeValue(Path keepPath, Void ignore, Integer nodesToResave) {
                    return Integer.valueOf(immutableTree.get(keepPath) == null ? nodesToResave.intValue() + 1 : nodesToResave.intValue());
                }
            })).intValue() > 0) {
                Path absolutePath = pruneRoot.child(relativePath);
                if (this.logger.logsDebug()) {
                    this.logger.debug(String.format("Need to rewrite %d nodes below path %s", new Object[]{Integer.valueOf(nodesToResave), absolutePath}));
                }
                final Node currentNode = loadNested(absolutePath);
                final ImmutableTree<Long> immutableTree2 = rowIdsToKeep;
                final List<Pair<Path, Node>> list = rowsToResaveAccumulator;
                final Path path = relativePath;
                pruneForest.foldKeptNodes(null, new TreeVisitor<Void, Void>() {
                    public Void onNodeValue(Path keepPath, Void ignore, Void ignore2) {
                        if (immutableTree2.get(keepPath) == null) {
                            list.add(new Pair(path.child(keepPath), currentNode.getChild(keepPath)));
                        }
                        return null;
                    }
                });
                return;
            }
            return;
        }
        Iterator i$ = rowIdsToPrune.getChildren().iterator();
        while (i$.hasNext()) {
            Entry<ChildKey, ImmutableTree<Long>> entry = (Entry) i$.next();
            ChildKey childKey = (ChildKey) entry.getKey();
            Path path2 = pruneRoot;
            pruneTreeRecursive(path2, relativePath.child(childKey), (ImmutableTree) entry.getValue(), rowIdsToKeep.getChild(childKey), pruneForest.child((ChildKey) entry.getKey()), rowsToResaveAccumulator);
        }
    }

    public void removeAllUserWrites() {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int count = this.database.delete(WRITES_TABLE, null, null);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Deleted %d (all) write(s) in %dms", new Object[]{Integer.valueOf(count), Long.valueOf(duration)}));
        }
    }

    public void purgeCache() {
        verifyInsideTransaction();
        this.database.delete(SERVER_CACHE_TABLE, null, null);
        this.database.delete(WRITES_TABLE, null, null);
        this.database.delete(TRACKED_QUERY_TABLE, null, null);
        this.database.delete(TRACKED_KEYS_TABLE, null, null);
    }

    public void beginTransaction() {
        Utilities.hardAssert(!this.insideTransaction ? true : $assertionsDisabled, "runInTransaction called when an existing transaction is already in progress.");
        if (this.logger.logsDebug()) {
            this.logger.debug("Starting transaction.");
        }
        this.database.beginTransaction();
        this.insideTransaction = true;
        this.transactionStart = System.currentTimeMillis();
    }

    public void endTransaction() {
        this.database.endTransaction();
        this.insideTransaction = $assertionsDisabled;
        long elapsed = System.currentTimeMillis() - this.transactionStart;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Transaction completed. Elapsed: %dms", new Object[]{Long.valueOf(elapsed)}));
        }
    }

    public void setTransactionSuccessful() {
        this.database.setTransactionSuccessful();
    }

    private void verifyInsideTransaction() {
        Utilities.hardAssert(this.insideTransaction, "Transaction expected to already be in progress.");
    }

    private int saveNested(Path path, Node node) {
        long estimatedSize = NodeSizeEstimator.estimateSerializedNodeSize(node);
        if (!(node instanceof ChildrenNode) || estimatedSize <= 16384) {
            saveNode(path, node);
            return 1;
        }
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Node estimated serialized size at path %s of %d bytes exceeds limit of %d bytes. Splitting up.", new Object[]{path, Long.valueOf(estimatedSize), Integer.valueOf(CHILDREN_NODE_SPLIT_SIZE_THRESHOLD)}));
        }
        int sum = 0;
        for (NamedNode child : node) {
            sum += saveNested(path.child(child.getName()), child.getNode());
        }
        if (!node.getPriority().isEmpty()) {
            saveNode(path.child(ChildKey.getPriorityKey()), node.getPriority());
            sum++;
        }
        saveNode(path, EmptyNode.Empty());
        return sum + 1;
    }

    private String partKey(Path path, int i) {
        return pathToKey(path) + String.format(PART_KEY_FORMAT, new Object[]{Integer.valueOf(i)});
    }

    private void saveNode(Path path, Node node) {
        byte[] serialized = serializeObject(node.getValue(true));
        if (serialized.length >= ROW_SPLIT_SIZE) {
            List<byte[]> parts = splitBytes(serialized, ROW_SPLIT_SIZE);
            if (this.logger.logsDebug()) {
                this.logger.debug("Saving huge leaf node with " + parts.size() + " parts.");
            }
            for (int i = 0; i < parts.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(TRACKED_QUERY_PATH_COLUMN_NAME, partKey(path, i));
                values.put(VALUE_COLUMN_NAME, (byte[]) parts.get(i));
                this.database.insertWithOnConflict(SERVER_CACHE_TABLE, null, values, 5);
            }
            return;
        }
        values = new ContentValues();
        values.put(TRACKED_QUERY_PATH_COLUMN_NAME, pathToKey(path));
        values.put(VALUE_COLUMN_NAME, serialized);
        this.database.insertWithOnConflict(SERVER_CACHE_TABLE, null, values, 5);
    }

    private Node loadNested(Path path) {
        List<String> pathStrings = new ArrayList();
        List<byte[]> payloads = new ArrayList();
        long queryStart = System.currentTimeMillis();
        Cursor cursor = loadNestedQuery(path, new String[]{TRACKED_QUERY_PATH_COLUMN_NAME, VALUE_COLUMN_NAME});
        long queryDuration = System.currentTimeMillis() - queryStart;
        long loadingStart = System.currentTimeMillis();
        while (cursor.moveToNext()) {
            try {
                pathStrings.add(cursor.getString(0));
                payloads.add(cursor.getBlob(1));
            } finally {
                cursor.close();
            }
        }
        long loadingDuration = System.currentTimeMillis() - loadingStart;
        long serializingStart = System.currentTimeMillis();
        Node node = EmptyNode.Empty();
        boolean sawDescendant = $assertionsDisabled;
        Map<Path, Node> priorities = new HashMap();
        int i = 0;
        while (i < payloads.size()) {
            Node savedNode;
            Path path2;
            if (((String) pathStrings.get(i)).endsWith(FIRST_PART_KEY)) {
                String pathString = (String) pathStrings.get(i);
                path2 = new Path(pathString.substring(0, pathString.length() - FIRST_PART_KEY.length()));
                int splitNodeRunLength = splitNodeRunLength(path2, pathStrings, i);
                if (this.logger.logsDebug()) {
                    this.logger.debug("Loading split node with " + splitNodeRunLength + " parts.");
                }
                savedNode = deserializeNode(joinBytes(payloads.subList(i, i + splitNodeRunLength)));
                i = (i + splitNodeRunLength) - 1;
            } else {
                savedNode = deserializeNode((byte[]) payloads.get(i));
                path2 = new Path((String) pathStrings.get(i));
            }
            if (savedPath.getBack() != null && savedPath.getBack().isPriorityChildName()) {
                priorities.put(savedPath, savedNode);
            } else if (savedPath.contains(path)) {
                Utilities.hardAssert(!sawDescendant ? true : $assertionsDisabled, "Descendants of path must come after ancestors.");
                node = savedNode.getChild(Path.getRelative(savedPath, path));
            } else if (path.contains(savedPath)) {
                sawDescendant = true;
                node = node.updateChild(Path.getRelative(path, savedPath), savedNode);
            } else {
                throw new IllegalStateException(String.format("Loading an unrelated row with path %s for %s", new Object[]{savedPath, path}));
            }
            i++;
        }
        for (Entry<Path, Node> entry : priorities.entrySet()) {
            node = node.updateChild(Path.getRelative(path, (Path) entry.getKey()), (Node) entry.getValue());
        }
        long serializeDuration = System.currentTimeMillis() - serializingStart;
        long duration = System.currentTimeMillis() - queryStart;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format("Loaded a total of %d rows for a total of %d nodes at %s in %dms (Query: %dms, Loading: %dms, Serializing: %dms)", new Object[]{Integer.valueOf(payloads.size()), Integer.valueOf(NodeSizeEstimator.nodeCount(node)), path, Long.valueOf(duration), Long.valueOf(queryDuration), Long.valueOf(loadingDuration), Long.valueOf(serializeDuration)}));
        }
        return node;
    }

    private int splitNodeRunLength(Path path, List<String> pathStrings, int startPosition) {
        int endPosition = startPosition + 1;
        String pathPrefix = pathToKey(path);
        if (((String) pathStrings.get(startPosition)).startsWith(pathPrefix)) {
            while (endPosition < pathStrings.size() && ((String) pathStrings.get(endPosition)).equals(partKey(path, endPosition - startPosition))) {
                endPosition++;
            }
            if (endPosition >= pathStrings.size() || !((String) pathStrings.get(endPosition)).startsWith(pathPrefix + PART_KEY_PREFIX)) {
                return endPosition - startPosition;
            }
            throw new IllegalStateException("Run did not finish with all parts");
        }
        throw new IllegalStateException("Extracting split nodes needs to start with path prefix");
    }

    private Cursor loadNestedQuery(Path path, String[] columns) {
        String pathPrefixStart = pathToKey(path);
        String pathPrefixEnd = pathPrefixStartToPrefixEnd(pathPrefixStart);
        String[] arguments = new String[(path.size() + 3)];
        String whereClause = buildAncestorWhereClause(path, arguments) + " OR (path > ? AND path < ?)";
        arguments[path.size() + 1] = pathPrefixStart;
        arguments[path.size() + 2] = pathPrefixEnd;
        return this.database.query(SERVER_CACHE_TABLE, columns, whereClause, arguments, null, null, TRACKED_QUERY_PATH_COLUMN_NAME);
    }

    private static String pathToKey(Path path) {
        if (path.isEmpty()) {
            return "/";
        }
        return path.toString() + "/";
    }

    private static String pathPrefixStartToPrefixEnd(String prefix) {
        if ($assertionsDisabled || prefix.endsWith("/")) {
            return prefix.substring(0, prefix.length() - 1) + '0';
        }
        throw new AssertionError("Path keys must end with a '/'");
    }

    private static String buildAncestorWhereClause(Path path, String[] arguments) {
        if ($assertionsDisabled || arguments.length >= path.size() + 1) {
            int count = 0;
            StringBuilder whereClause = new StringBuilder("(");
            while (!path.isEmpty()) {
                whereClause.append(TRACKED_QUERY_PATH_COLUMN_NAME);
                whereClause.append(" = ? OR ");
                arguments[count] = pathToKey(path);
                path = path.getParent();
                count++;
            }
            whereClause.append(TRACKED_QUERY_PATH_COLUMN_NAME);
            whereClause.append(" = ?)");
            arguments[count] = pathToKey(Path.getEmptyPath());
            return whereClause.toString();
        }
        throw new AssertionError();
    }

    private int removeNested(String table, Path path) {
        String pathPrefixEnd = pathPrefixStartToPrefixEnd(pathToKey(path));
        return this.database.delete(table, "path >= ? AND path < ?", new String[]{pathPrefixStart, pathPrefixEnd});
    }

    private static List<byte[]> splitBytes(byte[] bytes, int size) {
        int parts = ((bytes.length - 1) / size) + 1;
        List<byte[]> partList = new ArrayList(parts);
        for (int i = 0; i < parts; i++) {
            int length = Math.min(size, bytes.length - (i * size));
            byte[] part = new byte[length];
            System.arraycopy(bytes, i * size, part, 0, length);
            partList.add(part);
        }
        return partList;
    }

    private byte[] joinBytes(List<byte[]> payloads) {
        int totalSize = 0;
        for (byte[] payload : payloads) {
            totalSize += payload.length;
        }
        byte[] buffer = new byte[totalSize];
        int currentBytePosition = 0;
        for (byte[] payload2 : payloads) {
            System.arraycopy(payload2, 0, buffer, currentBytePosition, payload2.length);
            currentBytePosition += payload2.length;
        }
        return buffer;
    }

    private byte[] serializeObject(Object object) {
        try {
            return this.jsonMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize leaf node", e);
        }
    }

    private Node deserializeNode(byte[] value) {
        try {
            return NodeUtilities.NodeFromJSON(this.jsonMapper.readValue(value, Object.class));
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize node: " + new String(value, HTTP.UTF_8), e);
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException("Failed to serialize values to utf-8: " + Arrays.toString(value), e);
        }
    }

    private String commaSeparatedList(Collection<Long> items) {
        StringBuilder list = new StringBuilder();
        boolean first = true;
        for (Long longValue : items) {
            long item = longValue.longValue();
            if (!first) {
                list.append(",");
            }
            first = $assertionsDisabled;
            list.append(item);
        }
        return list.toString();
    }
}
