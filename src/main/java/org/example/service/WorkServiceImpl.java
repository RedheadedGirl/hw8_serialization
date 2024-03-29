package org.example.service;

import org.example.interfaces.WorkService;

import java.util.Date;
import java.util.List;

public class WorkServiceImpl implements WorkService {
    @Override
    public List<String> run(String item, double value, Date date) {
        return List.of(item, String.valueOf(value), date.toString());
    }

    @Override
    public List<String> work(String item) {
        return List.of(item);
    }

    @Override
    public double multiplyOnTwo(int number, Double multiplier) {
        return number * 2;
    }
}
