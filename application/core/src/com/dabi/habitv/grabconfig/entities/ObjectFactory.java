package com.dabi.habitv.grabconfig.entities;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _GrabConfig_QNAME = new QName("http://www.dabi.com/habitv/grabconfig/entities", "grabConfig");

    public ObjectFactory() {
    }

    public GrabConfig createGrabConfig() {
        return new GrabConfig();
    }

    public GrabConfig.Plugins createGrabConfigPlugins() {
        return new GrabConfig.Plugins();
    }

    public Plugin createPlugin() {
        return new Plugin();
    }

    public Plugin.Categories createPluginCategories() {
        return new Plugin.Categories();
    }

    public CategoryType createCategoryType() {
        return new CategoryType();
    }

    public CategoryType.Includes createCategoryTypeIncludes() {
        return new CategoryType.Includes();
    }

    public CategoryType.Excludes createCategoryTypeExcludes() {
        return new CategoryType.Excludes();
    }

    public CategoryType.Subcategories createCategoryTypeSubcategories() {
        return new CategoryType.Subcategories();
    }

    public CategoryType.Configuration createCategoryTypeConfiguration() {
        return new CategoryType.Configuration();
    }

    public Channel createChannel() {
        return new Channel();
    }

    public Category createCategory() {
        return new Category();
    }

    public Parameter createParameter() {
        return new Parameter();
    }

    @XmlElementDecl(namespace = "http://www.dabi.com/habitv/grabconfig/entities", name = "grabConfig")
    public JAXBElement<GrabConfig> createGrabConfig(GrabConfig value) {
        return new JAXBElement<GrabConfig>(_GrabConfig_QNAME, GrabConfig.class, null, value);
    }
}