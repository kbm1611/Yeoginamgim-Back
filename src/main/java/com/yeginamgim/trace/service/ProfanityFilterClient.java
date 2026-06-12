package com.yeginamgim.trace.service;

import java.util.List;

interface ProfanityFilterClient {
    ProfanityCheckResponse check(List<String> texts);
}
