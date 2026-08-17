package br.com.petterson.nbassistant.parser;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class XlsxParserTest {

    private final XlsxParser parser = new XlsxParser();

    @Test
    void shouldParseXlsxRowsIntoBlocks() throws Exception {
        byte[] xlsxBytes = createXlsx(wb -> {
            var sheet = wb.createSheet("Funcionarios");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("nome");
            header.createCell(1).setCellValue("cargo");

            var row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Ana");
            row1.createCell(1).setCellValue("Analista");

            var row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Bruno");
            row2.createCell(1).setCellValue("Gerente");
        });

        var file = new MockMultipartFile("file", "dados.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);

        assertThat(parsed.blocks().get(0).content()).contains("nome: Ana", "cargo: Analista");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Planilha 'Funcionarios' - Linha 2");
        assertThat(parsed.blocks().get(0).order()).isEqualTo(1);

        assertThat(parsed.blocks().get(1).content()).contains("nome: Bruno", "cargo: Gerente");
        assertThat(parsed.blocks().get(1).locationLabel()).isEqualTo("Planilha 'Funcionarios' - Linha 3");
        assertThat(parsed.blocks().get(1).order()).isEqualTo(2);
    }

    @Test
    void shouldHandleMultipleSheets() throws Exception {
        byte[] xlsxBytes = createXlsx(wb -> {
            var sheet1 = wb.createSheet("Vendas");
            var h1 = sheet1.createRow(0);
            h1.createCell(0).setCellValue("produto");
            var r1 = sheet1.createRow(1);
            r1.createCell(0).setCellValue("Cartao Kids");

            var sheet2 = wb.createSheet("Suporte");
            var h2 = sheet2.createRow(0);
            h2.createCell(0).setCellValue("ticket");
            var r2 = sheet2.createRow(1);
            r2.createCell(0).setCellValue("12345");
        });

        var file = new MockMultipartFile("file", "multi.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).locationLabel()).contains("Vendas");
        assertThat(parsed.blocks().get(1).locationLabel()).contains("Suporte");
    }

    @Test
    void shouldReturnEmptyBlocksForHeaderOnlySheet() throws Exception {
        byte[] xlsxBytes = createXlsx(wb -> {
            var sheet = wb.createSheet("Vazia");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("coluna1");
            header.createCell(1).setCellValue("coluna2");
        });

        var file = new MockMultipartFile("file", "vazia.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).isEmpty();
    }

    @Test
    void shouldHandleNumericCells() throws Exception {
        byte[] xlsxBytes = createXlsx(wb -> {
            var sheet = wb.createSheet("Numeros");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("item");
            header.createCell(1).setCellValue("valor");

            var row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Saldo");
            row1.createCell(1).setCellValue(1500.50);
        });

        var file = new MockMultipartFile("file", "numeros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content()).contains("item: Saldo", "valor: 1500.5");
    }

    // --- helper ---

    private byte[] createXlsx(java.util.function.Consumer<XSSFWorkbook> builder) throws Exception {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            builder.accept(wb);
            wb.write(out);
            return out.toByteArray();
        }
    }
}
