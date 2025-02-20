package org.tpl.chat.service.remote;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.service.model.FfmpegOutPut;
import org.tpl.chat.service.model.FileUploadModel;
import org.tpl.chat.service.remote.model.MultiPartFileUploadModel;
import org.tpl.chat.service.remote.model.RemoteFileUploadModel;
import org.tpl.chat.service.remote.usermanagement.UsermanagementApiAdapter;
import org.tpl.chat.service.util.FfmpegUtil;
import org.tpl.util.common.service.exception.UnauthorizedException;
import org.tpl.util.common.service.remote.FeignGeneralException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.nio.file.FileSystems;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class FmAdapter {

  private final FmApiClient fmApiClient;
  private final UsermanagementApiAdapter fsAuthAdapter;

  @SneakyThrows
  @Retryable(retryFor = {UnauthorizedException.class},
          maxAttempts = 5,
          backoff = @Backoff(delay = 100))
  public FileUploadModel upload(MessageType type, MultiPartFileUploadModel fileUploadModel){
    String fileUrl;
    String previewUrl = null;
    String token = fsAuthAdapter.getToken();
    FfmpegOutPut ffmpegOutputModel = null;
    try {
      if (type.equals(MessageType.VIDEO)){
        ffmpegOutputModel = FfmpegUtil.compress(fileUploadModel.getFile());
        fileUrl = fmApiClient.upload("Bearer " + token, new RemoteFileUploadModel(ffmpegOutputModel.getFile()));
        previewUrl = fmApiClient.upload("Bearer " + token, new RemoteFileUploadModel(ffmpegOutputModel.getPreviewFile()));
        Files.delete(FileSystems.getDefault().getPath(ffmpegOutputModel.getFile().getAbsolutePath()));
        Files.delete(FileSystems.getDefault().getPath(ffmpegOutputModel.getPreviewFile().getAbsolutePath()));
      }else fileUrl = fmApiClient.uploadByMultipartFile("Bearer " + token, fileUploadModel);
    }catch (FeignGeneralException e){
      if (e.getStatus() == 401){
        fsAuthAdapter.refreshToken();
        throw new UnauthorizedException();
      }
      throw e;
    }
    int duration = ffmpegOutputModel != null ? ffmpegOutputModel.getDuration() : 0;
    return new FileUploadModel(fileUrl, previewUrl, duration);
  }

}
