package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class XlsxParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String sheetName = sheet.getSheetName();

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;

                List<String> headers = new ArrayList<>();
                for (Cell cell : headerRow) {
                    headers.add(getCellValue(cell, evaluator));
                }

                int blockIndex = 1;
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    StringBuilder rowText = new StringBuilder();
                    for (int c = 0; c < headers.size() && c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            String value = getCellValue(cell, evaluator);
                            if (!value.isBlank()) {
                                rowText.append(headers.get(c).trim())
                                        .append(": ")
                                        .append(value.trim())
                                        .append("; ");
                            }
                        }
                    }

                    if (!rowText.isEmpty()) {
                        blocks.add(new ParsedBlock(
                                rowText.toString().trim(),
                                "Planilha '" + sheetName + "' - Linha " + (r + 1),
                                blockIndex++
                        ));
                    }
                }
            }
        }

        return new ParsedDocument(blocks);
    }

    private String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> formatEvaluatedCell(evaluator.evaluate(cell));
            default -> "";
        };
    }

    private String formatEvaluatedCell(CellValue evaluated) {
        if (evaluated == null) return "";
        return switch (evaluated.getCellType()) {
            case STRING -> evaluated.getStringValue();
            case NUMERIC -> String.valueOf(evaluated.getNumberValue());
            case BOOLEAN -> String.valueOf(evaluated.getBooleanValue());
            default -> "";
        };
    }
}
