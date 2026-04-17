package com.expertlink.service;

import com.expertlink.domain.SystemSetting;
import com.expertlink.dto.settings.RoleGroupsResponse;
import com.expertlink.dto.settings.MenuPermissionsResponse;
import com.expertlink.dto.settings.UpdateMenuPermissionsRequest;
import com.expertlink.dto.settings.UpdateRoleGroupsRequest;
import com.expertlink.repository.SystemSettingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingService {

    private static final String KEY_ROLE_GROUPS = "role-groups";
    private static final String KEY_MENU_PERMISSIONS = "menu-permissions";
    private static final List<String> DEFAULT_STEWARD = List.of("DOMAIN_STEWARD", "SUPER_ADMIN");
    private static final List<String> DEFAULT_EXPERT = List.of("EXPERT_USER", "SUPER_ADMIN");
    private static final List<String> DEFAULT_SUPER_ADMIN = List.of("SUPER_ADMIN");
    private static final List<String> MENU_KEYS = List.of(
            "dashboard",
            "engagements.mine",
            "engagements.new",
            "engagements.stewardQueue",
            "engagements.expertPending",
            "engagements.pointsLedger",
            "experts",
            "domains",
            "skills",
            "projects",
            "analysis",
            "settings",
            "admin.users",
            "admin.domainStewards",
            "admin.auditLogs",
            "admin.accessControl"
    );

    private final SystemSettingRepository systemSettingRepository;
    private final ObjectMapper objectMapper;

    public RoleGroupsResponse getRoleGroups() {
        Map<String, List<String>> raw = systemSettingRepository.findBySettingKey(KEY_ROLE_GROUPS)
                .map(SystemSetting::getSettingValue)
                .map(this::parseRoleGroupsMap)
                .orElseGet(this::defaultRoleGroupsMap);
        return RoleGroupsResponse.builder()
                .steward(sanitizeRoleList(raw.get("STEWARD"), DEFAULT_STEWARD))
                .expert(sanitizeRoleList(raw.get("EXPERT"), DEFAULT_EXPERT))
                .superAdmin(sanitizeRoleList(raw.get("SUPER_ADMIN"), DEFAULT_SUPER_ADMIN))
                .build();
    }

    public MenuPermissionsResponse getMenuPermissions() {
        Map<String, List<String>> map = systemSettingRepository.findBySettingKey(KEY_MENU_PERMISSIONS)
                .map(SystemSetting::getSettingValue)
                .map(this::parseRoleMenusMap)
                .orElseGet(this::defaultRoleMenusMap);
        return MenuPermissionsResponse.builder()
                .menuKeys(MENU_KEYS)
                .roleMenus(map)
                .build();
    }

    @Transactional
    public RoleGroupsResponse updateRoleGroups(UpdateRoleGroupsRequest req) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("STEWARD", sanitizeRoleList(req.getSteward(), DEFAULT_STEWARD));
        map.put("EXPERT", sanitizeRoleList(req.getExpert(), DEFAULT_EXPERT));
        map.put("SUPER_ADMIN", sanitizeRoleList(req.getSuperAdmin(), DEFAULT_SUPER_ADMIN));

        String json;
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (Exception ex) {
            throw new IllegalArgumentException("角色组配置序列化失败");
        }

        SystemSetting setting = systemSettingRepository.findBySettingKey(KEY_ROLE_GROUPS)
                .orElseGet(() -> SystemSetting.builder().settingKey(KEY_ROLE_GROUPS).build());
        setting.setSettingValue(json);
        systemSettingRepository.save(setting);
        return getRoleGroups();
    }

    @Transactional
    public MenuPermissionsResponse updateMenuPermissions(UpdateMenuPermissionsRequest req) {
        Map<String, List<String>> roleMenus = req != null ? req.getRoleMenus() : null;
        Map<String, List<String>> merged = mergeRoleMenus(roleMenus);
        String json;
        try {
            json = objectMapper.writeValueAsString(merged);
        } catch (Exception ex) {
            throw new IllegalArgumentException("菜单权限配置序列化失败");
        }
        SystemSetting setting = systemSettingRepository.findBySettingKey(KEY_MENU_PERMISSIONS)
                .orElseGet(() -> SystemSetting.builder().settingKey(KEY_MENU_PERMISSIONS).build());
        setting.setSettingValue(json);
        systemSettingRepository.save(setting);
        return getMenuPermissions();
    }

    private Map<String, List<String>> parseRoleGroupsMap(String json) {
        if (json == null || json.isBlank()) {
            return defaultRoleGroupsMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {
            });
        } catch (Exception ex) {
            return defaultRoleGroupsMap();
        }
    }

    private Map<String, List<String>> defaultRoleGroupsMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("STEWARD", DEFAULT_STEWARD);
        map.put("EXPERT", DEFAULT_EXPERT);
        map.put("SUPER_ADMIN", DEFAULT_SUPER_ADMIN);
        return map;
    }

    private Map<String, List<String>> parseRoleMenusMap(String json) {
        if (json == null || json.isBlank()) {
            return defaultRoleMenusMap();
        }
        try {
            Map<String, List<String>> parsed = objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {
            });
            return mergeRoleMenus(parsed);
        } catch (Exception ex) {
            return defaultRoleMenusMap();
        }
    }

    private Map<String, List<String>> defaultRoleMenusMap() {
        List<String> base = List.of(
                "dashboard",
                "engagements.mine",
                "engagements.new",
                "engagements.pointsLedger",
                "experts",
                "domains",
                "skills",
                "projects",
                "analysis",
                "settings"
        );
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("REGULAR_USER", base);
        map.put("DEPT_ADMIN", base);
        map.put("EXPERT_USER", concat(base, List.of("engagements.expertPending")));
        map.put("DOMAIN_STEWARD", concat(base, List.of("engagements.stewardQueue")));
        map.put("SUPER_ADMIN", MENU_KEYS);
        return map;
    }

    private Map<String, List<String>> mergeRoleMenus(Map<String, List<String>> input) {
        Map<String, List<String>> out = defaultRoleMenusMap();
        if (input == null || input.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, List<String>> e : input.entrySet()) {
            String role = e.getKey();
            if (role == null || role.isBlank()) {
                continue;
            }
            List<String> list = sanitizeMenuKeys(e.getValue());
            out.put(role.trim(), list);
        }
        return out;
    }

    private List<String> sanitizeMenuKeys(List<String> menuKeys) {
        if (menuKeys == null || menuKeys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String key : menuKeys) {
            if (key != null && MENU_KEYS.contains(key)) {
                set.add(key);
            }
        }
        return List.copyOf(set);
    }

    private static List<String> concat(List<String> a, List<String> b) {
        LinkedHashSet<String> set = new LinkedHashSet<>(a);
        set.addAll(b);
        return List.copyOf(set);
    }

    private List<String> sanitizeRoleList(List<String> roles, List<String> fallback) {
        if (roles == null || roles.isEmpty()) {
            return fallback;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String r : roles) {
            if (r != null && !r.isBlank()) {
                set.add(r.trim());
            }
        }
        if (set.isEmpty()) {
            return fallback;
        }
        return List.copyOf(set);
    }
}

