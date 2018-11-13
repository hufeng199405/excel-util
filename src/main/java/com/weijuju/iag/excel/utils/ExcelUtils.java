package com.weijuju.iag.excel.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * excel解析工具类
 *
 * @author hufeng
 * @create 2018-08-17 14:45
 */
@Slf4j
public class ExcelUtils {

    private ExcelUtils() {

    }

    /**
     * @param inputStream
     * @param ignoreLine  忽略几列
     * @return
     * @throws Exception
     */
    public static List<List<String>> parseExcelFile(InputStream inputStream, Integer ignoreLine) throws Exception {

        List<Sheet> sheets = generateExcelFile(inputStream);

        return generateExcelFile(sheets, ignoreLine);
    }

    /**
     * 根据流生成多个excel文件
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static List<Sheet> generateExcelFile(InputStream inputStream) throws Exception {

        List<Sheet> result = new ArrayList<>();

        try {

            Workbook workbook = WorkbookFactory.create(inputStream);

            if (workbook != null && workbook.getNumberOfSheets() > 0) {

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                    result.add(workbook.getSheetAt(i));
                }
            }
        } catch (Exception e) {

            if (inputStream != null) {

                inputStream.close();
            }

            throw e;
        }

        return result;
    }

    /**
     * 解析表格文件
     *
     * @param sheets
     * @return
     * @throws Exception
     */
    public static List<List<String>> generateExcelFile(List<Sheet> sheets, Integer ignoreLine) throws Exception {

        if (ignoreLine == null) {

            ignoreLine = 0;
        }

        List<List<String>> result = new ArrayList<>();

        try {

            if (CommUtils.listIsNotNull(sheets)) {

                for (Sheet sheet : sheets) {

                    int rowNum = sheet.getLastRowNum() + 1;
                    for (int i = 0; i < rowNum; i++) {

                        Row row = sheet.getRow(i);

                        List<String> lineValues = new ArrayList<>();

                        for (int j = 0; j < row.getLastCellNum(); j++) {

                            Cell cell = row.getCell(j);
                            // 获取单元格的值
                            String str = getCellValue(cell);
                            // 将得到的值放入列表中
                            lineValues.add(str);
                        }

                        if (CommUtils.listIsNotNull(lineValues) && i >= ignoreLine) {

                            result.add(lineValues);
                        }
                    }
                }
            }
        } catch (Exception e) {

            log.error("解析表格文件出错");
        }

        return result;
    }

    //获取单元格的值
    private static String getCellValue(Cell cell) {

        String cellValue = "";
        if (cell != null) {
            // 判断单元格数据的类型，不同类型调用不同的方法
            switch (cell.getCellType()) {
                // 数值类型
                case Cell.CELL_TYPE_NUMERIC:
                    // 进一步判断 ，单元格格式是日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {

                        cellValue = CommUtils.changeTimeByParam(cell.getDateCellValue(), CommUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                    } else {
                        //数值
                        double value = cell.getNumericCellValue();
                        int intValue = (int) value;
                        cellValue = value - intValue == 0 ? String.valueOf(intValue) : String.valueOf(value);
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                //判断单元格是公式格式，需要做一种特殊处理来得到相应的值
                case Cell.CELL_TYPE_FORMULA: {
                    try {
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException e) {
                        cellValue = String.valueOf(cell.getRichStringCellValue());
                        log.error("表格解析公式格式错出错", e);
                    }
                }
                break;
                case Cell.CELL_TYPE_BLANK:
                    cellValue = "";
                    break;
                case Cell.CELL_TYPE_ERROR:
                    cellValue = "";
                    break;
                default:
                    cellValue = cell.toString().trim();
                    break;
            }
        }
        return cellValue.trim();
    }
}
