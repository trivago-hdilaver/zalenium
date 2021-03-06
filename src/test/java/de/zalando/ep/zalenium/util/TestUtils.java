package de.zalando.ep.zalenium.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import de.zalando.ep.zalenium.container.DockerContainerClient;
import de.zalando.ep.zalenium.proxy.DockerSeleniumStarterRemoteProxy;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import com.beust.jcommander.JCommander;
import org.openqa.grid.web.servlet.handler.RequestType;
import org.openqa.grid.web.servlet.handler.WebDriverRequest;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestUtils {

    public static RegistrationRequest getRegistrationRequestForTesting(final int port, String proxyClass) {
        GridNodeConfiguration nodeConfiguration = new GridNodeConfiguration();
        new JCommander(nodeConfiguration, "-role", "wd", "-hubHost", "localhost", "-hubPort", "4444",
                "-host","localhost", "-port", String.valueOf(port), "-proxy", proxyClass, "-registerCycle", "5000",
                "-maxSession", "5");

        return RegistrationRequest.build(nodeConfiguration);
    }

    public static WebDriverRequest getMockedWebDriverRequestStartSession() {
        WebDriverRequest request = mock(WebDriverRequest.class);
        when(request.getRequestURI()).thenReturn("session");
        when(request.getServletPath()).thenReturn("session");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestType()).thenReturn(RequestType.START_SESSION);
        JsonObject jsonObject = new JsonObject();
        JsonObject desiredCapabilities = new JsonObject();
        desiredCapabilities.addProperty(CapabilityType.BROWSER_NAME, BrowserType.IE);
        desiredCapabilities.addProperty(CapabilityType.PLATFORM, Platform.WIN8.name());
        jsonObject.add("desiredCapabilities", desiredCapabilities);
        when(request.getBody()).thenReturn(jsonObject.toString());

        Enumeration<String> strings = Collections.emptyEnumeration();
        when(request.getHeaderNames()).thenReturn(strings);

        return request;
    }

    public static List<DesiredCapabilities> getDockerSeleniumCapabilitiesForTesting() {
        String screenResolution = String.format("%sx%s", DockerSeleniumStarterRemoteProxy.getConfiguredScreenWidth(),
                DockerSeleniumStarterRemoteProxy.getConfiguredScreenHeight());
        List<DesiredCapabilities> dsCapabilities = new ArrayList<>();
        DesiredCapabilities firefoxCapabilities = new DesiredCapabilities();
        firefoxCapabilities.setBrowserName(BrowserType.FIREFOX);
        firefoxCapabilities.setPlatform(Platform.LINUX);
        firefoxCapabilities.setCapability(RegistrationRequest.MAX_INSTANCES, 1);
        firefoxCapabilities.setCapability("screenResolution", screenResolution);
        firefoxCapabilities.setCapability("tz", DockerSeleniumStarterRemoteProxy.getConfiguredTimeZone());
        dsCapabilities.add(firefoxCapabilities);
        DesiredCapabilities chromeCapabilities = new DesiredCapabilities();
        chromeCapabilities.setBrowserName(BrowserType.CHROME);
        chromeCapabilities.setPlatform(Platform.LINUX);
        chromeCapabilities.setCapability(RegistrationRequest.MAX_INSTANCES, 1);
        chromeCapabilities.setCapability("screenResolution", screenResolution);
        chromeCapabilities.setCapability("tz", DockerSeleniumStarterRemoteProxy.getConfiguredTimeZone());
        dsCapabilities.add(chromeCapabilities);
        return dsCapabilities;
    }

    @SuppressWarnings("ConstantConditions")
    public static JsonElement getTestInformationSample(String fileName) throws IOException {
        URL testInfoLocation = TestUtils.class.getClassLoader().getResource(fileName);
        File testInformationFile = new File(testInfoLocation.getPath());
        String testInformation = FileUtils.readFileToString(testInformationFile, StandardCharsets.UTF_8);
        return new JsonParser().parse(testInformation);
    }

    public static ServletOutputStream getMockedServletOutputStream() {
        return new ServletOutputStream() {
            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public boolean isReady() {
                System.out.println("isReady");
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                System.out.println("setWriteListener");
            }

            @Override
            public void write(int b) throws IOException {
                this.stringBuilder.append((char) b );
            }

            public String toString() {
                return stringBuilder.toString();
            }
        };
    }

    public static CommonProxyUtilities mockCommonProxyUtilitiesForDashboardTesting(TemporaryFolder temporaryFolder) {
        CommonProxyUtilities commonProxyUtilities = mock(CommonProxyUtilities.class);
        when(commonProxyUtilities.currentLocalPath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(commonProxyUtilities.getShortDateAndTime()).thenCallRealMethod();
        return commonProxyUtilities;
    }

    public static void ensureRequiredInputFilesExist(TemporaryFolder temporaryFolder) throws IOException {
        temporaryFolder.newFile("list_template.html");
        temporaryFolder.newFile("dashboard_template.html");
        temporaryFolder.newFile("zalando.ico");
        temporaryFolder.newFolder("css");
        temporaryFolder.newFolder("js");
    }

    public static void ensureRequiredFilesExistForCleanup(TemporaryFolder temporaryFolder) throws IOException {
        ensureRequiredInputFilesExist(temporaryFolder);
        temporaryFolder.newFolder("videos");
        temporaryFolder.newFolder("videos", "logs");
        temporaryFolder.newFile("videos/list.html");
        temporaryFolder.newFile("videos/executedTestsInfo.json");
        temporaryFolder.newFile("videos/dashboard.html");
    }

    @SuppressWarnings("ConstantConditions")
    public static DockerContainerClient getMockedDockerContainerClient() {
        DockerClient dockerClient = mock(DockerClient.class);
        ExecCreation execCreation = mock(ExecCreation.class);
        LogStream logStream = mock(LogStream.class);
        when(logStream.readFully()).thenReturn("ANY_STRING");
        when(execCreation.id()).thenReturn("ANY_ID");

        ContainerCreation containerCreation = mock(ContainerCreation.class);
        when(containerCreation.id()).thenReturn("ANY_CONTAINER_ID");

        ImageInfo imageInfo = mock(ImageInfo.class);
        ContainerConfig containerConfig = mock(ContainerConfig.class);
        ContainerInfo containerInfo = mock(ContainerInfo.class);
        ContainerMount containerMount = mock(ContainerMount.class);
        when(containerMount.destination()).thenReturn("/tmp/mounted");
        when(containerMount.source()).thenReturn("/tmp/mounted");
        when(containerInfo.mounts()).thenReturn(ImmutableList.of(containerMount));

        try {
            URL logsLocation = TestUtils.class.getClassLoader().getResource("logs.tar");
            URL videosLocation = TestUtils.class.getClassLoader().getResource("videos.tar");
            File logsFile = new File(logsLocation.getPath());
            File videosFile = new File(videosLocation.getPath());
            when(dockerClient.archiveContainer(null, "/var/log/cont/")).thenReturn(new FileInputStream(logsFile));
            when(dockerClient.archiveContainer(null, "/videos/")).thenReturn(new FileInputStream(videosFile));

            String[] startVideo = {"bash", "-c", "start-video"};
            String[] stopVideo = {"bash", "-c", "stop-video"};
            String[] transferLogs = {"bash", "-c", "transfer-logs.sh"};
            when(dockerClient.execCreate(null, startVideo, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr())).thenReturn(execCreation);
            when(dockerClient.execCreate(null, stopVideo, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr())).thenReturn(execCreation);
            when(dockerClient.execCreate(null, transferLogs, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr())).thenReturn(execCreation);

            when(dockerClient.execStart(anyString())).thenReturn(logStream);
            doNothing().when(dockerClient).stopContainer(anyString(), anyInt());

            when(dockerClient.createContainer(any(ContainerConfig.class), anyString())).thenReturn(containerCreation);

            when(containerConfig.labels()).thenReturn(ImmutableMap.of("selenium_firefox_version", "52",
                    "selenium_chrome_version", "58"));
            when(imageInfo.config()).thenReturn(containerConfig);
            when(dockerClient.inspectContainer(null)).thenReturn(containerInfo);

            when(dockerClient.inspectImage(anyString())).thenReturn(imageInfo);
        } catch (DockerException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        DockerContainerClient.setContainerClient(dockerClient);
        return new DockerContainerClient();
    }

}
