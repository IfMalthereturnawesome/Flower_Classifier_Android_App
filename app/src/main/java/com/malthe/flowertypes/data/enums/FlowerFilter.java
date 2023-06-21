package com.malthe.flowertypes.data.enums;

public enum FlowerFilter {
    MY_PLANTS,

    NOT_MY_PLANTS;

    public static FlowerFilter getFilterForIndex(int index) {
        switch (index) {
            case 0:
                return MY_PLANTS;
            case 1:
                return NOT_MY_PLANTS;
            default:
                throw new IllegalArgumentException("Invalid index for FlowerFilter");
        }
    }
}
