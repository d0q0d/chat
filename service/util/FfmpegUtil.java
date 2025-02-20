package org.tpl.chat.service.util;

import lombok.SneakyThrows;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.tpl.chat.service.model.FfmpegOutPut;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.UUID;

public class FfmpegUtil {

  @SneakyThrows
  public static FfmpegOutPut compress(MultipartFile multipartFile) {
    File file = convertMultiPartToFile(multipartFile);
    return compressVideo(file);
  }

  @SneakyThrows
  public static FfmpegOutPut compressVideo(File file) {
    var ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
    var ffprobe = new FFprobe("/usr/bin/ffprobe");
    var wh = getWidthAndHeight(file);
    var width = Integer.parseInt(wh[0]);
    var height = Integer.parseInt(wh[1]);
    if (Integer.max(width, height) <= 720)
      return new FfmpegOutPut(file, getFirstFrameImage(file, ffmpeg, ffprobe) ,getVideoLength(file, ffprobe));
    else return convertAndSetFileSize(file, width, height, ffmpeg, ffprobe);
  }

  @SneakyThrows
  public static FfmpegOutPut convertAndSetFileSize(
      File file, int width, int height, FFmpeg ffmpeg, FFprobe ffprobe) {
    int max = Integer.max(width, height);
    int margin = max - 720;
    float reducePercentage = (float) margin / max;
    int widthReduction = (int) (width * reducePercentage);
    int heightReduction = (int) (height * reducePercentage);
    int finalWidth = (width - widthReduction);
    int finalHeight = (height - heightReduction);
    if (finalHeight % 2 != 0) finalHeight++;
    if (finalWidth % 2 != 0) finalWidth++;
    File outputTempFile = null;
    try {
      outputTempFile = File.createTempFile(UUID.randomUUID().toString(), ".mp4");
    } catch (IOException e) {
      e.printStackTrace();
    }
    var builder =
        new FFmpegBuilder()
            .setInput(file.getAbsolutePath())
            .overrideOutputFiles(true)
            .addOutput(outputTempFile.getAbsolutePath())
            .setFormat("mp4")
            .setVideoCodec("libx264")
            .setVideoResolution(finalWidth, finalHeight)
            .done();
    var executor = new FFmpegExecutor(ffmpeg, ffprobe);
    executor.createJob(builder).run();
    var videoLength = getVideoLength(outputTempFile, ffprobe);
    var videoPreview = getFirstFrameImage(outputTempFile, ffmpeg, ffprobe);
    Files.delete(FileSystems.getDefault().getPath(file.getAbsolutePath()));
    return new FfmpegOutPut(outputTempFile, videoPreview, videoLength);
  }

  public static String[] getWidthAndHeight(File file) throws IOException {
    var process =
        new ProcessBuilder(
                "ffprobe",
                "-v",
                "error",
                "-select_streams",
                "v:0",
                "-show_entries",
                "stream=width,height",
                "-of",
                "csv=s=x:p=0",
                "-i",
                file.getAbsolutePath())
            .redirectErrorStream(true)
            .start();
    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    var output = reader.readLine();
    reader.close();
    return output.split("x");
  }

  @SneakyThrows
  private static int getVideoLength(File file, FFprobe ffprobe) {
    FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
    FFmpegFormat format = probeResult.getFormat();
    return (int) format.duration;
  }

  @SneakyThrows
  private static File getFirstFrameImage(File file, FFmpeg ffmpeg, FFprobe ffprobe) {
    File outputTempFile =  File.createTempFile(UUID.randomUUID().toString(), ".jpg");
    FFmpegBuilder builder = new FFmpegBuilder()
            .setInput(file.getAbsolutePath())
            .overrideOutputFiles(true)
            .addOutput(outputTempFile.getAbsolutePath())
            .setFrames(1)
            .done();
    var executor = new FFmpegExecutor(ffmpeg, ffprobe);
    executor.createJob(builder).run();
    return outputTempFile;
  }

  public static File convertMultiPartToFile(MultipartFile file) throws IOException {
    var convFile =
        File.createTempFile(UUID.randomUUID().toString(), "-" + file.getOriginalFilename());
    var fileOutputStream = new FileOutputStream(convFile);
    fileOutputStream.write(file.getBytes());
    fileOutputStream.close();
    return convFile;
  }
}
