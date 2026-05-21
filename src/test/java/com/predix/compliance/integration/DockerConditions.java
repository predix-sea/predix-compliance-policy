package com.predix.compliance.integration;

import org.testcontainers.DockerClientFactory;

public final class DockerConditions {

    private DockerConditions() {}

    public static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception ex) {
            return false;
        }
    }
}
