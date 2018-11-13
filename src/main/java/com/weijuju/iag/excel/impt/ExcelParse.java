package com.weijuju.iag.excel.impt;

import com.google.gson.Gson;
import com.weijuju.iag.excel.AssessmentAward;
import com.weijuju.iag.excel.constant.ResponseCode;
import com.weijuju.iag.excel.model.ResponseModel;
import com.weijuju.iag.excel.model.ValidationResult;
import com.weijuju.iag.excel.utils.CommUtils;
import com.weijuju.iag.excel.utils.ExcelUtils;
import com.weijuju.iag.excel.utils.ResultHelper;
import com.weijuju.iag.excel.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * excel转化类
 *
 * @author 89005691
 * @create 2018-11-12 17:24
 */
@Slf4j
public class ExcelParse<T> {

    Map<Integer, String> fieldMap = new HashMap<>();

    public ResponseModel parseExcel(Class parstType, InputStream inputStream) throws Exception {

        List<T> result = this.parse(parstType, inputStream);

        if (CommUtils.listIsNotNull(result)) {

            int i = 1;

            for (T temp : result) {

                ValidationResult validationResult = ValidationUtils.validateEntity(temp);

                if (validationResult.hasErrors()) {

                    String msg = validationResult.getDefaultMessage();

                    // 替换el表达式
                    ExpressionParser ep = new SpelExpressionParser();

                    EvaluationContext ctx = new StandardEvaluationContext();
                    ctx.setVariable("count", i);

                    if (StringUtils.isNotEmpty(msg)) {

                        msg = ep.parseExpression(msg, new TemplateParserContext()).getValue(ctx).toString();
                    }

                    return ResultHelper.fail(ResponseCode.INVALID_PARAM.getCode(), msg);
                }

                i++;
            }
        }

        return new ResultHelper().success(result);
    }

    /**
     * 转化对象
     *
     * @param inputStream
     * @return
     */
    public List<T> parse(Class parstType, InputStream inputStream) {

        init(parstType);

        try {

            if (CommUtils.mapIsNotNull(fieldMap)) {

                List<List<String>> lists = ExcelUtils.parseExcelFile(inputStream, 1);

                if (CommUtils.listIsNotNull(lists)) {

                    List<T> result = new ArrayList<>();

                    Map<String, Object> jsonMap = new HashMap<>();

                    for (List<String> tempList : lists) {

                        int count = 1;

                        for (String temp : tempList) {

                            if (fieldMap.containsKey(count)) {

                                jsonMap.put(fieldMap.get(count), temp);
                            }

                            count++;
                        }

                        result.add((T) new Gson().fromJson(new Gson().toJson(jsonMap), parstType));

                        jsonMap.clear();
                    }

                    return result;
                }
            }
        } catch (Exception e) {

            log.error("转化对象出错", e);
        }

        return new ArrayList<>();
    }

    /**
     * 初始化
     *
     * @param parstType
     */
    private void init(Class parstType) {

        Field[] fields = parstType.getDeclaredFields();

        if (fields != null && fields.length > 0) {

            for (Field field : fields) {

                XlsImportColumn xlsImportGenColumn = field.getAnnotation(XlsImportColumn.class);

                if (xlsImportGenColumn != null) {

                    fieldMap.put(xlsImportGenColumn.order(), field.getName());
                }
            }
        }
    }

    public static void main(String[] args) {

        File file = new File("D:\\user\\89005691\\桌面\\导入奖项.xlsx");
        FileInputStream fileInputStream = null;

        try {

            fileInputStream = new FileInputStream(file);

            ResponseModel<AssessmentAward> responseModel = new ExcelParse<AssessmentAward>().parseExcel(AssessmentAward.class, fileInputStream);

            List<AssessmentAward> assessmentAwards = responseModel.getData();

            log.info(new Gson().toJson(assessmentAwards));
        } catch (FileNotFoundException e) {

            log.error("文件不存在", e);
        } catch (Exception e) {

            log.error("解析错误", e);
        } finally {

            if (fileInputStream != null) {

                try {

                    fileInputStream.close();
                } catch (Exception e) {

                    log.error("关闭流错误", e);
                }
            }
        }
    }
}
