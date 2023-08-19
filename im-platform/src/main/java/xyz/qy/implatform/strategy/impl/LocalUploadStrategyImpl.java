package xyz.qy.implatform.strategy.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.vo.UploadImageVO;

import java.io.File;
import java.io.IOException;

/**
 * 本地上传策略
 *
 * @author Polaris
 * @since 2023-08-19
 */
@Service("localUploadStrategyImpl")
public class LocalUploadStrategyImpl extends AbstractUploadStrategyImpl {

    /**
     * 本地路径
     */
    @Value("${upload.local.path}")
    private String localPath;

    /**
     * 访问url
     */
    @Value("${upload.local.url}")
    private String localUrl;

    @Override
    public Boolean exists(String filePath) {
        return new File(localPath + filePath).exists();
    }

    @Override
    public UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException {
        return null;
    }

    @Override
    public String uploadFile(String path, String fileName, MultipartFile file) throws IOException {
        return null;
    }
}
