package xyz.qy.implatform.strategy.impl;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.exception.GlobalException;
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
        // 判断目录是否存在
        createDirectory(localPath + path);
        UploadImageVO uploadImageVO = new UploadImageVO();
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            String url = localUrl + path + fileName;
            uploadImageVO.setOriginUrl(url);
            uploadImageVO.setThumbUrl(url);
        }
        return uploadImageVO;
    }

    @Override
    public String uploadFile(String path, String fileName, MultipartFile file) throws IOException {
        // 判断目录是否存在
        String url = null;
        createDirectory(localPath + path);
        UploadImageVO uploadImageVO = new UploadImageVO();
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            url = localUrl + path + fileName;
        }
        return url;
    }

    @Override
    public UploadImageVO getImageInfo(String path, String fileName) {
        UploadImageVO uploadImageVO = new UploadImageVO();
        String url = localUrl + path + fileName;
        uploadImageVO.setOriginUrl(url);
        uploadImageVO.setThumbUrl(url);
        return uploadImageVO;
    }

    @Override
    public String getFileUrl(String path, String fileName) {
        return localUrl + path + fileName;
    }

    private void createDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new GlobalException("创建目录失败");
            }
        }
    }
}
