package com.icourt.entity;

import lombok.Data;

import java.util.Map;

@Data
public class GetProxyVo {
    private String url;
    private Map<String, String> hearderMap;
}
