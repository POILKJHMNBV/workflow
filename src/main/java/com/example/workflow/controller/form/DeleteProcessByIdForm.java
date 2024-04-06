package com.example.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeleteProcessByIdForm {
    @NotBlank(message = "instanceId不能为空")
    private String instanceId;

    @NotBlank(message = "type不能为空")
    private String type;

    @NotBlank(message = "reason不能为空")
    private String reason;

    private String uuid;
}
