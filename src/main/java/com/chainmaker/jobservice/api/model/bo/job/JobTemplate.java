package com.chainmaker.jobservice.api.model.bo.job;

import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import lombok.Data;

import java.util.List;

@Data
public class JobTemplate {
    private Job job;
    private List<Task> tasks;
    private List<Service> services;
}
