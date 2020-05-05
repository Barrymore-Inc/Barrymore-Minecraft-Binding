package me.uquark.barrymoreminecraftbinding.googlecloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Auth {
    private static String accessToken = null;

    private static void retrieveAccessToken() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] args = {"/usr/bin/gcloud", "auth", "application-default", "print-access-token"};
        Process process = rt.exec(args);
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        process.waitFor();

        StringBuilder error = new StringBuilder();
        String line;
        while ((line = stderr.readLine()) != null) {
            error.append(line).append("\n");
        }
        String out = stdout.readLine();

        if (process.exitValue() != 0)
            throw new RuntimeException("Exit code: " + process.exitValue() + "; stderr: " + error);

        accessToken = out;
    }

    public static String getAccessToken() {
        if (accessToken == null)
            try {
                retrieveAccessToken();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        return accessToken;
    }
}
