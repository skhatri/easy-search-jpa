package org.easysearch;

/**
 * An implementation of this will provide with extra information about search attribute to be included in query.
 */
public interface CriteriaPath {
    String getAttribute();

    Class<?> getAttributeType();

    String getPath();
}
