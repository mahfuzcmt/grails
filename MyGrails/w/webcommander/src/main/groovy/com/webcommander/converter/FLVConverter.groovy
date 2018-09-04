package com.webcommander.converter

/**
 * Created By: sanjoy
 * Created: 19/09/13 : 11:03 AM
 */
class FLVConverter {
    private String ffmpegApp;

    public FLVConverter(String ffmpegApp) {
        this.ffmpegApp = ffmpegApp;
    }

    public void convert(String filenameIn, String filenameOut, int width, int height) throws IOException, InterruptedException {
        convert(filenameIn, filenameOut, width, height, -1);
    }

    public int convert(String filenameIn, String filenameOut, int width, int height, int quality)
    throws IOException, InterruptedException {
        ProcessBuilder processBuilder;
        if (quality > -1) {
            processBuilder = new ProcessBuilder(ffmpegApp, "-i", filenameIn, "-ar", "44100",
                    "-s", width + "*" + height, "-qscale", quality + "", filenameOut);
        } else {
            processBuilder = new ProcessBuilder(ffmpegApp, "-i", filenameIn, "-ar", "44100",
                    "-s", width + "*" + height, filenameOut);
        }
        Process process = processBuilder.start();
        InputStream stderr = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null)
        {
        }
        return process.waitFor();
    }

    public void makeThumb(String videoFilename, String thumbFilename, int width, int height, int hour, int min, float sec)
    throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegApp, "-y", "-i", videoFilename, "-vframes", "1",
                "-ss", hour + ":" + min + ":" + sec, "-f", "mjpeg", "-s", width + "*" + height, "-an", thumbFilename);
        Process process = processBuilder.start();
        InputStream stderr = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) ;
        process.waitFor();
    }
}
