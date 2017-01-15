package com.tngtech.qb;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Customer {

    private final int id;
    private final String name;

    public Customer(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Customer customer = (Customer) o;
        return id == customer.id &&
               Objects.equals(name, customer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("name", name)
                          .toString();
    }
}
