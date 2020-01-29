package com.optimove.sdk.optimove_sdk.optitrack;

public class CustomDimension {
    private int id;
    private String value;

    public CustomDimension(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof CustomDimension)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        CustomDimension c = (CustomDimension) o;

        // Compare the data members and return accordingly
        return id == c.getId()
                && value.equals(c.value);
    }
}
