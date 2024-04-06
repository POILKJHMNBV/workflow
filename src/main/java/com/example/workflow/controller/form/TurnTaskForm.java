package com.example.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class TurnTaskForm {

    @NotNull(message = "userId不能为空")
    @Min(value = 1, message = "userId不能小于1")
    private Integer userId;

    @NotNull(message = "assignId不能为空")
    @Min(value = 1, message = "assignId不能小于1")
    private Integer assignId;
}
