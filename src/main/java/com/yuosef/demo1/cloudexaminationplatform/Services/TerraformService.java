package com.yuosef.demo1.cloudexaminationplatform.Services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class TerraformService {

    public String apply() throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "terraform",
                "apply",
                "-auto-approve"
        );

        pb.directory(new File("Terraform-files"));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();
        return output.toString();
    }
}
