package org.easysearch.request;


import org.easysearch.CriteriaPath;

public class CriteriaPathDetail implements CriteriaPath {

    private final String attribute;
    private final String path;
    private final Class<?> attributeType;

    public CriteriaPathDetail(String attribute, String path, Class<?> attributeType) {
        this.attribute = attribute;
        this.path = path;
        this.attributeType = attributeType;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public Class<?> getAttributeType() {
        return attributeType;
    }

    @Override
    public String getPath() {
        return path;
    }
}
