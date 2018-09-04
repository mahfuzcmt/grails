<span id="isAllTaskComplete" style="display: none">${isAllTaskComplete}</span>
<g:each in="${taskList}" var="task">
    <span class="task" task-token="${task.token}">
        <label class="task-name">${task.name}</label>
        <span class="task-progress">${task.progress}%</span>
    </span>
</g:each>

