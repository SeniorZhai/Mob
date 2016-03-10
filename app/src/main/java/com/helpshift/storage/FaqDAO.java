package com.helpshift.storage;

import com.helpshift.Faq;
import java.util.List;

public interface FaqDAO {
    void addFaq(Faq faq);

    Faq getFaq(String str);

    List<Faq> getFaqsDataForSection(String str);

    List<Faq> getFaqsForSection(String str);

    int setIsHelpful(String str, Boolean bool);
}
