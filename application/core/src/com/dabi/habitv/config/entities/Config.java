package com.dabi.habitv.config.entities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "config", propOrder = {
    "proxy",
    "maxAttempts",
    "cmdProcessor",
    "demonTime",
    "fileNameCutSize",
    "workingDir",
    "indexDir",
    "providerPluginDir",
    "downloaderPluginDir",
    "exporterPluginDir",
    "downloadOuput",
    "downloader",
    "exporter",
    "taskDefinition"
})
@XmlRootElement(name = "config")
public class Config {

    protected List<Proxy> proxy;
    protected Integer maxAttempts;
    protected String cmdProcessor;
    protected Integer demonTime;
    protected Integer fileNameCutSize;
    @XmlElement(required = true)
    protected String workingDir;
    @XmlElement(required = true)
    protected String indexDir;
    @XmlElement(required = true)
    protected String providerPluginDir;
    @XmlElement(required = true)
    protected String downloaderPluginDir;
    @XmlElement(required = true)
    protected String exporterPluginDir;
    @XmlElement(required = true)
    protected String downloadOuput;
    @XmlElement(required = true)
    protected List<Downloader> downloader;
    protected List<Exporter> exporter;
    protected List<TaskDefinition> taskDefinition;

    public List<Proxy> getProxy() {
        if (proxy == null) {
            proxy = new ArrayList<Proxy>();
        }
        return this.proxy;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer value) {
        this.maxAttempts = value;
    }

    public String getCmdProcessor() {
        return cmdProcessor;
    }

    public void setCmdProcessor(String value) {
        this.cmdProcessor = value;
    }

    public Integer getDemonTime() {
        return demonTime;
    }

    public void setDemonTime(Integer value) {
        this.demonTime = value;
    }

    public Integer getFileNameCutSize() {
        return fileNameCutSize;
    }

    public void setFileNameCutSize(Integer value) {
        this.fileNameCutSize = value;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String value) {
        this.workingDir = value;
    }

    public String getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(String value) {
        this.indexDir = value;
    }

    public String getProviderPluginDir() {
        return providerPluginDir;
    }

    public void setProviderPluginDir(String value) {
        this.providerPluginDir = value;
    }

    public String getDownloaderPluginDir() {
        return downloaderPluginDir;
    }

    public void setDownloaderPluginDir(String value) {
        this.downloaderPluginDir = value;
    }

    public String getExporterPluginDir() {
        return exporterPluginDir;
    }

    public void setExporterPluginDir(String value) {
        this.exporterPluginDir = value;
    }

    public String getDownloadOuput() {
        return downloadOuput;
    }

    public void setDownloadOuput(String value) {
        this.downloadOuput = value;
    }

    public List<Downloader> getDownloader() {
        if (downloader == null) {
            downloader = new ArrayList<Downloader>();
        }
        return this.downloader;
    }

    public List<Exporter> getExporter() {
        if (exporter == null) {
            exporter = new ArrayList<Exporter>();
        }
        return this.exporter;
    }

    public List<TaskDefinition> getTaskDefinition() {
        if (taskDefinition == null) {
            taskDefinition = new ArrayList<TaskDefinition>();
        }
        return this.taskDefinition;
    }
}