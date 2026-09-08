package com.company.usermanagement.controller;

import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;
import com.company.usermanagement.service.TaskService;
import com.company.usermanagement.service.UserService;
import com.company.usermanagement.session.UserLoginSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExcelController {

    private final UserService userService;
    private final TaskService taskService;
    private final UserLoginSession userLoginSession;

    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcelTasks(@RequestBody List<Map<String, Object>> excelData) {
        try {
            List<TaskEntity> tasks = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            int successCount = 0;
            int skippedCount = 0;
            int emptyCellCount = 0;

            // Get default user (ID = 2) for fallback
            UserEntity defaultUser = userService.findById(2L);

            for (Map<String, Object> row : excelData) {
                try {
                    TaskEntity task = new TaskEntity();

                    // Count empty cells in this row
                    for (Object value : row.values()) {
                        if (value == null || value.toString().trim().isEmpty()) {
                            emptyCellCount++;
                        }
                    }

                    // 1. Assigned User - If empty or not found, use default user (ID: 2)
                    String assignedUser = getStringValue(row, "Assigned");
                    boolean userFound = false;

                    if (assignedUser != null && !assignedUser.trim().isEmpty()) {
                        UserEntity user = userService.findByUserName(assignedUser.trim());
                        if (user != null) {
                            task.setAssignedUser(user);
                            userFound = true;
                        } else {
                            warnings.add("User '" + assignedUser + "' not found. Using default user (ID: 2)");
                        }
                    }

                    // If user not found or not provided, use default user (ID: 2)
                    if (!userFound) {
                        if (defaultUser != null) {
                            task.setAssignedUser(defaultUser);
                        } else {
                            warnings.add("Default user (ID: 2) not found. Task will be created without assigned user.");
                        }
                    }

                    // 2. Priority - Set "-" if empty or null
                    String priority = getStringValue(row, "Priority");
                    task.setPriority((priority != null && !priority.trim().isEmpty()) ? priority.trim() : "-");

                    // 3. Client Name (Required - if empty, skip row)
                    String clientName = getStringValue(row, "Client Name");
                    if (clientName == null || clientName.trim().isEmpty()) {
                        errors.add("Client Name is required - row skipped");
                        skippedCount++;
                        continue;
                    }
                    task.setClientName(clientName.trim());

                    // 4. Issue - Set "-" if empty or null
                    String issue = getStringValue(row, "Issue");
                    task.setIssue((issue != null && !issue.trim().isEmpty()) ? issue.trim() : "-");

                    // 5. Redmine ID - Set 0 if empty or null
                    Long redmineId = getLongValue(row, "Redmine ID", 0L);
                    task.setRedmineId(redmineId != null ? redmineId : 0L);

                    // 6. Days - Set "-" if empty or null
                    Integer days = getIntegerValue(row, "Days");
                    task.setDays(days);

                    // 7. Requirement (Required - if empty, skip row)
                    String requirement = getStringValue(row, "Requirement");
                    if (requirement == null || requirement.trim().isEmpty()) {
                        errors.add("Requirement is required - row skipped");
                        skippedCount++;
                        continue;
                    }
                    task.setRequirement(requirement.trim());

                    // 8. Task Details - Set "-" if empty or null
                    String taskDetails = getStringValue(row, "Task Details");
                    task.setTaskDetails((taskDetails != null && !taskDetails.trim().isEmpty()) ? taskDetails.trim() : "-");

                    // 9. Status - Set "-" if empty or null
                    String status = getStringValue(row, "Status");
                    task.setStatus((status != null && !status.trim().isEmpty()) ? status.trim() : "-");

                    // 10. Fixed On - Set "-" if empty or null
                    String fixedOn = getStringValue(row, "Fixed On");
                    task.setFixedOn((fixedOn != null && !fixedOn.trim().isEmpty()) ? fixedOn.trim() : "-");

                    // 11. Remarks - Set "-" if empty or null
                    String remarks = getStringValue(row, "Remarks");
                    task.setRemarks((remarks != null && !remarks.trim().isEmpty()) ? remarks.trim() : "-");

                    task.setCreatedBy(userLoginSession.getUserId());
                    task.setUpdatedBy(userLoginSession.getUserId());
                    task.setIsActive(true);

                    tasks.add(task);
                    successCount++;

                } catch (Exception e) {
                    errors.add("Error processing row: " + e.getMessage());
                    skippedCount++;
                }
            }

            // Save all tasks
            if (!tasks.isEmpty()) {
                taskService.saveAll(tasks);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", successCount);
            response.put("skipped", skippedCount);
            response.put("emptyCells", emptyCellCount);
            response.put("errors", errors);
            response.put("warnings", warnings);

            String message = String.format("Successfully uploaded %d tasks", successCount);
            if (skippedCount > 0) {
                message += String.format(" (%d rows skipped)", skippedCount);
            }
            if (emptyCellCount > 0) {
                message += String.format(" (%d empty cells handled)", emptyCellCount);
            }
            if (!warnings.isEmpty()) {
                message += String.format(" (%d warnings)", warnings.size());
            }
            response.put("message", message);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error uploading Excel data: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        try {
            // Create sample data with null values to show what's allowed
            List<Map<String, Object>> sampleData = new ArrayList<>();

            // Sample row 1 - with all fields
            Map<String, Object> row1 = new LinkedHashMap<>();
            row1.put("Assigned", "Vikas");
            row1.put("Priority", "P1");
            row1.put("Client Name", "PT");
            row1.put("Issue", "Bug");
            row1.put("Redmine ID", "#30744");
            row1.put("Days", "5");
            row1.put("Requirement", "Kotak Bank Negotiation Report");
            row1.put("Task Details", "Issue not generated");
            row1.put("Status", "Fixed");
            row1.put("Fixed On", "UAT");
            row1.put("Remarks", "Done from my side");
            sampleData.add(row1);

            // Sample row 2 - with null values
            Map<String, Object> row2 = new LinkedHashMap<>();
            row2.put("Assigned", "Ankit");
            row2.put("Priority", "P2");
            row2.put("Client Name", "GAIL");
            row2.put("Issue", "CR");
            row2.put("Redmine ID", "#56846");
            row2.put("Days", null);
            row2.put("Requirement", "GAIL: Forgot Password – Reset Link");
            row2.put("Task Details", null);
            row2.put("Status", "Fixed");
            row2.put("Fixed On", "Production");
            row2.put("Remarks", null);
            sampleData.add(row2);

            // Sample row 3 - minimal required fields
            Map<String, Object> row3 = new LinkedHashMap<>();
            row3.put("Assigned", null);
            row3.put("Priority", null);
            row3.put("Client Name", "ABC Corp");
            row3.put("Issue", null);
            row3.put("Redmine ID", null);
            row3.put("Days", null);
            row3.put("Requirement", "Fix login issue");
            row3.put("Task Details", null);
            row3.put("Status", null);
            row3.put("Fixed On", null);
            row3.put("Remarks", null);
            sampleData.add(row3);

            // Generate Excel file
            byte[] excelContent = generateExcelFile(sampleData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "task_template.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelContent);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/export-tasks")
    public ResponseEntity<byte[]> exportAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            byte[] excelContent = generateTasksExport(tasks);

            String fileName = "tasks_export_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generateTasksExport(List<TaskDTO> tasks) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        String[] headers = {
                "Task ID", "Client", "Requirement", "Task Details", "Remarks",
                "Assigned To", "Priority", "Status", "Issue", "Fixed On",
                "Days", "Redmine ID", "Active", "Created At", "Updated At",
                "Created By", "Updated By"
        };

        Map<Long, String> userNameById = new HashMap<>();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (TaskDTO task : tasks) {
                Row row = sheet.createRow(rowNum++);
                Object[] values = {
                        task.getTaskId() != null ? "TSK-" + task.getTaskId() : "",
                        nullToDash(task.getClientName()),
                        nullToDash(task.getRequirement()),
                        nullToDash(task.getTaskDetails()),
                        nullToDash(task.getRemarks()),
                        nullToDash(task.getAssignUserName()),
                        nullToDash(task.getPriority()),
                        nullToDash(task.getStatus()),
                        nullToDash(task.getIssue()),
                        nullToDash(task.getFixedOn()),
                        task.getDays() != null ? task.getDays() : "",
                        task.getRedmineId() != null ? task.getRedmineId() : "",
                        task.getIsActive() != null ? (task.getIsActive() ? "Yes" : "No") : "",
                        task.getCreateAt() != null ? task.getCreateAt().format(dateFmt) : "",
                        task.getUpdateAt() != null ? task.getUpdateAt().format(dateFmt) : "",
                        resolveUserName(task.getCreateBy(), userNameById),
                        resolveUserName(task.getUpdatedBy(), userNameById)
                };

                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    Object value = values[i];
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error exporting tasks to Excel: " + e.getMessage(), e);
        }
    }

    private String resolveUserName(Long userId, Map<Long, String> cache) {
        if (userId == null) {
            return "-";
        }
        return cache.computeIfAbsent(userId, id -> {
            UserEntity user = userService.findById(id);
            if (user != null && user.getUserName() != null && !user.getUserName().isBlank()) {
                return user.getUserName();
            }
            return "-";
        });
    }

    private String nullToDash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value;
    }

    private byte[] generateExcelFile(List<Map<String, Object>> sampleData) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Task Template");

            // Create instruction row
            Row instructionRow = sheet.createRow(0);
            Cell instructionCell = instructionRow.createCell(0);
            instructionCell.setCellValue("Instructions: Required fields: Client Name, Requirement. Others are optional.");

            CellStyle instructionStyle = workbook.createCellStyle();
            Font instructionFont = workbook.createFont();
            instructionFont.setItalic(true);
            instructionFont.setFontHeightInPoints((short) 10);
            instructionStyle.setFont(instructionFont);
            instructionCell.setCellStyle(instructionStyle);

            // Merge cells for instruction
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Create header row (row 1)
            Row headerRow = sheet.createRow(1);
            String[] headers = {"Assigned", "Priority", "Client Name", "Issue", "Redmine ID",
                    "Days", "Requirement", "Task Details", "Status", "Fixed On", "Remarks"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill sample data starting from row 2
            int rowNum = 2;
            for (Map<String, Object> rowData : sampleData) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                for (String header : headers) {
                    Cell cell = row.createCell(colNum++);
                    Object value = rowData.get(header);

                    if (value != null) {
                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel template: " + e.getMessage(), e);
        }
    }

    // Helper methods - ALL return "-" if empty or null
    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        String strValue = value.toString().trim();
        return strValue.isEmpty() ? null : strValue;
    }

    private Long getLongValue(Map<String, Object> row, String key, Long defaultValue) {
        Object value = row.get(key);
        if (value == null) return defaultValue;
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            String strValue = value.toString().trim();
            if (strValue.startsWith("#")) {
                strValue = strValue.substring(1);
            }
            if (strValue.isEmpty()) return defaultValue;
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer getIntegerValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            String strValue = value.toString().trim();
            if (strValue.equals("-") || strValue.isEmpty()) return null;
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
