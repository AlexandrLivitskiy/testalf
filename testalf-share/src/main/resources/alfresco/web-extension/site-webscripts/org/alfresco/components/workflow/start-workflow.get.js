<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/workflow.lib.js">

// Получаем список определений воркфлоу
var allWorkflows = getWorkflowDefinitions();

// Указываем workflow, которые нужно скрыть (по имени)
var hiddenWorkflowNames = [
//   "activiti$activitiAdhoc",
//   "activiti$activitiReview",
//   "activiti$activitiParallelReview",
//   "activiti$activitiReviewPooled",
//   "activiti$activitiParallelGroupReview",
   "activiti$my-process"
];

// Фильтруем список
model.workflowDefinitions = [];
for (var i = 0; i < allWorkflows.length; i++) {
   var wf = allWorkflows[i];
   if (hiddenWorkflowNames.indexOf(wf.name) === -1) {
      model.workflowDefinitions.push(wf);
   }
}

function main()
{
   // Widget instantiation metadata...
   var startWorkflow = {
      id : "StartWorkflow", 
      name : "Alfresco.component.StartWorkflow",
      options : {
         failureMessage : "message.failure",
         submitButtonMessageKey : "button.startWorkflow",
         defaultUrl : getSiteUrl("my-tasks"),
         selectedItems : (page.url.args.selectedItems != null) ? page.url.args.selectedItems: "",
         destination : (page.url.args.destination != null) ? page.url.args.destination : "",
         workflowDefinitions : model.workflowDefinitions
      }
   };
   model.widgets = [startWorkflow];
}

main();
