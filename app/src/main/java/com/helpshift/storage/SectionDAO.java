package com.helpshift.storage;

import com.helpshift.Section;
import java.util.List;
import org.json.JSONArray;

public interface SectionDAO {
    void clearSectionsData();

    List<Section> getAllSections();

    Section getSection(String str);

    void storeSections(JSONArray jSONArray);
}
