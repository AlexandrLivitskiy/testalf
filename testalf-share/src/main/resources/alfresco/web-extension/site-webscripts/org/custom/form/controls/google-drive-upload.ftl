<div class="form-field google-upload-wrapper">

    <label class="google-upload-btn" for="${fieldHtmlId}">
        📎 Обрати файли
    </label>

    <input type="file"
           id="${fieldHtmlId}"
           multiple
           class="google-upload-input" />

    <input type="hidden"
           id="${fieldHtmlId}-value"
           name="${field.name}" />

    <div class="google-upload-hint">
        Можно обрати декілька файлів (до 500Мб)
    </div>

    <div id="${fieldHtmlId}-list"
         class="google-upload-list"></div>

</div>

<style>
.google-upload-wrapper{
    margin-top:8px;
}

.google-upload-input{
    display:none;
}

.google-upload-btn{
    display:inline-block;
    background:#f7f7ee;
    color:#fff;
    padding:10px 18px;
    border-radius:4px;
    cursor:pointer;
    font-weight:bold;
    font-size:14px;
    transition:0.2s;
}

.google-upload-btn:hover{
    background:#094f97;
}

.google-upload-hint{
    margin-top:8px;
    color:#666;
    font-size:12px;
}

.google-upload-list{
    margin-top:12px;
}

.google-upload-list div{
    background:#f5f5f5;
    border:1px solid #ddd;
    padding:8px 10px;
    margin-bottom:6px;
    border-radius:4px;

    display: flex;
    align-items: center;
}

.google-upload-list div.loading {
    background: #eef6ff;
    animation: pulse 1.2s infinite;
}

@keyframes pulse {
    0% { opacity: 1; }
    50% { opacity: 0.6; }
    100% { opacity: 1; }
}

.upload-file-spinner {
    width: 14px;
    height: 14px;
    border: 2px solid #ccc;
    border-top: 2px solid #0b79d0;
    border-radius: 50%;
    animation: upload-file-spin 0.8s linear infinite;
}

@keyframes upload-file-spin {
    100% { transform: rotate(360deg); }
}

.google-upload-list span {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.google-upload-list button {
    margin-left: 25px;
    flex-shrink: 0;
}

.google-upload-list div.error {
    background: #ffecec;
    border-color: #ffb3b3;
}

.google-upload-list div.error span {
    color: #b00020;
}
</style>

<script>
(async function initUpload(){
    var input = document.getElementById("${fieldHtmlId}");
    if (!input) {
        setTimeout(initUpload, 100);
        return;
    }

    var hidden = document.getElementById("${fieldHtmlId}-value");
    var list   = document.getElementById("${fieldHtmlId}-list");
    var refs = JSON.stringify([]);
    var nodeRefTempFolder;
    const csrfToken = decodeURIComponent(document.cookie
            .split("; ")
            .find(row => row.startsWith("Alfresco-CSRFToken="))
            ?.split("=")[1]);

    const createWorkflowFolder = async () => {
        const folderName = "wf_tmp_" + Date.now();
        const getUserHome = async () => {
            const res = await fetch("/share/proxy/alfresco/custom/userhome", {
                credentials: "same-origin"
            });
            return (await res.json()).nodeRef;
        };
        const parent = await getUserHome();

        const response = await fetch("/share/proxy/alfresco/api/type/cm%3Afolder/formprocessor", {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    "X-Requested-With": "XMLHttpRequest",
                    "Alfresco-CSRFToken": csrfToken
                },
                body: JSON.stringify({
                    alf_destination: parent,
                    prop_cm_name: folderName,
                    prop_cm_title: folderName,
                    prop_cm_description: ""
                })
            }
        );
        const text = await response.text();
        if (!response.ok) {
            throw new Error(text);
        }
        return JSON.parse(text).persistedObject;
    };

    input.addEventListener("change", async function(){
        if (!nodeRefTempFolder) {
            nodeRefTempFolder = await createWorkflowFolder();
            document.querySelector("[name='prop_tawf_googleDriveUploadFiles']").value = nodeRefTempFolder;
        }
        for (let file of input.files) {
            let fd = new FormData();
            fd.append("filedata", file);
            fd.append("filename", file.name);
            fd.append("destination", nodeRefTempFolder);
            fd.append("uploaddirectory", "");
            fd.append("overwrite", "false");
            fd.append("thumbnails", "doclib");
            fd.append("updatenameandmimetype", "false");

            let div = document.createElement("div");
            div.classList.add("loading");
            <!--                    div.style.marginBottom = "6px";-->
            let text = document.createElement("span");
            text.innerHTML = "⬆ " + file.name;
            let loader = document.createElement("div");
            loader.className = "upload-file-spinner";
            div.appendChild(text);
            div.appendChild(loader);
            list.appendChild(div);

            let uploadURL = "/share/proxy/alfresco/api/upload" +
                (csrfToken ? "?Alfresco-CSRFToken=" + encodeURIComponent(csrfToken) : "");
            fetch(uploadURL, {
                method: "POST",
                body: fd,
                credentials: "same-origin",
                headers: {
                    "Alfresco-CSRFToken": csrfToken,
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
            .then(async function(r){
                let txt = await r.text();
                if (!r.ok) {
                    throw new Error(txt);
                }
                return JSON.parse(txt);
            })
            .then(function(data){
                let refsArray = JSON.parse(refs);
                refsArray.push(data.nodeRef);
                refs = JSON.stringify(refsArray);

                text.innerHTML = "✔ " + file.name;
                div.classList.remove("loading");
                loader.remove();
                let btn = document.createElement("button");
                btn.type = "button";
                btn.innerHTML = "X Відмінити";
<!--                    btn.style.marginLeft = "10px";-->

                btn.onclick = function () {
                    if (!confirm("Видалити файл " + file.name + "?")) {
                        return;
                    }

                    let path = data.nodeRef
                            .replace("://", "/")
                            .replace("workspace/", "workspace/");

                    fetch("/share/proxy/alfresco/slingshot/doclib/action/file/node/" + path, {
                        method: "DELETE",
                        credentials: "same-origin",
                        headers: {
                            "Alfresco-CSRFToken": csrfToken,
                            "X-Requested-With": "XMLHttpRequest"
                        }
                    })
                    .then(function (r) {
                        if (!r.ok) {
                            throw new Error("Delete failed: " + r.status);
                        }
                        refs = JSON.stringify(JSON.parse(refs).filter(function (x) {
                            return x !== data.nodeRef;
                        }));
                        div.remove();
                    })
                    .catch(function (e) {
                        alert("Помилка видалення");
                        console.error(e);
                    });
                };
                div.appendChild(btn);
            })
            .catch(function(err) {
                div.classList.remove("loading");
                loader.remove();
                div.classList.add("error");
                text.innerHTML = "❌ " + file.name + " (помилка завантаження)";
                console.error("Upload error:", err);
            });
        }
    });

    var form = document.querySelectorAll("form")[1];

    if (!form) {
        return;
    }

    function showOverlay() {
        var overlay = document.getElementById("workflow-loading-overlay");

        if (overlay) {
            overlay.style.display = "block";
        }

        document.querySelectorAll('button,input[type="submit"]').forEach(function(btn){
            btn.disabled = true;
        });
    }

    function hideOverlay() {
        var overlay = document.getElementById("workflow-loading-overlay");

        if (overlay) {
            overlay.style.display = "none";
        }

        document.querySelectorAll('button,input[type="submit"]').forEach(function(btn){
            btn.disabled = false;
        });
    }

    form.addEventListener("submit", function () {
        showOverlay();
    });

    setInterval(function () {
        var invalid = document.querySelector(".error, .invalid");
        var prompts = [...document.querySelectorAll(".yui-dialog")];
        var promptIsVisible = prompts.filter(x => window.getComputedStyle(x).visibility === "visible")[0];
        if (invalid || promptIsVisible) {
            hideOverlay();
        }
    }, 500);
})();
</script>

<div id="workflow-loading-overlay" style="
    display:none;
    position:fixed;
    z-index:99999;
    top:0;
    left:0;
    width:100%;
    height:100%;
    background:rgba(0,0,0,0.25);
">
    <!--    background:rgba(255,255,255,0.7);-->

    <div style="
        position:absolute;
        top:50%;
        left:50%;
        transform:translate(-50%, -50%);
        text-align:center;
        font-size:16px;
        color:#333;
    ">

        <div class="google-spinner"></div>

        <div style="margin-top:12px;">
            Створення Google документів...
        </div>

    </div>
</div>

<style>
.google-spinner{
    width:48px;
    height:48px;
    border:5px solid #ddd;
    border-top:5px solid #4285f4;
    border-radius:50%;
    animation:google-spin 1s linear infinite;
    margin:auto;
}

@keyframes google-spin{
    0% { transform:rotate(0deg); }
    100% { transform:rotate(360deg); }
}
</style>

<!--<script>-->
<!--document.addEventListener("DOMContentLoaded", function () {-->

<!--input.addEventListener("change", async function(){-->
<!--if (!nodeRefTempFolder) {-->
<!--nodeRefTempFolder = await createWorkflowFolder();-->
<!--document.querySelector("[name='prop_tawf_googleDriveUploadFiles']").value = nodeRefTempFolder;-->
<!--}-->
<!--var files = input.files;-->
<!--for (var i = 0; i < files.length; i++) {-->
<!--(function(file){-->
<!--var fd = new FormData();-->
<!--fd.append("filedata", file);-->
<!--fd.append("filename", file.name);-->
<!--fd.append("destination", nodeRefTempFolder);-->
<!--fd.append("uploaddirectory", "");-->
<!--fd.append("overwrite", "false");-->
<!--fd.append("thumbnails", "doclib");-->
<!--fd.append("updatenameandmimetype", "false");-->

<!--let uploadURL = "/share/proxy/alfresco/api/upload" +-->
<!--(csrfToken ? "?Alfresco-CSRFToken=" + encodeURIComponent(csrfToken) : "");-->
<!--fetch(uploadURL, {-->
<!--method: "POST",-->
<!--body: fd,-->
<!--credentials: "same-origin",-->
<!--headers: {-->
<!--"Alfresco-CSRFToken": csrfToken,-->
<!--"X-Requested-With": "XMLHttpRequest"-->
<!--}-->
<!--})-->
<!--.then(async function(r){-->
<!--var txt = await r.text();-->
<!--if (!r.ok) {-->
<!--throw new Error(txt);-->
<!--}-->
<!--return JSON.parse(txt);-->
<!--})-->
<!--.then(function(data){-->
<!--var refsArray = JSON.parse(refs);-->
<!--refsArray.push(data.nodeRef);-->
<!--refs = JSON.stringify(refsArray);-->

<!--var div = document.createElement("div");-->
<!--&lt;!&ndash;                    div.style.marginBottom = "6px";&ndash;&gt;-->

<!--var text = document.createElement("span");-->
<!--text.innerHTML = "✔ " + file.name;-->

<!--var btn = document.createElement("button");-->
<!--btn.type = "button";-->
<!--btn.innerHTML = "X Відмінити";-->
<!--&lt;!&ndash;                    btn.style.marginLeft = "10px";&ndash;&gt;-->

<!--btn.onclick = function () {-->

<!--if (!confirm("Видалити файл " + file.name + "?")) {-->
<!--return;-->
<!--}-->

<!--var path = data.nodeRef-->
<!--.replace("://", "/")-->
<!--.replace("workspace/", "workspace/");-->

<!--fetch("/share/proxy/alfresco/slingshot/doclib/action/file/node/" + path, {-->
<!--method: "DELETE",-->
<!--credentials: "same-origin",-->
<!--headers: {-->
<!--"Alfresco-CSRFToken": csrfToken,-->
<!--"X-Requested-With": "XMLHttpRequest"-->
<!--}-->
<!--})-->
<!--.then(function (r) {-->
<!--if (!r.ok) {-->
<!--throw new Error("Delete failed: " + r.status);-->
<!--}-->
<!--refs = JSON.stringify(JSON.parse(refs).filter(function (x) {-->
<!--return x !== data.nodeRef;-->
<!--}));-->
<!--div.remove();-->
<!--})-->
<!--.catch(function (e) {-->
<!--alert("Помилка видалення");-->
<!--console.error(e);-->
<!--});-->
<!--};-->

<!--div.appendChild(text);-->
<!--div.appendChild(btn);-->
<!--list.appendChild(div);-->
<!--})-->
<!--.catch(function(err){-->
<!--console.error("Upload error:", err);-->
<!--});-->
<!--})(files[i]);-->
<!--}-->
<!--});-->

<!--});-->
<!--</script>-->