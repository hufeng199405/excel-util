package com.weijuju.iag.excel;

import com.weijuju.iag.excel.impt.XlsImportColumn;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

/**
 * 奖项管理业务对象
 *
 * @author 89005691
 * @create 2018-09-03 15:44
 */
@Data
public class AssessmentAward {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * uuid
     */
    private String uuid;

    /**
     * 奖项类别（1 地区最高荣誉奖  2 特设奖 3常规奖）
     */
    private Integer awardCategory;

    /**
     * 奖项类别（1 地区最高荣誉奖  2 特设奖 3常规奖）
     */
    @NotBlank(message = "第#{#count}列不能为空")
    @XlsImportColumn(order = 2)
    private String awardCategoryName;

    /**
     * 奖项名称
     */
    @XlsImportColumn(order = 1)
    private String awardName;

    /**
     * 奖项code
     */
    private String awardCode;

    /**
     * 前端申报开始时间
     */
    @XlsImportColumn(order = 3)
    private Date startTime;

    /**
     * 前端申报结束时间
     */
    @XlsImportColumn(order = 4)
    private Date endTime;

    /**
     * 点赞开始时间
     */
    @XlsImportColumn(order = 7)
    private Date likeStartTime;

    /**
     * 点赞结束时间
     */
    @XlsImportColumn(order = 8)
    private Date likeEndTime;

    /**
     * 申报条件
     */
    @XlsImportColumn(order = 9)
    private String evaluationCriteria;

    /**
     * 评选标准
     */
    @XlsImportColumn(order = 10)
    private String awardIntroduction;

    /**
     * 申报地区
     */
    private String applyArea;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人code
     */
    private String createName;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 修改人code
     */
    private String updateName;

    /**
     * 删除时间
     */
    private Date deleteTime;

    /**
     * 删除人code
     */
    private String deleteName;

    /**
     * 状态   1 有效  0无效
     */
    private Boolean status;

    /**
     * 后端申报开始时间
     */
    @XlsImportColumn(order = 5)
    private Date adminStartTime;

    /**
     * 后端申报结束时间
     */
    @XlsImportColumn(order = 6)
    private Date adminEndTime;

    /**
     * 奖项备注
     */
    @XlsImportColumn(order = 11)
    private String remark;
}
