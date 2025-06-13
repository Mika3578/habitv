package com.dabi.habitv.grabconfig.entities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "plugins",
    "channel"
})
@XmlRootElement(name = "grabConfig")
public class GrabConfig {

    protected Plugins plugins;
    protected List<Channel> channel;

    public Plugins getPlugins() {
        return plugins;
    }

    public void setPlugins(Plugins value) {
        this.plugins = value;
    }

    public List<Channel> getChannel() {
        if (channel == null) {
            channel = new ArrayList<Channel>();
        }
        return this.channel;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "plugin"
    })
    public static class Plugins {

        protected List<Plugin> plugin;

        public List<Plugin> getPlugin() {
            if (plugin == null) {
                plugin = new ArrayList<Plugin>();
            }
            return this.plugin;
        }
    }
}