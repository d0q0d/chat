package org.tpl.chat.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class FfmpegOutPut {
    private File file;
    private File previewFile;
    private int duration;
}
