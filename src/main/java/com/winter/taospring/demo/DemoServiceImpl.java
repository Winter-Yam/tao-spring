package com.winter.taospring.demo;

import com.winter.taospring.annotation.Service;

@Service
public class DemoServiceImpl implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is " + name + ",from service.";
    }
}
