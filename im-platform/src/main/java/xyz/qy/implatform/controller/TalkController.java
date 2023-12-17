package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITalkService;

import javax.validation.Valid;

/**
 * @description: 说说
 * @author: HouTianYun
 * @create: 2023-11-19 22:00
 **/
@Api(tags = "说说")
@RestController
@RequestMapping("/talk")
public class TalkController {
    @Autowired
    private ITalkService talkService;

    @ApiOperation(value = "新增说说", notes = "新增说说")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody TalkAddDTO talkAddDTO) {
        talkService.addTalk(talkAddDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询说说", notes = "分页查询说说")
    @GetMapping("/pageQueryTalkList")
    public Result pageQueryTalkList() {
        return ResultUtils.success(talkService.pageQueryTalkList());
    }
}
