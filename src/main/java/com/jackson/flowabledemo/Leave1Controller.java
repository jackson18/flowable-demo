package com.jackson.flowabledemo;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DiagramLayout;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ========================================================
 * 日 期：2020/12/6 下午5:47
 * 作 者：jiabinqi
 * 版 本：1.0.0
 * 类说明：
 * ========================================================
 * 修订日期     修订人    描述
 */
@RestController
@RequestMapping("/test1")
public class Leave1Controller {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    /**
     * 流程部署时，涉及表：
     * ACT_RE_DEPLOYMENT
     * ACT_RE_PROCDEF
     * ACT_GE_BYTEARRAY
     */
    @RequestMapping("/deploy")
    public String deploy() {
        // 方法1，classPath方式部署
//        Deployment deployment = repositoryService.createDeployment()
//                .addClasspathResource("processes/leave.bpmn20.xml")
//                .category("leave")
//                .key("leave")
//                .name("这是一个请假流程")
//                .deploy();
//        return deployment.toString();

        // 方法2，流方式部署
        InputStream inputStream = Leave1Controller.class.getClassLoader()
                .getResourceAsStream("processes/leave.bpmn20.xml");
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream("leave", inputStream)
                .category("leave")
                .key("leave")
                .name("这是一个请假流程")
                .deploy();
        return deployment.toString();

        // 方法3，手工构造bpmnModel并部署

    }

    /**
     * 启动流程
     */
    @RequestMapping("/startProcess")
    public String startProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave");
        return "processInstanceId: " + processInstance.getProcessInstanceId();
    }

    /**
     * 根据任务执行人查询任务
     */
    @RequestMapping("/getTaskByAssignee")
    public String getTaskByAssignee(String assignee) {
        Task task = taskService.createTaskQuery().taskAssignee(assignee).singleResult();
        return task.toString();
    }

    /**
     * 完成任务
     */
    @RequestMapping("/completeTask")
    public boolean completeTask(String taskId) {
        taskService.complete(taskId);
        return true;
    }

    /**
     * 完成任务，带流程变更
     */
    @RequestMapping("/completeTaskWithVar")
    public boolean completeTaskWithVar() {
        String taskId = "565e473e-3d96-11eb-969d-9e52f57ce458";
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("请假原因", "出去约会");
        varMap.put("请假天数", 3);
        taskService.complete(taskId, varMap);
        return true;
    }

    /**
     * 查询历史任务
     */
    @RequestMapping("/getHistoryTaskByAssignee")
    public String getHistoryTaskByAssignee(String assignee) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().finished().taskAssignee(assignee).list();
        StringBuilder sb = new StringBuilder();
        for (HistoricTaskInstance taskInstance : list) {
            sb.append(taskInstance.getId()).append(" ").append(taskInstance.getName()).append("</br>");
        }
        return sb.toString();
    }

    /**
     * 获取流程元素坐标信息
     */
    @RequestMapping("/getProcessElementLayout")
    public String getProcessElementLayout() {
        String processDefineId = "leave:3:6bcad758-37d1-11eb-b334-d2baa7ac3c3c";
        DiagramLayout diagramLayout = repositoryService.getProcessDiagramLayout(processDefineId);
        return diagramLayout.getElements().toString();
    }

    /**
     * 获取BpmnModel
     */
    @RequestMapping("/getBpmnModel")
    public String getBpmnModel() {
        String processDefineId = "leave:3:6bcad758-37d1-11eb-b334-d2baa7ac3c3c";
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefineId);
        Process process = bpmnModel.getProcesses().get(0);
        FlowElement flowElement = process.getFlowElement("startEvent1");
        return flowElement.toString();
    }

    /**
     * 流程定义信息查询
     */
    @RequestMapping("/queryProcessDefList")
    public String queryProcessDefList() {
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().list();
        StringBuilder sb = new StringBuilder();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            sb.append(processDefinition.getName()).append("</br>");
        }
        return sb.toString();
    }

    /**
     * 启动流程
     */
    @RequestMapping("/startProcessInstanceById")
    public String startProcessInstanceById() {
        String processDefineId = "leave:3:6bcad758-37d1-11eb-b334-d2baa7ac3c3c";
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefineId);
        return processInstance.getId();
    }

    /**
     * 判断流程是否结束
     */
    @RequestMapping("checkProcessOver")
    public String checkProcessOver() {
        String processInstanceId = "f7e4d205-3d23-11eb-bc91-9e52f57ce458";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            return "当前流程已结束";
        } else {
            return processInstance.getId();
        }
    }

    /**
     * 获取历史流程变量
     */
    @RequestMapping("/getHistoryProcessVar")
    public String getHistoryProcessVar() {
        String processInstanceId = "565c2459-3d96-11eb-969d-9e52f57ce458";
        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
        for (HistoricVariableInstance hvi : list) {
            System.out.println(hvi.getVariableName() + ":" + hvi.getValue());
        }
        return processInstanceId;
    }

    /**
     * 查看历史流程实例
     */
    @RequestMapping("/getHistoryProcessInstance")
    public void getHistoryProcessInstance() {
        List<HistoricProcessInstance> processInstanceList = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").list();
        for (HistoricProcessInstance historicProcessInstance : processInstanceList) {
            System.out.println(historicProcessInstance.getName() + "," + historicProcessInstance.getId() + "," + historicProcessInstance.getStartTime());
        }
    }

    /**
     * 查看流程执行历史记录，历史活动（一个流程的执行一共经历了多少个活动）
     */
    @RequestMapping("/getHistoryActivityInstance")
    public void getHistoryActivityInstance() {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId("565c2459-3d96-11eb-969d-9e52f57ce458 ").list();
        for (HistoricActivityInstance hai : list) {
            System.out.println(hai.getActivityId() + "," + hai.getActivityName() + "," + hai.getTaskId());
        }
    }
}
