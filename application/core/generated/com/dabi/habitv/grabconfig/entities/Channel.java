package com.dabi.habitv.grabconfig.entities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "channel", propOrder = {
    "name",
    "category",
    "status"
})
public class Channel {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected List<Category> category;
    protected String status;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public List<Category> getCategory() {
        if (category == null) {
            category = new ArrayList<Category>();
        }
        return this.category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }
}