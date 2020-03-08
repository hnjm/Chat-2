package cn.kk20.chat.api.controller;

import cn.kk20.chat.api.model.request.ApplyBean;
import cn.kk20.chat.api.model.request.CreateGroupBean;
import cn.kk20.chat.api.model.request.VerifyBean;
import cn.kk20.chat.base.http.ResultData;
import cn.kk20.chat.base.http.dto.SimpleDto;
import cn.kk20.chat.dao.model.ApplyLogModel;
import cn.kk20.chat.dao.model.GroupModel;
import cn.kk20.chat.service.ApplyLogService;
import cn.kk20.chat.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Roy Z
 * @Date: 2020/3/5 14:25
 * @Version: v1.0
 */
@RestController("group")
@Api(tags = "GroupController")
public class GroupController {

    @Autowired
    GroupService groupService;
    @Autowired
    ApplyLogService applyLogService;

    @PostMapping("create")
    @ApiOperation(value = "创建群", notes = "功能：新建群组")
    @ApiImplicitParam(name = "createGroupBean", value = "群基本信息", dataType = "CreateGroupBean")
    public ResultData create(@RequestBody CreateGroupBean createGroupBean) {
        GroupModel model = new GroupModel();
        model.setCreator(createGroupBean.getCreator());
        model.setName(createGroupBean.getName());
        model.setDescription(createGroupBean.getDescription());
        try {
            groupService.create(model);
            return ResultData.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.fail(500, e.getMessage());
        }
    }

    @PostMapping("/add")
    @ApiOperation(value = "申请加入群", notes = "功能：提交加入群申请")
    @ApiImplicitParam(name = "applyBean", value = "申请参数", dataType = "ApplyBean")
    public ResultData addFriend(@RequestBody ApplyBean applyBean) {
        if (applyBean == null || applyBean.getType() != 1 || applyBean.getApplyUserId() == null
                || applyBean.getTargetUserId() == null) {
            return ResultData.requestError();
        }

        try {
            ApplyLogModel applyLogModel = new ApplyLogModel();
            applyLogModel.setType(1);
            applyLogModel.setApplyUserId(applyBean.getApplyUserId());
            applyLogModel.setTargetUserId(applyBean.getTargetUserId());
            applyLogModel.setRemark(applyBean.getRemark());
            applyLogService.addApply(applyLogModel);
            return ResultData.success(new SimpleDto("申请已提交"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.fail(500, e.getMessage());
        }
    }

    @PostMapping("/verify")
    @ApiOperation(value = "审批-加入群申请", notes = "功能：审批加入群组申请")
    @ApiImplicitParam(name = "verifyBean", value = "审批参数", dataType = "VerifyBean")
    public ResultData verify(@RequestBody VerifyBean verifyBean) {
        if (verifyBean == null || verifyBean.getApplyId() == null || verifyBean.getVerifyUserId() == null
                || verifyBean.getAgree() == null) {
            return ResultData.requestError();
        }

        try {
            ApplyLogModel applyLogModel = new ApplyLogModel();
            applyLogModel.setId(verifyBean.getApplyId());
            applyLogModel.setVerifyUserId(verifyBean.getVerifyUserId());
            applyLogModel.setIsAgree(verifyBean.getAgree());
            applyLogModel.setRemark(verifyBean.getRemark());
            applyLogService.verifyApply(applyLogModel);
            return ResultData.success(new SimpleDto("操作成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.fail(500, e.getMessage());
        }
    }

}