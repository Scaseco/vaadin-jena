package org.aksw.jenax.treequery2;

public interface HasSlice {
    Long offset();
    HasSlice offset(Long offset);

    Long limit();
    HasSlice limit(Long limit);
}
