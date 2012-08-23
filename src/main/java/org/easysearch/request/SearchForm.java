package org.easysearch.request;

import java.util.ArrayList;
import java.util.List;

import org.easysearch.CriteriaPath;
import org.easysearch.SearchCriteria;

/**
 * An implementation of SearchCriteria
 */
public class SearchForm implements SearchCriteria {

    private String name;

    private String postcode;

    @Override
    public List<CriteriaPath> getPaths() {
        List<CriteriaPath> paths = new ArrayList<CriteriaPath>();
        paths.add(new CriteriaPathDetail("name", "user.name", String.class));
        paths.add(new CriteriaPathDetail("postcode", "company.postCode", String.class));
        return paths;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
