package org.example.interfaces;

import java.util.Date;
import java.util.List;

import static org.example.enums.StoreType.FILE;
import static org.example.enums.StoreType.IN_MEMORY;

public interface WorkService {
    @Cache(cacheType = FILE, prefix = "prefix", zip = true, ignoreFields = {String.class, Double.class})
    List<String> run(String item, double value, Date date);

    @Cache(cacheType = IN_MEMORY, enoughStoreAmount = 2)
    List<String> work(String item);

    @Cache(cacheType = FILE, ignoreFields = {Double.class})
    double multiplyOnTwo(int number, Double multiplier);
}

