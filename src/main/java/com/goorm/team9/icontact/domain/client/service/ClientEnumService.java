package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientEnumService {

    public <E extends Enum<E> & EnumWithDescription> List<Map<String, String>> getEnumList(E[] values) {
        return Arrays.stream(values)
                .map(e -> Map.of("key", e.name(), "description", e.getDescription()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEnumListWithApiCode(Role[] values) {
        return Arrays.stream(values)
                .map(e -> Map.<String, Object>of(
                        "key", e.name(),
                        "description", e.getDescription(),
                        "apiCode", e.getApiCode()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> getFilteredCareers(Role role) {
        return Arrays.stream(Career.values())
                .filter(c -> c.getApiCode() == role.getApiCode())
                .map(c -> Map.of("key", c.name(), "description", c.getDescription()))
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> getFilteredInterestsByApiCode(String apiCode) {
        return Arrays.stream(Interest.values())
                .filter(interest -> interest.getApiCode().equalsIgnoreCase(apiCode))
                .map(interest -> Map.of("key", interest.name(), "description", interest.getDescription()))
                .collect(Collectors.toList());
    }
}

