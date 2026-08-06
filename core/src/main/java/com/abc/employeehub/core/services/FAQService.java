package com.abc.employeehub.core.services;

import java.util.List;
import java.util.Map;

public interface FAQService {

    List<Map<String, String>> loadFAQs(String faqPath);

    List<Map<String, String>> sortFAQs(List<Map<String, String>> faqs);
}
