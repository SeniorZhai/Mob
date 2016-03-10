package com.helpshift.customadapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.helpshift.D.attr;
import com.helpshift.D.layout;
import com.helpshift.Faq;
import com.helpshift.util.HSTransliterator;
import com.helpshift.util.Styles;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Faq> items;

    private static class ViewHolder {
        public TextView text;

        private ViewHolder() {
        }
    }

    public SearchAdapter(Context context, int resource, List<Faq> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__simple_list_item_1, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(16908308);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Faq item = (Faq) this.items.get(position);
        ArrayList<String> matchedWords = item.getSearchTerms();
        String title = item.getTitle();
        if (matchedWords == null || matchedWords.size() <= 0) {
            holder.text.setText(title);
        } else {
            int highlightColor = Styles.getColor(this.context, attr.hs__searchHighlightColor);
            Spannable spannedTitle = new SpannableString(title);
            Iterator i$;
            String word;
            int index;
            if (title.equals(HSTransliterator.unidecode(title))) {
                title = title.toLowerCase();
                i$ = matchedWords.iterator();
                while (i$.hasNext()) {
                    word = (String) i$.next();
                    if (word.length() >= 3) {
                        for (index = TextUtils.indexOf(title, word, 0); index >= 0; index = TextUtils.indexOf(title, word, word.length() + index)) {
                            spannedTitle.setSpan(new BackgroundColorSpan(highlightColor), index, word.length() + index, 33);
                        }
                    }
                }
            } else {
                int titleLength = title.length();
                String transliteration = BuildConfig.FLAVOR;
                ArrayList<Integer> titleIndex = new ArrayList();
                for (int i = 0; i < titleLength; i++) {
                    String charTransliteration = HSTransliterator.unidecode(title.charAt(i) + BuildConfig.FLAVOR);
                    for (int j = 0; j < charTransliteration.length(); j++) {
                        transliteration = transliteration + charTransliteration.charAt(j);
                        titleIndex.add(Integer.valueOf(i));
                    }
                }
                transliteration = transliteration.toLowerCase();
                i$ = matchedWords.iterator();
                while (i$.hasNext()) {
                    word = ((String) i$.next()).toLowerCase();
                    if (word.length() >= 3) {
                        for (index = TextUtils.indexOf(transliteration, word, 0); index >= 0; index = TextUtils.indexOf(transliteration, word, word.length() + index)) {
                            spannedTitle.setSpan(new BackgroundColorSpan(highlightColor), ((Integer) titleIndex.get(index)).intValue(), ((Integer) titleIndex.get((word.length() + index) - 1)).intValue() + 1, 33);
                        }
                    }
                }
            }
            holder.text.setText(spannedTitle);
        }
        return convertView;
    }
}
