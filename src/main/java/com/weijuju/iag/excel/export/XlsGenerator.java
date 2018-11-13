package com.weijuju.iag.excel.export;

import com.weijuju.iag.excel.utils.CommUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 以poi为基础将一个bean转换为Excel工具类
 * <p>
 * 可以使用{@link XlsGenColumn}指定bean属性所对应的标题栏以及排序位置<br>
 * <p>
 * 如果bean的属性是一个复杂类型,可通过{@link XlsGenAbstractHandle}自定义去处理相关属性的映射值<br>
 * <p>
 * 对于常见的Java基本类型(int,long,boolean,varchar,string,date)已做过处理
 * <p>
 * <br>
 */
@Slf4j
public class XlsGenerator {

    private Workbook workbook;

    private OutputStream outputStream;

    /**
     * Excel表的表名是不可重复的
     */
    private Set<String> sheetNames = new HashSet<>(1);

    /**
     * bean与sheet的映射
     */
    private Map<String, Class<?>> sheetBeanMap = new HashMap<>(8);

    /**
     * bean具体与table映射
     */
    private Map<String, List<TableInfo>> beanTableMap = new HashMap<>(8);

    /**
     * 推荐使用内部Builder类构建实例
     */
    public static class Builder {

        /**
         * 创建一个xls文档辅助生成类
         *
         * @param outputStream
         * @return
         */
        public static XlsGenerator xlsGenerator(OutputStream outputStream) {
            return new XlsGenerator(HSSFWorkbook.class, 0, outputStream);
        }

        /**
         * 创建一个xlsx文档辅助生成类
         *
         * @param outputStream
         * @return
         */
        public static XlsGenerator xlsxGenerator(OutputStream outputStream) {
            return new XlsGenerator(XSSFWorkbook.class, 0, outputStream);
        }

        /**
         * 创建一个xlsx文档辅助生成类(带有滑动窗口,数据量较大时可获得较低内存占用)
         *
         * @param outputStream
         * @param rowAccessWindowsSize
         * @return
         */
        public static XlsGenerator xlsxGenerator(OutputStream outputStream, int rowAccessWindowsSize) {
            return new XlsGenerator(SXSSFWorkbook.class, rowAccessWindowsSize, outputStream);
        }
    }

    public Selector selector(String sheetName, Class<?> beanClass) {
        if (StringUtils.isEmpty(sheetName)) {
            throw new RuntimeException("创建Excel的表名字不能为空");
        }
        if (beanClass == null) {
            throw new RuntimeException("需要映射的Bean Class信息不能为空");
        }
        if (!sheetNames.contains(sheetName)) {
            sheetNames.add(sheetName);
            sheetBeanMap.put(sheetName, beanClass);
            proccessTitle(sheetName, 0, beanClass);
        }
        if (sheetNames.contains(sheetName)) {
            if (!beanClass.equals(sheetBeanMap.get(sheetName))) {
                int rowNum = this.workbook.getSheet(sheetName).getLastRowNum() + 2;
                proccessTitle(sheetName, rowNum, beanClass);
            }
        }
        return new Selector(sheetName);
    }

    public void flush() {
        try {
            this.workbook.write(outputStream);
        } catch (IOException e) {
            log.info("写入表格发生异常;", e);
        } finally {
            if (this.workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) XlsGenerator.this.workbook).dispose();
            }
        }
    }

    public class Selector {

        private String sheetName;

        public Selector(String sheetName) {
            this.sheetName = sheetName;
        }


        public void write(List<?> list) {

            if (CommUtils.listIsNotNull(list)) {
                return;
            }

            Sheet sheet = XlsGenerator.this.workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = XlsGenerator.this.workbook.createSheet(sheetName);
            }

            List<TableInfo> tableInfos = beanTableMap.get(sheetName);

            try {
                for (Object item : list) {
                    Row tmpRow = sheet.createRow(sheet.getLastRowNum() + 1);
                    for (int i = 0; i < tableInfos.size(); i++) {
                        Field field = item.getClass().getDeclaredField(tableInfos.get(i).getFieldName());
                        field.setAccessible(true);
                        Object object = field.get(item);
                        XlsGenNewAbstractHandle newHandle = tableInfos.get(i).getNewHandle();
                        if (newHandle != null && !newHandle.getClass().equals(XlsGenNewAbstractHandle.class)) {
                            processTableValueWithNewHandle(newHandle, tmpRow, i, object, item);
                            continue;
                        }

                        XlsGenAbstractHandle handle = tableInfos.get(i).getHandle();
                        if (handle != null && !(handle.getClass().equals(XlsGenAbstractHandle.class))) {
                            processTableValueWithHandle(handle, tmpRow, i, object);
                            continue;
                        }
                        processTableValue(tmpRow, i, field.getType(), object);
                    }
                }
            } catch (Exception ex) {
                log.info("写入表格发生异常:", ex);
            }
        }
    }

    /**
     * 创建excel工具类
     *
     * @param instanseClass        {@link XSSFWorkbook}创建的是xlsx <br>
     *                             {@link SXSSFWorkbook}创建的是xlsx(数据量较大时选用会避免内存溢出)<br>
     *                             {@link HSSFWorkbook}创建xls
     * @param rowAccessWindowsSize 当instanseClass={@link SXSSFWorkbook}时有用,值越小所占用的内存越小 建议5000-10000
     * @param outputStream
     */
    private XlsGenerator(Class<? extends Workbook> instanseClass, int rowAccessWindowsSize, OutputStream outputStream) {
        if (outputStream == null) {
            throw new RuntimeException("outputStream不能为空");
        }

        if (instanseClass == null) {
            throw new RuntimeException("POI表格对象不能为空");
        }

        if (instanseClass.equals(XSSFWorkbook.class)) {
            this.workbook = new XSSFWorkbook();
        }
        this.outputStream = outputStream;
        if (instanseClass.equals(SXSSFWorkbook.class)) {
            this.workbook = new SXSSFWorkbook(rowAccessWindowsSize);
        }

        if (instanseClass.equals(HSSFWorkbook.class)) {
            this.workbook = new HSSFWorkbook();
        }

        if (this.workbook == null) {
            throw new RuntimeException("instanseClass必须是[XSSFWorkbook,SXSSFWorkbook,HSSFWorkbook]类型中的一种");
        }
    }

    /**
     * 初始化标题栏信息
     *
     * @param beanClass 标题栏bean
     */
    private void proccessTitle(String sheetName, int rowNum, Class<?> beanClass) {
        List<TableInfo> tableInfos = beanTableMap.get(sheetName);
        if (CommUtils.listIsNotNull(tableInfos)) {
            tableInfos = new ArrayList<>(beanClass.getDeclaredFields().length);
        }

        if (!beanClass.equals(beanTableMap.get(sheetName))) {
            tableInfos = new ArrayList<>(beanClass.getDeclaredFields().length);
        }

        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            TableInfo info = new TableInfo();
            //判断字段是否属于排除字段
            XlsGenColumnExclude genColumnExclude = field.getAnnotation(XlsGenColumnExclude.class);
            if (genColumnExclude != null) {
                continue;
            }
            XlsGenColumn genColumn = field.getAnnotation(XlsGenColumn.class);
            if (genColumn == null) {
                info.setFieldName(field.getName());
                info.setTitle(field.getName());
                info.setOrder(0);
                tableInfos.add(info);
                continue;
            }
            info.setFieldName(field.getName());
            info.setTitle(genColumn.value());
            info.setOrder(genColumn.order());
            Class<? extends XlsGenAbstractHandle> handleClass = genColumn.handle();
            if (handleClass != null && !handleClass.equals(XlsGenAbstractHandle.class)) {
                XlsGenAbstractHandle handle = null;
                try {
                    handle = XlsGenAbstractHandle.class.newInstance();
                } catch (InstantiationException e) {
                    log.error("获取属性转换类异常;", e);
                } catch (IllegalAccessException e) {
                    log.error("获取属性转换类异常;", e);
                }
                info.setHandle(handle);
            }
            Class<? extends XlsGenNewAbstractHandle> newHandleClass = genColumn.newHandle();
            if (newHandleClass != null && !newHandleClass.equals(XlsGenNewAbstractHandle.class)) {
                XlsGenNewAbstractHandle newHandle = null;
                try {
                    newHandle = XlsGenNewAbstractHandle.class.newInstance();
                } catch (IllegalAccessException e) {
                    log.error("获取属性转换类异常;", e);
                } catch (InstantiationException e) {
                    log.error("获取属性转换类异常;", e);
                }
                info.setNewHandle(newHandle);
            }

            tableInfos.add(info);
        }
        Collections.sort(tableInfos, new Comparator<TableInfo>() {
            @Override
            public int compare(TableInfo o1, TableInfo o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });
        Sheet sheet = this.workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);
            for (int i = 0; i < tableInfos.size(); i++) {
                //判断字段是否属于排除字段
                row.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(tableInfos.get(i).getTitle());
            }
            beanTableMap.put(sheetName, tableInfos);
            return;
        }
        int lastRowNum = sheet.getLastRowNum() + 1;
        if (rowNum > lastRowNum) {
            sheet.createRow(lastRowNum);
            lastRowNum++;
        }
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < tableInfos.size(); i++) {
            row.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(tableInfos.get(i).getTitle());
        }
        beanTableMap.put(sheetName, tableInfos);
    }


    private void processTableValue(Row row, int column, Class<?> fieldType, Object value) {
        if (row == null || fieldType == null || value == null) {
            return;
        }
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            row.createCell(column, Cell.CELL_TYPE_NUMERIC).setCellValue((Integer) value);
            return;
        }
        if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            row.createCell(column, Cell.CELL_TYPE_NUMERIC).setCellValue((Double) value);
            return;
        }
        if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            row.createCell(column, Cell.CELL_TYPE_NUMERIC).setCellValue((Float) value);
            return;
        }
        if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            row.createCell(column, Cell.CELL_TYPE_NUMERIC).setCellValue((Long) value);
            return;
        }
        if (fieldType.equals(byte.class) || fieldType.equals(Character.class)) {
            row.createCell(column, Cell.CELL_TYPE_STRING).setCellValue((Character) value);
            return;
        }
        if (fieldType.equals(String.class)) {
            row.createCell(column, Cell.CELL_TYPE_STRING).setCellValue((String) value);
            return;
        }
        if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            row.createCell(column, Cell.CELL_TYPE_BOOLEAN).setCellValue((Boolean) value);
            return;
        }
        if (fieldType.equals(Date.class)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
            row.createCell(column, Cell.CELL_TYPE_STRING).setCellValue(format.format((Date) value));
            return;
        }
    }

    private void processTableValueWithHandle(XlsGenAbstractHandle handle, Row row, int column, Object object) {
        String result = handle.doHandle(object);
        row.createCell(column, Cell.CELL_TYPE_STRING).setCellValue(result);
    }

    private void processTableValueWithNewHandle(XlsGenNewAbstractHandle handle, Row row, int column, Object object, Object instanse) {
        String result = handle.doHandle(object, instanse);
        row.createCell(column, Cell.CELL_TYPE_STRING).setCellValue(result);
    }
}

@Data
class TableInfo {
    private String title;
    private String fieldName;
    private Integer order;
    private XlsGenAbstractHandle handle;
    private XlsGenNewAbstractHandle newHandle;
}