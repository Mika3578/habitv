package com.dabi.habitv.grabconfig.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "plugin", propOrder = {
    "name",
    "categories",
    "status",
    "deleted"
})
public class Plugin {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected Categories categories;
    protected String status;
    protected Boolean deleted;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories value) {
        this.categories = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "category"
    })
    public static class Categories {

        protected java.util.List<CategoryType> category;

        public java.util.List<CategoryType> getCategory() {
            if (category == null) {
                category = new java.util.ArrayList<CategoryType>();
            }
            return this.category;
        }
    }
}