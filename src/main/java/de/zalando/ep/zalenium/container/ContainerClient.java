package de.zalando.ep.zalenium.container;

import java.io.InputStream;
import java.util.List;

public interface ContainerClient {

    void setNodeId(String nodeId);

    String getContainerId(String containerName);

    InputStream copyFiles(String containerId, String folderName);

    void stopContainer(String containerId);

    void executeCommand(String containerId, String[] command);

    String getLatestDownloadedImage(String imageName);

    String getLabelValue(String image, String label);

    int getRunningContainers(String image);

    void createContainer(String zaleniumContainerName, String containerName, String image, List<String> envVars);
}
