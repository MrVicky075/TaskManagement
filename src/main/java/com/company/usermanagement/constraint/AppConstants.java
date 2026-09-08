package com.company.usermanagement.constraint;

import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor
public final class AppConstants {

    public static final String SESSION_USER ="userLoginSession";
    public static final String ROLE_ADMIN ="ADMIN";
    public static final String ROLE_SR_DEVELOPER = "SR_DEVELOPER";
    public static final String ROLE_DEVELOPER ="DEVELOPER";
    public static final String ROLE_SUPPORT ="SUPPORT";

    // Fixed On
    public static Map<String, String> getFixedOnList() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("-", "-");
        map.put("LOCAL", "LOCAL");
        map.put("UAT", "UAT");
        map.put("PRODUCTION", "PRODUCTION");
        return map;
    }

    // Priority
    public static Map<String,String > getPriorityList(){
        Map<String,String> map = new LinkedHashMap<>();
        map.put("-","-");
        map.put("P1","P1");
        map.put("P2","P2");
        map.put("P3","P3");
        map.put("P4","P4");
        map.put("P5","P5");
        map.put("P6","P6");
        map.put("HOLD","HOLD");
        return map;
    }

    // Paid status
    public static Map<String, String> getPaidList() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Paid", "Paid");
        map.put("UnPaid", "UnPaid");
        return map;
    }

    // Issue Type
    public static Map<String,String > getIssueTypeList(){
        Map<String,String> map = new LinkedHashMap<>();
        map.put("-", "-");
        map.put("Bug", "Bug");
        map.put("ISSUE","ISSUE");
        map.put("Requirement","Requirement");
        map.put("Email", "Email");
        map.put("Change Request", "Change Request (CR)");
        map.put("Production Issue", "Production Issue");
        map.put("Other", "Other");
        return map;
    }

    public static Map<String, String> getStatusList() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("-", "-");
        map.put("New Task", "New Task");
        map.put("Assigned", "Assigned");
        map.put("Working", "Working");
        map.put("Complete", "Complete");
        map.put("Hold", "Hold");
        return map;
    }

    public static Map<String, String> getUserRoles() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("DEVELOPER", "Developer");
        map.put("SR_DEVELOPER", "Sr. Developer");
        map.put("SUPPORT", "Support Team");
        return map;
    }

}
