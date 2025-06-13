package com.dabi.habitv.grabconfig.entities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "category", propOrder = {
    "id",
    "name",
    "toDownload",
    "include",
    "exclude",
    "category",
    "extension",
    "status",
    "parameter"
})
public class Category {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String name;
    protected Boolean toDownload;
    protected List<String> include;
    protected List<String> exclude;
    protected List<Category> category;
    @XmlElement(required = true)
    protected String extension;
    protected Integer status;
    protected List<Parameter> parameter;

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

    public Boolean isToDownload() {
        return toDownload;
    }

    public void setToDownload(Boolean value) {
        this.toDownload = value;
    }

    public List<String> getInclude() {
        if (include == null) {
            include = new ArrayList<String>();
        }
        return this.include;
    }

    public List<String> getExclude() {
        if (exclude == null) {
            exclude = new ArrayList<String>();
        }
        return this.exclude;
    }

    public List<Category> getCategory() {
        if (category == null) {
            category = new ArrayList<Category>();
        }
        return this.category;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String value) {
        this.extension = value;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer value) {
        this.status = value;
    }

    public List<Parameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<Parameter>();
        }
        return this.parameter;
    }
}