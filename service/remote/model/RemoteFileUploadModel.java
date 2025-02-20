package org.tpl.chat.service.remote.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class RemoteFileUploadModel {
    private File file;
}
