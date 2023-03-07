package com.chainmaker.jobservice.api.config;

import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.service.JobParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author gaokang
 * @date 2022-11-21 15:48
 * @description:
 * @version:
 */
@Configuration
public class CatalogConf {
    @Autowired
    CatalogConfig catalogConfig;
    @Autowired
    JobParserService jobParserService;

    @PostConstruct
    public void init() {
        jobParserService.setCatalogConfig(catalogConfig);
    }
}
