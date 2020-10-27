package com.commercetools.sync.customers.helpers;

import com.commercetools.sync.commons.helpers.BaseGraphQlRequest;

import javax.annotation.Nonnull;
import java.util.Set;

public class CustomerGraphQlRequest extends BaseGraphQlRequest<CustomerGraphQlRequest, CustomerGraphQlResult> {

        private static final String ENDPOINT = "customers";

        public CustomerGraphQlRequest(@Nonnull final Set<String> keysToSearch) {
            super(keysToSearch, ENDPOINT, CustomerGraphQlResult.class);
        }

    /**
     * Returns an instance of this class to be used in the superclass's generic methods. Please see the JavaDoc in the
     * overridden method for further details.
     *
     * @return an instance of this class.
     */
    @Override
    protected CustomerGraphQlRequest getThis() {
        return this;
    }
}
