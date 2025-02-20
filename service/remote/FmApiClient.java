package org.tpl.chat.service.remote;

import org.tpl.chat.service.remote.model.RemoteFileUploadModel;
import org.tpl.chat.service.remote.model.MultiPartFileUploadModel;
import org.tpl.util.common.service.remote.DefaultFeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${fm.service.name}", url = "${fm.baseUrl}", configuration = DefaultFeignErrorDecoder.class)
public interface FmApiClient {
  @PostMapping(path = "${fm.upload.url}", consumes = "multipart/form-data")
  String upload(
      @RequestHeader("Authorization") String token,
      @ModelAttribute RemoteFileUploadModel fileUploadModel);

  @PostMapping(path = "${fm.upload.url}", consumes = "multipart/form-data")
  String uploadByMultipartFile(
          @RequestHeader("Authorization") String token,
          @ModelAttribute MultiPartFileUploadModel fileUploadModel);
}
