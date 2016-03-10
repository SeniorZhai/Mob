package com.helpshift;

import com.android.volley.DefaultRetryPolicy;
import com.helpshift.constants.MessageColumns;
import com.helpshift.external.DoubleMetaphone;
import com.helpshift.util.HSTransliterator;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;

public final class HSSearch {
    public static final String TAG = "HelpShiftDebug";
    private static boolean indexing = false;
    private static boolean markDeinit = false;
    private static DoubleMetaphone metaPhone = new DoubleMetaphone();

    public enum HS_SEARCH_OPTIONS {
        FULL_SEARCH,
        METAPHONE_SEARCH,
        KEYWORD_SEARCH
    }

    public static void init() {
        if (!indexing) {
            Thread indexThread = new Thread(new Runnable() {
                public void run() {
                    HSTransliterator.init();
                }
            });
            indexThread.setDaemon(true);
            indexThread.start();
        }
    }

    public static void deinit() {
        if (indexing) {
            markDeinit = true;
        } else {
            HSTransliterator.deinit();
        }
    }

    public static String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", BuildConfig.FLAVOR);
    }

    public static ArrayList<String> generateTokens(String str) {
        ArrayList<String> tokens = new ArrayList();
        Matcher m = Pattern.compile("\\w+").matcher(str);
        while (m.find()) {
            if (m.group(0).length() > 2) {
                tokens.add(m.group(0));
            }
        }
        return tokens;
    }

    public static ArrayList<String> generateTokensForSearchQuery(String str) {
        ArrayList<String> tokens = new ArrayList();
        Matcher m = Pattern.compile("\\w+").matcher(str);
        while (m.find()) {
            if (m.group(0).length() > 2 || str.length() > 2) {
                tokens.add(m.group(0));
            }
        }
        return tokens;
    }

    public static ArrayList<String> generateNgrams(int min, int max, String str) {
        ArrayList<String> output = new ArrayList();
        int i = min;
        while (i < str.length() && i <= max) {
            output.add(str.substring(0, i));
            i++;
        }
        return output;
    }

    public static ArrayList<HashMap<String, String>> generateVariations(final String word, final String wordType) {
        HashSet<HashMap<String, String>> output = new HashSet();
        output.add(new HashMap() {
        });
        Iterator i$ = generateNgrams(2, 10, word).iterator();
        while (i$.hasNext()) {
            final String ngram = (String) i$.next();
            output.add(new HashMap() {
            });
        }
        output.add(new HashMap() {
        });
        return new ArrayList(output);
    }

    public static ArrayList<HashMap<String, String>> generateSearchVariations(final String word) {
        HashSet<HashMap<String, String>> output = new HashSet();
        output.add(new HashMap() {
        });
        output.add(new HashMap() {
        });
        return new ArrayList(output);
    }

    public static String sanitize(String str) {
        return HSTransliterator.unidecode(stripHtml(str).toLowerCase());
    }

    public static int calcFreq(String type, String token) {
        if (token.length() == 1) {
            return 5;
        }
        if (type == "ngram") {
            return token.length();
        }
        if (type == "word") {
            return 30;
        }
        if (type == "metaphone") {
            return 1;
        }
        if (type == "imp_word") {
            return HttpStatus.SC_MULTIPLE_CHOICES;
        }
        if (type == "tag_word") {
            return 150;
        }
        return 1;
    }

    public static HashMap<String, Integer> indexDocument(String title, String doc, List<String> tags) {
        ArrayList<HashMap<String, String>> output = new ArrayList();
        Iterator i$ = generateTokens(sanitize(doc)).iterator();
        while (i$.hasNext()) {
            output.addAll(generateVariations((String) i$.next(), "word"));
        }
        i$ = generateTokens(sanitize(title)).iterator();
        while (i$.hasNext()) {
            output.addAll(generateVariations((String) i$.next(), "imp_word"));
        }
        for (String tag : tags) {
            output.addAll(generateVariations(sanitize(tag), "tag_word"));
        }
        HashMap<String, Integer> indexDoc = new HashMap();
        i$ = output.iterator();
        while (i$.hasNext()) {
            HashMap tokenMap = (HashMap) i$.next();
            String token = (String) tokenMap.get("value");
            int tokenFreq = 0;
            if (indexDoc.containsKey(token)) {
                tokenFreq = ((Integer) indexDoc.get(token)).intValue();
            }
            indexDoc.put(token, Integer.valueOf(tokenFreq + calcFreq((String) tokenMap.get(MessageColumns.TYPE), token)));
        }
        return indexDoc;
    }

    public static HashMap indexDocuments(ArrayList<Faq> docs) {
        HashMap fullIndex = new HashMap();
        if (indexing) {
            return null;
        }
        if (!HSTransliterator.isLoaded()) {
            HSTransliterator.init();
            markDeinit = true;
        }
        indexing = true;
        HashMap tfidfIndex = buildTfidfIndex(docs);
        HashMap fuzzyIndex = buildFuzzyIndex(docs);
        fullIndex.put(HSFunnel.REPORTED_ISSUE, tfidfIndex);
        fullIndex.put(HSFunnel.READ_FAQ, fuzzyIndex);
        indexing = false;
        if (!markDeinit) {
            return fullIndex;
        }
        deinit();
        markDeinit = false;
        return fullIndex;
    }

    protected static ArrayList<HashMap<String, String>> filterSearchQuery(ArrayList<HashMap<String, String>> queryTerms, HS_SEARCH_OPTIONS options) {
        ArrayList<HashMap<String, String>> terms = new ArrayList();
        Iterator i$ = queryTerms.iterator();
        while (i$.hasNext()) {
            HashMap<String, String> termMap = (HashMap) i$.next();
            String type = (String) termMap.get(MessageColumns.TYPE);
            if (options == HS_SEARCH_OPTIONS.FULL_SEARCH) {
                terms.add(termMap);
            } else if (options == HS_SEARCH_OPTIONS.METAPHONE_SEARCH && type.equals("metaphone")) {
                terms.add(termMap);
            } else if (options == HS_SEARCH_OPTIONS.KEYWORD_SEARCH && (type.equals("word") || type.equals("ngram"))) {
                terms.add(termMap);
            }
        }
        return terms;
    }

    public static ArrayList<HashMap> queryDocs(String query, HashMap tfidf, HS_SEARCH_OPTIONS options) {
        String docId;
        HashMap docRanks = new HashMap();
        HashSet<String> resultDocSet = null;
        HashMap<String, ArrayList<String>> matchedTermsMap = new HashMap();
        ArrayList<HashMap<String, String>> terms = new ArrayList();
        Iterator i$ = generateTokensForSearchQuery(sanitize(query)).iterator();
        while (i$.hasNext()) {
            terms.addAll(filterSearchQuery(generateSearchVariations((String) i$.next()), options));
        }
        if (tfidf != null) {
            i$ = terms.iterator();
            while (i$.hasNext()) {
                HashMap<String, String> termMap = (HashMap) i$.next();
                String term = (String) termMap.get("value");
                String type = (String) termMap.get(MessageColumns.TYPE);
                HashMap termDocMap = (HashMap) tfidf.get(term);
                if (termDocMap != null && termDocMap.keySet().size() > 0) {
                    for (String docId2 : termDocMap.keySet()) {
                        ArrayList<String> matchTerms = (ArrayList) matchedTermsMap.get(docId2);
                        if (matchTerms == null) {
                            matchTerms = new ArrayList();
                        }
                        if (term.length() > 0) {
                            matchTerms.add(term);
                        }
                        matchedTermsMap.put(docId2, matchTerms);
                        Double docRank = (Double) docRanks.get(docId2);
                        Double docContribution = Double.valueOf(((Double) termDocMap.get(docId2)).doubleValue() * ((double) calcFreq(type, term)));
                        if (docRank != null) {
                            docRanks.put(docId2, Double.valueOf(docRank.doubleValue() + docContribution.doubleValue()));
                        } else {
                            docRanks.put(docId2, docContribution);
                        }
                    }
                    HashSet termDocSet = new HashSet();
                    termDocSet.addAll(termDocMap.keySet());
                    HashSet resultDocSet2;
                    if (resultDocSet2 == null || resultDocSet2.isEmpty()) {
                        resultDocSet2 = new HashSet(termDocSet);
                    } else {
                        resultDocSet2.addAll(termDocSet);
                    }
                }
            }
        }
        TreeMap<String, Double> treeMap;
        if (resultDocSet == null || resultDocSet.isEmpty()) {
            treeMap = new TreeMap(new RankComparator(docRanks));
            treeMap.putAll(docRanks);
            return sortMatchedTermsMap(treeMap, matchedTermsMap);
        } else if (resultDocSet.size() == 1) {
            HashMap docIdTermsMap = new HashMap();
            ArrayList<HashMap> resultDoc = new ArrayList();
            docId2 = (String) resultDocSet.iterator().next();
            docIdTermsMap.put(HSFunnel.READ_FAQ, docId2);
            docIdTermsMap.put("t", matchedTermsMap.get(docId2));
            resultDoc.add(docIdTermsMap);
            return resultDoc;
        } else {
            HashMap resultDocRanks = new HashMap();
            i$ = resultDocSet.iterator();
            while (i$.hasNext()) {
                docId2 = (String) i$.next();
                resultDocRanks.put(docId2, docRanks.get(docId2));
            }
            treeMap = new TreeMap(new RankComparator(resultDocRanks));
            treeMap.putAll(resultDocRanks);
            return sortMatchedTermsMap(treeMap, matchedTermsMap);
        }
    }

    private static ArrayList<HashMap> sortMatchedTermsMap(TreeMap sortedDocRanks, HashMap matchedTermsMap) {
        ArrayList<HashMap> sortedResults = new ArrayList();
        for (String docId : sortedDocRanks.keySet()) {
            HashMap docIdTermsMap = new HashMap();
            docIdTermsMap.put(HSFunnel.READ_FAQ, docId);
            docIdTermsMap.put("t", matchedTermsMap.get(docId));
            sortedResults.add(docIdTermsMap);
        }
        return sortedResults;
    }

    protected static HashMap buildTfidfIndex(ArrayList<Faq> docs) {
        Integer totalDocNum = Integer.valueOf(docs.size());
        ArrayList<HashMap> indexedDocs = new ArrayList();
        Integer i = Integer.valueOf(0);
        Iterator i$ = docs.iterator();
        while (i$.hasNext()) {
            Faq doc = (Faq) i$.next();
            HashMap indexedDoc = new HashMap();
            indexedDoc.put("terms", indexDocument(doc.getTitle(), doc.getBody(), doc.getTags()));
            indexedDoc.put(DBLikedChannelsHelper.KEY_ID, i + BuildConfig.FLAVOR);
            i = Integer.valueOf(i.intValue() + 1);
            indexedDocs.add(indexedDoc);
        }
        HashMap globalTerms = new HashMap();
        i$ = indexedDocs.iterator();
        while (i$.hasNext()) {
            HashMap doc2 = (HashMap) i$.next();
            String docId = (String) doc2.get(DBLikedChannelsHelper.KEY_ID);
            HashMap<String, Integer> terms = (HashMap) doc2.get("terms");
            for (String term : terms.keySet()) {
                int termFreq = ((Integer) terms.get(term)).intValue();
                HashMap termMap;
                if (globalTerms.containsKey(term)) {
                    termMap = (HashMap) globalTerms.get(term);
                    Integer maxFreq = (Integer) termMap.get("maxFreq");
                    if (maxFreq == null) {
                        maxFreq = Integer.valueOf(0);
                    }
                    Integer docFreq = (Integer) termMap.get("docFreq");
                    if (docFreq == null) {
                        docFreq = Integer.valueOf(0);
                    } else {
                        docFreq = Integer.valueOf(docFreq.intValue() + 1);
                    }
                    if (maxFreq.intValue() < termFreq) {
                        termMap.put("maxFreq", Integer.valueOf(termFreq));
                    }
                    termMap.put("docFreq", docFreq);
                    globalTerms.put(term, termMap);
                } else {
                    termMap = new HashMap();
                    termMap.put("maxFreq", Integer.valueOf(termFreq));
                    termMap.put("docFreq", Integer.valueOf(1));
                    globalTerms.put(term, termMap);
                }
            }
        }
        HashMap<String, HashMap<String, Double>> tfidf = new HashMap();
        i$ = indexedDocs.iterator();
        while (i$.hasNext()) {
            doc2 = (HashMap) i$.next();
            docId = (String) doc2.get(DBLikedChannelsHelper.KEY_ID);
            terms = (HashMap) doc2.get("terms");
            for (String term2 : terms.keySet()) {
                HashMap<String, Double> termMap2 = (HashMap) tfidf.get(term2);
                if (termMap2 == null) {
                    termMap2 = new HashMap();
                }
                HashMap globalTerm = (HashMap) globalTerms.get(term2);
                Integer docTf = (Integer) globalTerm.get("docFreq");
                termMap2.put(docId, Double.valueOf(Double.valueOf((double) (((Integer) terms.get(term2)).intValue() / ((Integer) globalTerm.get("maxFreq")).intValue())).doubleValue() * Double.valueOf(Math.log10((double) (totalDocNum.intValue() / docTf.intValue()))).doubleValue()));
                tfidf.put(term2, termMap2);
            }
        }
        return tfidf;
    }

    protected static HashMap buildFuzzyIndex(ArrayList<Faq> docs) {
        HashMap fuzzyIndex = new HashMap();
        int i = 0;
        Iterator it = docs.iterator();
        while (it.hasNext()) {
            Iterator i$ = generateTokens(sanitize(((Faq) it.next()).getTitle())).iterator();
            while (i$.hasNext()) {
                String token = ((String) i$.next()).toLowerCase();
                if (token.length() > 3) {
                    HashMap tokenMap = new HashMap();
                    tokenMap.put("w", token);
                    tokenMap.put(DBLikedChannelsHelper.KEY_ID, i + BuildConfig.FLAVOR);
                    String firstCharacter = token.substring(0, 1);
                    ArrayList<HashMap> firstCharIndex = (ArrayList) fuzzyIndex.get(firstCharacter);
                    if (firstCharIndex == null) {
                        firstCharIndex = new ArrayList();
                    }
                    firstCharIndex.add(tokenMap);
                    fuzzyIndex.put(firstCharacter, firstCharIndex);
                    String secondCharacter = token.substring(1, 2);
                    ArrayList<HashMap> secondCharIndex = (ArrayList) fuzzyIndex.get(secondCharacter);
                    if (secondCharIndex == null) {
                        secondCharIndex = new ArrayList();
                    }
                    secondCharIndex.add(tokenMap);
                    fuzzyIndex.put(secondCharacter, secondCharIndex);
                }
            }
            i++;
        }
        return fuzzyIndex;
    }

    public static ArrayList<HashMap> getFuzzyMatches(String query, HashMap fuzzyIndex) {
        ArrayList<HashMap> resultArray = new ArrayList();
        if (fuzzyIndex != null) {
            HashMap faqIdsWithKeywords = new HashMap();
            Iterator it = generateTokens(sanitize(query)).iterator();
            while (it.hasNext()) {
                String token = (String) it.next();
                String rootChar = token.substring(0, 1);
                List<String> neighbourChars = new ArrayList(getNeighbourCharacters(rootChar));
                neighbourChars.add(rootChar);
                for (String character : neighbourChars) {
                    ArrayList<HashMap> wordsList = (ArrayList) fuzzyIndex.get(character);
                    if (wordsList != null) {
                        Iterator i$ = wordsList.iterator();
                        while (i$.hasNext()) {
                            HashMap wordMap = (HashMap) i$.next();
                            String wordToken = (String) wordMap.get("w");
                            if (((double) calculateWordDistance(wordToken, token)) > 0.5d) {
                                String id = (String) wordMap.get(DBLikedChannelsHelper.KEY_ID);
                                ArrayList<String> matchWordList = (ArrayList) faqIdsWithKeywords.get(id);
                                if (matchWordList == null) {
                                    matchWordList = new ArrayList();
                                }
                                matchWordList.add(wordToken);
                                faqIdsWithKeywords.put(id, matchWordList);
                            }
                        }
                    }
                }
            }
            for (String docId : faqIdsWithKeywords.keySet()) {
                HashMap docIdTermsMap = new HashMap();
                docIdTermsMap.put(HSFunnel.READ_FAQ, docId);
                docIdTermsMap.put("t", faqIdsWithKeywords.get(docId));
                resultArray.add(docIdTermsMap);
            }
        }
        return resultArray;
    }

    private static List<String> getNeighbourCharacters(String inputCharacter) {
        HashMap<String, String[]> characterTable = new HashMap();
        characterTable.put("a", new String[]{HSFunnel.LIBRARY_QUIT, "w", HSFunnel.PERFORMED_SEARCH, "z"});
        characterTable.put(HSFunnel.BROWSED_FAQ_LIST, new String[]{"v", HSFunnel.MARKED_HELPFUL, HSFunnel.RESOLUTION_REJECTED});
        characterTable.put(HSFunnel.OPEN_ISSUE, new String[]{HSFunnel.OPEN_INBOX, HSFunnel.READ_FAQ, "v"});
        characterTable.put(HSFunnel.LIBRARY_OPENED_DECOMP, new String[]{HSFunnel.PERFORMED_SEARCH, "z", HSFunnel.OPEN_INBOX});
        characterTable.put("e", new String[]{"w", HSFunnel.PERFORMED_SEARCH, HSFunnel.LIBRARY_OPENED_DECOMP, HSFunnel.REVIEWED_APP});
        characterTable.put(HSFunnel.READ_FAQ, new String[]{HSFunnel.LIBRARY_OPENED_DECOMP, "g", HSFunnel.OPEN_ISSUE, HSFunnel.OPEN_INBOX});
        characterTable.put("g", new String[]{HSFunnel.MARKED_HELPFUL, HSFunnel.READ_FAQ, "v", HSFunnel.BROWSED_FAQ_LIST});
        characterTable.put(HSFunnel.MARKED_HELPFUL, new String[]{"g", "j", HSFunnel.BROWSED_FAQ_LIST, HSFunnel.RESOLUTION_REJECTED});
        characterTable.put(HSFunnel.REPORTED_ISSUE, new String[]{HSFunnel.MARKED_UNHELPFUL, HSFunnel.LIBRARY_OPENED, "k", "j"});
        characterTable.put("j", new String[]{HSFunnel.MESSAGE_ADDED, HSFunnel.RESOLUTION_REJECTED, HSFunnel.MARKED_HELPFUL, "k"});
        characterTable.put("k", new String[]{"j", HSFunnel.SUPPORT_LAUNCH, HSFunnel.MESSAGE_ADDED});
        characterTable.put(HSFunnel.SUPPORT_LAUNCH, new String[]{"k", HSFunnel.CONVERSATION_POSTED, HSFunnel.MESSAGE_ADDED});
        characterTable.put(HSFunnel.MESSAGE_ADDED, new String[]{HSFunnel.RESOLUTION_REJECTED, HSFunnel.BROWSED_FAQ_LIST, HSFunnel.SUPPORT_LAUNCH});
        characterTable.put(HSFunnel.RESOLUTION_REJECTED, new String[]{HSFunnel.BROWSED_FAQ_LIST, "j", HSFunnel.MESSAGE_ADDED});
        characterTable.put(HSFunnel.LIBRARY_OPENED, new String[]{HSFunnel.SUPPORT_LAUNCH, "k", HSFunnel.CONVERSATION_POSTED});
        characterTable.put(HSFunnel.CONVERSATION_POSTED, new String[]{HSFunnel.SUPPORT_LAUNCH, HSFunnel.LIBRARY_OPENED});
        characterTable.put(HSFunnel.LIBRARY_QUIT, new String[]{"w", "a"});
        characterTable.put(HSFunnel.REVIEWED_APP, new String[]{HSFunnel.PERFORMED_SEARCH, HSFunnel.LIBRARY_OPENED_DECOMP, "e", HSFunnel.READ_FAQ});
        characterTable.put(HSFunnel.PERFORMED_SEARCH, new String[]{"a", "z", HSFunnel.LIBRARY_OPENED_DECOMP});
        characterTable.put("t", new String[]{HSFunnel.REVIEWED_APP, HSFunnel.READ_FAQ, "g", HSFunnel.RESOLUTION_ACCEPTED});
        characterTable.put(HSFunnel.MARKED_UNHELPFUL, new String[]{"j", HSFunnel.MARKED_HELPFUL, HSFunnel.REPORTED_ISSUE, HSFunnel.RESOLUTION_ACCEPTED});
        characterTable.put("v", new String[]{HSFunnel.OPEN_ISSUE, "g", HSFunnel.BROWSED_FAQ_LIST});
        characterTable.put("w", new String[]{HSFunnel.LIBRARY_QUIT, "a", HSFunnel.PERFORMED_SEARCH});
        characterTable.put(HSFunnel.OPEN_INBOX, new String[]{"z", HSFunnel.PERFORMED_SEARCH, HSFunnel.OPEN_ISSUE});
        characterTable.put(HSFunnel.RESOLUTION_ACCEPTED, new String[]{"g", HSFunnel.MARKED_HELPFUL, "t", HSFunnel.MARKED_UNHELPFUL});
        characterTable.put("z", new String[]{"a", HSFunnel.PERFORMED_SEARCH, HSFunnel.OPEN_INBOX});
        if (characterTable.containsKey(inputCharacter)) {
            return Arrays.asList((Object[]) characterTable.get(inputCharacter));
        }
        return new ArrayList();
    }

    private static float calculateWordDistance(String originalString, String comparisionString) {
        originalString = originalString.trim();
        comparisionString = comparisionString.trim();
        originalString = originalString.toLowerCase();
        comparisionString = comparisionString.toLowerCase();
        int n = originalString.length();
        int m = comparisionString.length();
        int n2 = n + 1;
        if (n != 0) {
            int m2 = m + 1;
            if (m != 0) {
                int k;
                int maxLength;
                int[] d = new int[(n2 * m2)];
                for (k = 0; k < n2; k++) {
                    d[k] = k;
                }
                for (k = 0; k < m2; k++) {
                    d[k * n2] = k;
                }
                for (int i = 1; i < n2; i++) {
                    int j = 1;
                    while (j < m2) {
                        int cost;
                        if (originalString.charAt(i - 1) == comparisionString.charAt(j - 1)) {
                            cost = 0;
                        } else {
                            cost = 1;
                        }
                        d[(j * n2) + i] = smallestOf(d[((j - 1) * n2) + i] + 1, d[((j * n2) + i) - 1] + 1, d[(((j - 1) * n2) + i) - 1] + cost);
                        if (i > 1 && j > 1) {
                            if (originalString.charAt(i - 1) == comparisionString.charAt(j - 2)) {
                                if (originalString.charAt(i - 2) == comparisionString.charAt(j - 1)) {
                                    d[(j * n2) + i] = smallestOf(d[(j * n2) + i], d[(((j - 2) * n2) + i) - 2] + cost);
                                }
                            }
                        }
                        j++;
                    }
                }
                int distance = d[(n2 * m2) - 1];
                if (n2 > m2) {
                    maxLength = n2;
                } else {
                    maxLength = m2;
                }
                m = m2;
                return DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - (((float) distance) / ((float) maxLength));
            }
            m = m2;
        }
        return 0.0f;
    }

    private static int smallestOf(int a, int b, int c) {
        int min = a;
        if (b < min) {
            min = b;
        }
        if (c < min) {
            return c;
        }
        return min;
    }

    private static int smallestOf(int a, int b) {
        int min = a;
        if (b < min) {
            return b;
        }
        return min;
    }
}
