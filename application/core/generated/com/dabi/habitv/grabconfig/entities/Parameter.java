package com.dabi.habitv.grabconfig.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameter", propOrder = {
    "key",
    "value"
})
public class Parameter {

    @XmlElement(required = true)
    protected String key;
    @XmlElement(required = true)
    protected String value;

    public String getKey() {
        return key;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}