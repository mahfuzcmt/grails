package com.webcommander.util.JavaShell

class ShellRunner {
    public static Boolean exeCuteCommand(String command) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            //builder.directory(new File(System.getProperty("user.home")));
            Process process = builder.start();
            process.waitFor();
            println("Executed Command: " + command)
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line=buf.readLine())!=null) {
                println(line);
            }
        }  catch (Exception e) {
            e.printStackTrace();
            return false
        }
        return true
    }
}
