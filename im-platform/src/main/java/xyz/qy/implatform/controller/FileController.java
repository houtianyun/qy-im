package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.enums.FilePathEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.strategy.context.UploadStrategyContext;
import xyz.qy.implatform.vo.UploadImageVO;

import javax.annotation.Resource;

@Slf4j
@RestController
@Api(tags = "文件上传")
public class FileController {

    @Resource
    private UploadStrategyContext uploadStrategyContext;

    @ApiOperation(value = "上传图片", notes = "上传图片,上传后返回原图和缩略图的url")
    @PostMapping("/image/upload")
    public Result<UploadImageVO> uploadImage(MultipartFile file) {
        return ResultUtils.success(uploadStrategyContext.executeUploadImageStrategy(file, FilePathEnum.IMAGE.getPath()));
    }

    @ApiOperation(value = "上传文件", notes = "上传文件，上传后返回文件url")
    @PostMapping("/file/upload")
    public Result<String> uploadFile(MultipartFile file) {
        return ResultUtils.success(uploadStrategyContext.executeUploadFileStrategy(file, FilePathEnum.FILE.getPath()), "");
    }

}
