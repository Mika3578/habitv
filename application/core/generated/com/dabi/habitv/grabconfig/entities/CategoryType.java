package com.dabi.habitv.grabconfig.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "categoryType", propOrder = {
    "id",
    "name",
    "download",
    "downloadable",
    "template",
    "includes",
    "excludes",
    "subcategories",
    "extension",
    "status",
    "configuration",
    "deleted"
})
public class CategoryType {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String name;
    protected Boolean download;
    protected Boolean downloadable;
    protected Boolean template;
    protected Includes includes;
    protected Excludes excludes;
    protected Subcategories subcategories;
    protected String extension;
    protected String status;
    protected Configuration configuration;
    protected Boolean deleted;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Boolean isDownload() {
        return download;
    }

    public void setDownload(Boolean value) {
        this.download = value;
    }

    public Boolean isDownloadable() {
        return downloadable;
    }

    public void setDownloadable(Boolean value) {
        this.downloadable = value;
    }

    public Boolean isTemplate() {
        return template;
    }

    public void setTemplate(Boolean value) {
        this.template = value;
    }

    public Includes getIncludes() {
        return includes;
    }

    public void setIncludes(Includes value) {
        this.includes = value;
    }

    public Excludes getExcludes() {
        return excludes;
    }

    public void setExcludes(Excludes value) {
        this.excludes = value;
    }

    public Subcategories getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(Subcategories value) {
        this.subcategories = value;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String value) {
        this.extension = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration value) {
        this.configuration = value;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "include"
    })
    public static class Includes {

        protected java.util.List<String> include;

        public java.util.List<String> getInclude() {
            if (include == null) {
                include = new java.util.ArrayList<String>();
            }
            return this.include;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "exclude"
    })
    public static class Excludes {

        protected java.util.List<String> exclude;

        public java.util.List<String> getExclude() {
            if (exclude == null) {
                exclude = new java.util.ArrayList<String>();
            }
            return this.exclude;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "category"
    })
    public static class Subcategories {

        protected java.util.List<CategoryType> category;

        public java.util.List<CategoryType> getCategory() {
            if (category == null) {
                category = new java.util.ArrayList<CategoryType>();
            }
            return this.category;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Configuration {

        @XmlElement(name = "any")
        protected java.util.List<Object> any;

        public java.util.List<Object> getAny() {
            if (any == null) {
                any = new java.util.ArrayList<Object>();
            }
            return this.any;
        }
    }
}