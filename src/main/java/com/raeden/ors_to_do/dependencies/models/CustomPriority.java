package com.raeden.ors_to_do.dependencies.models;

import java.io.Serializable;

public class CustomPriority implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String colorHex;

    public CustomPriority(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    public String getName() { return name; }
    public String getColorHex() { return colorHex; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomPriority that = (CustomPriority) obj;
        return name.equals(that.name);
    }
}