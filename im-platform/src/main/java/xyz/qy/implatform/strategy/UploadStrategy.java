package xyz.qy.implatform.strategy;

import xyz.qy.implatform.vo.UploadImageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传图片策略
 */
public interface UploadStrategy {
    /**
     * 上传图片
     *
     * @param file 文件
     * @param path 上传路径
     * @return UploadImageVO 图片路径VO
     */
    UploadImageVO uploadImageCommon(MultipartFile file, String path);

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 上传路径
     * @return 文件链接
     */
    String uploadFileCommon(MultipartFile file, String path);
}
