package com.cocosw.bottomsheet;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import java.util.Arrays;
import java.util.Comparator;

class SimpleSectionedGridAdapter extends BaseAdapter {
    protected static final int TYPE_FILLER = 0;
    protected static final int TYPE_HEADER = 1;
    protected static final int TYPE_HEADER_FILLER = 2;
    private ListAdapter mBaseAdapter;
    private int mColumnWidth;
    private Context mContext;
    private GridView mGridView;
    private int mHeaderLayoutResId;
    private int mHeaderTextViewResId;
    private int mHeaderWidth;
    private int mHorizontalSpacing;
    private Section[] mInitialSections = new Section[TYPE_FILLER];
    private View mLastViewSeen;
    private LayoutInflater mLayoutInflater;
    private int mNumColumns;
    private int mSectionResourceId;
    SparseArray<Section> mSections = new SparseArray();
    private int mStrechMode;
    private boolean mValid = true;
    private int mWidth;
    private int requestedColumnWidth;
    private int requestedHorizontalSpacing;

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;
        int type = SimpleSectionedGridAdapter.TYPE_FILLER;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return this.title;
        }
    }

    public static class ViewHolder {
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray();
                view.setTag(viewHolder);
            }
            View childView = (View) viewHolder.get(id);
            if (childView != null) {
                return childView;
            }
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
            return childView;
        }
    }

    public SimpleSectionedGridAdapter(Context context, BaseAdapter baseAdapter, int sectionResourceId, int headerLayoutResId, int headerTextViewResId) {
        this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mSectionResourceId = sectionResourceId;
        this.mHeaderLayoutResId = headerLayoutResId;
        this.mHeaderTextViewResId = headerTextViewResId;
        this.mBaseAdapter = baseAdapter;
        this.mContext = context;
        this.mBaseAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                SimpleSectionedGridAdapter.this.mValid = !SimpleSectionedGridAdapter.this.mBaseAdapter.isEmpty();
                SimpleSectionedGridAdapter.this.notifyDataSetChanged();
            }

            public void onInvalidated() {
                SimpleSectionedGridAdapter.this.mValid = false;
                SimpleSectionedGridAdapter.this.notifyDataSetInvalidated();
            }
        });
    }

    public void setGridView(GridView gridView) {
        if (gridView instanceof PinnedSectionGridView) {
            this.mGridView = gridView;
            this.mStrechMode = gridView.getStretchMode();
            this.mWidth = gridView.getWidth() - (this.mGridView.getPaddingLeft() + this.mGridView.getPaddingRight());
            this.mNumColumns = ((PinnedSectionGridView) gridView).getNumColumns();
            this.requestedColumnWidth = ((PinnedSectionGridView) gridView).getColumnWidth();
            this.requestedHorizontalSpacing = ((PinnedSectionGridView) gridView).getHorizontalSpacing();
            return;
        }
        throw new IllegalArgumentException("Does your grid view extends PinnedSectionGridView?");
    }

    private int getHeaderSize() {
        if (this.mHeaderWidth > 0) {
            return this.mHeaderWidth;
        }
        if (this.mWidth != this.mGridView.getWidth()) {
            this.mStrechMode = this.mGridView.getStretchMode();
            this.mWidth = ((PinnedSectionGridView) this.mGridView).getAvailableWidth() - (this.mGridView.getPaddingLeft() + this.mGridView.getPaddingRight());
            this.mNumColumns = ((PinnedSectionGridView) this.mGridView).getNumColumns();
            this.requestedColumnWidth = ((PinnedSectionGridView) this.mGridView).getColumnWidth();
            this.requestedHorizontalSpacing = ((PinnedSectionGridView) this.mGridView).getHorizontalSpacing();
        }
        int spaceLeftOver = (this.mWidth - (this.mNumColumns * this.requestedColumnWidth)) - ((this.mNumColumns - 1) * this.requestedHorizontalSpacing);
        switch (this.mStrechMode) {
            case TYPE_FILLER /*0*/:
                this.mWidth -= spaceLeftOver;
                this.mColumnWidth = this.requestedColumnWidth;
                this.mHorizontalSpacing = this.requestedHorizontalSpacing;
                break;
            case TYPE_HEADER /*1*/:
                this.mColumnWidth = this.requestedColumnWidth;
                if (this.mNumColumns <= TYPE_HEADER) {
                    this.mHorizontalSpacing = this.requestedHorizontalSpacing + spaceLeftOver;
                    break;
                }
                this.mHorizontalSpacing = this.requestedHorizontalSpacing + (spaceLeftOver / (this.mNumColumns - 1));
                break;
            case TYPE_HEADER_FILLER /*2*/:
                this.mColumnWidth = this.requestedColumnWidth + (spaceLeftOver / this.mNumColumns);
                this.mHorizontalSpacing = this.requestedHorizontalSpacing;
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                this.mColumnWidth = this.requestedColumnWidth;
                this.mHorizontalSpacing = this.requestedHorizontalSpacing;
                this.mWidth = (this.mWidth - spaceLeftOver) + (this.mHorizontalSpacing * TYPE_HEADER_FILLER);
                break;
        }
        this.mHeaderWidth = this.mWidth + ((this.mNumColumns - 1) * (this.mColumnWidth + this.mHorizontalSpacing));
        return this.mHeaderWidth;
    }

    public void setSections(Section... sections) {
        this.mInitialSections = sections;
        setSections();
    }

    public void setSections() {
        this.mSections.clear();
        getHeaderSize();
        Arrays.sort(this.mInitialSections, new Comparator<Section>() {
            public int compare(Section o, Section o1) {
                if (o.firstPosition == o1.firstPosition) {
                    return SimpleSectionedGridAdapter.TYPE_FILLER;
                }
                return o.firstPosition < o1.firstPosition ? -1 : SimpleSectionedGridAdapter.TYPE_HEADER;
            }
        });
        int offset = TYPE_FILLER;
        for (int i = TYPE_FILLER; i < this.mInitialSections.length; i += TYPE_HEADER) {
            Section sectionAdd;
            Section section = this.mInitialSections[i];
            for (int j = TYPE_FILLER; j < this.mNumColumns - 1; j += TYPE_HEADER) {
                sectionAdd = new Section(section.firstPosition, section.title);
                sectionAdd.type = TYPE_HEADER_FILLER;
                sectionAdd.sectionedPosition = sectionAdd.firstPosition + offset;
                this.mSections.append(sectionAdd.sectionedPosition, sectionAdd);
                offset += TYPE_HEADER;
            }
            sectionAdd = new Section(section.firstPosition, section.title);
            sectionAdd.type = TYPE_HEADER;
            sectionAdd.sectionedPosition = sectionAdd.firstPosition + offset;
            this.mSections.append(sectionAdd.sectionedPosition, sectionAdd);
            offset += TYPE_HEADER;
            if (i < this.mInitialSections.length - 1) {
                int nextPos = this.mInitialSections[i + TYPE_HEADER].firstPosition;
                int dummyCount = this.mNumColumns - ((nextPos - section.firstPosition) % this.mNumColumns);
                if (this.mNumColumns != dummyCount) {
                    for (int k = TYPE_FILLER; k < dummyCount; k += TYPE_HEADER) {
                        sectionAdd = new Section(section.firstPosition, section.title);
                        sectionAdd.type = TYPE_FILLER;
                        sectionAdd.sectionedPosition = nextPos + offset;
                        this.mSections.append(sectionAdd.sectionedPosition, sectionAdd);
                        offset += TYPE_HEADER;
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = TYPE_FILLER;
        int i = TYPE_FILLER;
        while (i < this.mSections.size() && ((Section) this.mSections.valueAt(i)).firstPosition <= position) {
            offset += TYPE_HEADER;
            i += TYPE_HEADER;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return -1;
        }
        int offset = TYPE_FILLER;
        int i = TYPE_FILLER;
        while (i < this.mSections.size() && ((Section) this.mSections.valueAt(i)).sectionedPosition <= sectionedPosition) {
            offset--;
            i += TYPE_HEADER;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return this.mSections.get(position) != null;
    }

    public int getCount() {
        return this.mValid ? this.mBaseAdapter.getCount() + this.mSections.size() : TYPE_FILLER;
    }

    public Object getItem(int position) {
        return isSectionHeaderPosition(position) ? this.mSections.get(position) : this.mBaseAdapter.getItem(sectionedPositionToPosition(position));
    }

    public long getItemId(int position) {
        return isSectionHeaderPosition(position) ? (long) (ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED - this.mSections.indexOfKey(position)) : this.mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position) ? getViewTypeCount() - 1 : this.mBaseAdapter.getItemViewType(sectionedPositionToPosition(position));
    }

    public boolean isEnabled(int position) {
        return isSectionHeaderPosition(position) ? false : this.mBaseAdapter.isEnabled(sectionedPositionToPosition(position));
    }

    public int getViewTypeCount() {
        return this.mBaseAdapter.getViewTypeCount() + TYPE_HEADER;
    }

    public boolean areAllItemsEnabled() {
        return this.mBaseAdapter.areAllItemsEnabled();
    }

    public boolean hasStableIds() {
        return this.mBaseAdapter.hasStableIds();
    }

    public boolean isEmpty() {
        return this.mBaseAdapter.isEmpty();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeaderPosition(position)) {
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(this.mSectionResourceId, parent, false);
            } else if (convertView.findViewById(this.mHeaderLayoutResId) == null) {
                convertView = this.mLayoutInflater.inflate(this.mSectionResourceId, parent, false);
            }
            HeaderLayout header;
            switch (((Section) this.mSections.get(position)).type) {
                case TYPE_HEADER /*1*/:
                    header = (HeaderLayout) convertView.findViewById(this.mHeaderLayoutResId);
                    if (!TextUtils.isEmpty(((Section) this.mSections.get(position)).title)) {
                        ((TextView) convertView.findViewById(this.mHeaderTextViewResId)).setText(((Section) this.mSections.get(position)).title);
                    }
                    header.setHeaderWidth(getHeaderSize());
                    return convertView;
                case TYPE_HEADER_FILLER /*2*/:
                    header = (HeaderLayout) convertView.findViewById(this.mHeaderLayoutResId);
                    if (!TextUtils.isEmpty(((Section) this.mSections.get(position)).title)) {
                        ((TextView) convertView.findViewById(this.mHeaderTextViewResId)).setText(((Section) this.mSections.get(position)).title);
                    }
                    header.setHeaderWidth(TYPE_FILLER);
                    return convertView;
                default:
                    return getFillerView(this.mLastViewSeen);
            }
        }
        convertView = this.mBaseAdapter.getView(sectionedPositionToPosition(position), convertView, parent);
        this.mLastViewSeen = convertView;
        return convertView;
    }

    private FillerView getFillerView(View lastViewSeen) {
        FillerView fillerView = new FillerView(this.mContext);
        fillerView.setMeasureTarget(lastViewSeen);
        return fillerView;
    }

    public int getHeaderLayoutResId() {
        return this.mHeaderLayoutResId;
    }
}
