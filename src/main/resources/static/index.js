(function() {
    "use strict";

    const USERNAME = "nickle";
    const PASSWORD = "nickle";

    const API_URL = "http://localhost:8080";

    let currToken;

    window.addEventListener("load", main);

    async function main() {
        document.getElementById("create").addEventListener("click", create);
        document.getElementById("launch").addEventListener("click", launch);
        document.getElementById("delete").addEventListener("click", deleteUser);
        document.getElementById("newTask").addEventListener("click", createTask);
        document.getElementById("listTasks").addEventListener("click", listTasks);
        document.getElementById("removeTask").addEventListener("click", deleteTask);
    }

    async function create() {
        let result = await fetch(API_URL + "/api/auth/new", {
            method: "POST",
            headers: {
                Authorization: ("Basic " + btoa(USERNAME + ":" + PASSWORD))
            }
        }).then(data => data.text())
    .then(JSON.parse);
        console.log(result);
    }

    async function launch() {
        let result = await fetch(API_URL + "/api/auth", {
            method: "POST",
            headers: {
                Authorization: ("Basic " + btoa(USERNAME + ":" + PASSWORD))
            }
        }).then(data => data.text()).then(JSON.parse);
        currToken = result.authToken;
        console.log(currToken);
    }

    async function deleteUser() {
        let result = await fetch(API_URL + "/api/auth/delete", {
            method: "POST",
            headers: {
                Authorization: ("Basic " + btoa(USERNAME + ":" + PASSWORD))
            }
        }).then(data => data.text())
    .then(JSON.parse);
        console.log(result);
    }

    async function createTask() {
        let dataBody = {};
        let task = {};
        task.taskDescription = "This is a test description";
        task.completed = false;
        let due = new Date();
        due.setMonth(8);
        due.setDate(8);
        due.setFullYear(2020);
        task.timeDue = Math.floor(due.getTime());
        dataBody.task = task;

        await fetch(API_URL + "/api/tasks/add", {
            method: "POST",
            headers: {
                'Authorization': "Bearer " + currToken,
                'Content-type': "application/json"
            },
            body: JSON.stringify(dataBody)
        }).then(data => data.text())
    .then(JSON.parse).then(console.log);
    }

    async function listTasks() {
        await fetch(API_URL + "/api/tasks/all", {
            method: "GET",
            headers: {
                'Authorization': "Bearer " + currToken
            }
        }).then(data => data.text())
    .then(JSON.parse).then(console.log);
    }

    async function deleteTask() {
        let dataBody = {};
        dataBody.taskId = 8;
        await fetch(API_URL + "/api/tasks/remove", {
            method: "POST",
            headers: {
                'Authorization': "Bearer " + currToken,
                'Content-type': "application/json"
            },
            body: JSON.stringify(dataBody)
        }).then(data => data.text())
    .then(JSON.parse).then(console.log);
    }

})();