package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkStarDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.vo.TalkVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @description: 动态
 * @author: HouTianYun
 * @create: 2023-11-19 22:00
 **/
@Api(tags = "动态")
@RestController
@RequestMapping("/talk")
public class TalkController {
    @Autowired
    private ITalkService talkService;

    @Autowired
    private ITalkStarService talkStarService;

    @ApiOperation(value = "新增动态", notes = "新增动态")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody TalkAddDTO talkAddDTO) {
        talkService.addTalk(talkAddDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "更新动态", notes = "更新动态")
    @PostMapping("/update")
    public Result update(@Valid @RequestBody TalkUpdateDTO talkUpdateDTO) {
        talkService.updateTalk(talkUpdateDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询动态", notes = "分页查询动态")
    @GetMapping("/pageQueryTalkList")
    public Result pageQueryTalkList() {
        return ResultUtils.success(talkService.pageQueryTalkList());
    }

    @ApiModelProperty(value = "删除动态", notes = "删除动态")
    @DeleteMapping("/delete")
    public Result del(@Valid @RequestBody TalkDelDTO talkDelDTO) {
        talkService.delTalk(talkDelDTO);
        return ResultUtils.success();
    }

    @ApiModelProperty(value = "查询动态详情", notes = "查询动态详情")
    @GetMapping("/getTalkDetail/{talkId}")
    public Result<TalkVO> getTalkDetail(@NotNull(message = "参数异常") @PathVariable Long talkId) {
        return ResultUtils.success(talkService.getTalkDetail(talkId));
    }

    @ApiModelProperty(value = "动态点赞", notes = "动态点赞")
    @PostMapping("/like")
    public Result like(@Valid @RequestBody TalkStarDTO talkStarDTO) {
        talkStarService.like(talkStarDTO);
        return ResultUtils.success();
    }

    @ApiModelProperty(value = "取消点赞", notes = "取消点赞")
    @PostMapping("/cancelLike")
    public Result cancelLike(@Valid @RequestBody TalkStarDTO talkStarDTO) {
        talkStarService.cancelLike(talkStarDTO);
        return ResultUtils.success();
    }
}
